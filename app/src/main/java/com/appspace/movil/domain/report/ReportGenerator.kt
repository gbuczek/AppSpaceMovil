package com.appspace.movil.domain.report

import com.appspace.movil.model.CleanReport
import com.appspace.movil.model.FileCategory
import com.appspace.movil.model.ReportDetail
import com.appspace.movil.model.TaskType
import com.appspace.movil.data.repository.CleanResult
import com.appspace.movil.data.repository.DuplicateGroup

/**
 * Generador de informes de limpieza
 */
class ReportGenerator {
    
    /**
     * Genera un informe después de limpiar duplicados
     */
    fun generateDuplicateReport(
        duplicateGroups: List<DuplicateGroup>,
        deletedGroups: List<DuplicateGroup>
    ): CleanReport {
        val totalSpaceFreed = deletedGroups.sumOf { it.spaceWasted }
        val totalFilesDeleted = deletedGroups.sumOf { it.duplicates.size }
        
        val details = deletedGroups.map { group ->
            ReportDetail(
                description = "Duplicados de: ${group.original.name}",
                spaceFreed = group.spaceWasted,
                count = group.duplicates.size,
                category = group.original.category
            )
        }
        
        return CleanReport(
            taskType = TaskType.CLEAN_DUPLICATES,
            spaceFreed = totalSpaceFreed,
            filesDeleted = totalFilesDeleted,
            filesScanned = duplicateGroups.sumOf { it.totalFiles },
            duplicatesFound = duplicateGroups.size,
            details = details,
            summary = generateDuplicateSummary(totalFilesDeleted, totalSpaceFreed, duplicateGroups.size)
        )
    }
    
    /**
     * Genera un informe después de limpiar caché
     */
    fun generateCacheReport(
        result: CleanResult,
        cacheStatsBefore: Long
    ): CleanReport {
        val details = listOf(
            ReportDetail(
                description = "Caché de aplicación",
                spaceFreed = result.spaceFreed,
                count = result.filesDeleted,
                category = FileCategory.CACHE
            )
        )
        
        return CleanReport(
            taskType = TaskType.CLEAN_CACHE,
            spaceFreed = result.spaceFreed,
            filesDeleted = result.filesDeleted,
            cacheCleared = result.filesDeleted,
            details = details,
            summary = generateCacheSummary(result.spaceFreed, result.success)
        )
    }
    
    /**
     * Genera un informe después de limpieza general
     */
    fun generateGeneralReport(
        duplicateReport: CleanReport?,
        cacheReport: CleanReport?,
        downloadsReport: CleanReport?
    ): CleanReport {
        val totalSpaceFreed = (duplicateReport?.spaceFreed ?: 0) + 
                              (cacheReport?.spaceFreed ?: 0) + 
                              (downloadsReport?.spaceFreed ?: 0)
        
        val totalFilesDeleted = (duplicateReport?.filesDeleted ?: 0) + 
                                (cacheReport?.filesDeleted ?: 0) + 
                                (downloadsReport?.filesDeleted ?: 0)
        
        val allDetails = mutableListOf<ReportDetail>()
        duplicateReport?.details?.let { allDetails.addAll(it) }
        cacheReport?.details?.let { allDetails.addAll(it) }
        downloadsReport?.details?.let { allDetails.addAll(it) }
        
        return CleanReport(
            taskType = TaskType.CLEAN_ALL,
            spaceFreed = totalSpaceFreed,
            filesDeleted = totalFilesDeleted,
            duplicatesFound = duplicateReport?.duplicatesFound ?: 0,
            cacheCleared = cacheReport?.cacheCleared ?: 0,
            details = allDetails,
            summary = generateGeneralSummary(totalSpaceFreed, totalFilesDeleted, 
                duplicateReport != null, cacheReport != null, downloadsReport != null)
        )
    }
    
    /**
     * Genera un informe de escaneo
     */
    fun generateScanReport(
        totalFiles: Int,
        totalSize: Long,
        duplicateCount: Int,
        duplicateSize: Long,
        categoryStats: Map<FileCategory, Long>
    ): CleanReport {
        val details = categoryStats.map { (category, size) ->
            ReportDetail(
                description = "${category.name.lowercase().replaceFirstChar { it.uppercase() }}",
                spaceFreed = size,
                category = category
            )
        }
        
        return CleanReport(
            taskType = TaskType.SCAN,
            filesScanned = totalFiles,
            spaceFreed = 0,
            duplicatesFound = duplicateCount,
            details = details,
            summary = generateScanSummary(totalFiles, totalSize, duplicateCount, duplicateSize)
        )
    }
    
    /**
     * Genera resumen para limpieza de duplicados
     */
    private fun generateDuplicateSummary(filesDeleted: Int, spaceFreed: Long, groupsFound: Int): String {
        return "Se eliminaron $filesDeleted archivos duplicados de $groupsFound grupos, " +
               "liberando ${formatSize(spaceFreed)} de espacio."
    }
    
    /**
     * Genera resumen para limpieza de caché
     */
    private fun generateCacheSummary(spaceFreed: Long, success: Boolean): String {
        return if (success) {
            "Caché limpiada correctamente. Se liberaron ${formatSize(spaceFreed)}."
        } else {
            "La limpieza de caché encontró errores. Se liberó ${formatSize(spaceFreed)}."
        }
    }
    
    /**
     * Genera resumen para limpieza general
     */
    private fun generateGeneralSummary(
        spaceFreed: Long,
        filesDeleted: Int,
        hasDuplicates: Boolean,
        hasCache: Boolean,
        hasDownloads: Boolean
    ): String {
        val tasks = mutableListOf<String>()
        if (hasDuplicates) tasks.add("duplicados")
        if (hasCache) tasks.add("caché")
        if (hasDownloads) tasks.add("descargas")
        
        return "Limpieza completada. Se eliminaron $filesDeleted archivos " +
               "(${tasks.joinToString(", ")}) liberando ${formatSize(spaceFreed)}."
    }
    
    /**
     * Genera resumen para escaneo
     */
    private fun generateScanSummary(totalFiles: Int, totalSize: Long, duplicateCount: Int, duplicateSize: Long): String {
        return "Se escanearon $totalFiles archivos (${formatSize(totalSize)}). " +
               "Se encontraron $duplicateCount archivos duplicados que ocupan ${formatSize(duplicateSize)}."
    }
    
    /**
     * Convierte CleanResult a CleanReport
     */
    fun fromCleanResult(result: CleanResult, taskType: TaskType): CleanReport {
        return CleanReport(
            taskType = taskType,
            spaceFreed = result.spaceFreed,
            filesDeleted = result.filesDeleted,
            summary = if (result.success) {
                "Operación completada exitosamente. Se liberó ${formatSize(result.spaceFreed)}."
            } else {
                "Operación completada con errores. Se liberó ${formatSize(result.spaceFreed)}."
            }
        )
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
 * Historial de informes para mostrar en la UI
 */
data class ReportHistory(
    val reports: List<CleanReport>,
    val totalSpaceFreed: Long,
    val totalFilesDeleted: Int,
    val lastCleanDate: Long?
) {
    val formattedTotalSpaceFreed: String
        get() {
            return when {
                totalSpaceFreed < 1024 -> "$totalSpaceFreed B"
                totalSpaceFreed < 1024 * 1024 -> "${totalSpaceFreed / 1024} KB"
                totalSpaceFreed < 1024 * 1024 * 1024 -> "${totalSpaceFreed / (1024 * 1024)} MB"
                else -> "${totalSpaceFreed / (1024 * 1024 * 1024)} GB"
            }
        }
}
