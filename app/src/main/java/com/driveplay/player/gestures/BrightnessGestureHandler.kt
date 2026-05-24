package com.driveplay.player.gestures

import android.app.Activity
import android.view.WindowManager
import kotlin.math.max
import kotlin.math.min

class BrightnessGestureHandler(private val activity: Activity) {
    private var currentBrightness = 0.5f

    init {
        val layoutParams = activity.window.attributes
        currentBrightness = if (layoutParams.screenBrightness < 0f) {
            0.5f // Default mid value
        } else {
            layoutParams.screenBrightness
        }
    }

    fun onScroll(deltaYPercent: Float): Int {
        // swipe up = positive change, down = negative change
        val change = deltaYPercent
        currentBrightness = min(1.0f, max(0.0f, currentBrightness + change))

        val layoutParams = activity.window.attributes
        layoutParams.screenBrightness = currentBrightness
        activity.window.attributes = layoutParams

        return (currentBrightness * 100).toInt()
    }
}
