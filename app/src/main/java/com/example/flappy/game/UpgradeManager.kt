package com.example.flappy.game

class UpgradeManager {
    val activeUpgrades: List<UpgradeType>
        get() = selectedUpgrades

    var currentChoices: List<UpgradeType> = emptyList()
        private set

    private val selectedUpgrades = mutableListOf<UpgradeType>()
    private var nextUpgradeScore = GameConfig.UPGRADE_INTERVAL_SCORE

    fun reset() {
        selectedUpgrades.clear()
        currentChoices = emptyList()
        nextUpgradeScore = GameConfig.UPGRADE_INTERVAL_SCORE
    }

    fun startChoiceIfReady(score: Int, upcomingBossScore: Int): Boolean {
        if (currentChoices.isNotEmpty()) {
            return true
        }

        while (nextUpgradeScore < score) {
            nextUpgradeScore += GameConfig.UPGRADE_INTERVAL_SCORE
        }

        if (score < nextUpgradeScore || score >= upcomingBossScore) {
            return false
        }

        currentChoices = UpgradeType.values()
            .toList()
            .shuffled()
            .take(GameConfig.UPGRADE_CHOICES)
        nextUpgradeScore += GameConfig.UPGRADE_INTERVAL_SCORE
        return true
    }

    fun skipChoicesThrough(score: Int) {
        while (nextUpgradeScore <= score) {
            nextUpgradeScore += GameConfig.UPGRADE_INTERVAL_SCORE
        }
    }

    fun choose(index: Int, modifiers: RunModifiers): UpgradeType? {
        val upgrade = currentChoices.getOrNull(index) ?: return null
        upgrade.applyTo(modifiers)
        selectedUpgrades += upgrade
        currentChoices = emptyList()
        return upgrade
    }
}
