package com.appspace.movil.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appspace.movil.data.local.AppDatabase
import com.appspace.movil.data.local.FileEntity
import com.appspace.movil.data.repository.CacheCleaner
import com.appspace.movil.data.repository.DuplicateDetector
import com.appspace.movil.data.repository.FileRepository
import com.appspace.movil.data.repository.FileScanProgress
import com.appspace.movil.domain.agent.CleanAgent
import com.appspace.movil.domain.report.ReportGenerator
import com.appspace.movil.model.AgentAction
import com.appspace.movil.model.ChatMessage
import com.appspace.movil.model.CleanReport
import com.appspace.movil.model.FileCategory
import com.appspace.movil.model.FileItem
import com.appspace.movil.model.MessageType
import com.appspace.movil.model.StorageStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel principal de la aplicación
 */
class CleanViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val fileDao = database.fileDao()
    private val reportDao = database.reportDao()
    
    private val fileRepository = FileRepository(application)
    private val duplicateDetector = DuplicateDetector()
    private val cacheCleaner = CacheCleaner(application)
    private val cleanAgent = CleanAgent()
    private val reportGenerator = ReportGenerator()
    
    // Estado de la UI
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    // Archivos escaneados
    private val _files = MutableStateFlow<List<FileItem>>(emptyList())
    val files: StateFlow<List<FileItem>> = _files.asStateFlow()
    
    // Estadísticas de almacenamiento
    private val _storageStats = MutableStateFlow<StorageStats?>(null)
    val storageStats: StateFlow<StorageStats?> = _storageStats.asStateFlow()
    
    // Acciones recomendadas por el agente
    private val _agentActions = MutableStateFlow<List<AgentAction>>(emptyList())
    val agentActions: StateFlow<List<AgentAction>> = _agentActions.asStateFlow()
    
    // Mensajes del chat
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()
    
    // Informes
    private val _reports = MutableStateFlow<List<CleanReport>>(emptyList())
    val reports: StateFlow<List<CleanReport>> = _reports.asStateFlow()
    
    init {
        viewModelScope.launch {
            loadReports()
            loadStorageStats()
        }
    }
    
    /**
     * Escanea todos los archivos
     */
    fun scanFiles() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true, scanProgress = 0)
            
            fileRepository.scanAllFiles().collect { progress ->
                when (progress) {
                    is FileScanProgress.ScanningCategory -> {
                        _uiState.value = _uiState.value.copy(
                            scanProgress = progress.scannedSoFar,
                            currentCategory = progress.category.name
                        )
                    }
                    is FileScanProgress.CategoryComplete -> {
                        // Actualizar progreso
                    }
                    is FileScanProgress.Complete -> {
                        _files.value = progress.files
                        _uiState.value = _uiState.value.copy(
                            isScanning = false,
                            scanProgress = progress.totalScanned,
                            currentCategory = null
                        )
                        
                        // Guardar en base de datos
                        saveFilesToDatabase(progress.files)
                        
                        // Calcular estadísticas
                        calculateStorageStats(progress.files)
                        
                        // Generar recomendaciones
                        generateRecommendations(progress.files)
                        
                        // Generar informe de escaneo
                        generateScanReport(progress.files)
                    }
                }
            }
        }
    }
    
    /**
     * Genera recomendaciones del agente IA
     */
    private fun generateRecommendations(files: List<FileItem>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzing = true)
            
            // Encontrar duplicados
            val duplicateGroups = duplicateDetector.findDuplicates(files)
            
            // Obtener tamaño de caché
            val cacheSize = cacheCleaner.calculateCacheSize()
            
            // Calcular uso de almacenamiento
            val storageUsage = _storageStats.value?.usagePercent ?: 0f
            
            // Generar acciones recomendadas
            val actions = cleanAgent.analyzeAndRecommend(
                files = files,
                duplicateGroups = duplicateGroups,
                cacheSize = cacheSize,
                storageUsagePercent = storageUsage
            )
            
            _agentActions.value = actions
            _uiState.value = _uiState.value.copy(isAnalyzing = false)
            
            // Agregar mensaje inicial del agente
            if (actions.isNotEmpty()) {
                addAgentMessage(
                    "He analizado tu dispositivo y encontré varias oportunidades para liberar espacio. " +
                    "Revisa las recomendaciones a continuación.",
                    actions = actions
                )
            } else {
                addAgentMessage(
                    "¡Tu dispositivo está en buen estado! No encontré problemas significativos que requieran atención."
                )
            }
        }
    }
    
    /**
     * Ejecuta una acción aprobada
     */
    fun executeAction(action: AgentAction) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCleaning = true)
            
            var report: CleanReport? = null
            
            when (action.type) {
                AgentAction.ActionType.DELETE_DUPLICATES -> {
                    // Eliminar duplicados
                    val deletedCount = fileRepository.deleteFiles(action.files)
                    report = reportGenerator.generateDuplicateReport(
                        duplicateGroups = emptyList(),
                        deletedGroups = emptyList()
                    ).copy(
                        filesDeleted = deletedCount,
                        spaceFreed = action.files.sumOf { it.size }
                    )
                    
                    // Actualizar lista de archivos
                    val deletedIds = action.files.map { it.id }
                    _files.value = _files.value.filter { it.id !in deletedIds }
                }
                
                AgentAction.ActionType.DELETE_CACHE -> {
                    // Limpiar caché
                    val result = cacheCleaner.cleanAll()
                    report = reportGenerator.fromCleanResult(result, com.appspace.movil.model.TaskType.CLEAN_CACHE)
                }
                
                else -> {
                    // Acciones genéricas
                    val deletedCount = fileRepository.deleteFiles(action.files)
                    report = CleanReport(
                        taskType = com.appspace.movil.model.TaskType.CUSTOM,
                        filesDeleted = deletedCount,
                        spaceFreed = action.files.sumOf { it.size },
                        summary = "Se eliminaron $deletedCount archivos"
                    )
                    
                    // Actualizar lista
                    val deletedIds = action.files.map { it.id }
                    _files.value = _files.value.filter { it.id !in deletedIds }
                }
            }
            
            // Guardar informe
            report?.let { saveReport(it) }
            
            _uiState.value = _uiState.value.copy(isCleaning = false, lastReport = report)
            
            // Agregar mensaje de confirmación
            if (report != null) {
                addAgentMessage(
                    "✅ ${report.summary}\n\nEspacio liberado: ${report.formattedSpaceFreed}",
                    isUser = false,
                    report = report
                )
            }
        }
    }
    
    /**
     * Envía un mensaje al agente IA
     */
    fun sendMessage(message: String) {
        viewModelScope.launch {
            // Agregar mensaje del usuario
            val userMessage = ChatMessage(
                content = message,
                isUser = true
            )
            _chatMessages.value = _chatMessages.value + userMessage
            
            // Procesar con el agente
            val response = cleanAgent.answerQuestion(message)
            
            // Agregar respuesta del agente
            val agentMessage = ChatMessage(
                content = response,
                isUser = false
            )
            _chatMessages.value = _chatMessages.value + agentMessage
        }
    }
    
    /**
     * Agrega un mensaje del agente
     */
    private fun addAgentMessage(
        content: String,
        isUser: Boolean = false,
        actions: List<AgentAction> = emptyList(),
        report: CleanReport? = null
    ) {
        val messageType = when {
            report != null -> MessageType.REPORT
            actions.isNotEmpty() -> MessageType.ACTIONS
            else -> MessageType.TEXT
        }
        
        val message = ChatMessage(
            content = content,
            isUser = isUser,
            actions = actions,
            messageType = messageType
        )
        _chatMessages.value = _chatMessages.value + message
    }
    
    /**
     * Carga estadísticas de almacenamiento
     */
    private suspend fun loadStorageStats() {
        val stat = android.os.StatFs(android.os.Environment.getDataDirectory().path)
        val totalSpace = stat.totalBytes
        val freeSpace = stat.availableBytes
        val usedSpace = totalSpace - freeSpace
        
        _storageStats.value = StorageStats(
            totalSpace = totalSpace,
            usedSpace = usedSpace,
            freeSpace = freeSpace
        )
    }
    
    /**
     * Calcula estadísticas basadas en los archivos escaneados
     */
    private fun calculateStorageStats(files: List<FileItem>) {
        val categoryStats = files.groupBy { it.category }.mapValues { (category, categoryFiles) ->
            categoryFiles.sumOf { it.size }
        }
        
        val currentStats = _storageStats.value ?: return
        
        _storageStats.value = currentStats.copy(
            categoryStats = categoryStats.map { (category, size) ->
                category to com.appspace.movil.model.CategoryStat(
                    category = category,
                    totalSize = size,
                    fileCount = categoryFiles.size
                )
            }.toMap()
        )
    }
    
    /**
     * Guarda archivos en la base de datos
     */
    private suspend fun saveFilesToDatabase(files: List<FileItem>) {
        val entities = files.map { FileEntity.fromFileItem(it) }
        fileDao.insertFiles(entities)
    }
    
    /**
     * Guarda un informe en la base de datos
     */
    private suspend fun saveReport(report: CleanReport) {
        // reportDao.insertReport(ReportEntity.fromReport(report))
        _reports.value = _reports.value + report
    }
    
    /**
     * Carga informes de la base de datos
     */
    private suspend fun loadReports() {
        // reportDao.getAllReports().collect { entities ->
        //     _reports.value = entities.map { it.toReport() }
        // }
    }
    
    /**
     * Genera informe de escaneo
     */
    private fun generateScanReport(files: List<FileItem>) {
        viewModelScope.launch {
            val totalSize = files.sumOf { it.size }
            val duplicateGroups = duplicateDetector.findDuplicates(files)
            val duplicateCount = duplicateGroups.sumOf { it.duplicates.size }
            val duplicateSize = duplicateGroups.sumOf { it.spaceWasted }
            
            val categoryStats = files.groupBy { it.category }.mapValues { it.value.sumOf { f -> f.size } }
            
            val report = reportGenerator.generateScanReport(
                totalFiles = files.size,
                totalSize = totalSize,
                duplicateCount = duplicateCount,
                duplicateSize = duplicateSize,
                categoryStats = categoryStats
            )
            
            saveReport(report)
        }
    }
    
    /**
     * Obtiene archivos por categoría
     */
    fun getFilesByCategory(category: FileCategory) {
        viewModelScope.launch {
            val categoryFiles = _files.value.filter { it.category == category }
            // Se puede usar para mostrar en la UI
        }
    }
    
    /**
     * Reinicia el estado
     */
    fun resetState() {
        _uiState.value = UiState()
    }
}

/**
 * Estado de la UI
 */
data class UiState(
    val isScanning: Boolean = false,
    val isAnalyzing: Boolean = false,
    val isCleaning: Boolean = false,
    val scanProgress: Int = 0,
    val currentCategory: String? = null,
    val lastReport: CleanReport? = null,
    val error: String? = null
)
