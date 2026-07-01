package com.example.flappy.game

enum class UpgradeType(
    val title: String,
    val description: String
) {
    Featherweight(
        title = "Featherweight",
        description = "Gravity and fall speed are softer."
    ),
    PowerFlap(
        title = "Power Flap",
        description = "Each tap gives a stronger upward burst."
    ),
    TinyBlock(
        title = "Tiny Block",
        description = "Your hitbox gets smaller."
    ),
    SlowField(
        title = "Slow Field",
        description = "Obstacles slow down. Boss shots will too."
    ),
    WideGaps(
        title = "Wide Gaps",
        description = "Future gaps open wider and spawn farther apart."
    ),
    SecondWind(
        title = "Second Wind",
        description = "Gain a shield that blocks one crash."
    ),
    BossBurn(
        title = "Boss Burn",
        description = "Boss health drains faster and gaps open a little wider."
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
        }
    }
}
