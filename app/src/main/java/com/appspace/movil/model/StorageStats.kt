package com.appspace.movil.model

/**
 * Estadísticas de almacenamiento
 */
data class StorageStats(
    val totalSpace: Long,
    val usedSpace: Long,
    val freeSpace: Long,
    val categoryStats: Map<FileCategory, CategoryStat> = emptyMap()
) {
    val usagePercent: Float
        get() = (usedSpace.toFloat() / totalSpace.toFloat()) * 100
    
    val formattedTotal: String
        get() = FileItem.formatSize(totalSpace)
    
    val formattedUsed: String
        get() = FileItem.formatSize(usedSpace)
    
    val formattedFree: String
        get() = FileItem.formatSize(freeSpace)
}

/**
 * Estadísticas por categoría
 */
data class CategoryStat(
    val category: FileCategory,
    val totalSize: Long,
    val fileCount: Int,
    val removableSize: Long = 0,
    val removableCount: Int = 0
) {
    val formattedTotalSize: String
        get() = FileItem.formatSize(totalSize)
    
    val formattedRemovableSize: String
        get() = FileItem.formatSize(removableSize)
}
