package com.example.flappy.game

import com.example.flappy.model.ObstacleManager
import com.example.flappy.model.ObstaclePair
import com.example.flappy.model.Player

class GameWorld {
    val player = Player()
    val activeObstacles: List<ObstaclePair>
        get() = obstacleManager.activeObstacles
    val upgradeChoices: List<UpgradeType>
        get() = upgradeManager.currentChoices
    val activeUpgrades: List<UpgradeType>
        get() = upgradeManager.activeUpgrades
    val shieldCharges: Int
        get() = runModifiers.shieldCharges

    var state = GameState.Ready
        private set

    var mode = GameMode.Classic
        private set

    var score = 0
        private set

    var bestScore = 0
        private set

    private val obstacleManager = ObstacleManager()
    private val upgradeManager = UpgradeManager()
    private val runModifiers = RunModifiers()
    private var screenWidth = 0f
    private var screenHeight = 0f
    private var hasValidSize = false

    fun resize(width: Int, height: Int) {
        if (width <= 0 || height <= 0) {
            return
        }

        screenWidth = width.toFloat()
        screenHeight = height.toFloat()
        hasValidSize = true
        resetToReady()
    }

    fun handleTap() {
        if (!hasValidSize) {
            return
        }

        when (state) {
            GameState.Ready -> {
                state = GameState.Running
                player.flap()
            }
            GameState.Running -> player.flap()
            GameState.ChoosingUpgrade -> Unit
            GameState.GameOver -> {
                resetToReady()
                state = GameState.Running
                player.flap()
            }
        }
    }

    fun chooseUpgrade(index: Int) {
        if (state != GameState.ChoosingUpgrade) {
            return
        }

        upgradeManager.choose(index, runModifiers) ?: return
        player.applyModifiers(screenHeight, runModifiers)
        obstacleManager.applyModifiers(runModifiers)
        state = GameState.Running
    }

    fun update(deltaSeconds: Float) {
        if (!hasValidSize || state != GameState.Running) {
            return
        }

        when (mode) {
            GameMode.Classic -> updateClassicMode(deltaSeconds)
            GameMode.Boss -> {
                // Phase 2 will route boss movement, attacks, and boss health here.
            }
        }
    }

    private fun updateClassicMode(deltaSeconds: Float) {
        player.update(deltaSeconds)
        obstacleManager.update(deltaSeconds, player.bounds.left)
        val passedObstacles = obstacleManager.consumePassedCount()
        score += passedObstacles

        if (obstacleManager.collidesWith(player.bounds) || player.isOutsideVerticalBounds(screenHeight)) {
            handlePlayerHit()
            return
        }

        if (passedObstacles > 0 && upgradeManager.startChoiceIfReady(score)) {
            state = GameState.ChoosingUpgrade
        }
    }

    private fun resetToReady() {
        score = 0
        mode = GameMode.Classic
        state = GameState.Ready
        runModifiers.reset()
        upgradeManager.reset()
        player.reset(screenWidth, screenHeight, runModifiers)
        obstacleManager.reset(screenWidth, screenHeight, runModifiers)
    }

    private fun handlePlayerHit() {
        if (runModifiers.tryConsumeShield()) {
            player.recoverFromHit(screenHeight)
            obstacleManager.reset(screenWidth, screenHeight, runModifiers)
            return
        }

        endRun()
    }

    private fun endRun() {
        bestScore = maxOf(bestScore, score)
        state = GameState.GameOver
    }
}
