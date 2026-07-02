package com.example.flappy.game

enum class UpgradeType(
    val title: String,
    val description: String,
    val rarity: UpgradeRarity,
    val maxStacks: Int
) {
    Featherweight(
        title = "Featherweight",
        description = "Gravity and fall speed are softer.",
        rarity = UpgradeRarity.Common,
        maxStacks = 4
    ),
    PowerFlap(
        title = "Power Flap",
        description = "Each tap gives a stronger upward burst.",
        rarity = UpgradeRarity.Common,
        maxStacks = 3
    ),
    TinyBlock(
        title = "Tiny Block",
        description = "Your hitbox gets smaller.",
        rarity = UpgradeRarity.Rare,
        maxStacks = 3
    ),
    SlowField(
        title = "Slow Field",
        description = "Obstacles slow down. Boss shots will too.",
        rarity = UpgradeRarity.Common,
        maxStacks = 4
    ),
    WideGaps(
        title = "Wide Gaps",
        description = "Future gaps open wider and spawn farther apart.",
        rarity = UpgradeRarity.Common,
        maxStacks = 4
    ),
    SecondWind(
        title = "Second Wind",
        description = "Gain a shield that blocks one crash.",
        rarity = UpgradeRarity.Rare,
        maxStacks = 5
    ),
    BossBurn(
        title = "Boss Burn",
        description = "Boss health drains faster and gaps open a little wider.",
        rarity = UpgradeRarity.Rare,
        maxStacks = 4
    ),
    QuickShot(
        title = "Quick Shot",
        description = "Boss-fight shots recharge faster.",
        rarity = UpgradeRarity.Common,
        maxStacks = 4
    ),
    HeavyShot(
        title = "Heavy Shot",
        description = "Player projectiles hit bosses harder.",
        rarity = UpgradeRarity.Rare,
        maxStacks = 4
    ),
    SplitShot(
        title = "Split Shot",
        description = "Fire extra boss-fight shots in a small spread.",
        rarity = UpgradeRarity.Epic,
        maxStacks = 2
    ),
    PiercingShot(
        title = "Piercing Shot",
        description = "Shots fly faster and hit harder.",
        rarity = UpgradeRarity.Epic,
        maxStacks = 3
    );

    fun applyTo(modifiers: RunModifiers) {
        when (this) {
            Featherweight -> modifiers.softenGravity()
            PowerFlap -> modifiers.strengthenFlap()
            TinyBlock -> modifiers.shrinkPlayer()
            SlowField -> modifiers.slowObstacles()
            WideGaps -> modifiers.widenGaps()
            SecondWind -> modifiers.addShield()
            BossBurn -> {
                modifiers.improveBossDamage()
                modifiers.widenGaps()
            }
            QuickShot -> modifiers.improveShotCooldown()
            HeavyShot -> modifiers.improveShotDamage()
            SplitShot -> modifiers.addSplitShot()
            PiercingShot -> modifiers.improvePiercingShot()
        }
    }
}
