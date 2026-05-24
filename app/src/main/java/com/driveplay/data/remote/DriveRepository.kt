package com.driveplay.data.remote

import com.driveplay.data.remote.models.DriveFileDto
import com.driveplay.data.remote.models.DriveFileListResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveRepository @Inject constructor(
    private val driveApiService: DriveApiService
) {
    suspend fun getFolderContents(
        folderId: String,
        pageToken: String? = null
    ): Result<DriveFileListResponse> = withContext(Dispatchers.IO) {
        try {
            val query = "'$folderId' in parents and trashed = false"
            val response = driveApiService.listFiles(query = query, pageToken = pageToken)
            Result.success(response)
        } catch (e: HttpException) {
            Result.failure(wrapHttpException(e))
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFileDetails(fileId: String): Result<DriveFileDto> = withContext(Dispatchers.IO) {
        try {
            val response = driveApiService.getFile(fileId = fileId)
            Result.success(response)
        } catch (e: HttpException) {
            Result.failure(wrapHttpException(e))
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchFiles(
        searchTerm: String,
        pageToken: String? = null
    ): Result<DriveFileListResponse> = withContext(Dispatchers.IO) {
        try {
            val query = "name contains '$searchTerm' and mimeType != 'application/vnd.google-apps.folder' and trashed = false"
            val response = driveApiService.listFiles(query = query, pageToken = pageToken)
            Result.success(response)
        } catch (e: HttpException) {
            Result.failure(wrapHttpException(e))
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun wrapHttpException(e: HttpException): Exception {
        return when (e.code()) {
            403 -> Exception("Drive API Access Denied: 403 Forbidden")
            404 -> Exception("Drive API File Not Found: 404 Not Found")
            else -> Exception("Drive API Server Error: ${e.code()}")
        }
    }
}
