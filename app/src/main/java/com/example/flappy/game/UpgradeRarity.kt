package com.example.flappy.game

enum class UpgradeRarity(
    val label: String,
    val weight: Int
) {
    Common("Common", 70),
    Rare("Rare", 24),
    Epic("Epic", 6)
}
