package com.example.flappy.game

enum class MetaUpgradeKind {
    SlotUnlock,
    PermanentBoost
}

enum class MetaUpgrade(
    val title: String,
    val description: String,
    val cost: Int,
    val kind: MetaUpgradeKind,
    val effectDescription: String,
    private val effect: (RunModifiers) -> Unit
) {
    SlotOne(
        title = "Starting Slot I",
        description = "Unlock one free opening upgrade pick each run.",
        cost = 90,
        kind = MetaUpgradeKind.SlotUnlock,
        effectDescription = "+1 starting slot",
        effect = {}
    ),
    SlotTwo(
        title = "Starting Slot II",
        description = "Add another free opening upgrade pick.",
        cost = 160,
        kind = MetaUpgradeKind.SlotUnlock,
        effectDescription = "+1 starting slot",
        effect = {}
    ),
    SlotThree(
        title = "Starting Slot III",
        description = "Round out your opening build with one more slot.",
        cost = 240,
        kind = MetaUpgradeKind.SlotUnlock,
        effectDescription = "+1 starting slot",
        effect = {}
    ),
    FeatherweightCore(
        title = "Featherweight Core",
        description = "All future runs start with softer gravity.",
        cost = 130,
        kind = MetaUpgradeKind.PermanentBoost,
        effectDescription = "Permanent gravity reduction",
        effect = { modifiers -> modifiers.softenGravity() }
    ),
    PowerFlapCore(
        title = "Power Flap Core",
        description = "All future runs start with a stronger flap.",
        cost = 170,
        kind = MetaUpgradeKind.PermanentBoost,
        effectDescription = "Permanent flap boost",
        effect = { modifiers -> modifiers.strengthenFlap() }
    ),
    WideGapsCore(
        title = "Wide Gaps Core",
        description = "All future runs begin with wider obstacle gaps.",
        cost = 210,
        kind = MetaUpgradeKind.PermanentBoost,
        effectDescription = "Permanent gap increase",
        effect = { modifiers -> modifiers.widenGaps() }
    ),
    SecondWindCore(
        title = "Second Wind Core",
        description = "All future runs begin with one shield charge.",
        cost = 260,
        kind = MetaUpgradeKind.PermanentBoost,
        effectDescription = "Start with a shield",
        effect = { modifiers -> modifiers.addShield() }
    ),
    QuickShotCore(
        title = "Quick Shot Core",
        description = "All future boss fights begin with faster shots.",
        cost = 320,
        kind = MetaUpgradeKind.PermanentBoost,
        effectDescription = "Permanent shot cooldown boost",
        effect = { modifiers -> modifiers.improveShotCooldown() }
    );

    fun applyTo(modifiers: RunModifiers) {
        effect(modifiers)
    }

    companion object {
        val count: Int = values().size
    }
}