package com.example.flappy.game

enum class UpgradeOfferType(
    val title: String,
    val subtitle: String
) {
    StartingDraft(
        title = "Starting Loadout",
        subtitle = "Choose your free opening upgrades"
    ),
    Regular(
        title = "Choose Your Upgrade",
        subtitle = "Boss arrives at score"
    ),
    BossReward(
        title = "Boss Reward",
        subtitle = "Claim a stronger reward"
    ),
    Debug(
        title = "Debug Upgrade",
        subtitle = "Testing choice"
    )
}
