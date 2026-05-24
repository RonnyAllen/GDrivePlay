package com.driveplay.data.remote

import com.driveplay.data.remote.models.DriveFileDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class SubtitleTrack(
    val fileId: String,
    val name: String,
    val streamUrl: String,
    val mimeType: String,
    val language: String
)

@Singleton
class SubtitleRepository @Inject constructor(
    private val driveRepository: DriveRepository
) {
    suspend fun getSubtitlesForVideo(
        videoName: String,
        parentFolderId: String,
        accessToken: String
    ): List<SubtitleTrack> = withContext(Dispatchers.IO) {
        val result = driveRepository.getFolderContents(parentFolderId)
        if (result.isFailure) return@withContext emptyList()

        val files = result.getOrNull()?.files ?: return@withContext emptyList()
        val videoBaseName = sanitizeNameForMatching(videoName)

        return@withContext files.filter { file ->
            isSubtitleFile(file.mimeType, file.name) && 
            sanitizeNameForMatching(file.name).startsWith(videoBaseName)
        }.map { file ->
            SubtitleTrack(
                fileId = file.id,
                name = file.name,
                streamUrl = buildAuthStreamUrl(file.id, accessToken),
                mimeType = getSubtitleMimeType(file.name),
                language = parseLanguageFromName(file.name)
            )
        }
    }

    private fun sanitizeNameForMatching(name: String): String {
        val lastDot = name.lastIndexOf('.')
        val baseName = if (lastDot != -1) name.substring(0, lastDot) else name
        return baseName.lowercase().trim().replace(Regex("[\\p{Punct}\\s]+"), "")
    }

    private fun isSubtitleFile(mimeType: String, filename: String): Boolean {
        val extensions = listOf(".srt", ".vtt", ".ass")
        return mimeType == "application/x-subrip" || 
               mimeType == "text/vtt" || 
               extensions.any { filename.endsWith(it, ignoreCase = true) }
    }

    private fun getSubtitleMimeType(filename: String): String {
        return when {
            filename.endsWith(".vtt", ignoreCase = true) -> "text/vtt"
            filename.endsWith(".ass", ignoreCase = true) -> "text/x-ssa"
            else -> "application/x-subrip" // Default to SubRip / .srt
        }
    }

    private fun parseLanguageFromName(filename: String): String {
        val nameLower = filename.lowercase()
        return when {
            nameLower.contains(".eng") || nameLower.contains(".en") || nameLower.contains("english") -> "English"
            nameLower.contains(".spa") || nameLower.contains(".es") || nameLower.contains("spanish") -> "Spanish"
            nameLower.contains(".fra") || nameLower.contains(".fr") || nameLower.contains("french") -> "French"
            nameLower.contains(".ger") || nameLower.contains(".de") || nameLower.contains("german") -> "German"
            else -> "Default"
        }
    }

    private fun buildAuthStreamUrl(fileId: String, accessToken: String): String {
        return "https://www.googleapis.com/drive/v3/files/$fileId?alt=media&supportsAllDrives=true"
    }
}
