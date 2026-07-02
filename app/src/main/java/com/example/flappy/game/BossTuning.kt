package com.example.flappy.game

data class BossTuning(
    val level: Int,
    val maxHealth: Float,
    val projectileSpeedMultiplier: Float,
    val attackDelayMultiplier: Float,
    val hoverSpeedMultiplier: Float,
    val attackPatterns: List<BossAttackPattern>
) {
    companion object {
        fun forLevel(level: Int): BossTuning {
            val safeLevel = level.coerceAtLeast(1)
            val levelIndex = safeLevel - 1

            val patterns = mutableListOf(
                BossAttackPattern.Single,
                BossAttackPattern.Offset
            )
            if (safeLevel >= 3) patterns += BossAttackPattern.Double
            if (safeLevel >= 4) patterns += BossAttackPattern.Aimed
            if (safeLevel >= 5) patterns += BossAttackPattern.Spread
            if (safeLevel >= 6) patterns += BossAttackPattern.Burst

            return BossTuning(
                level = safeLevel,
                maxHealth = GameConfig.BOSS_MAX_HEALTH *
                    (1f + levelIndex * GameConfig.BOSS_HEALTH_GAIN_PER_LEVEL),
                projectileSpeedMultiplier = (1f + levelIndex * GameConfig.BOSS_PROJECTILE_SPEED_GAIN_PER_LEVEL)
                    .coerceAtMost(GameConfig.BOSS_PROJECTILE_SPEED_MAX_MULTIPLIER),
                attackDelayMultiplier = (1f - levelIndex * GameConfig.BOSS_ATTACK_DELAY_LOSS_PER_LEVEL)
                    .coerceAtLeast(GameConfig.BOSS_ATTACK_DELAY_MIN_MULTIPLIER),
                hoverSpeedMultiplier = (1f + levelIndex * GameConfig.BOSS_HOVER_SPEED_GAIN_PER_LEVEL)
                    .coerceAtMost(GameConfig.BOSS_HOVER_SPEED_MAX_MULTIPLIER),
                attackPatterns = patterns
            )
        }
    }
}
