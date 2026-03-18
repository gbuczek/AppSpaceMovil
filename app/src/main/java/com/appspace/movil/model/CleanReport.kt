package com.appspace.movil.model

/**
 * Informe de limpieza generado después de cada tarea
 */
data class CleanReport(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val taskType: TaskType,
    val spaceFreed: Long = 0,
    val filesDeleted: Int = 0,
    val filesScanned: Int = 0,
    val duplicatesFound: Int = 0,
    val cacheCleared: Int = 0,
    val details: List<ReportDetail> = emptyList(),
    val summary: String = ""
) {
    val formattedSpaceFreed: String
        get() = FileItem.formatSize(spaceFreed)
    
    val formattedDate: String
        get() {
            val date = java.util.Date(timestamp)
            val format = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            return format.format(date)
        }
}

/**
 * Detalle individual dentro del informe
 */
data class ReportDetail(
    val description: String,
    val spaceFreed: Long = 0,
    val count: Int = 0,
    val category: FileCategory? = null
)

/**
 * Tipos de tareas que generan informe
 */
enum class TaskType {
    SCAN,
    CLEAN_CACHE,
    CLEAN_DUPLICATES,
    CLEAN_DOWNLOADS,
    CLEAN_ALL,
    ORGANIZE_FILES,
    CUSTOM
}
