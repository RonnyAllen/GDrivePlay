package com.driveplay.domain.usecase

import com.driveplay.data.remote.SubtitleRepository
import com.driveplay.data.remote.SubtitleTrack
import javax.inject.Inject

class GetSubtitlesForVideoUseCase @Inject constructor(
    private val subtitleRepository: SubtitleRepository
) {
    suspend operator fun invoke(
        videoName: String,
        parentFolderId: String,
        accessToken: String
    ): List<SubtitleTrack> {
        if (parentFolderId.isBlank()) return emptyList()
        return subtitleRepository.getSubtitlesForVideo(videoName, parentFolderId, accessToken)
    }
}
