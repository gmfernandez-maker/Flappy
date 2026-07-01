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
}
