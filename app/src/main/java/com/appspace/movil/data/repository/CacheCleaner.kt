package com.appspace.movil.data.repository

import android.content.Context
import com.appspace.movil.model.FileCategory
import com.appspace.movil.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Limpiador de caché y archivos temporales
 */
class CacheCleaner(private val context: Context) {
    
    /**
     * Obtiene todos los archivos de caché
     */
    suspend fun getCacheFiles(): List<FileItem> = withContext(Dispatchers.IO) {
        val cacheFiles = mutableListOf<FileItem>()
        
        // Caché interna de la app
        val appCache = context.cacheDir
        if (appCache.exists()) {
            scanCacheDirectory(appCache, cacheFiles)
        }
        
        // Caché externa
        val externalCache = context.externalCacheDir
        if (externalCache?.exists() == true) {
            scanCacheDirectory(externalCache, cacheFiles)
        }
        
        // Caché de otras apps (requiere permisos especiales)
        val systemCacheDir = File("/data/data/*/cache")
        // Nota: Esto requiere root o permisos de sistema
        
        cacheFiles
    }
    
    /**
     * Escanea un directorio de caché
     */
    private fun scanCacheDirectory(directory: File, resultList: MutableList<FileItem>) {
        try {
            directory.walkTopDown().maxDepth(5).forEach { file ->
                if (file.isFile && !file.isHidden) {
                    resultList.add(
                        FileItem(
                            path = file.absolutePath,
                            name = file.name,
                            size = file.length(),
                            lastModified = file.lastModified(),
                            category = FileCategory.CACHE
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    
    /**
     * Limpia la caché de la aplicación
     */
    suspend fun cleanAppCache(): CleanResult = withContext(Dispatchers.IO) {
        var freedSpace = 0L
        var deletedCount = 0
        val errors = mutableListOf<String>()
        
        try {
            // Limpiar caché interna
            context.cacheDir.deleteRecursively()
            
            // Limpiar caché externa
            context.externalCacheDir?.deleteRecursively()
            
            // Recalcular espacio liberado
            freedSpace = calculateCacheSize()
            deletedCount = 1 // Se considera 1 operación de limpieza
            
            CleanResult(
                success = true,
                spaceFreed = freedSpace,
                filesDeleted = deletedCount,
                errors = errors
            )
        } catch (e: Exception) {
            errors.add(e.message ?: "Error desconocido")
            CleanResult(
                success = false,
                spaceFreed = freedSpace,
                filesDeleted = deletedCount,
                errors = errors
            )
        }
    }
    
    /**
     * Limpia archivos temporales
     */
    suspend fun cleanTempFiles(): CleanResult = withContext(Dispatchers.IO) {
        var freedSpace = 0L
        var deletedCount = 0
        val errors = mutableListOf<String>()
        
        try {
            // Directorio temporal
            val tempDir = File(context.cacheDir, "temp")
            if (tempDir.exists()) {
                val files = tempDir.listFiles()
                files?.forEach { file ->
                    try {
                        freedSpace += file.length()
                        if (file.delete()) {
                            deletedCount++
                        }
                    } catch (e: Exception) {
                        errors.add("No se pudo eliminar: ${file.name}")
                    }
                }
            }
            
            // Archivos .tmp en el sistema
            val tmpFiles = findTmpFiles()
            tmpFiles.forEach { file ->
                try {
                    freedSpace += file.length()
                    if (file.delete()) {
                        deletedCount++
                    }
                } catch (e: Exception) {
                    errors.add("No se pudo eliminar: ${file.name}")
                }
            }
            
            CleanResult(
                success = true,
                spaceFreed = freedSpace,
                filesDeleted = deletedCount,
                errors = errors
            )
        } catch (e: Exception) {
            errors.add(e.message ?: "Error desconocido")
            CleanResult(
                success = false,
                spaceFreed = freedSpace,
                filesDeleted = deletedCount,
                errors = errors
            )
        }
    }
    
    /**
     * Encuentra archivos .tmp
     */
    private fun findTmpFiles(): List<File> {
        val tmpFiles = mutableListOf<File>()
        
        try {
            // Buscar en directorios comunes
            val searchDirs = listOf(
                context.cacheDir,
                context.externalCacheDir,
                context.filesDir
            )
            
            searchDirs.forEach { dir ->
                dir?.walkTopDown()?.maxDepth(3)?.forEach { file ->
                    if (file.isFile && 
                        (file.extension == "tmp" || file.extension == "temp" || file.name.endsWith(".tmp"))) {
                        tmpFiles.add(file)
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        
        return tmpFiles
    }
    
    /**
     * Limpia caché de thumbnails
     */
    suspend fun cleanThumbnailCache(): CleanResult = withContext(Dispatchers.IO) {
        var freedSpace = 0L
        var deletedCount = 0
        val errors = mutableListOf<String>()
        
        try {
            val thumbCache = File(context.cacheDir, "thumbnails")
            if (thumbCache.exists()) {
                val files = thumbCache.listFiles()
                files?.forEach { file ->
                    try {
                        freedSpace += file.length()
                        if (file.delete()) {
                            deletedCount++
                        }
                    } catch (e: Exception) {
                        errors.add("No se pudo eliminar: ${file.name}")
                    }
                }
            }
            
            CleanResult(
                success = true,
                spaceFreed = freedSpace,
                filesDeleted = deletedCount,
                errors = errors
            )
        } catch (e: Exception) {
            errors.add(e.message ?: "Error desconocido")
            CleanResult(
                success = false,
                spaceFreed = freedSpace,
                filesDeleted = deletedCount,
                errors = errors
            )
        }
    }
    
    /**
     * Limpia todo el caché
     */
    suspend fun cleanAll(): CleanResult = withContext(Dispatchers.IO) {
        var totalFreedSpace = 0L
        var totalDeletedCount = 0
        val allErrors = mutableListOf<String>()
        
        // Limpiar caché de app
        val appCacheResult = cleanAppCache()
        totalFreedSpace += appCacheResult.spaceFreed
        totalDeletedCount += appCacheResult.filesDeleted
        allErrors.addAll(appCacheResult.errors)
        
        // Limpiar archivos temporales
        val tempResult = cleanTempFiles()
        totalFreedSpace += tempResult.spaceFreed
        totalDeletedCount += tempResult.filesDeleted
        allErrors.addAll(tempResult.errors)
        
        // Limpiar caché de thumbnails
        val thumbResult = cleanThumbnailCache()
        totalFreedSpace += thumbResult.spaceFreed
        totalDeletedCount += thumbResult.filesDeleted
        allErrors.addAll(thumbResult.errors)
        
        CleanResult(
            success = allErrors.isEmpty(),
            spaceFreed = totalFreedSpace,
            filesDeleted = totalDeletedCount,
            errors = allErrors
        )
    }
    
    /**
     * Calcula el tamaño total del caché
     */
    suspend fun calculateCacheSize(): Long = withContext(Dispatchers.IO) {
        var totalSize = 0L
        
        try {
            // Caché interna
            context.cacheDir?.let {
                totalSize += getDirectorySize(it)
            }
            
            // Caché externa
            context.externalCacheDir?.let {
                totalSize += getDirectorySize(it)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        
        totalSize
    }
    
    /**
     * Calcula el tamaño de un directorio
     */
    private fun getDirectorySize(directory: File): Long {
        var size = 0L
        
        try {
            directory.walkTopDown().maxDepth(5).forEach { file ->
                if (file.isFile) {
                    size += file.length()
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        
        return size
    }
    
    /**
     * Obtiene estadísticas de caché
     */
    suspend fun getCacheStats(): CacheStats = withContext(Dispatchers.IO) {
        val appCacheSize = context.cacheDir?.let { getDirectorySize(it) } ?: 0L
        val externalCacheSize = context.externalCacheDir?.let { getDirectorySize(it) } ?: 0L
        val thumbnailCacheSize = File(context.cacheDir, "thumbnails").let { 
            if (it.exists()) getDirectorySize(it) else 0L 
        }
        
        CacheStats(
            appCacheSize = appCacheSize,
            externalCacheSize = externalCacheSize,
            thumbnailCacheSize = thumbnailCacheSize,
            totalCacheSize = appCacheSize + externalCacheSize + thumbnailCacheSize
        )
    }
}

/**
 * Resultado de una operación de limpieza
 */
data class CleanResult(
    val success: Boolean,
    val spaceFreed: Long,
    val filesDeleted: Int,
    val errors: List<String>
) {
    val formattedSpaceFreed: String
        get() = FileItem.formatSize(spaceFreed)
}

/**
 * Estadísticas de caché
 */
data class CacheStats(
    val appCacheSize: Long,
    val externalCacheSize: Long,
    val thumbnailCacheSize: Long,
    val totalCacheSize: Long
) {
    val formattedAppCache: String
        get() = FileItem.formatSize(appCacheSize)
    
    val formattedExternalCache: String
        get() = FileItem.formatSize(externalCacheSize)
    
    val formattedThumbnailCache: String
        get() = FileItem.formatSize(thumbnailCacheSize)
    
    val formattedTotal: String
        get() = FileItem.formatSize(totalCacheSize)
}
