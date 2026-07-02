package com.example.flappy.game

class RunModifiers {
    var gravityMultiplier = 1f
        private set
    var flapStrengthMultiplier = 1f
        private set
    var maxFallSpeedMultiplier = 1f
        private set
    var playerSizeMultiplier = 1f
        private set
    var obstacleGapMultiplier = 1f
        private set
    var obstacleSpeedMultiplier = 1f
        private set
    var obstacleSpacingMultiplier = 1f
        private set
    var bossSurvivalDamageMultiplier = 1f
        private set
    var bossProjectileSpeedMultiplier = 1f
        private set
    var playerShotDamageMultiplier = 1f
        private set
    var playerShotCooldownMultiplier = 1f
        private set
    var playerShotSpeedMultiplier = 1f
        private set
    var extraPlayerShots = 0
        private set
    var shieldCharges = 0
        private set

    fun reset() {
        gravityMultiplier = 1f
        flapStrengthMultiplier = 1f
        maxFallSpeedMultiplier = 1f
        playerSizeMultiplier = 1f
        obstacleGapMultiplier = 1f
        obstacleSpeedMultiplier = 1f
        obstacleSpacingMultiplier = 1f
        bossSurvivalDamageMultiplier = 1f
        bossProjectileSpeedMultiplier = 1f
        playerShotDamageMultiplier = 1f
        playerShotCooldownMultiplier = 1f
        playerShotSpeedMultiplier = 1f
        extraPlayerShots = 0
        shieldCharges = 0
    }

    fun softenGravity() {
        gravityMultiplier = (gravityMultiplier * 0.9f).coerceAtLeast(0.65f)
        maxFallSpeedMultiplier = (maxFallSpeedMultiplier * 0.95f).coerceAtLeast(0.75f)
    }

    fun strengthenFlap() {
        flapStrengthMultiplier = (flapStrengthMultiplier * 1.1f).coerceAtMost(1.35f)
    }

    fun shrinkPlayer() {
        playerSizeMultiplier = (playerSizeMultiplier * 0.9f).coerceAtLeast(0.72f)
    }

    fun widenGaps() {
        obstacleGapMultiplier = (obstacleGapMultiplier * 1.09f).coerceAtMost(1.28f)
        obstacleSpacingMultiplier = (obstacleSpacingMultiplier * 1.03f).coerceAtMost(1.16f)
    }

    fun slowObstacles() {
        obstacleSpeedMultiplier = (obstacleSpeedMultiplier * 0.9f).coerceAtLeast(0.7f)
        bossProjectileSpeedMultiplier = (bossProjectileSpeedMultiplier * 0.94f).coerceAtLeast(0.72f)
    }

    fun addShield() {
        shieldCharges += 1
    }

    fun improveBossDamage() {
        bossSurvivalDamageMultiplier = (bossSurvivalDamageMultiplier * 1.18f).coerceAtMost(1.75f)
    }

    fun improveShotCooldown() {
        playerShotCooldownMultiplier = (playerShotCooldownMultiplier * 0.88f).coerceAtLeast(0.58f)
    }

    fun improveShotDamage() {
        playerShotDamageMultiplier = (playerShotDamageMultiplier * 1.18f).coerceAtMost(1.95f)
    }

    fun addSplitShot() {
        extraPlayerShots = (extraPlayerShots + 1).coerceAtMost(2)
        playerShotDamageMultiplier = (playerShotDamageMultiplier * 0.96f).coerceAtLeast(0.9f)
    }

    fun improvePiercingShot() {
        playerShotSpeedMultiplier = (playerShotSpeedMultiplier * 1.16f).coerceAtMost(1.55f)
        playerShotDamageMultiplier = (playerShotDamageMultiplier * 1.12f).coerceAtMost(1.95f)
    }

    fun tryConsumeShield(): Boolean {
        if (shieldCharges <= 0) {
            return false
        }

        shieldCharges -= 1
        return true
    }
}
