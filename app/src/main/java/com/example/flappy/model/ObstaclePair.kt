package com.example.flappy.model

import android.graphics.RectF

class ObstaclePair(
    left: Float,
    private val width: Float,
    private val gapTop: Float,
    private val gapBottom: Float,
    private val screenHeight: Float,
    private var speed: Float
) {
    var left = left
        private set

    val topRect = RectF()
    val bottomRect = RectF()
    private var scored = false

    val isOffScreen: Boolean
        get() = left + width < 0f

    init {
        updateRects()
    }

    fun update(deltaSeconds: Float) {
        left -= speed * deltaSeconds
        updateRects()
    }

    fun setSpeed(newSpeed: Float) {
        speed = newSpeed
    }

    fun consumeIfPassed(playerLeft: Float): Boolean {
        if (!scored && left + width < playerLeft) {
            scored = true
            return true
        }

        return false
    }

    fun collidesWith(bounds: RectF): Boolean {
        return RectF.intersects(bounds, topRect) || RectF.intersects(bounds, bottomRect)
    }

    private fun updateRects() {
        topRect.set(left, 0f, left + width, gapTop)
        bottomRect.set(left, gapBottom, left + width, screenHeight)
    }
}
