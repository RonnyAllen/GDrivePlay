package com.driveplay.domain.usecase

import com.driveplay.data.remote.DriveRepository
import com.driveplay.domain.model.PlaylistItem
import com.driveplay.domain.util.NaturalSortComparator
import javax.inject.Inject

class GetFolderContentsUseCase @Inject constructor(
    private val driveRepository: DriveRepository
) {
    private val acceptedVideoTypes = listOf(
        "video/mp4",
        "video/x-matroska",
        "video/webm",
        "video/avi",
        "video/quicktime",
        "video/x-msvideo",
        "video/3gpp",
        "video/mpeg",
        "video/ogg"
    )

    sealed class FolderResult {
        data class Success(val folders: List<PlaylistItem>, val videos: List<PlaylistItem>, val nextPageToken: String?) : FolderResult()
        data class Error(val exception: Throwable) : FolderResult()
    }

    suspend operator fun invoke(folderId: String, pageToken: String? = null): FolderResult {
        val result = driveRepository.getFolderContents(folderId, pageToken)
        if (result.isFailure) {
            return FolderResult.Error(result.exceptionOrNull() ?: Exception("Unknown API error"))
        }

        val driveResponse = result.getOrThrow()
        val allItems = driveResponse.files.map { file ->
            PlaylistItem(
                fileId = file.id,
                name = file.name,
                durationMs = file.videoMediaMetadata?.durationMillis ?: 0L,
                size = file.size ?: 0L,
                thumbnailLink = file.thumbnailLink,
                mimeType = file.mimeType,
                parentFolderId = folderId
            )
        }

        // Separate folders and videos
        val folders = allItems.filter { it.mimeType == "application/vnd.google-apps.folder" }
            .sortedWith(compareBy(NaturalSortComparator) { it.name })

        val videos = allItems.filter { acceptedVideoTypes.contains(it.mimeType) }
            .sortedWith(compareBy(NaturalSortComparator) { it.name })

        return FolderResult.Success(folders, videos, driveResponse.nextPageToken)
    }
}
