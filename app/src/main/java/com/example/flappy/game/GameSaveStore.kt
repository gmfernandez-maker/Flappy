package com.example.flappy.game

import android.content.Context

class GameSaveStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): MetaProgression {
        return MetaProgression(
            bestScore = prefs.getInt(KEY_BEST_SCORE, 0),
            coins = prefs.getInt(KEY_COINS, 0),
            ownedMetaUpgradeCount = prefs.getInt(KEY_META_UPGRADES, 0),
            unlockedCharacterCount = prefs.getInt(KEY_CHARACTERS, 0),
            selectedCharacterIndex = prefs.getInt(KEY_SELECTED_CHARACTER, -1)
        ).also { progression ->
            progression.ownedMetaUpgradeCount = progression.ownedMetaUpgradeCount.coerceIn(0, MetaUpgrade.count)
            progression.unlockedCharacterCount = progression.unlockedCharacterCount.coerceIn(0, CharacterSkin.count)
            if (progression.unlockedCharacterCount == 0) {
                progression.selectedCharacterIndex = -1
            } else if (progression.selectedCharacterIndex !in 0 until progression.unlockedCharacterCount) {
                progression.selectedCharacterIndex = progression.unlockedCharacterCount - 1
            }
        }
    }

    fun save(progression: MetaProgression) {
        prefs.edit()
            .putInt(KEY_BEST_SCORE, progression.bestScore)
            .putInt(KEY_COINS, progression.coins)
            .putInt(KEY_META_UPGRADES, progression.ownedMetaUpgradeCount.coerceIn(0, MetaUpgrade.count))
            .putInt(KEY_CHARACTERS, progression.unlockedCharacterCount.coerceIn(0, CharacterSkin.count))
            .putInt(KEY_SELECTED_CHARACTER, progression.selectedCharacterIndex)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "flappy_save_data"
        const val KEY_BEST_SCORE = "best_score"
        const val KEY_COINS = "coins"
        const val KEY_META_UPGRADES = "meta_upgrades"
        const val KEY_CHARACTERS = "characters"
        const val KEY_SELECTED_CHARACTER = "selected_character"
    }
}