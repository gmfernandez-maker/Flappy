package com.example.flappy.game

data class RunSummary(
    val score: Int = 0,
    val bestScore: Int = 0,
    val coinsEarned: Int = 0,
    val totalCoins: Int = 0,
    val upgrades: Int = 0,
    val bossesDefeated: Int = 0,
    val survivalSeconds: Float = 0f
)
