package com.example.flappy.model

import android.graphics.RectF
import com.example.flappy.game.GameConfig
import com.example.flappy.game.RunModifiers
import kotlin.random.Random

class ObstacleManager {
    val activeObstacles: List<ObstaclePair>
        get() = obstacles

    private val obstacles = mutableListOf<ObstaclePair>()
    private var screenWidth = 0f
    private var screenHeight = 0f
    private var obstacleWidth = 0f
    private var gapHeight = 0f
    private var spacing = 0f
    private var speed = 0f
    private var verticalMargin = 0f
    private var pendingPassedCount = 0

    fun reset(width: Float, height: Float, modifiers: RunModifiers) {
        screenWidth = width
        screenHeight = height
        applyModifiers(modifiers)
        pendingPassedCount = 0
        obstacles.clear()
        spawnObstacle(screenWidth + obstacleWidth)
    }

    fun applyModifiers(modifiers: RunModifiers) {
        obstacleWidth = (screenWidth * GameConfig.OBSTACLE_WIDTH_SCREEN_RATIO).coerceIn(76f, 150f)
        gapHeight = (screenHeight * GameConfig.OBSTACLE_GAP_SCREEN_RATIO * modifiers.obstacleGapMultiplier)
            .coerceIn(GameConfig.OBSTACLE_GAP_MIN, GameConfig.OBSTACLE_GAP_UPGRADE_MAX)
        spacing = screenWidth * GameConfig.OBSTACLE_SPACING_SCREEN_RATIO * modifiers.obstacleSpacingMultiplier
        speed = screenWidth * GameConfig.OBSTACLE_SPEED_SCREEN_RATIO * modifiers.obstacleSpeedMultiplier
        verticalMargin = screenHeight * GameConfig.OBSTACLE_VERTICAL_MARGIN_RATIO
        obstacles.forEach { obstacle -> obstacle.setSpeed(speed) }
    }

    fun update(deltaSeconds: Float, playerLeft: Float) {
        if (screenWidth <= 0f || screenHeight <= 0f) {
            return
        }

        obstacles.forEach { obstacle ->
            obstacle.update(deltaSeconds)
            if (obstacle.consumeIfPassed(playerLeft)) {
                pendingPassedCount += 1
            }
        }

        obstacles.removeAll { it.isOffScreen }

        val lastObstacle = obstacles.lastOrNull()
        if (lastObstacle == null || lastObstacle.left <= screenWidth - spacing) {
            spawnObstacle(screenWidth + obstacleWidth)
        }
    }

    fun consumePassedCount(): Int {
        val count = pendingPassedCount
        pendingPassedCount = 0
        return count
    }

    fun clear() {
        pendingPassedCount = 0
        obstacles.clear()
    }

    fun collidesWith(bounds: RectF): Boolean {
        return obstacles.any { it.collidesWith(bounds) }
    }

    private fun spawnObstacle(left: Float) {
        val minCenter = verticalMargin + gapHeight / 2f
        val maxCenter = screenHeight - verticalMargin - gapHeight / 2f
        val gapCenter = if (maxCenter > minCenter) {
            Random.nextFloat() * (maxCenter - minCenter) + minCenter
        } else {
            screenHeight / 2f
        }

        obstacles += ObstaclePair(
            left = left,
            width = obstacleWidth,
            gapTop = gapCenter - gapHeight / 2f,
            gapBottom = gapCenter + gapHeight / 2f,
            screenHeight = screenHeight,
            speed = speed
        )
    }
}
