package com.appspace.movil.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object para operaciones con informes
 */
@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ReportEntity>>
    
    @Query("SELECT * FROM reports WHERE taskType = :taskType ORDER BY timestamp DESC")
    fun getReportsByType(taskType: String): Flow<List<ReportEntity>>
    
    @Query("SELECT * FROM reports WHERE id = :id")
    suspend fun getReportById(id: Long): ReportEntity?
    
    @Query("SELECT COUNT(*) FROM reports")
    suspend fun getReportCount(): Int
    
    @Query("SELECT SUM(spaceFreed) FROM reports")
    suspend fun getTotalSpaceFreed(): Long?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long
    
    @Update
    suspend fun updateReport(report: ReportEntity)
    
    @Delete
    suspend fun deleteReport(report: ReportEntity)
    
    @Query("DELETE FROM reports WHERE timestamp < :olderThan")
    suspend fun deleteOldReports(olderThan: Long)
    
    @Query("DELETE FROM reports")
    suspend fun deleteAllReports()
}
