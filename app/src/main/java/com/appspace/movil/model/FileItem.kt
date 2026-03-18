package com.appspace.movil.model

/**
 * Representa un archivo en el dispositivo
 */
data class FileItem(
    val id: Long = 0,
    val path: String,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val category: FileCategory,
    val isDuplicate: Boolean = false,
    val hash: String? = null,
    val mimeType: String? = null,
    val thumbnailPath: String? = null
) {
    val formattedSize: String
        get() = formatSize(size)
    
    companion object {
        fun formatSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
                else -> "${bytes / (1024 * 1024 * 1024)} GB"
            }
        }
    }
}

/**
 * Categorías de archivos
 */
enum class FileCategory {
    PHOTOS,
    VIDEOS,
    DOCUMENTS,
    AUDIO,
    CACHE,
    DOWNLOADS,
    APK,
    OTHER
}
