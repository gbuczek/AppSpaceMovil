package com.appspace.movil.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para operaciones con archivos
 */
@Dao
interface FileDao {
    @Query("SELECT * FROM files ORDER BY lastModified DESC")
    fun getAllFiles(): Flow<List<FileEntity>>
    
    @Query("SELECT * FROM files WHERE category = :category ORDER BY size DESC")
    fun getFilesByCategory(category: String): Flow<List<FileEntity>>
    
    @Query("SELECT * FROM files WHERE isDuplicate = 1 ORDER BY size DESC")
    fun getDuplicateFiles(): Flow<List<FileEntity>>
    
    @Query("SELECT * FROM files WHERE hash = :hash")
    fun getFilesByHash(hash: String): List<FileEntity>
    
    @Query("SELECT COUNT(*) FROM files")
    suspend fun getFileCount(): Int
    
    @Query("SELECT SUM(size) FROM files")
    suspend fun getTotalSize(): Long?
    
    @Query("SELECT category, COUNT(*) as count, SUM(size) as totalSize FROM files GROUP BY category")
    suspend fun getCategoryStats(): List<Map<String, Any>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<FileEntity>): List<Long>
    
    @Update
    suspend fun updateFile(file: FileEntity)
    
    @Delete
    suspend fun deleteFile(file: FileEntity)
    
    @Query("DELETE FROM files WHERE id = :id")
    suspend fun deleteFileById(id: Long)
    
    @Query("DELETE FROM files WHERE isDuplicate = 1")
    suspend fun deleteAllDuplicates()
    
    @Query("DELETE FROM files WHERE category = :category")
    suspend fun deleteFilesByCategory(category: String)
    
    @Query("DELETE FROM files")
    suspend fun deleteAllFiles()
}
