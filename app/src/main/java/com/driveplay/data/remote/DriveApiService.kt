package com.driveplay.data.remote

import com.driveplay.data.remote.models.DriveFileDto
import com.driveplay.data.remote.models.DriveFileListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DriveApiService {

    @GET("drive/v3/files")
    suspend fun listFiles(
        @Query("q") query: String,
        @Query("fields") fields: String = "nextPageToken, files(id, name, mimeType, size, parents, thumbnailLink, videoMediaMetadata)",
        @Query("pageSize") pageSize: Int = 50,
        @Query("pageToken") pageToken: String? = null,
        @Query("supportsAllDrives") supportsAllDrives: Boolean = true,
        @Query("includeItemsFromAllDrives") includeItemsFromAllDrives: Boolean = true
    ): DriveFileListResponse

    @GET("drive/v3/files/{fileId}")
    suspend fun getFile(
        @Path("fileId") fileId: String,
        @Query("fields") fields: String = "id, name, mimeType, size, parents, thumbnailLink, videoMediaMetadata",
        @Query("supportsAllDrives") supportsAllDrives: Boolean = true
    ): DriveFileDto
}
