package com.driveplay.data.remote.models

import com.google.gson.annotations.SerializedName

data class DriveFileListResponse(
    @SerializedName("nextPageToken") val nextPageToken: String?,
    @SerializedName("files") val files: List<DriveFileDto>
)

data class DriveFileDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("mimeType") val mimeType: String,
    @SerializedName("size") val size: Long? = 0L,
    @SerializedName("parents") val parents: List<String>? = emptyList(),
    @SerializedName("thumbnailLink") val thumbnailLink: String? = null,
    @SerializedName("videoMediaMetadata") val videoMediaMetadata: VideoMediaMetadataDto? = null
)

data class VideoMediaMetadataDto(
    @SerializedName("width") val width: Int? = 0,
    @SerializedName("height") val height: Int? = 0,
    @SerializedName("durationMillis") val durationMillis: Long? = 0L
)
