package com.example.flappy.model

import android.graphics.RectF

class BossTelegraph(
    left: Float,
    right: Float,
    centerY: Float,
    height: Float,
    val totalSeconds: Float
) {
    val bounds = RectF(left, centerY - height / 2f, right, centerY + height / 2f)

    var remainingSeconds = totalSeconds
        private set

    val progress: Float
        get() = if (totalSeconds <= 0f) {
            1f
        } else {
            1f - (remainingSeconds / totalSeconds).coerceIn(0f, 1f)
        }

    fun update(deltaSeconds: Float): Boolean {
        remainingSeconds -= deltaSeconds
        return remainingSeconds <= 0f
    }
}
