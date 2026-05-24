package com.driveplay.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Entity(tableName = "queue_items")
data class QueueItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileId: String,
    val name: String,
    val durationMs: Long,
    val size: Long,
    val thumbnailLink: String?,
    val mimeType: String,
    val parentFolderId: String,
    val displayOrder: Int
)

@Entity(tableName = "saved_playlists")
data class SavedPlaylistEntity(
    @PrimaryKey val playlistName: String,
    val itemCount: Int,
    val thumbnailCollage: String // Comma separated list of first 4 thumbnails
)

@Dao
interface PlaylistDao {
    // Active Queue Management
    @Query("SELECT * FROM queue_items ORDER BY displayOrder ASC")
    suspend fun getActiveQueue(): List<QueueItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQueue(items: List<QueueItemEntity>)

    @Query("DELETE FROM queue_items")
    suspend fun clearQueue()

    // Saved Playlist Operations
    @Query("SELECT * FROM saved_playlists")
    suspend fun getAllPlaylists(): List<SavedPlaylistEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: SavedPlaylistEntity)

    @Query("DELETE FROM saved_playlists WHERE playlistName = :name")
    suspend fun deletePlaylist(name: String)
}
