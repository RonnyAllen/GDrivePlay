package com.driveplay.player.gestures

import android.app.Activity
import android.view.WindowManager
import kotlin.math.max
import kotlin.math.min

class BrightnessGestureHandler(private val activity: Activity) {
    private var initialBrightness = 0.5f

    fun onStart() {
        val layoutParams = activity.window.attributes
        initialBrightness = if (layoutParams.screenBrightness < 0f) {
            0.5f // Default mid value
        } else {
            layoutParams.screenBrightness
        }
    }

    fun onScroll(scrollRatio: Float): Int {
        // swipe up = positive change, down = negative change
        val newBrightness = min(1.0f, max(0.0f, initialBrightness + scrollRatio))

        val layoutParams = activity.window.attributes
        layoutParams.screenBrightness = newBrightness
        activity.window.attributes = layoutParams

        return (newBrightness * 100).toInt()
    }
}
