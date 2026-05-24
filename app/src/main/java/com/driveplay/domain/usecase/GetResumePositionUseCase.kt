package com.driveplay.domain.usecase

import com.driveplay.data.db.WatchHistoryDao
import com.driveplay.domain.model.WatchHistoryEntry
import javax.inject.Inject

class GetResumePositionUseCase @Inject constructor(
    private val watchHistoryDao: WatchHistoryDao
) {
    suspend operator fun invoke(fileId: String): WatchHistoryEntry? {
        val entity = watchHistoryDao.getWatchHistoryEntry(fileId) ?: return null
        return WatchHistoryEntry(
            fileId = entity.fileId,
            positionMs = entity.positionMs,
            durationMs = entity.durationMs,
            timestamp = entity.timestamp
        )
    }
}
