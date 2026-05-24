package com.driveplay.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val fileId: String,
    val positionMs: Long,
    val durationMs: Long,
    val timestamp: Long,
    val name: String,
    val thumbnailLink: String? = null
)

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY timestamp DESC")
    fun getWatchHistoryFlow(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE fileId = :fileId")
    suspend fun getWatchHistoryEntry(fileId: String): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE fileId = :fileId")
    suspend fun delete(fileId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()
}
