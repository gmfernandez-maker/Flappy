package com.example.flappy.model

import android.graphics.RectF
import com.example.flappy.game.GameConfig
import com.example.flappy.game.RunModifiers

class Player {
    val bounds = RectF()

    private var velocityY = 0f
    private var gravity = 0f
    private var flapVelocity = 0f
    private var maxFallSpeed = 0f

    fun reset(screenWidth: Float, screenHeight: Float, modifiers: RunModifiers) {
        val size = playerSize(screenHeight, modifiers)
        val left = screenWidth * GameConfig.PLAYER_X_SCREEN_RATIO
        val top = screenHeight * 0.5f - size / 2f

        bounds.set(left, top, left + size, top + size)
        velocityY = 0f
        applyPhysics(screenHeight, modifiers)
    }

    fun applyModifiers(screenHeight: Float, modifiers: RunModifiers) {
        val size = playerSize(screenHeight, modifiers)
        val centerY = bounds.centerY()
        bounds.set(bounds.left, centerY - size / 2f, bounds.left + size, centerY + size / 2f)
        applyPhysics(screenHeight, modifiers)
    }

    fun flap() {
        velocityY = flapVelocity
    }

    fun update(deltaSeconds: Float) {
        velocityY = (velocityY + gravity * deltaSeconds).coerceAtMost(maxFallSpeed)
        bounds.offset(0f, velocityY * deltaSeconds)
    }

    fun isOutsideVerticalBounds(screenHeight: Float): Boolean {
        return bounds.top < 0f || bounds.bottom > screenHeight
    }

    fun recoverFromHit(screenHeight: Float) {
        val size = bounds.height()
        val minCenter = size / 2f
        val maxCenter = screenHeight - size / 2f
        val recoveryCenterY = (screenHeight * 0.45f).coerceIn(minCenter, maxCenter)
        bounds.offsetTo(bounds.left, recoveryCenterY - size / 2f)
        velocityY = flapVelocity * 0.35f
    }

    private fun playerSize(screenHeight: Float, modifiers: RunModifiers): Float {
        return (screenHeight * GameConfig.PLAYER_SIZE_SCREEN_RATIO * modifiers.playerSizeMultiplier)
            .coerceIn(34f, 92f)
    }

    private fun applyPhysics(screenHeight: Float, modifiers: RunModifiers) {
        gravity = screenHeight * GameConfig.GRAVITY_SCREEN_RATIO * modifiers.gravityMultiplier
        flapVelocity =
            screenHeight * GameConfig.FLAP_VELOCITY_SCREEN_RATIO * modifiers.flapStrengthMultiplier
        maxFallSpeed =
            screenHeight * GameConfig.MAX_FALL_SPEED_SCREEN_RATIO * modifiers.maxFallSpeedMultiplier
    }
}
