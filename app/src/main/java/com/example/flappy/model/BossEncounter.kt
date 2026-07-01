package com.example.flappy.model

import android.graphics.RectF
import com.example.flappy.game.GameConfig
import com.example.flappy.game.RunModifiers
import kotlin.random.Random

class BossEncounter {
    val boss = Boss()

    val bossProjectiles: List<Projectile>
        get() = enemyProjectiles

    val playerProjectiles: List<Projectile>
        get() = friendlyProjectiles

    private val enemyProjectiles = mutableListOf<Projectile>()
    private val friendlyProjectiles = mutableListOf<Projectile>()
    private var screenWidth = 0f
    private var screenHeight = 0f
    private var bossAttackTimer = 0f
    private var playerShotCooldown = 0f

    fun reset(width: Float, height: Float, modifiers: RunModifiers) {
        screenWidth = width
        screenHeight = height
        enemyProjectiles.clear()
        friendlyProjectiles.clear()
        playerShotCooldown = 0f
        bossAttackTimer = nextAttackDelay()
        boss.reset(width, height)
    }

    fun clear() {
        enemyProjectiles.clear()
        friendlyProjectiles.clear()
        playerShotCooldown = 0f
        bossAttackTimer = 0f
        boss.clear()
    }

    fun clearEnemyPressure() {
        enemyProjectiles.clear()
    }

    fun firePlayerShot(playerBounds: RectF) {
        if (playerShotCooldown > 0f || boss.isDefeated) {
            return
        }

        val shotWidth = (screenWidth * 0.07f).coerceIn(54f, 96f)
        val shotHeight = (screenHeight * 0.016f).coerceIn(18f, 38f)
        val left = playerBounds.right + screenWidth * 0.015f
        friendlyProjectiles += Projectile(
            owner = ProjectileOwner.Player,
            left = left,
            centerY = playerBounds.centerY(),
            width = shotWidth,
            height = shotHeight,
            velocityX = screenWidth * GameConfig.PLAYER_PROJECTILE_SPEED_SCREEN_RATIO,
            damage = GameConfig.PLAYER_PROJECTILE_DAMAGE
        )
        playerShotCooldown = GameConfig.PLAYER_SHOT_COOLDOWN_SECONDS
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
        resolvePlayerShots()
        spawnBossShots(deltaSeconds, modifiers)

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

    private fun spawnBossShots(deltaSeconds: Float, modifiers: RunModifiers) {
        bossAttackTimer -= deltaSeconds
        if (bossAttackTimer > 0f || boss.isDefeated) {
            return
        }

        val size = (screenHeight * GameConfig.BOSS_PROJECTILE_SIZE_SCREEN_RATIO).coerceIn(34f, 72f)
        val verticalSpread = boss.bounds.height() * 0.75f
        val centerY = (
            boss.bounds.centerY() + Random.nextFloat() * verticalSpread - verticalSpread / 2f
            ).coerceIn(size / 2f, screenHeight - size / 2f)
        val speedRoll = 0.85f + Random.nextFloat() * 0.35f

        enemyProjectiles += Projectile(
            owner = ProjectileOwner.Boss,
            left = boss.bounds.left - size,
            centerY = centerY,
            width = size,
            height = size,
            velocityX = -screenWidth *
                GameConfig.BOSS_PROJECTILE_SPEED_SCREEN_RATIO *
                modifiers.bossProjectileSpeedMultiplier *
                speedRoll,
            damage = 1f
        )
        bossAttackTimer = nextAttackDelay()
    }

    private fun nextAttackDelay(): Float {
        return GameConfig.BOSS_ATTACK_DELAY_MIN +
            Random.nextFloat() * (GameConfig.BOSS_ATTACK_DELAY_MAX - GameConfig.BOSS_ATTACK_DELAY_MIN)
    }
}
