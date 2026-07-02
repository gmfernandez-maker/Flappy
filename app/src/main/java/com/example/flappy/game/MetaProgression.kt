package com.example.flappy.game

import android.graphics.Color

data class MetaProgression(
    var bestScore: Int = 0,
    var coins: Int = 0,
    var ownedMetaUpgradeCount: Int = 0,
    var unlockedCharacterCount: Int = 0,
    var selectedCharacterIndex: Int = -1
) {
    val startingUpgradeSlots: Int
        get() = MetaUpgrade.values()
            .take(ownedMetaUpgradeCount)
            .count { it.kind == MetaUpgradeKind.SlotUnlock }

    val ownedMetaUpgrades: List<MetaUpgrade>
        get() = MetaUpgrade.values().take(ownedMetaUpgradeCount)

    val selectedCharacter: CharacterSkin?
        get() {
            val index = selectedCharacterIndex
            return if (index in 0 until unlockedCharacterCount) {
                CharacterSkin.values()[index]
            } else {
                null
            }
        }

    fun addCoins(amount: Int) {
        if (amount <= 0) {
            return
        }

        coins += amount
    }

    fun recordBestScore(score: Int) {
        bestScore = maxOf(bestScore, score)
    }

    fun purchaseNextMetaUpgrade(): MetaUpgrade? {
        val nextUpgrade = MetaUpgrade.values().getOrNull(ownedMetaUpgradeCount) ?: return null
        ownedMetaUpgradeCount += 1
        return nextUpgrade
    }

    fun purchaseNextCharacter(): CharacterSkin? {
        val nextCharacter = CharacterSkin.values().getOrNull(unlockedCharacterCount) ?: return null
        unlockedCharacterCount += 1
        selectedCharacterIndex = unlockedCharacterCount - 1
        return nextCharacter
    }

    fun selectCharacter(index: Int): Boolean {
        if (index !in 0 until unlockedCharacterCount) {
            return false
        }

        selectedCharacterIndex = index
        return true
    }

    fun canBuyNextMetaUpgrade(): Boolean {
        return ownedMetaUpgradeCount < MetaUpgrade.count
    }

    fun canBuyNextCharacter(): Boolean {
        return unlockedCharacterCount < CharacterSkin.count
    }

    fun nextMetaUpgrade(): MetaUpgrade? {
        return MetaUpgrade.values().getOrNull(ownedMetaUpgradeCount)
    }

    fun nextCharacter(): CharacterSkin? {
        return CharacterSkin.values().getOrNull(unlockedCharacterCount)
    }

    fun applyPermanentBonuses(modifiers: RunModifiers) {
        ownedMetaUpgrades
            .filter { it.kind == MetaUpgradeKind.PermanentBoost }
            .forEach { it.applyTo(modifiers) }
    }

    fun playerColor(defaultColor: Int): Int {
        return selectedCharacter?.color ?: defaultColor
    }

    companion object {
        val DEFAULT_CHARACTER_COLOR = Color.rgb(128, 168, 196)
    }
}