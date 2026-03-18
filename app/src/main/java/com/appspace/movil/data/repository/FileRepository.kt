package com.appspace.movil.data.repository

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.appspace.movil.model.FileCategory
import com.appspace.movil.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Repositorio para operaciones con el sistema de archivos
 */
class FileRepository(private val context: Context) {
    
    /**
     * Escanea todos los archivos del dispositivo
     */
    suspend fun scanAllFiles(): Flow<FileScanProgress> = flow {
        val allFiles = mutableListOf<FileItem>()
        var totalScanned = 0
        var currentCategory = ""
        
        // Escanear cada categoría
        val categories = listOf(
            FileCategory.PHOTOS,
            FileCategory.VIDEOS,
            FileCategory.AUDIO,
            FileCategory.DOCUMENTS,
            FileCategory.DOWNLOADS,
            FileCategory.APK,
            FileCategory.CACHE
        )
        
        for (category in categories) {
            currentCategory = category.name
            emit(FileScanProgress.ScanningCategory(category, totalScanned))
            
            val files = getFilesByCategory(category)
            allFiles.addAll(files)
            totalScanned += files.size
            
            emit(FileScanProgress.CategoryComplete(category, files.size, totalScanned))
        }
        
        emit(FileScanProgress.Complete(allFiles, totalScanned))
    }.flowOn(Dispatchers.IO)
    
    /**
     * Obtiene archivos por categoría
     */
    suspend fun getFilesByCategory(category: FileCategory): List<FileItem> = withContext(Dispatchers.IO) {
        when (category) {
            FileCategory.PHOTOS -> getPhotos()
            FileCategory.VIDEOS -> getVideos()
            FileCategory.AUDIO -> getAudioFiles()
            FileCategory.DOCUMENTS -> getDocuments()
            FileCategory.DOWNLOADS -> getDownloads()
            FileCategory.APK -> getApkFiles()
            FileCategory.CACHE -> getCacheFiles()
            FileCategory.OTHER -> emptyList()
        }
    }
    
    /**
     * Escanea fotos usando MediaStore
     */
    private fun getPhotos(): List<FileItem> {
        val photos = mutableListOf<FileItem>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_MODIFIED
        )
        
        try {
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
            )
            
            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dataColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val dateColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                
                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val path = it.getString(dataColumn)
                    val name = it.getString(nameColumn)
                    val size = it.getLong(sizeColumn)
                    val dateModified = it.getLong(dateColumn)
                    
                    photos.add(
                        FileItem(
                            id = id,
                            path = path,
                            name = name,
                            size = size,
                            lastModified = dateModified * 1000,
                            category = FileCategory.PHOTOS,
                            mimeType = "image/*"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return photos
    }
    
    /**
     * Escanea videos usando MediaStore
     */
    private fun getVideos(): List<FileItem> {
        val videos = mutableListOf<FileItem>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_MODIFIED
        )
        
        try {
            val cursor = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Video.Media.DATE_MODIFIED} DESC"
            )
            
            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val dataColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val nameColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val dateColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
                
                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val path = it.getString(dataColumn)
                    val name = it.getString(nameColumn)
                    val size = it.getLong(sizeColumn)
                    val dateModified = it.getLong(dateColumn)
                    
                    videos.add(
                        FileItem(
                            id = id,
                            path = path,
                            name = name,
                            size = size,
                            lastModified = dateModified * 1000,
                            category = FileCategory.VIDEOS,
                            mimeType = "video/*"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return videos
    }
    
    /**
     * Escanea archivos de audio
     */
    private fun getAudioFiles(): List<FileItem> {
        val audioFiles = mutableListOf<FileItem>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_MODIFIED
        )
        
        try {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"
            )
            
            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val nameColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dateColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
                
                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val path = it.getString(dataColumn)
                    val name = it.getString(nameColumn)
                    val size = it.getLong(sizeColumn)
                    val dateModified = it.getLong(dateColumn)
                    
                    audioFiles.add(
                        FileItem(
                            id = id,
                            path = path,
                            name = name,
                            size = size,
                            lastModified = dateModified * 1000,
                            category = FileCategory.AUDIO,
                            mimeType = "audio/*"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return audioFiles
    }
    
    /**
     * Escanea documentos
     */
    private suspend fun getDocuments(): List<FileItem> {
        val documents = mutableListOf<FileItem>()
        val documentExtensions = setOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf")
        
        scanDirectoryForExtensions(documentExtensions, documents, FileCategory.DOCUMENTS)
        
        return documents
    }
    
    /**
     * Escanea la carpeta de Descargas
     */
    private suspend fun getDownloads(): List<FileItem> {
        val downloads = mutableListOf<FileItem>()
        val downloadDir = File(context.getExternalFilesDir(null), "Download")
        
        if (downloadDir.exists()) {
            scanDirectory(downloadDir, downloads, FileCategory.DOWNLOADS)
        }
        
        // También escanear la carpeta pública de Descargas
        val publicDownloadDir = android.os.Environment.getExternalStoragePublicDirectory(
            android.os.Environment.DIRECTORY_DOWNLOADS
        )
        
        if (publicDownloadDir.exists()) {
            scanDirectory(publicDownloadDir, downloads, FileCategory.DOWNLOADS)
        }
        
        return downloads
    }
    
    /**
     * Escanea archivos APK
     */
    private suspend fun getApkFiles(): List<FileItem> {
        val apkFiles = mutableListOf<FileItem>()
        val apkExtensions = setOf("apk")
        
        scanDirectoryForExtensions(apkExtensions, apkFiles, FileCategory.APK)
        
        return apkFiles
    }
    
    /**
     * Escanea archivos de caché
     */
    private suspend fun getCacheFiles(): List<FileItem> {
        val cacheFiles = mutableListOf<FileItem>()
        
        // Caché de la aplicación
        val appCache = context.cacheDir
        if (appCache.exists()) {
            scanDirectory(appCache, cacheFiles, FileCategory.CACHE)
        }
        
        // Caché externa
        val externalCache = context.externalCacheDir
        if (externalCache?.exists() == true) {
            scanDirectory(externalCache, cacheFiles, FileCategory.CACHE)
        }
        
        return cacheFiles
    }
    
    /**
     * Escanea un directorio buscando extensiones específicas
     */
    private suspend fun scanDirectoryForExtensions(
        extensions: Set<String>,
        resultList: MutableList<FileItem>,
        category: FileCategory
    ) {
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED
        )
        
        val selection = extensions.joinToString(" OR ") { 
            "${MediaStore.Files.FileColumns.DATA} LIKE '%.$it'" 
        }
        
        try {
            val cursor = context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                null,
                "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
            )
            
            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val dataColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val nameColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                
                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val path = it.getString(dataColumn)
                    val name = it.getString(nameColumn)
                    val size = it.getLong(sizeColumn)
                    val dateModified = it.getLong(dateColumn)
                    
                    resultList.add(
                        FileItem(
                            id = id,
                            path = path,
                            name = name,
                            size = size,
                            lastModified = dateModified * 1000,
                            category = category
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Escanea un directorio recursivamente
     */
    private fun scanDirectory(
        directory: File,
        resultList: MutableList<FileItem>,
        category: FileCategory,
        maxDepth: Int = 3,
        currentDepth: Int = 0
    ) {
        if (currentDepth >= maxDepth) return
        
        try {
            directory.listFiles()?.forEach { file ->
                if (file.isFile && !file.isHidden) {
                    resultList.add(
                        FileItem(
                            path = file.absolutePath,
                            name = file.name,
                            size = file.length(),
                            lastModified = file.lastModified(),
                            category = category
                        )
                    )
                } else if (file.isDirectory && !file.isHidden) {
                    scanDirectory(file, resultList, category, maxDepth, currentDepth + 1)
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
    
    /**
     * Elimina un archivo
     */
    suspend fun deleteFile(file: FileItem): Boolean = withContext(Dispatchers.IO) {
        try {
            val fileObj = File(file.path)
            if (fileObj.exists()) {
                fileObj.delete()
            } else {
                // Intentar eliminar a través de MediaStore
                val uri = when (file.category) {
                    FileCategory.PHOTOS -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    FileCategory.VIDEOS -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    FileCategory.AUDIO -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    else -> null
                }
                
                uri?.let {
                    context.contentResolver.delete(it, "${MediaStore.MediaColumns._ID}=?", 
                        arrayOf(file.id.toString()))
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Elimina múltiples archivos
     */
    suspend fun deleteFiles(files: List<FileItem>): Int = withContext(Dispatchers.IO) {
        var deletedCount = 0
        files.forEach { file ->
            if (deleteFile(file)) {
                deletedCount++
            }
        }
        deletedCount
    }
}

/**
 * Progreso del escaneo
 */
sealed class FileScanProgress {
    data class ScanningCategory(val category: FileCategory, val scannedSoFar: Int) : FileScanProgress()
    data class CategoryComplete(val category: FileCategory, val count: Int, val total: Int) : FileScanProgress()
    data class Complete(val files: List<FileItem>, val totalScanned: Int) : FileScanProgress()
}
