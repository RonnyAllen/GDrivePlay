package com.driveplay.domain.usecase

import com.driveplay.data.remote.DriveRepository
import javax.inject.Inject

enum class DriveIdType { FOLDER, FILE }

data class DriveLinkResult(
    val type: DriveIdType,
    val resolvedFolderId: String,
    val targetFileId: String? = null
)

class LoadFolderByLinkUseCase @Inject constructor(
    private val driveRepository: DriveRepository
) {
    suspend operator fun invoke(url: String): Result<DriveLinkResult> {
        val parsed = extractDriveId(url) ?: return Result.failure(Exception("Unsupported or invalid Google Drive URL format"))

        return when (parsed.first) {
            DriveIdType.FOLDER -> {
                Result.success(
                    DriveLinkResult(
                        type = DriveIdType.FOLDER,
                        resolvedFolderId = parsed.second
                    )
                )
            }
            DriveIdType.FILE -> {
                val fileId = parsed.second
                val detailsResult = driveRepository.getFileDetails(fileId)
                if (detailsResult.isFailure) {
                    return Result.failure(detailsResult.exceptionOrNull() ?: Exception("Failed to fetch file details"))
                }
                val dto = detailsResult.getOrThrow()
                val parentId = dto.parents?.firstOrNull() ?: "root"
                Result.success(
                    DriveLinkResult(
                        type = DriveIdType.FILE,
                        resolvedFolderId = parentId,
                        targetFileId = fileId
                    )
                )
            }
        }
    }

    private fun extractDriveId(url: String): Pair<DriveIdType, String>? {
        // Folder: /drive/folders/{ID} or /drive/u/0/folders/{ID}
        val folderRegex = Regex("(?:folders)/([a-zA-Z0-9_-]{25,})")
        folderRegex.find(url)?.groupValues?.get(1)?.let {
            return Pair(DriveIdType.FOLDER, it)
        }
        // File: /file/d/{ID}/view  or  ?id={ID}  or  &id={ID}
        val fileRegex = Regex("(?:/d/|[?&]id=)([a-zA-Z0-9_-]{25,})")
        fileRegex.find(url)?.groupValues?.get(1)?.let {
            return Pair(DriveIdType.FILE, it)
        }
        return null
    }
}
