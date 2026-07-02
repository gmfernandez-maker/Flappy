package com.example.flappy.game

class UpgradeManager {
    val activeUpgrades: List<UpgradeType>
        get() = selectedUpgrades
    val upgradeStacks: List<UpgradeStack>
        get() = UpgradeType.values()
            .mapNotNull { type ->
                val count = stackCounts[type] ?: 0
                if (count > 0) UpgradeStack(type, count) else null
            }

    var currentChoices: List<UpgradeType> = emptyList()
        private set
    var currentOfferType = UpgradeOfferType.Regular
        private set

    private val selectedUpgrades = mutableListOf<UpgradeType>()
    private val stackCounts = mutableMapOf<UpgradeType, Int>()
    private var nextUpgradeScore = GameConfig.UPGRADE_INTERVAL_SCORE

    fun reset() {
        selectedUpgrades.clear()
        stackCounts.clear()
        currentChoices = emptyList()
        currentOfferType = UpgradeOfferType.Regular
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

        rollChoices(UpgradeOfferType.Regular)
        nextUpgradeScore += GameConfig.UPGRADE_INTERVAL_SCORE
        return true
    }

    fun skipChoicesThrough(score: Int) {
        while (nextUpgradeScore <= score) {
            nextUpgradeScore += GameConfig.UPGRADE_INTERVAL_SCORE
        }
    }

    fun forceChoice() {
        rollChoices(UpgradeOfferType.Debug)
    }

    fun startStartingDraft() {
        rollChoices(UpgradeOfferType.StartingDraft)
    }

    fun startBossRewardChoice() {
        rollChoices(UpgradeOfferType.BossReward)
    }

    fun choose(index: Int, modifiers: RunModifiers): UpgradeType? {
        val upgrade = currentChoices.getOrNull(index) ?: return null
        if ((stackCounts[upgrade] ?: 0) >= upgrade.maxStacks) {
            return null
        }

        upgrade.applyTo(modifiers)
        selectedUpgrades += upgrade
        stackCounts[upgrade] = (stackCounts[upgrade] ?: 0) + 1
        currentChoices = emptyList()
        currentOfferType = UpgradeOfferType.Regular
        return upgrade
    }

    fun stackCount(type: UpgradeType): Int {
        return stackCounts[type] ?: 0
    }

    private fun rollChoices(offerType: UpgradeOfferType) {
        currentOfferType = offerType
        val available = UpgradeType.values()
            .filter { type -> (stackCounts[type] ?: 0) < type.maxStacks }

        currentChoices = if (available.size <= GameConfig.UPGRADE_CHOICES) {
            available.shuffled()
        } else {
            val choices = mutableListOf<UpgradeType>()
            while (choices.size < GameConfig.UPGRADE_CHOICES) {
                val nextChoice = weightedPick(
                    available = available.filterNot { type -> choices.contains(type) },
                    offerType = offerType
                )
                if (nextChoice == null) {
                    break
                }
                choices += nextChoice
            }
            choices
        }
    }

    private fun weightedPick(
        available: List<UpgradeType>,
        offerType: UpgradeOfferType
    ): UpgradeType? {
        if (available.isEmpty()) {
            return null
        }

        val weighted = available.map { type -> type to adjustedWeight(type, offerType) }
        val totalWeight = weighted.sumOf { (_, weight) -> weight }
        if (totalWeight <= 0) {
            return available.random()
        }

        var roll = kotlin.random.Random.nextInt(totalWeight)
        weighted.forEach { (type, weight) ->
            if (roll < weight) {
                return type
            }
            roll -= weight
        }

        return weighted.last().first
    }

    private fun adjustedWeight(type: UpgradeType, offerType: UpgradeOfferType): Int {
        val baseWeight = type.rarity.weight
        val rarityBonus = when (offerType) {
            UpgradeOfferType.Regular,
            UpgradeOfferType.StartingDraft,
            UpgradeOfferType.Debug -> 0
            UpgradeOfferType.BossReward -> when (type.rarity) {
                UpgradeRarity.Common -> -25
                UpgradeRarity.Rare -> 26
                UpgradeRarity.Epic -> 38
            }
        }
        val stackPenalty = (stackCounts[type] ?: 0) * 8
        return (baseWeight + rarityBonus - stackPenalty).coerceAtLeast(1)
    }
}
