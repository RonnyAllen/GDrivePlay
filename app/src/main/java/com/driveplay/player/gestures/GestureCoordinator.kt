package com.driveplay.player.gestures

import android.app.Activity
import android.content.Context
import kotlin.math.abs
import kotlin.math.hypot

enum class GestureZone { LEFT_MARGIN, RIGHT_MARGIN, CENTER }
enum class LockedGesture { BRIGHTNESS, VOLUME, SEEK, NONE }

class GestureCoordinator(
    private val context: Context,
    private val activity: Activity
) {
    val brightnessHandler = BrightnessGestureHandler(activity)
    val volumeHandler = VolumeGestureHandler(context)
    val seekHandler = SeekGestureHandler()

    private var currentZone = GestureZone.CENTER
    private var lockedGesture = LockedGesture.NONE

    private var initialX = 0f
    private var initialY = 0f

    // Sensitivity slider scales: 1 (least sensitive) to 5 (most sensitive), default 3.
    // volume/brightness: 1% change per (6 - sensitivity) * 2 dp of vertical drag.
    // 1% per 10dp (sens 1) to 1% per 2dp (sens 5). At default 3: 1% per 6dp.
    private var swipeSensitivity = 3

    fun setSensitivity(sensitivity: Int) {
        swipeSensitivity = sensitivity
    }

    fun onDown(x: Float, y: Float, screenWidth: Int) {
        initialX = x
        initialY = y
        lockedGesture = LockedGesture.NONE
        currentZone = when {
            x < screenWidth * 0.15f -> GestureZone.LEFT_MARGIN
            x > screenWidth * 0.85f -> GestureZone.RIGHT_MARGIN
            else -> GestureZone.CENTER
        }
    }

    // Process drag scroll movements. Returns updated metrics to be displayed on overlays.
    // dx and dy are total offsets from initial touch positions.
    fun onMove(
        dx: Float,
        dy: Float,
        screenWidth: Int,
        screenHeight: Int,
        density: Float,
        durationMs: Long,
        currentPlayheadMs: Long,
        volumeBoost: Int
    ): GestureResponse {
        val distance = hypot(dx, dy)
        val thresholdPx = 8f * density // 8dp threshold

        if (lockedGesture == LockedGesture.NONE) {
            if (distance < thresholdPx) return GestureResponse.None

            // Perform lock-in checking to prevent diagonal conflicts
            lockedGesture = when {
                abs(dy) > abs(dx) && currentZone == GestureZone.LEFT_MARGIN -> {
                    brightnessHandler.onStart()
                    LockedGesture.BRIGHTNESS
                }
                abs(dy) > abs(dx) && currentZone == GestureZone.RIGHT_MARGIN -> {
                    volumeHandler.onStart(volumeBoost)
                    LockedGesture.VOLUME
                }
                abs(dx) > abs(dy) && currentZone == GestureZone.CENTER -> {
                    seekHandler.onStart(currentPlayheadMs)
                    LockedGesture.SEEK
                }
                else -> LockedGesture.NONE
            }
        }

        if (lockedGesture == LockedGesture.NONE) return GestureResponse.None

        // Calculate sensitivity multipliers
        // At sensitivity 3, 1% volume/brightness change requires 6dp (6 * density pixels)
        val sensitivityDp = (6 - swipeSensitivity) * 2f
        val stepPx = sensitivityDp * density

        return when (lockedGesture) {
            LockedGesture.BRIGHTNESS -> {
                // Swipe up decreases dy (screen coordinates), so invert dy
                val scrollRatio = -dy / (100f * stepPx)
                val percent = brightnessHandler.onScroll(scrollRatio)
                GestureResponse.BrightnessChanged(percent)
            }
            LockedGesture.VOLUME -> {
                val scrollRatio = -dy / (100f * stepPx)
                val percent = volumeHandler.onScroll(scrollRatio)
                GestureResponse.VolumeChanged(percent)
            }
            LockedGesture.SEEK -> {
                // Scroll horizontal ratio
                val scrollRatio = dx / screenWidth.toFloat()
                val targetMs = seekHandler.onScroll(scrollRatio, durationMs)
                GestureResponse.SeekScrubbing(targetMs)
            }
            LockedGesture.NONE -> GestureResponse.None
        }
    }

    fun onRelease(): LockedGesture {
        val ended = lockedGesture
        lockedGesture = LockedGesture.NONE
        return ended
    }
}

sealed class GestureResponse {
    object None : GestureResponse()
    data class BrightnessChanged(val percent: Int) : GestureResponse()
    data class VolumeChanged(val percent: Int) : GestureResponse()
    data class SeekScrubbing(val targetMs: Long) : GestureResponse()
}
