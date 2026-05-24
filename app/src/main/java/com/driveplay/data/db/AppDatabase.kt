package com.driveplay.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        WatchHistoryEntity::class,
        QueueItemEntity::class,
        SavedPlaylistEntity::class,
        OfflineFileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun offlineFileDao(): OfflineFileDao
}
