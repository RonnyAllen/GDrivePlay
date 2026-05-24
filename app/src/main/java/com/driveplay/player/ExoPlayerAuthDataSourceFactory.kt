package com.driveplay.player

import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import com.driveplay.auth.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExoPlayerAuthDataSourceFactory @Inject constructor(
    private val tokenManager: TokenManager,
    private val cache: SimpleCache
) : DataSource.Factory {

    override fun createDataSource(): DataSource {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                // Fetches a fresh token on every request if within 60s of expiry
                val token = runBlocking {
                    tokenManager.getValidTokenBlocking()
                }
                val request = chain.request().newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)
            }
            .build()

        val httpFactory = OkHttpDataSource.Factory(okHttpClient)

        return CacheDataSource(
            cache,
            httpFactory.createDataSource(),
            CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR
        )
    }
}
