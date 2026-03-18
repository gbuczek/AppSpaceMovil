package com.appspace.movil.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.appspace.movil.model.FileCategory

/**
 * Entidad Room para almacenar archivos escaneados
 */
@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val path: String,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val category: String, // FileCategory como String
    val isDuplicate: Boolean = false,
    val hash: String? = null,
    val mimeType: String? = null,
    val thumbnailPath: String? = null,
    val scanDate: Long = System.currentTimeMillis()
) {
    fun toFileItem(): com.appspace.movil.model.FileItem {
        return com.appspace.movil.model.FileItem(
            id = id,
            path = path,
            name = name,
            size = size,
            lastModified = lastModified,
            category = FileCategory.valueOf(category),
            isDuplicate = isDuplicate,
            hash = hash,
            mimeType = mimeType,
            thumbnailPath = thumbnailPath
        )
    }
    
    companion object {
        fun fromFileItem(file: com.appspace.movil.model.FileItem): FileEntity {
            return FileEntity(
                id = file.id,
                path = file.path,
                name = file.name,
                size = file.size,
                lastModified = file.lastModified,
                category = file.category.name,
                isDuplicate = file.isDuplicate,
                hash = file.hash,
                mimeType = file.mimeType,
                thumbnailPath = file.thumbnailPath
            )
        }
    }
}
