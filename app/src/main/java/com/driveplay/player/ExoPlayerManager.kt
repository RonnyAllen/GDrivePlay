package com.driveplay.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.driveplay.data.remote.SubtitleTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExoPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authDataSourceFactory: ExoPlayerAuthDataSourceFactory
) {
    private var player: ExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null

    fun getOrCreatePlayer(): ExoPlayer {
        if (player == null) {
            trackSelector = DefaultTrackSelector(context)

            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    /* minBufferMs = */ 5_000,
                    /* maxBufferMs = */ 50_000,
                    /* bufferForPlaybackMs = */ 2_500,
                    /* bufferForPlaybackAfterRebufferMs = */ 5_000
                )
                .build()

            val mediaSourceFactory = DefaultMediaSourceFactory(authDataSourceFactory)

            player = ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .setTrackSelector(trackSelector!!)
                .setLoadControl(loadControl)
                .build().apply {
                    repeatMode = Player.REPEAT_MODE_OFF
                    playWhenReady = true
                }
        }
        return player!!
    }

    fun prepareMedia(
        videoUrl: String,
        subtitles: List<SubtitleTrack> = emptyList(),
        startPositionMs: Long = 0L,
        playbackSpeed: Float = 1.0f,
        enablePitchCorrection: Boolean = true
    ) {
        val activePlayer = getOrCreatePlayer()

        // Build Subtitle configurations routing through auth factory
        val subtitleConfigs = subtitles.map { track ->
            MediaItem.SubtitleConfiguration.Builder(Uri.parse(track.streamUrl))
                .setMimeType(track.mimeType)
                .setLanguage(track.language)
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build()
        }

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(videoUrl))
            .setSubtitleConfigurations(subtitleConfigs)
            .build()

        activePlayer.setMediaItem(mediaItem)
        if (startPositionMs > 0L) {
            activePlayer.seekTo(startPositionMs)
        }
        activePlayer.playbackParameters = PlaybackParameters(playbackSpeed, if (enablePitchCorrection) 1.0f else playbackSpeed)
        activePlayer.prepare()
    }

    private var loudnessEnhancer: android.media.audiofx.LoudnessEnhancer? = null

    fun setPlaybackSpeed(speed: Float, pitchCorrection: Boolean) {
        player?.playbackParameters = PlaybackParameters(speed, if (pitchCorrection) 1.0f else speed)
    }

    fun setSubtitleLanguageOverride(language: String) {
        val selector = trackSelector ?: return
        val parameters = selector.parameters.buildUpon()
            .setPreferredTextLanguage(language)
            .build()
        selector.parameters = parameters
    }

    fun setVolumeBoost(boostPercent: Int) {
        val activePlayer = player ?: return
        val audioSessionId = activePlayer.audioSessionId
        if (audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
            if (loudnessEnhancer == null || loudnessEnhancer?.id != audioSessionId) {
                loudnessEnhancer?.release()
                try {
                    loudnessEnhancer = android.media.audiofx.LoudnessEnhancer(audioSessionId).apply {
                        enabled = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val gainmB = (boostPercent * 20).coerceIn(0, 2000) // 0 to 20 dB gain (2000mB)
            try {
                loudnessEnhancer?.setTargetGain(gainmB)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun releasePlayer() {
        loudnessEnhancer?.release()
        loudnessEnhancer = null
        player?.release()
        player = null
        trackSelector = null
    }
}
