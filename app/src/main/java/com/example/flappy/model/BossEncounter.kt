package com.example.flappy.model

import android.graphics.RectF
import com.example.flappy.game.BossAttackPattern
import com.example.flappy.game.BossTuning
import com.example.flappy.game.GameConfig
import com.example.flappy.game.RunModifiers
import kotlin.random.Random

class BossEncounter {
    val boss = Boss()

    val bossProjectiles: List<Projectile>
        get() = enemyProjectiles

    val playerProjectiles: List<Projectile>
        get() = friendlyProjectiles
    val bossTelegraphs: List<BossTelegraph>
        get() = pendingBossShots.map { pendingShot -> pendingShot.telegraph }

    private val enemyProjectiles = mutableListOf<Projectile>()
    private val friendlyProjectiles = mutableListOf<Projectile>()
    private val pendingBossShots = mutableListOf<PendingBossShot>()
    private var screenWidth = 0f
    private var screenHeight = 0f
    private var bossAttackTimer = 0f
    private var playerShotCooldown = 0f
    private var tuning = BossTuning.forLevel(1)

    fun reset(width: Float, height: Float, bossLevel: Int) {
        screenWidth = width
        screenHeight = height
        tuning = BossTuning.forLevel(bossLevel)
        enemyProjectiles.clear()
        friendlyProjectiles.clear()
        pendingBossShots.clear()
        playerShotCooldown = 0f
        bossAttackTimer = nextAttackDelay()
        boss.reset(width, height, tuning)
    }

    fun clear() {
        enemyProjectiles.clear()
        friendlyProjectiles.clear()
        pendingBossShots.clear()
        playerShotCooldown = 0f
        bossAttackTimer = 0f
        boss.clear()
    }

    fun clearEnemyPressure() {
        enemyProjectiles.clear()
        pendingBossShots.clear()
    }

    fun firePlayerShot(playerBounds: RectF, modifiers: RunModifiers) {
        if (playerShotCooldown > 0f || boss.isDefeated) {
            return
        }

        val shotWidth = (screenWidth * 0.07f).coerceIn(54f, 96f)
        val shotHeight = (screenHeight * 0.016f).coerceIn(18f, 38f)
        val left = playerBounds.right + screenWidth * 0.015f
        val shotCount = 1 + modifiers.extraPlayerShots
        val verticalGap = shotHeight * 1.5f
        val firstOffset = -(shotCount - 1) * verticalGap / 2f
        repeat(shotCount) { index ->
            val centerY = (playerBounds.centerY() + firstOffset + index * verticalGap)
                .coerceIn(shotHeight / 2f, screenHeight - shotHeight / 2f)
            friendlyProjectiles += Projectile(
                owner = ProjectileOwner.Player,
                left = left,
                centerY = centerY,
                width = shotWidth,
                height = shotHeight,
                velocityX = screenWidth *
                    GameConfig.PLAYER_PROJECTILE_SPEED_SCREEN_RATIO *
                    modifiers.playerShotSpeedMultiplier,
                damage = GameConfig.PLAYER_PROJECTILE_DAMAGE * modifiers.playerShotDamageMultiplier
            )
        }
        playerShotCooldown = GameConfig.PLAYER_SHOT_COOLDOWN_SECONDS *
            modifiers.playerShotCooldownMultiplier
    }

    fun update(
        deltaSeconds: Float,
        playerBounds: RectF,
        modifiers: RunModifiers
    ): BossUpdateResult {
        if (boss.isDefeated) {
            return BossUpdateResult(playerWasHit = false, bossDefeated = true)
        }

        boss.update(deltaSeconds)
        playerShotCooldown = (playerShotCooldown - deltaSeconds).coerceAtLeast(0f)
        boss.takeDamage(
            GameConfig.BOSS_SURVIVAL_DAMAGE_PER_SECOND *
                modifiers.bossSurvivalDamageMultiplier *
                deltaSeconds
        )

        updateProjectiles(deltaSeconds)
        updatePendingShots(deltaSeconds, modifiers)
        resolvePlayerShots()
        spawnBossShots(deltaSeconds, playerBounds, modifiers)

        val hitByBossBody = RectF.intersects(playerBounds, boss.bounds)
        val hitByProjectile = enemyProjectiles.firstOrNull { it.collidesWith(playerBounds) }
        if (hitByProjectile != null) {
            enemyProjectiles.remove(hitByProjectile)
        }

        return BossUpdateResult(
            playerWasHit = hitByBossBody || hitByProjectile != null,
            bossDefeated = boss.isDefeated
        )
    }

    private fun updateProjectiles(deltaSeconds: Float) {
        enemyProjectiles.forEach { projectile -> projectile.update(deltaSeconds) }
        friendlyProjectiles.forEach { projectile -> projectile.update(deltaSeconds) }
        enemyProjectiles.removeAll { projectile -> projectile.isOffScreen(screenWidth) }
        friendlyProjectiles.removeAll { projectile -> projectile.isOffScreen(screenWidth) }
    }

    private fun resolvePlayerShots() {
        val hits = friendlyProjectiles.filter { projectile -> projectile.collidesWith(boss.bounds) }
        hits.forEach { projectile ->
            boss.takeDamage(projectile.damage)
        }
        friendlyProjectiles.removeAll(hits.toSet())
    }

    private fun spawnBossShots(
        deltaSeconds: Float,
        playerBounds: RectF,
        modifiers: RunModifiers
    ) {
        bossAttackTimer -= deltaSeconds
        if (bossAttackTimer > 0f || boss.isDefeated) {
            return
        }

        val size = (screenHeight * GameConfig.BOSS_PROJECTILE_SIZE_SCREEN_RATIO).coerceIn(34f, 72f)
        when (tuning.attackPatterns.random()) {
            BossAttackPattern.Single -> fireBossProjectile(
                centerY = boss.bounds.centerY(),
                size = size,
                speedRoll = randomSpeedRoll(),
                modifiers = modifiers
            )
            BossAttackPattern.Offset -> fireBossProjectile(
                centerY = randomBossLane(size),
                size = size,
                speedRoll = randomSpeedRoll(),
                modifiers = modifiers
            )
            BossAttackPattern.Double -> {
                val gap = size * 1.85f
                val centerY = randomBossLane(size)
                fireBossProjectile(centerY - gap, size, randomSpeedRoll(), modifiers)
                fireBossProjectile(centerY + gap, size, randomSpeedRoll(), modifiers)
            }
            BossAttackPattern.Aimed -> queueTelegraphedShot(
                centerY = playerBounds.centerY(),
                size = size,
                speedRoll = 1.12f + Random.nextFloat() * 0.22f,
                modifiers = modifiers
            )
            BossAttackPattern.Spread -> fireSafeGapSpread(playerBounds, size, modifiers)
            BossAttackPattern.Burst -> {
                val centerY = playerBounds.centerY()
                fireBossProjectile(centerY, size, 1.18f, modifiers)
                fireBossProjectile(centerY, size, 1.03f, modifiers, xOffset = size * 1.45f)
                fireBossProjectile(centerY, size, 0.9f, modifiers, xOffset = size * 2.9f)
            }
        }
        bossAttackTimer = nextAttackDelay()
    }

    private fun updatePendingShots(deltaSeconds: Float, modifiers: RunModifiers) {
        val readyShots = pendingBossShots.filter { pendingShot -> pendingShot.update(deltaSeconds) }
        readyShots.forEach { pendingShot ->
            fireBossProjectile(
                centerY = pendingShot.centerY,
                size = pendingShot.size,
                speedRoll = pendingShot.speedRoll,
                modifiers = modifiers,
                xOffset = pendingShot.xOffset
            )
        }
        pendingBossShots.removeAll(readyShots.toSet())
    }

    private fun queueTelegraphedShot(
        centerY: Float,
        size: Float,
        speedRoll: Float,
        modifiers: RunModifiers,
        xOffset: Float = 0f
    ) {
        val safeCenterY = centerY.coerceIn(size / 2f, screenHeight - size / 2f)
        val telegraph = BossTelegraph(
            left = 0f,
            right = boss.bounds.left,
            centerY = safeCenterY,
            height = size * GameConfig.BOSS_TELEGRAPH_HEIGHT_MULTIPLIER,
            totalSeconds = GameConfig.BOSS_AIMED_TELEGRAPH_SECONDS *
                if (boss.isEnraged) GameConfig.BOSS_ENRAGE_ATTACK_DELAY_MULTIPLIER else 1f
        )
        pendingBossShots += PendingBossShot(
            centerY = safeCenterY,
            size = size,
            speedRoll = speedRoll,
            xOffset = xOffset,
            telegraph = telegraph
        )
    }

    private fun fireSafeGapSpread(
        playerBounds: RectF,
        size: Float,
        modifiers: RunModifiers
    ) {
        val safeCenterY = playerBounds.centerY()
            .coerceIn(size * 2.7f, screenHeight - size * 2.7f)
        val laneGap = size * 2.35f
        val lanes = listOf(
            safeCenterY - laneGap * 1.45f,
            safeCenterY - laneGap,
            safeCenterY + laneGap,
            safeCenterY + laneGap * 1.45f
        ).filter { centerY ->
            centerY >= size / 2f && centerY <= screenHeight - size / 2f
        }

        lanes.forEachIndexed { index, centerY ->
            fireBossProjectile(
                centerY = centerY,
                size = size,
                speedRoll = if (index % 2 == 0) 0.96f else 1.08f,
                modifiers = modifiers
            )
        }
    }

    private fun fireBossProjectile(
        centerY: Float,
        size: Float,
        speedRoll: Float,
        modifiers: RunModifiers,
        xOffset: Float = 0f
    ) {
        val safeCenterY = centerY.coerceIn(size / 2f, screenHeight - size / 2f)
        enemyProjectiles += Projectile(
            owner = ProjectileOwner.Boss,
            left = boss.bounds.left - size - xOffset,
            centerY = safeCenterY,
            width = size,
            height = size,
            velocityX = -screenWidth *
                GameConfig.BOSS_PROJECTILE_SPEED_SCREEN_RATIO *
                tuning.projectileSpeedMultiplier *
                enragedProjectileMultiplier() *
                modifiers.bossProjectileSpeedMultiplier *
                speedRoll,
            damage = 1f
        )
    }

    private fun randomBossLane(size: Float): Float {
        val verticalSpread = boss.bounds.height() * 0.78f
        return (
            boss.bounds.centerY() + Random.nextFloat() * verticalSpread - verticalSpread / 2f
            ).coerceIn(size / 2f, screenHeight - size / 2f)
    }

    private fun randomSpeedRoll(): Float {
        return 0.85f + Random.nextFloat() * 0.35f
    }

    private fun nextAttackDelay(): Float {
        val baseDelay = GameConfig.BOSS_ATTACK_DELAY_MIN +
            Random.nextFloat() * (GameConfig.BOSS_ATTACK_DELAY_MAX - GameConfig.BOSS_ATTACK_DELAY_MIN)
        return baseDelay * tuning.attackDelayMultiplier * enragedAttackDelayMultiplier()
    }

    private fun enragedProjectileMultiplier(): Float {
        return if (boss.isEnraged) GameConfig.BOSS_ENRAGE_PROJECTILE_SPEED_MULTIPLIER else 1f
    }

    private fun enragedAttackDelayMultiplier(): Float {
        return if (boss.isEnraged) GameConfig.BOSS_ENRAGE_ATTACK_DELAY_MULTIPLIER else 1f
    }

    private data class PendingBossShot(
        val centerY: Float,
        val size: Float,
        val speedRoll: Float,
        val xOffset: Float,
        val telegraph: BossTelegraph
    ) {
        fun update(deltaSeconds: Float): Boolean {
            return telegraph.update(deltaSeconds)
        }
    }
}
