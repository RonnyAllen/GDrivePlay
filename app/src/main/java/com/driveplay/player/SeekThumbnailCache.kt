package com.driveplay.player

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.LruCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeekThumbnailCache @Inject constructor() {
    // Round to the nearest 5s bucket to maximise cache hits during fast scrubbing
    private val cache = LruCache<Long, Bitmap>(30)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun getThumbnail(
        streamUrl: String,
        token: String,
        positionMs: Long,
        onResult: (Bitmap?) -> Unit
    ) {
        val bucketMs = (positionMs / 5000L) * 5000L   // snap to 5s grid
        val cachedFrame = cache.get(bucketMs)
        if (cachedFrame != null) {
            onResult(cachedFrame)
            return
        }

        scope.launch {
            val retriever = MediaMetadataRetriever()
            try {
                // Drive streams require auth headers for frame extraction too
                retriever.setDataSource(
                    streamUrl,
                    mapOf("Authorization" to "Bearer $token")
                )
                val frame = retriever.getFrameAtTime(
                    bucketMs * 1000L,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
                if (frame != null) {
                    cache.put(bucketMs, frame)
                }
                withContext(Dispatchers.Main) {
                    onResult(frame)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            } finally {
                try {
                    retriever.release()
                } catch (e: Exception) {
                    // Ignore release errors
                }
            }
        }
    }

    fun clear() {
        cache.evictAll()
    }
}
