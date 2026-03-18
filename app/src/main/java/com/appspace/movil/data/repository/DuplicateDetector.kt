package com.appspace.movil.data.repository

import com.appspace.movil.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

/**
 * Detector de archivos duplicados usando hash MD5
 */
class DuplicateDetector {
    
    /**
     * Encuentra archivos duplicados en una lista
     */
    suspend fun findDuplicates(files: List<FileItem>): List<DuplicateGroup> = withContext(Dispatchers.IO) {
        // Filtrar archivos por tamaño (solo los que tienen tamaño similar)
        val filesBySize = files.groupBy { it.size }
        
        val duplicateGroups = mutableListOf<DuplicateGroup>()
        
        // Solo verificar archivos con el mismo tamaño
        filesBySize.forEach { (_, filesWithSameSize) ->
            if (filesWithSameSize.size > 1) {
                // Calcular hash para archivos del mismo tamaño
                val filesByHash = mutableMapOf<String, MutableList<FileItem>>()
                
                filesWithSameSize.forEach { file ->
                    val hash = calculateHash(file)
                    if (hash != null) {
                        if (!filesByHash.containsKey(hash)) {
                            filesByHash[hash] = mutableListOf()
                        }
                        filesByHash[hash]?.add(file)
                    }
                }
                
                // Grupos con más de un archivo son duplicados
                filesByHash.forEach { (_, filesWithSameHash) ->
                    if (filesWithSameHash.size > 1) {
                        // Marcar todos como duplicados excepto el más antiguo
                        val sortedFiles = filesWithSameHash.sortedBy { it.lastModified }
                        val original = sortedFiles.first()
                        val duplicates = sortedFiles.drop(1)
                        
                        duplicateGroups.add(
                            DuplicateGroup(
                                original = original,
                                duplicates = duplicates,
                                hash = filesWithSameHash.first().hash,
                                spaceWasted = duplicates.sumOf { it.size }
                            )
                        )
                    }
                }
            }
        }
        
        duplicateGroups.sortedByDescending { it.spaceWasted }
    }
    
    /**
     * Calcula el hash MD5 de un archivo
     */
    private fun calculateHash(file: FileItem): String? {
        return try {
            val fileObj = File(file.path)
            if (!fileObj.exists() || !fileObj.canRead()) {
                return null
            }
            
            // Para archivos muy grandes, usar solo una parte para mejorar rendimiento
            val inputStream = FileInputStream(fileObj)
            val messageDigest = MessageDigest.getInstance("MD5")
            
            val buffer = ByteArray(8192)
            var bytesRead: Int
            
            // Leer todo el archivo para hash completo
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                messageDigest.update(buffer, 0, bytesRead)
            }
            
            inputStream.close()
            
            val hashBytes = messageDigest.digest()
            val hexString = StringBuilder()
            
            for (b in hashBytes) {
                val hex = Integer.toHexString(0xff and b.toInt())
                if (hex.length == 1) hexString.append('0')
                hexString.append(hex)
            }
            
            hexString.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Calcula hash rápido usando solo el inicio y fin del archivo
     * (más rápido pero menos preciso)
     */
    private fun calculateQuickHash(file: FileItem): String? {
        return try {
            val fileObj = File(file.path)
            if (!fileObj.exists() || !fileObj.canRead()) {
                return null
            }
            
            val inputStream = FileInputStream(fileObj)
            val messageDigest = MessageDigest.getInstance("MD5")
            
            val buffer = ByteArray(8192)
            
            // Leer primeros bytes
            val bytesRead = inputStream.read(buffer)
            if (bytesRead > 0) {
                messageDigest.update(buffer, 0, bytesRead)
            }
            
            // Si el archivo es grande, leer también los últimos bytes
            if (file.size > 16384) {
                inputStream.seek(fileObj.length() - 8192)
                val lastBytesRead = inputStream.read(buffer)
                if (lastBytesRead > 0) {
                    messageDigest.update(buffer, 0, lastBytesRead)
                }
            }
            
            inputStream.close()
            
            val hashBytes = messageDigest.digest()
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Encuentra duplicados similares (mismo nombre o nombre similar)
     * Útil para fotos con diferentes resoluciones
     */
    suspend fun findSimilarByName(files: List<FileItem>): List<SimilarFileGroup> = withContext(Dispatchers.IO) {
        val groupsByName = mutableMapOf<String, MutableList<FileItem>>()
        
        files.forEach { file ->
            // Extraer nombre base sin extensión ni números
            val baseName = extractBaseName(file.name)
            
            if (!groupsByName.containsKey(baseName)) {
                groupsByName[baseName] = mutableListOf()
            }
            groupsByName[baseName]?.add(file)
        }
        
        val similarGroups = mutableListOf<SimilarFileGroup>()
        
        groupsByName.forEach { (_, filesWithSimilarName) ->
            if (filesWithSimilarName.size > 1) {
                similarGroups.add(
                    SimilarFileGroup(
                        baseName = filesWithSimilarName.first().name,
                        files = filesWithSimilarName,
                        totalSize = filesWithSimilarName.sumOf { it.size }
                    )
                )
            }
        }
        
        similarGroups
    }
    
    /**
     * Extrae el nombre base de un archivo (sin números de duplicado)
     */
    private fun extractBaseName(fileName: String): String {
        // Eliminar extensión
        val nameWithoutExt = fileName.substringBeforeLast(".")
        
        // Eliminar patrones como (1), (2), _copy, etc.
        return nameWithoutExt
            .replace(Regex("\\s*\\(\\d+\\)\\s*"), "")
            .replace(Regex("\\s*_?copy\\d*\\s*"), "")
            .replace(Regex("\\s*\\d+\\s*"), "")
            .trim()
            .lowercase()
    }
}

/**
 * Grupo de archivos duplicados
 */
data class DuplicateGroup(
    val original: FileItem,
    val duplicates: List<FileItem>,
    val hash: String?,
    val spaceWasted: Long
) {
    val totalFiles: Int
        get() = duplicates.size + 1
    
    val formattedSpaceWasted: String
        get() = FileItem.formatSize(spaceWasted)
    
    val allFiles: List<FileItem>
        get() = listOf(original) + duplicates
}

/**
 * Grupo de archivos similares por nombre
 */
data class SimilarFileGroup(
    val baseName: String,
    val files: List<FileItem>,
    val totalSize: Long
) {
    val fileCount: Int
        get() = files.size
    
    val formattedTotalSize: String
        get() = FileItem.formatSize(totalSize)
}
