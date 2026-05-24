package com.driveplay.player.gestures

import kotlin.math.max
import kotlin.math.min

class SeekGestureHandler {
    private var initialPosition = 0L
    private var targetPosition = 0L

    fun onStart(currentPositionMs: Long) {
        initialPosition = currentPositionMs
        targetPosition = currentPositionMs
    }

    fun onScroll(deltaXPercent: Float, durationMs: Long): Long {
        if (durationMs <= 0) return 0L

        // Seek scaling: slow swipe = precise, fast swipe = wide range
        // Dragging full screen width would cover 3 minutes or 20% of duration (whichever is larger)
        val fullRange = max(180_000L, (durationMs * 0.2f).toLong())
        val deltaMs = (deltaXPercent * fullRange).toLong()

        targetPosition = min(durationMs, max(0L, initialPosition + deltaMs))
        return targetPosition
    }
}
