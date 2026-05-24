package com.driveplay.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Entity(tableName = "offline_files")
data class OfflineFileEntity(
    @PrimaryKey val fileId: String,
    val name: String,
    val size: Long,
    val localPath: String,
    val thumbnailLink: String?,
    val durationMs: Long,
    val downloadTimestamp: Long
)

@Dao
interface OfflineFileDao {
    @Query("SELECT * FROM offline_files ORDER BY downloadTimestamp DESC")
    suspend fun getOfflineFiles(): List<OfflineFileEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM offline_files WHERE fileId = :fileId)")
    suspend fun isFileOffline(fileId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflineFile(file: OfflineFileEntity)

    @Query("DELETE FROM offline_files WHERE fileId = :fileId")
    suspend fun deleteOfflineFile(fileId: String)
}
