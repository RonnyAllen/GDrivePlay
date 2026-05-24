package com.driveplay.player.gestures

import android.content.Context
import android.media.AudioManager
import kotlin.math.max
import kotlin.math.min

class VolumeGestureHandler(context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    fun onScroll(deltaYPercent: Float): Int {
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolumeFloat = maxVolume.toFloat()

        // Calculate delta step
        val volumeDelta = deltaYPercent * maxVolumeFloat
        var newVolume = (currentVolume + volumeDelta).toInt()
        newVolume = min(maxVolume, max(0, newVolume))

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
        return ((newVolume.toFloat() / maxVolumeFloat) * 100).toInt()
    }
}
