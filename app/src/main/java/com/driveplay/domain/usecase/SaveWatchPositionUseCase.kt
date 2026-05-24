package com.driveplay.domain.usecase

import com.driveplay.data.db.WatchHistoryDao
import com.driveplay.data.db.WatchHistoryEntity
import javax.inject.Inject

class SaveWatchPositionUseCase @Inject constructor(
    private val watchHistoryDao: WatchHistoryDao
) {
    suspend operator fun invoke(
        fileId: String,
        name: String,
        positionMs: Long,
        durationMs: Long,
        thumbnailLink: String?
    ) {
        if (durationMs <= 0) return

        val ratio = positionMs.toDouble() / durationMs.toDouble()
        if (ratio in 0.05..0.95) {
            val entity = WatchHistoryEntity(
                fileId = fileId,
                positionMs = positionMs,
                durationMs = durationMs,
                timestamp = System.currentTimeMillis(),
                name = name,
                thumbnailLink = thumbnailLink
            )
            watchHistoryDao.insertOrUpdate(entity)
        } else if (ratio > 0.95) {
            // Delete historical bookmark if video is finished (>95%)
            watchHistoryDao.delete(fileId)
        }
    }
}
