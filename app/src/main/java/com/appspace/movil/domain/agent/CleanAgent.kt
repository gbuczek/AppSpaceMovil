package com.appspace.movil.domain.agent

import com.appspace.movil.model.ActionType
import com.appspace.movil.model.AgentAction
import com.appspace.movil.model.FileCategory
import com.appspace.movil.model.FileItem
import com.appspace.movil.model.RiskLevel
import com.appspace.movil.data.repository.DuplicateGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Agente IA que analiza y recomienda acciones de limpieza
 */
class CleanAgent {
    
    private val thresholds = AgentThresholds()
    
    /**
     * Analiza los archivos y genera recomendaciones
     */
    suspend fun analyzeAndRecommend(
        files: List<FileItem>,
        duplicateGroups: List<DuplicateGroup>,
        cacheSize: Long,
        storageUsagePercent: Float
    ): List<AgentAction> = withContext(Dispatchers.Default) {
        val actions = mutableListOf<AgentAction>()
        
        // Analizar duplicados
        actions.addAll(analyzeDuplicates(duplicateGroups))
        
        // Analizar caché
        actions.addAll(analyzeCache(cacheSize))
        
        // Analizar archivos grandes
        actions.addAll(analyzeLargeFiles(files))
        
        // Analizar descargas antiguas
        actions.addAll(analyzeOldDownloads(files))
        
        // Analizar APKs
        actions.addAll(analyzeApkFiles(files))
        
        // Analizar uso general de almacenamiento
        actions.addAll(analyzeStorageUsage(storageUsagePercent, files))
        
        // Ordenar por impacto (espacio a liberar)
        actions.sortedByDescending { it.spaceToFree }
    }
    
    /**
     * Analiza archivos duplicados
     */
    private fun analyzeDuplicates(duplicateGroups: List<DuplicateGroup>): List<AgentAction> {
        val actions = mutableListOf<AgentAction>()
        
        if (duplicateGroups.isEmpty()) return actions
        
        val totalSpaceWasted = duplicateGroups.sumOf { it.spaceWasted }
        val totalDuplicates = duplicateGroups.sumOf { it.duplicates.size }
        
        if (totalDuplicates > 0) {
            val allDuplicateFiles = duplicateGroups.flatMap { it.duplicates }
            
            actions.add(
                AgentAction(
                    type = ActionType.DELETE_DUPLICATES,
                    title = "Eliminar archivos duplicados",
                    description = "Se encontraron $totalDuplicates archivos duplicados que están ocupando espacio innecesariamente",
                    files = allDuplicateFiles.take(50), // Mostrar solo los primeros 50
                    spaceToFree = totalSpaceWasted,
                    riskLevel = RiskLevel.LOW,
                    recommendation = generateDuplicateRecommendation(totalDuplicates, totalSpaceWasted)
                )
            )
        }
        
        return actions
    }
    
    /**
     * Analiza caché
     */
    private fun analyzeCache(cacheSize: Long): List<AgentAction> {
        val actions = mutableListOf<AgentAction>()
        
        if (cacheSize > thresholds.cacheWarning) {
            actions.add(
                AgentAction(
                    type = ActionType.DELETE_CACHE,
                    title = "Limpiar caché",
                    description = "El caché del sistema está ocupando ${formatSize(cacheSize)}. Limpiarlo puede liberar espacio sin riesgo.",
                    files = emptyList(),
                    spaceToFree = cacheSize,
                    riskLevel = RiskLevel.LOW,
                    recommendation = generateCacheRecommendation(cacheSize)
                )
            )
        }
        
        return actions
    }
    
    /**
     * Analiza archivos grandes
     */
    private fun analyzeLargeFiles(files: List<FileItem>): List<AgentAction> {
        val actions = mutableListOf<AgentAction>()
        
        // Archivos más grandes de 100MB
        val largeFiles = files.filter { it.size > thresholds.largeFile }
            .sortedByDescending { it.size }
            .take(20)
        
        if (largeFiles.isNotEmpty()) {
            val totalSize = largeFiles.sumOf { it.size }
            
            actions.add(
                AgentAction(
                    type = ActionType.DELETE_LARGE_FILES,
                    title = "Revisar archivos grandes",
                    description = "Estos ${largeFiles.size} archivos están ocupando ${formatSize(totalSize)}. ¿Quieres revisarlos?",
                    files = largeFiles,
                    spaceToFree = totalSize,
                    riskLevel = RiskLevel.MEDIUM,
                    recommendation = generateLargeFilesRecommendation(largeFiles)
                )
            )
        }
        
        return actions
    }
    
    /**
     * Analiza descargas antiguas
     */
    private fun analyzeOldDownloads(files: List<FileItem>): List<AgentAction> {
        val actions = mutableListOf<AgentAction>()
        
        val now = System.currentTimeMillis()
        val thirtyDaysAgo = now - (30 * 24 * 60 * 60 * 1000L)
        
        val oldDownloads = files.filter { file ->
            file.category == FileCategory.DOWNLOADS && file.lastModified < thirtyDaysAgo
        }.sortedBy { it.lastModified }
        
        if (oldDownloads.isNotEmpty()) {
            val totalSize = oldDownloads.sumOf { it.size }
            
            actions.add(
                AgentAction(
                    type = ActionType.DELETE_OLD_DOWNLOADS,
                    title = "Descargas antiguas",
                    description = "Tienes ${oldDownloads.size} descargas con más de 30 días de antigüedad",
                    files = oldDownloads.take(50),
                    spaceToFree = totalSize,
                    riskLevel = RiskLevel.MEDIUM,
                    recommendation = generateDownloadsRecommendation(oldDownloads.size, totalSize)
                )
            )
        }
        
        return actions
    }
    
    /**
     * Analiza archivos APK
     */
    private fun analyzeApkFiles(files: List<FileItem>): List<AgentAction> {
        val actions = mutableListOf<AgentAction>()
        
        val apkFiles = files.filter { it.category == FileCategory.APK }
        
        if (apkFiles.isNotEmpty()) {
            val totalSize = apkFiles.sumOf { it.size }
            
            actions.add(
                AgentAction(
                    type = ActionType.DELETE_UNUSED_APK,
                    title = "Archivos APK",
                    description = "Se encontraron ${apkFiles.size} archivos APK que probablemente ya no necesitas",
                    files = apkFiles,
                    spaceToFree = totalSize,
                    riskLevel = RiskLevel.LOW,
                    recommendation = generateApkRecommendation(apkFiles.size, totalSize)
                )
            )
        }
        
        return actions
    }
    
    /**
     * Analiza uso general de almacenamiento
     */
    private fun analyzeStorageUsage(usagePercent: Float, files: List<FileItem>): List<AgentAction> {
        val actions = mutableListOf<AgentAction>()
        
        if (usagePercent > thresholds.criticalStorage) {
            actions.add(
                AgentAction(
                    type = ActionType.CUSTOM,
                    title = "¡Atención! Almacenamiento casi lleno",
                    description = "El ${usagePercent.toInt()}% de tu almacenamiento está usado. Es recomendable liberar espacio urgentemente.",
                    files = emptyList(),
                    spaceToFree = 0,
                    riskLevel = RiskLevel.HIGH,
                    recommendation = "Tu dispositivo está casi lleno. Te recomiendo empezar por eliminar los archivos duplicados y limpiar el caché."
                )
            )
        } else if (usagePercent > thresholds.warningStorage) {
            actions.add(
                AgentAction(
                    type = ActionType.CUSTOM,
                    title = "Almacenamiento bajo",
                    description = "El ${usagePercent.toInt()}% de tu almacenamiento está usado.",
                    files = emptyList(),
                    spaceToFree = 0,
                    riskLevel = RiskLevel.MEDIUM,
                    recommendation = "Considera liberar algo de espacio eliminando archivos que no necesites."
                )
            )
        }
        
        return actions
    }
    
    /**
     * Genera recomendación para duplicados
     */
    private fun generateDuplicateRecommendation(count: Int, space: Long): String {
        return when {
            count > 100 -> "¡Wow! Tienes muchos duplicados. Eliminarlos liberará ${formatSize(space)}. ¡Es totalmente seguro!"
            count > 50 -> "Encontré $count archivos duplicados. Puedes eliminarlos sin problema y liberar ${formatSize(space)}."
            count > 10 -> "Hay $count archivos repetidos. Te sugiero eliminarlos para ganar ${formatSize(space)} de espacio."
            else -> "Solo hay $count duplicados, pero cada byte cuenta. ¡Libera ${formatSize(space)}!"
        }
    }
    
    /**
     * Genera recomendación para caché
     */
    private fun generateCacheRecommendation(cacheSize: Long): String {
        return when {
            cacheSize > 1024 * 1024 * 1024 -> "¡Tu caché es enorme! (${formatSize(cacheSize)}). Limpiarla es 100% seguro y no afectará tus datos."
            cacheSize > 500 * 1024 * 1024 -> "El caché está ocupando ${formatSize(cacheSize)}. Es seguro limpiarlo."
            else -> "El caché ocupa ${formatSize(cacheSize)}. Puedes limpiarlo cuando quieras sin riesgo."
        }
    }
    
    /**
     * Genera recomendación para archivos grandes
     */
    private fun generateLargeFilesRecommendation(files: List<FileItem>): String {
        val categories = files.groupingBy { it.category }.eachCount()
        val topCategory = categories.maxByOrNull { it.value }?.key
        
        return "Tienes ${files.size} archivos grandes. " +
            (topCategory?.let { "La mayoría son ${it.name.lowercase()}. " } ?: "") +
            "Revisa cuáles puedes eliminar o mover a la nube."
    }
    
    /**
     * Genera recomendación para descargas
     */
    private fun generateDownloadsRecommendation(count: Int, size: Long): String {
        return "Las descargas antiguas ocupan ${formatSize(size)}. " +
            "¿Cuántas de esas ${count} descargas realmente necesitas conservar?"
    }
    
    /**
     * Genera recomendación para APKs
     */
    private fun generateApkRecommendation(count: Int, size: Long): String {
        return "Los archivos APK son instaladores que ya no necesitas después de instalar la app. " +
            "Eliminar ${count} APKs liberará ${formatSize(size)}."
    }
    
    /**
     * Procesa una acción aprobada
     */
    suspend fun processAction(action: AgentAction): AgentActionResult {
        return when (action.type) {
            ActionType.DELETE_DUPLICATES -> {
                AgentActionResult(
                    success = true,
                    message = "Se eliminaron ${action.files.size} archivos duplicados",
                    spaceFreed = action.files.sumOf { it.size },
                    filesAffected = action.files.size
                )
            }
            ActionType.DELETE_CACHE -> {
                AgentActionResult(
                    success = true,
                    message = "Caché limpiada correctamente",
                    spaceFreed = action.spaceToFree,
                    filesAffected = 1
                )
            }
            else -> {
                AgentActionResult(
                    success = true,
                    message = "Acción completada",
                    spaceFreed = action.spaceToFree,
                    filesAffected = action.files.size
                )
            }
        }
    }
    
    /**
     * Responde a preguntas del usuario
     */
    fun answerQuestion(question: String): String {
        val lowerQuestion = question.lowercase()
        
        return when {
            lowerQuestion.contains("hola") || lowerQuestion.contains("buenas") -> 
                "¡Hola! 👋 Soy tu asistente de limpieza. ¿En qué puedo ayudarte hoy?"
            
            lowerQuestion.contains("espacio") || lowerQuestion.contains("almacenamiento") ->
                "Puedo ayudarte a liberar espacio eliminando archivos duplicados, caché y descargas antiguas. ¿Quieres que escanee tu dispositivo?"
            
            lowerQuestion.contains("duplicad") ->
                "Los archivos duplicados son copias idénticas que ocupan espacio innecesariamente. Puedo encontrarlos y eliminarlos de forma segura."
            
            lowerQuestion.contains("cache") || lowerQuestion.contains("caché") ->
                "El caché son archivos temporales que las apps crean para funcionar más rápido. Se puede limpiar sin riesgo cuando necesitas espacio."
            
            lowerQuestion.contains("gracias") ->
                "¡De nada! 😊 Estoy aquí para ayudarte a mantener tu dispositivo limpio y rápido."
            
            lowerQuestion.contains("ayuda") || lowerQuestion.contains("qué puedes hacer") ->
                "Puedo: \n• Encontrar y eliminar archivos duplicados\n• Limpiar caché\n• Organizar tus archivos\n• Liberar espacio\n• Generar informes de limpieza"
            
            else -> "Interesante pregunta. Por ahora estoy aprendiendo, pero puedo ayudarte a limpiar y organizar tu dispositivo. ¿Quieres que escanee en busca de archivos duplicados?"
        }
    }
    
    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}

/**
 * Umbrales para recomendaciones
 */
data class AgentThresholds(
    val cacheWarning: Long = 500 * 1024 * 1024, // 500 MB
    val largeFile: Long = 100 * 1024 * 1024,    // 100 MB
    val warningStorage: Float = 80f,            // 80%
    val criticalStorage: Float = 90f            // 90%
)

/**
 * Resultado de procesar una acción
 */
data class AgentActionResult(
    val success: Boolean,
    val message: String,
    val spaceFreed: Long,
    val filesAffected: Int
) {
    val formattedSpaceFreed: String
        get() {
            return when {
                spaceFreed < 1024 -> "$spaceFreed B"
                spaceFreed < 1024 * 1024 -> "${spaceFreed / 1024} KB"
                spaceFreed < 1024 * 1024 * 1024 -> "${spaceFreed / (1024 * 1024)} MB"
                else -> "${spaceFreed / (1024 * 1024 * 1024)} GB"
            }
        }
}
