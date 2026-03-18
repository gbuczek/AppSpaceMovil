package com.appspace.movil.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.appspace.movil.model.TaskType

/**
 * Entidad Room para almacenar informes de limpieza
 */
@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val taskType: String, // TaskType como String
    val spaceFreed: Long = 0,
    val filesDeleted: Int = 0,
    val filesScanned: Int = 0,
    val duplicatesFound: Int = 0,
    val cacheCleared: Int = 0,
    val detailsJson: String? = null,
    val summary: String = ""
) {
    fun toReport(): com.appspace.movil.model.CleanReport {
        return com.appspace.movil.model.CleanReport(
            id = id,
            timestamp = timestamp,
            taskType = TaskType.valueOf(taskType),
            spaceFreed = spaceFreed,
            filesDeleted = filesDeleted,
            filesScanned = filesScanned,
            duplicatesFound = duplicatesFound,
            cacheCleared = cacheCleared,
            summary = summary
        )
    }
    
    companion object {
        fun fromReport(report: com.appspace.movil.model.CleanReport): ReportEntity {
            return ReportEntity(
                id = report.id,
                timestamp = report.timestamp,
                taskType = report.taskType.name,
                spaceFreed = report.spaceFreed,
                filesDeleted = report.filesDeleted,
                filesScanned = report.filesScanned,
                duplicatesFound = report.duplicatesFound,
                cacheCleared = report.cacheCleared,
                summary = report.summary
            )
        }
    }
}
