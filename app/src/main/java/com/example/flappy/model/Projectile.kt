package com.example.flappy.model

import android.graphics.RectF

class Projectile(
    val owner: ProjectileOwner,
    left: Float,
    centerY: Float,
    width: Float,
    height: Float,
    private val velocityX: Float,
    val damage: Float
) {
    val bounds = RectF(left, centerY - height / 2f, left + width, centerY + height / 2f)

    fun update(deltaSeconds: Float) {
        bounds.offset(velocityX * deltaSeconds, 0f)
    }

    fun collidesWith(target: RectF): Boolean {
        return RectF.intersects(bounds, target)
    }

    fun isOffScreen(screenWidth: Float): Boolean {
        return bounds.right < 0f || bounds.left > screenWidth
    }
}
