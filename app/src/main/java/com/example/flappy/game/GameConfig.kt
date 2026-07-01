package com.example.flappy.game

object GameConfig {
    const val PLAYER_SIZE_SCREEN_RATIO = 0.06f
    const val PLAYER_X_SCREEN_RATIO = 0.24f

    const val GRAVITY_SCREEN_RATIO = 1.65f
    const val FLAP_VELOCITY_SCREEN_RATIO = -0.58f
    const val MAX_FALL_SPEED_SCREEN_RATIO = 1.18f

    const val OBSTACLE_WIDTH_SCREEN_RATIO = 0.16f
    const val OBSTACLE_GAP_SCREEN_RATIO = 0.33f
    const val OBSTACLE_GAP_MIN = 250f
    const val OBSTACLE_GAP_MAX = 440f
    const val OBSTACLE_SPACING_SCREEN_RATIO = 0.64f
    const val OBSTACLE_SPEED_SCREEN_RATIO = 0.37f
    const val OBSTACLE_VERTICAL_MARGIN_RATIO = 0.1f
    const val OBSTACLE_GAP_UPGRADE_MAX = 560f

    const val UPGRADE_INTERVAL_SCORE = 3
    const val UPGRADE_CHOICES = 3
    const val BOSS_SCORE_THRESHOLD = 8
    const val BOSS_SCORE_INTERVAL = 10
    const val BOSS_DEFEAT_SCORE_BONUS = 3

    const val BOSS_MAX_HEALTH = 100f
    const val BOSS_WIDTH_SCREEN_RATIO = 0.22f
    const val BOSS_HEIGHT_SCREEN_RATIO = 0.17f
    const val BOSS_HOVER_AMPLITUDE_SCREEN_RATIO = 0.22f
    const val BOSS_HOVER_SPEED = 1.65f
    const val BOSS_SURVIVAL_DAMAGE_PER_SECOND = 1.35f

    const val BOSS_ATTACK_DELAY_MIN = 1.05f
    const val BOSS_ATTACK_DELAY_MAX = 1.65f
    const val BOSS_PROJECTILE_SPEED_SCREEN_RATIO = 0.54f
    const val BOSS_PROJECTILE_SIZE_SCREEN_RATIO = 0.032f

    const val PLAYER_SHOT_COOLDOWN_SECONDS = 0.22f
    const val PLAYER_PROJECTILE_SPEED_SCREEN_RATIO = 0.96f
    const val PLAYER_PROJECTILE_DAMAGE = 10.5f
}
