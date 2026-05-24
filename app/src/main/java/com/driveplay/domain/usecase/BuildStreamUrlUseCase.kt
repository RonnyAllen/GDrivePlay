package com.driveplay.domain.usecase

import com.driveplay.domain.model.StreamUrl
import javax.inject.Inject

class BuildStreamUrlUseCase @Inject constructor() {
    operator fun invoke(fileId: String, accessToken: String): StreamUrl {
        val url = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media&supportsAllDrives=true"
        return StreamUrl(url, accessToken)
    }
}
