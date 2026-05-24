package com.driveplay.player.gestures

import android.content.Context
import android.media.AudioManager
import kotlin.math.max
import kotlin.math.min

class VolumeGestureHandler(context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    private var initialVolume = 0
    private var initialBoost = 0

    fun onStart(currentBoost: Int) {
        initialVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        initialBoost = currentBoost
    }

    fun onScroll(scrollRatio: Float): Int {
        val maxVolumeFloat = maxVolume.toFloat()
        
        // At sensitivity 3, drag ratio maps to volume.
        // Let's define the total virtual range as: system volume range + boost range
        // Since boost range is 0 to 100%, let's represent it as a virtual range of maxVolume steps.
        // Total virtual steps = maxVolume * 2
        val virtualMax = maxVolume * 2f
        val deltaSteps = scrollRatio * virtualMax
        
        val initialVirtualSteps = if (initialVolume < maxVolume) {
            initialVolume.toFloat()
        } else {
            maxVolume + (initialBoost / 100f) * maxVolume
        }
        
        val targetVirtualSteps = min(virtualMax, max(0f, initialVirtualSteps + deltaSteps))
        
        return if (targetVirtualSteps <= maxVolume) {
            // System volume range
            val newVolume = targetVirtualSteps.toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
            ((newVolume.toFloat() / maxVolumeFloat) * 100).toInt() // 0 to 100%
        } else {
            // Boost range
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
            val boostFraction = (targetVirtualSteps - maxVolume) / maxVolumeFloat
            val boostPercent = (boostFraction * 100).toInt().coerceIn(0, 100)
            100 + boostPercent // 100% to 200%
        }
    }
}

