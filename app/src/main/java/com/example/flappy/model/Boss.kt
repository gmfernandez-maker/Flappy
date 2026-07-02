package com.example.flappy.model

import android.graphics.RectF
import com.example.flappy.game.BossPhase
import com.example.flappy.game.BossTuning
import com.example.flappy.game.GameConfig
import kotlin.math.sin

class Boss {
    val bounds = RectF()

    var health = GameConfig.BOSS_MAX_HEALTH
        private set

    var level = 1
        private set

    val healthFraction: Float
        get() = (health / maxHealth).coerceIn(0f, 1f)

    val isDefeated: Boolean
        get() = health <= 0f

    val phase: BossPhase
        get() = when {
            healthFraction <= GameConfig.BOSS_ENRAGE_HEALTH_FRACTION -> BossPhase.Enraged
            healthFraction <= GameConfig.BOSS_PRESSURE_HEALTH_FRACTION -> BossPhase.Pressure
            else -> BossPhase.Opening
        }

    val isEnraged: Boolean
        get() = phase == BossPhase.Enraged

    private var screenWidth = 0f
    private var screenHeight = 0f
    private var bossWidth = 0f
    private var bossHeight = 0f
    private var baseCenterY = 0f
    private var hoverAmplitude = 0f
    private var hoverTime = 0f
    private var hoverSpeedMultiplier = 1f
    private var maxHealth = GameConfig.BOSS_MAX_HEALTH

    fun reset(width: Float, height: Float, tuning: BossTuning) {
        screenWidth = width
        screenHeight = height
        level = tuning.level
        maxHealth = tuning.maxHealth
        hoverSpeedMultiplier = tuning.hoverSpeedMultiplier
        bossWidth = (width * GameConfig.BOSS_WIDTH_SCREEN_RATIO).coerceIn(150f, 270f)
        bossHeight = (height * GameConfig.BOSS_HEIGHT_SCREEN_RATIO).coerceIn(180f, 330f)
        baseCenterY = height * 0.5f
        hoverAmplitude = height * GameConfig.BOSS_HOVER_AMPLITUDE_SCREEN_RATIO
        hoverTime = 0f
        health = maxHealth
        updateBounds(baseCenterY)
    }

    fun clear() {
        bounds.setEmpty()
        health = 0f
    }

    fun update(deltaSeconds: Float) {
        hoverTime += deltaSeconds
        val centerY = (
            baseCenterY +
                sin(hoverTime * GameConfig.BOSS_HOVER_SPEED * hoverSpeedMultiplier) * hoverAmplitude
            ).coerceIn(bossHeight / 2f, screenHeight - bossHeight / 2f)
        updateBounds(centerY)
    }

    fun takeDamage(amount: Float) {
        health = (health - amount).coerceAtLeast(0f)
    }

    fun defeat() {
        health = 0f
    }

    private fun updateBounds(centerY: Float) {
        val left = screenWidth - bossWidth - screenWidth * 0.06f
        bounds.set(left, centerY - bossHeight / 2f, left + bossWidth, centerY + bossHeight / 2f)
    }
}
