package com.example.flappy.game

import com.example.flappy.model.Boss
import com.example.flappy.model.BossEncounter
import com.example.flappy.model.BossTelegraph
import com.example.flappy.model.ObstacleManager
import com.example.flappy.model.ObstaclePair
import com.example.flappy.model.Player
import com.example.flappy.model.Projectile

class GameWorld(private val saveStore: GameSaveStore) {
    val player = Player()
    val activeObstacles: List<ObstaclePair>
        get() = obstacleManager.activeObstacles
    val boss: Boss
        get() = bossEncounter.boss
    val bossProjectiles: List<Projectile>
        get() = bossEncounter.bossProjectiles
    val playerProjectiles: List<Projectile>
        get() = bossEncounter.playerProjectiles
    val bossTelegraphs: List<BossTelegraph>
        get() = bossEncounter.bossTelegraphs
    val upgradeChoices: List<UpgradeType>
        get() = upgradeManager.currentChoices
    val activeUpgrades: List<UpgradeType>
        get() = upgradeManager.activeUpgrades
    val upgradeStacks: List<UpgradeStack>
        get() = upgradeManager.upgradeStacks
    val currentUpgradeOfferType: UpgradeOfferType
        get() = upgradeManager.currentOfferType
    val metaUpgrades: List<MetaUpgrade>
        get() = MetaUpgrade.values().toList()
    val characterSkins: List<CharacterSkin>
        get() = CharacterSkin.values().toList()
    val bestScore: Int
        get() = progression.bestScore
    val coinCount: Int
        get() = progression.coins
    val startingUpgradeSlots: Int
        get() = progression.startingUpgradeSlots
    val selectedCharacterIndex: Int
        get() = progression.selectedCharacterIndex
    val selectedCharacterColor: Int
        get() = progression.playerColor(MetaProgression.DEFAULT_CHARACTER_COLOR)
    val nextMetaUpgrade: MetaUpgrade?
        get() = progression.nextMetaUpgrade()
    val nextMetaUpgradeIndex: Int
        get() = progression.ownedMetaUpgradeCount
    val nextCharacter: CharacterSkin?
        get() = progression.nextCharacter()
    val nextCharacterIndex: Int
        get() = progression.unlockedCharacterCount
    val shieldCharges: Int
        get() = runModifiers.shieldCharges
    val bossHealthFraction: Float
        get() = boss.healthFraction
    val nextBossScore: Int
        get() = upcomingBossScore
    val runSummary: RunSummary
        get() = lastRunSummary
    val runTimeSeconds: Float
        get() = currentRunSeconds
    val bossesDefeated: Int
        get() = currentBossesDefeated
    val currentBossLevel: Int
        get() = if (mode == GameMode.Boss) boss.level else currentBossesDefeated + 1
    val bossPhaseLabel: String
        get() = boss.phase.label
    val bossEnraged: Boolean
        get() = boss.isEnraged
    val transitionProgress: Float
        get() = if (transitionDuration <= 0f) {
            1f
        } else {
            (transitionTimer / transitionDuration).coerceIn(0f, 1f)
        }
    val debugTimeScale: Float
        get() = if (debugSlowMotion) 0.35f else 1f

    var state = GameState.Title
        private set

    var mode = GameMode.Classic
        private set

    var score = 0
        private set

    var debugPanelOpen = false
        private set

    var debugShowHitboxes = false
        private set

    var debugSlowMotion = false
        private set

    private val obstacleManager = ObstacleManager()
    private val bossEncounter = BossEncounter()
    private val upgradeManager = UpgradeManager()
    private val progression = saveStore.load()
    private val runModifiers = RunModifiers()
    private var screenWidth = 0f
    private var screenHeight = 0f
    private var hasValidSize = false
    private var upcomingBossScore = GameConfig.BOSS_SCORE_THRESHOLD
    private var transitionTimer = 0f
    private var transitionDuration = 0f
    private var currentRunSeconds = 0f
    private var currentBossesDefeated = 0
    private var currentRunCoinsEarned = 0
    private var pendingStartingChoices = 0
    private var lastRunSummary = RunSummary()

    fun resize(width: Int, height: Int) {
        if (width <= 0 || height <= 0) {
            return
        }

        screenWidth = width.toFloat()
        screenHeight = height.toFloat()
        hasValidSize = true
        resetToTitle()
    }

    fun handleTap() {
        if (!hasValidSize) {
            return
        }

        when (state) {
            GameState.Title -> Unit
            GameState.Running -> {
                player.flap()
                if (mode == GameMode.Boss) {
                    bossEncounter.firePlayerShot(player.bounds, runModifiers)
                }
            }
            GameState.UpgradeShop,
            GameState.CharacterShop -> Unit
            GameState.ChoosingUpgrade -> Unit
            GameState.BossWarning,
            GameState.BossDefeated -> Unit
            GameState.GameOver -> returnToTitle()
        }
    }

    fun chooseUpgrade(index: Int) {
        if (state != GameState.ChoosingUpgrade) {
            return
        }

        upgradeManager.choose(index, runModifiers) ?: return
        player.applyModifiers(screenHeight, runModifiers)
        obstacleManager.applyModifiers(runModifiers)
        var finishedStartingDraft = false
        if (pendingStartingChoices > 0) {
            pendingStartingChoices -= 1
            if (pendingStartingChoices > 0) {
                upgradeManager.startStartingDraft()
                state = GameState.ChoosingUpgrade
                return
            }

            finishedStartingDraft = true
        }

        state = GameState.Running
        if (finishedStartingDraft) {
            player.flap()
        }
    }

    fun handleMainMenuAction(action: MainMenuAction) {
        when (action) {
            MainMenuAction.StartRun -> startRun()
            MainMenuAction.OpenUpgradeShop -> openUpgradeShop()
            MainMenuAction.OpenCharacterShop -> openCharacterShop()
        }
    }

    fun openUpgradeShop() {
        if (state == GameState.Running || state == GameState.ChoosingUpgrade || state == GameState.BossWarning || state == GameState.BossDefeated) {
            return
        }

        state = GameState.UpgradeShop
    }

    fun openCharacterShop() {
        if (state == GameState.Running || state == GameState.ChoosingUpgrade || state == GameState.BossWarning || state == GameState.BossDefeated) {
            return
        }

        state = GameState.CharacterShop
    }

    fun returnToTitle() {
        if (state == GameState.Running || state == GameState.ChoosingUpgrade || state == GameState.BossWarning || state == GameState.BossDefeated) {
            return
        }

        resetToTitle()
    }

    fun purchaseNextMetaUpgrade(): Boolean {
        if (state != GameState.UpgradeShop) {
            return false
        }

        val nextUpgrade = progression.nextMetaUpgrade() ?: return false
        if (progression.coins < nextUpgrade.cost) {
            return false
        }

        progression.coins -= nextUpgrade.cost
        progression.purchaseNextMetaUpgrade()
        saveStore.save(progression)
        return true
    }

    fun chooseCharacter(index: Int): Boolean {
        if (state != GameState.CharacterShop) {
            return false
        }

        if (index !in 0 until CharacterSkin.count) {
            return false
        }

        if (index < progression.unlockedCharacterCount) {
            val selected = progression.selectCharacter(index)
            saveStore.save(progression)
            return selected
        }

        if (index != progression.unlockedCharacterCount) {
            return false
        }

        val nextCharacter = progression.nextCharacter() ?: return false
        if (progression.coins < nextCharacter.price) {
            return false
        }

        progression.coins -= nextCharacter.price
        progression.purchaseNextCharacter()
        saveStore.save(progression)
        return true
    }

    fun upgradeStackCount(type: UpgradeType): Int {
        return upgradeManager.stackCount(type)
    }

    fun handleDebugAction(action: DebugAction) {
        when (action) {
            DebugAction.TogglePanel -> debugPanelOpen = !debugPanelOpen
            DebugAction.ClosePanel -> debugPanelOpen = false
            DebugAction.ForceUpgrade -> forceUpgradeChoice()
            DebugAction.ForceBoss -> forceBossFight()
            DebugAction.SkipToNextBoss -> skipToNextBoss()
            DebugAction.AddShield -> addDebugShield()
            DebugAction.ToggleHitboxes -> debugShowHitboxes = !debugShowHitboxes
            DebugAction.ToggleSlowMotion -> debugSlowMotion = !debugSlowMotion
            DebugAction.DefeatBoss -> defeatCurrentBoss()
        }
    }

    fun update(deltaSeconds: Float) {
        if (!hasValidSize || debugPanelOpen) {
            return
        }

        val scaledDeltaSeconds = deltaSeconds * debugTimeScale

        when (state) {
            GameState.Running -> {
                currentRunSeconds += scaledDeltaSeconds
                when (mode) {
                    GameMode.Classic -> updateClassicMode(scaledDeltaSeconds)
                    GameMode.Boss -> updateBossMode(scaledDeltaSeconds)
                }
            }
            GameState.BossWarning -> updateBossWarning(scaledDeltaSeconds)
            GameState.BossDefeated -> updateBossDefeated(scaledDeltaSeconds)
            GameState.Title,
            GameState.UpgradeShop,
            GameState.CharacterShop,
            GameState.ChoosingUpgrade,
            GameState.GameOver -> Unit
        }
    }

    private fun updateClassicMode(deltaSeconds: Float) {
        player.update(deltaSeconds)
        obstacleManager.update(deltaSeconds, player.bounds.left)
        val passedObstacles = obstacleManager.consumePassedCount()
        score += passedObstacles
        if (passedObstacles > 0) {
            progression.addCoins(passedObstacles)
            currentRunCoinsEarned += passedObstacles
            progression.recordBestScore(score)
            saveStore.save(progression)
        }

        if (obstacleManager.collidesWith(player.bounds) || player.isOutsideVerticalBounds(screenHeight)) {
            handlePlayerHit()
            return
        }

        if (score >= upcomingBossScore) {
            startBossWarning()
            return
        }

        if (passedObstacles > 0 && upgradeManager.startChoiceIfReady(score, upcomingBossScore)) {
            state = GameState.ChoosingUpgrade
        }
    }

    private fun updateBossMode(deltaSeconds: Float) {
        player.update(deltaSeconds)
        val bossResult = bossEncounter.update(deltaSeconds, player.bounds, runModifiers)

        if (player.isOutsideVerticalBounds(screenHeight) || bossResult.playerWasHit) {
            handlePlayerHit()
            return
        }

        if (bossResult.bossDefeated) {
            completeBossEncounter()
        }
    }

    private fun updateBossWarning(deltaSeconds: Float) {
        transitionTimer -= deltaSeconds
        if (transitionTimer <= 0f) {
            transitionTimer = 0f
            state = GameState.Running
        }
    }

    private fun updateBossDefeated(deltaSeconds: Float) {
        transitionTimer -= deltaSeconds
        if (transitionTimer > 0f) {
            return
        }

        transitionTimer = 0f
        bossEncounter.clear()
        mode = GameMode.Classic
        obstacleManager.reset(screenWidth, screenHeight, runModifiers)
        upgradeManager.startBossRewardChoice()
        state = if (upgradeManager.currentChoices.isEmpty()) {
            GameState.Running
        } else {
            GameState.ChoosingUpgrade
        }
    }

    private fun resetToTitle() {
        score = 0
        mode = GameMode.Classic
        state = GameState.Title
        upcomingBossScore = GameConfig.BOSS_SCORE_THRESHOLD
        transitionTimer = 0f
        transitionDuration = 0f
        currentRunSeconds = 0f
        currentBossesDefeated = 0
        currentRunCoinsEarned = 0
        pendingStartingChoices = 0
        runModifiers.reset()
        upgradeManager.reset()
        bossEncounter.clear()
        player.reset(screenWidth, screenHeight, runModifiers)
        obstacleManager.reset(screenWidth, screenHeight, runModifiers)
    }

    private fun startRun() {
        score = 0
        mode = GameMode.Classic
        upcomingBossScore = GameConfig.BOSS_SCORE_THRESHOLD
        transitionTimer = 0f
        transitionDuration = 0f
        currentRunSeconds = 0f
        currentBossesDefeated = 0
        currentRunCoinsEarned = 0
        pendingStartingChoices = progression.startingUpgradeSlots
        runModifiers.reset()
        progression.applyPermanentBonuses(runModifiers)
        upgradeManager.reset()
        bossEncounter.clear()
        player.reset(screenWidth, screenHeight, runModifiers)
        obstacleManager.reset(screenWidth, screenHeight, runModifiers)
        if (pendingStartingChoices > 0) {
            upgradeManager.startStartingDraft()
            state = GameState.ChoosingUpgrade
        } else {
            state = GameState.Running
            player.flap()
        }
    }

    private fun handlePlayerHit() {
        if (runModifiers.tryConsumeShield()) {
            player.recoverFromHit(screenHeight)
            when (mode) {
                GameMode.Classic -> obstacleManager.reset(screenWidth, screenHeight, runModifiers)
                GameMode.Boss -> bossEncounter.clearEnemyPressure()
            }
            return
        }

        endRun()
    }

    private fun startBossWarning() {
        mode = GameMode.Boss
        state = GameState.BossWarning
        transitionDuration = GameConfig.BOSS_WARNING_SECONDS
        transitionTimer = transitionDuration
        obstacleManager.clear()
        bossEncounter.reset(
            width = screenWidth,
            height = screenHeight,
            bossLevel = currentBossesDefeated + 1
        )
    }

    private fun completeBossEncounter() {
        score += GameConfig.BOSS_DEFEAT_SCORE_BONUS
        currentBossesDefeated += 1
        progression.addCoins(GameConfig.BOSS_BONUS_COINS)
        currentRunCoinsEarned += GameConfig.BOSS_BONUS_COINS
        progression.recordBestScore(score)
        saveStore.save(progression)
        upcomingBossScore += GameConfig.BOSS_SCORE_INTERVAL
        upgradeManager.skipChoicesThrough(score)
        bossEncounter.clearEnemyPressure()
        transitionDuration = GameConfig.BOSS_DEFEATED_SECONDS
        transitionTimer = transitionDuration
        state = GameState.BossDefeated
    }

    private fun endRun() {
        progression.recordBestScore(score)
        val scoreBonusCoins = score / GameConfig.SCORE_TO_COIN_RATIO
        if (scoreBonusCoins > 0) {
            progression.addCoins(scoreBonusCoins)
            currentRunCoinsEarned += scoreBonusCoins
        }
        saveStore.save(progression)
        lastRunSummary = RunSummary(
            score = score,
            bestScore = progression.bestScore,
            coinsEarned = currentRunCoinsEarned,
            totalCoins = progression.coins,
            upgrades = activeUpgrades.size,
            bossesDefeated = currentBossesDefeated,
            survivalSeconds = currentRunSeconds
        )
        resetToTitle()
    }

    private fun forceUpgradeChoice() {
        ensureRunActive()
        if (mode == GameMode.Boss) {
            bossEncounter.clear()
            mode = GameMode.Classic
            obstacleManager.reset(screenWidth, screenHeight, runModifiers)
        }

        upgradeManager.forceChoice()
        state = GameState.ChoosingUpgrade
        debugPanelOpen = false
    }

    private fun forceBossFight() {
        ensureRunActive()
        startBossWarning()
        debugPanelOpen = false
    }

    private fun skipToNextBoss() {
        ensureRunActive()
        if (mode == GameMode.Boss) {
            return
        }

        score = upcomingBossScore
        startBossWarning()
        debugPanelOpen = false
    }

    private fun addDebugShield() {
        ensureRunActive()
        runModifiers.addShield()
    }

    private fun defeatCurrentBoss() {
        ensureRunActive()
        if (mode != GameMode.Boss) {
            forceBossFight()
            return
        }

        boss.defeat()
        debugPanelOpen = false
    }

    private fun ensureRunActive() {
        if (state == GameState.Title || state == GameState.GameOver) {
            startRun()
        }
    }

    fun currentMenuScreen(): GameState {
        return state
    }
}
