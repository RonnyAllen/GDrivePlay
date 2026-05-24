package com.driveplay.domain.model

data class PlaylistItem(
    val fileId: String,
    val name: String,
    val durationMs: Long,
    val size: Long,
    val thumbnailLink: String?,
    val mimeType: String,
    val parentFolderId: String
)

data class WatchHistoryEntry(
    val fileId: String,
    val positionMs: Long,
    val durationMs: Long,
    val timestamp: Long
)

data class StreamUrl(
    val url: String,
    val token: String
)
