package com.driveplay.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.room.Room
import com.driveplay.auth.TokenManager
import com.driveplay.data.NetworkMonitor
import com.driveplay.data.db.AppDatabase
import com.driveplay.data.db.OfflineFileDao
import com.driveplay.data.db.PlaylistDao
import com.driveplay.data.db.WatchHistoryDao
import com.driveplay.data.prefs.UserPreferencesDataStore
import com.driveplay.data.remote.DriveApiService
import com.driveplay.player.ExoPlayerAuthDataSourceFactory
import com.driveplay.player.SeekThumbnailCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val token = kotlinx.coroutines.runBlocking {
                    try {
                        tokenManager.getValidTokenBlocking()
                    } catch (e: Exception) {
                        ""
                    }
                }
                val builder = chain.request().newBuilder()
                if (token.isNotEmpty()) {
                    builder.header("Authorization", "Bearer $token")
                }
                chain.proceed(builder.build())
            }
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideDriveApiService(retrofit: Retrofit): DriveApiService {
        return retrofit.create(DriveApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext ctx: Context): AppDatabase {
        return Room.databaseBuilder(
            ctx,
            AppDatabase::class.java,
            "driveplay_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideWatchHistoryDao(db: AppDatabase): WatchHistoryDao {
        return db.watchHistoryDao()
    }

    @Provides
    fun providePlaylistDao(db: AppDatabase): PlaylistDao {
        return db.playlistDao()
    }

    @Provides
    fun provideOfflineFileDao(db: AppDatabase): OfflineFileDao {
        return db.offlineFileDao()
    }

    @Provides
    @Singleton
    fun provideSimpleCache(@ApplicationContext ctx: Context): SimpleCache {
        val cacheDir = File(ctx.cacheDir, "exoplayer_cache")
        return SimpleCache(
            cacheDir,
            LeastRecentlyUsedCacheEvictor(200L * 1024 * 1024),
            StandaloneDatabaseProvider(ctx)
        )
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext ctx: Context): DataStore<Preferences> {
        return ctx.dataStore
    }

    @Provides
    @Singleton
    fun provideAuthDataSourceFactory(
        tokenManager: TokenManager,
        cache: SimpleCache
    ): ExoPlayerAuthDataSourceFactory {
        return ExoPlayerAuthDataSourceFactory(tokenManager, cache)
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext ctx: Context): NetworkMonitor {
        return NetworkMonitor(ctx)
    }

    @Provides
    @Singleton
    fun provideSeekThumbnailCache(): SeekThumbnailCache {
        return SeekThumbnailCache()
    }
}
