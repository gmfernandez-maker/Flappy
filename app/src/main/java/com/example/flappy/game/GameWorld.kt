package com.example.flappy.game

import com.example.flappy.model.Boss
import com.example.flappy.model.BossEncounter
import com.example.flappy.model.ObstacleManager
import com.example.flappy.model.ObstaclePair
import com.example.flappy.model.Player
import com.example.flappy.model.Projectile

class GameWorld {
    val player = Player()
    val activeObstacles: List<ObstaclePair>
        get() = obstacleManager.activeObstacles
    val boss: Boss
        get() = bossEncounter.boss
    val bossProjectiles: List<Projectile>
        get() = bossEncounter.bossProjectiles
    val playerProjectiles: List<Projectile>
        get() = bossEncounter.playerProjectiles
    val upgradeChoices: List<UpgradeType>
        get() = upgradeManager.currentChoices
    val activeUpgrades: List<UpgradeType>
        get() = upgradeManager.activeUpgrades
    val shieldCharges: Int
        get() = runModifiers.shieldCharges
    val bossHealthFraction: Float
        get() = boss.healthFraction
    val nextBossScore: Int
        get() = upcomingBossScore

    var state = GameState.Ready
        private set

    var mode = GameMode.Classic
        private set

    var score = 0
        private set

    var bestScore = 0
        private set

    private val obstacleManager = ObstacleManager()
    private val bossEncounter = BossEncounter()
    private val upgradeManager = UpgradeManager()
    private val runModifiers = RunModifiers()
    private var screenWidth = 0f
    private var screenHeight = 0f
    private var hasValidSize = false
    private var upcomingBossScore = GameConfig.BOSS_SCORE_THRESHOLD

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
            GameState.Running -> {
                player.flap()
                if (mode == GameMode.Boss) {
                    bossEncounter.firePlayerShot(player.bounds)
                }
            }
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
            GameMode.Boss -> updateBossMode(deltaSeconds)
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

        if (score >= upcomingBossScore) {
            startBossMode()
            return
        }

        if (passedObstacles > 0 && upgradeManager.startChoiceIfReady(score, upcomingBossScore)) {
            state = GameState.ChoosingUpgrade
        }
    }

    private fun updateBossMode(deltaSeconds: Float) {
        player.update(deltaSeconds)
        val bossResult = bossEncounter.update(deltaSeconds, player.bounds, runModifiers)

        if (player.isOutsideVerticalBounds(screenHeight) || bossResult.playerWasHit) {
            handlePlayerHit()
            return
        }

        if (bossResult.bossDefeated) {
            completeBossEncounter()
        }
    }

    private fun resetToReady() {
        score = 0
        mode = GameMode.Classic
        state = GameState.Ready
        upcomingBossScore = GameConfig.BOSS_SCORE_THRESHOLD
        runModifiers.reset()
        upgradeManager.reset()
        bossEncounter.clear()
        player.reset(screenWidth, screenHeight, runModifiers)
        obstacleManager.reset(screenWidth, screenHeight, runModifiers)
    }

    private fun handlePlayerHit() {
        if (runModifiers.tryConsumeShield()) {
            player.recoverFromHit(screenHeight)
            when (mode) {
                GameMode.Classic -> obstacleManager.reset(screenWidth, screenHeight, runModifiers)
                GameMode.Boss -> bossEncounter.clearEnemyPressure()
            }
            return
        }

        endRun()
    }

    private fun startBossMode() {
        mode = GameMode.Boss
        obstacleManager.clear()
        bossEncounter.reset(screenWidth, screenHeight, runModifiers)
    }

    private fun completeBossEncounter() {
        score += GameConfig.BOSS_DEFEAT_SCORE_BONUS
        upcomingBossScore += GameConfig.BOSS_SCORE_INTERVAL
        upgradeManager.skipChoicesThrough(score)
        bossEncounter.clear()
        mode = GameMode.Classic
        obstacleManager.reset(screenWidth, screenHeight, runModifiers)
    }

    private fun endRun() {
        bestScore = maxOf(bestScore, score)
        state = GameState.GameOver
    }
}
