package com.driveplay.player

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.driveplay.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MediaSessionService : MediaSessionService() {

    @Inject
    lateinit var exoPlayerManager: ExoPlayerManager

    private var mediaSession: MediaSession? = null
    private var wifiLock: WifiManager.WifiLock? = null
    private var wasTransientlyLost = false
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        val player = exoPlayerManager.getOrCreatePlayer()
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                wasTransientlyLost = false
                player.pause() // Permanent loss — do NOT auto-resume
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                wasTransientlyLost = true
                player.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                player.volume = 0.2f // Duck volume
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                player.volume = 1.0f
                if (wasTransientlyLost) { // Only resume if it was transient
                    wasTransientlyLost = false
                    player.play()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val player = exoPlayerManager.getOrCreatePlayer()

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        requestAudioFocus()

        // Configure WifiLock
        val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "DrivePlay:WifiLock")

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    wifiLock?.acquire()
                } else {
                    // Release lock on user-initiated pause
                    if (wifiLock?.isHeld == true) {
                        wifiLock?.release()
                    }
                }
            }
        })
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attr = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .setAudioAttributes(attr)
                .setAcceptsDelayedFocusGain(true)
                .build()

            audioFocusRequest?.let {
                audioManager.requestAudioFocus(it)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        abandonAudioFocus()
        if (wifiLock?.isHeld == true) {
            wifiLock?.release()
        }
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
