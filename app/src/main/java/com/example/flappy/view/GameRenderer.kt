package com.example.flappy.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import com.example.flappy.game.DebugAction
import com.example.flappy.game.CharacterSkin
import com.example.flappy.game.GameState
import com.example.flappy.game.GameMode
import com.example.flappy.game.MainMenuAction
import com.example.flappy.game.MetaUpgrade
import com.example.flappy.game.GameWorld
import com.example.flappy.game.UpgradeRarity

class GameRenderer(private val world: GameWorld) {
    private val backgroundPaint = Paint().apply {
        color = Color.rgb(24, 32, 42)
        style = Paint.Style.FILL
    }

    private val playerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 205, 66)
        style = Paint.Style.FILL
    }

    private val shieldOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
    }

    private val obstaclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(76, 190, 112)
        style = Paint.Style.FILL
    }

    private val bossPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(198, 78, 96)
        style = Paint.Style.FILL
    }

    private val bossCorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 140, 112)
        style = Paint.Style.FILL
    }

    private val bossProjectilePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 92, 92)
        style = Paint.Style.FILL
    }

    private val bossTelegraphPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(92, 255, 92, 92)
        style = Paint.Style.FILL
    }

    private val bossTelegraphEdgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(180, 255, 184, 92)
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val playerProjectilePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(86, 224, 216)
        style = Paint.Style.FILL
    }

    private val healthTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(58, 68, 80)
        style = Paint.Style.FILL
    }

    private val healthFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 205, 66)
        style = Paint.Style.FILL
    }

    private val overlayPaint = Paint().apply {
        color = Color.argb(188, 7, 12, 18)
        style = Paint.Style.FILL
    }

    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(35, 45, 57)
        style = Paint.Style.FILL
    }

    private val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(105, 130, 153)
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val debugButtonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(48, 62, 78)
        style = Paint.Style.FILL
    }

    private val debugActivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(86, 224, 216)
        style = Paint.Style.FILL
    }

    private val debugOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 255, 255)
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val hitboxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 255, 255)
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 205, 66)
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val smallTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(218, 226, 235)
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private val leftTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val leftBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(218, 226, 235)
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private val textBounds = Rect()
    private val cardRect = RectF()
    private val healthRect = RectF()
    private val healthFillRect = RectF()
    private val bossCoreRect = RectF()
    private val playerShieldRect = RectF()
    private val debugRect = RectF()
    private val debugControlRect = RectF()
    private val menuButtonRect = RectF()
    private val shopItemRect = RectF()
    private val backButtonRect = RectF()

    private val debugPanelActions = listOf(
        DebugAction.ForceUpgrade,
        DebugAction.ForceBoss,
        DebugAction.SkipToNextBoss,
        DebugAction.AddShield,
        DebugAction.ToggleHitboxes,
        DebugAction.ToggleSlowMotion,
        DebugAction.DefeatBoss,
        DebugAction.ClosePanel
    )

    fun upgradeChoiceIndexAt(x: Float, y: Float, width: Int, height: Int): Int? {
        val choices = world.upgradeChoices
        choices.forEachIndexed { index, _ ->
            upgradeCardRect(index, choices.size, width.toFloat(), height.toFloat(), cardRect)
            if (cardRect.contains(x, y)) {
                return index
            }
        }

        return null
    }

    fun mainMenuActionAt(x: Float, y: Float, width: Int, height: Int): MainMenuAction? {
        val actions = listOf(
            MainMenuAction.StartRun,
            MainMenuAction.OpenUpgradeShop,
            MainMenuAction.OpenCharacterShop
        )

        actions.forEachIndexed { index, action ->
            mainMenuButtonRect(index, actions.size, width.toFloat(), height.toFloat(), menuButtonRect)
            if (menuButtonRect.contains(x, y)) {
                return action
            }
        }

        return null
    }

    fun upgradeShopActionAt(x: Float, y: Float, width: Int, height: Int): Boolean {
        if (world.nextMetaUpgrade == null) {
            return false
        }
        upgradeShopCardRect(width.toFloat(), height.toFloat(), shopItemRect)
        return shopItemRect.contains(x, y)
    }

    fun characterShopActionAt(x: Float, y: Float, width: Int, height: Int): Int? {
        val skins = world.characterSkins
        skins.forEachIndexed { index, _ ->
            characterShopCardRect(index, skins.size, width.toFloat(), height.toFloat(), shopItemRect)
            if (shopItemRect.contains(x, y)) {
                return index
            }
        }

        return null
    }

    fun shopBackActionAt(x: Float, y: Float, width: Int, height: Int): Boolean {
        shopBackButtonRect(width.toFloat(), height.toFloat(), backButtonRect)
        return backButtonRect.contains(x, y)
    }

    fun debugActionAt(x: Float, y: Float, width: Int, height: Int): DebugAction? {
        debugButtonRect(width.toFloat(), height.toFloat(), debugRect)
        if (debugRect.contains(x, y)) {
            return DebugAction.TogglePanel
        }

        if (!world.debugPanelOpen) {
            return null
        }

        debugPanelActions.forEachIndexed { index, action ->
            debugControlRect(index, width.toFloat(), height.toFloat(), debugControlRect)
            if (debugControlRect.contains(x, y)) {
                return action
            }
        }

        return null
    }

    fun render(canvas: Canvas) {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        canvas.drawRect(0f, 0f, width, height, backgroundPaint)
        drawObstacles(canvas)
        drawBossEncounter(canvas, width, height)
        drawPlayer(canvas, height)
        if (world.debugShowHitboxes) {
            drawHitboxes(canvas)
        }
        drawHud(canvas, width, height)

        if (world.state == GameState.ChoosingUpgrade) {
            drawUpgradeChoices(canvas, width, height)
        } else if (world.state == GameState.Title) {
            drawMainMenu(canvas, width, height)
        } else if (world.state == GameState.UpgradeShop) {
            drawUpgradeShop(canvas, width, height)
        } else if (world.state == GameState.CharacterShop) {
            drawCharacterShop(canvas, width, height)
        }

        drawDebugTools(canvas, width, height)
    }

    private fun drawObstacles(canvas: Canvas) {
        world.activeObstacles.forEach { obstacle ->
            canvas.drawRect(obstacle.topRect, obstaclePaint)
            canvas.drawRect(obstacle.bottomRect, obstaclePaint)
        }
    }

    private fun drawBossEncounter(canvas: Canvas, width: Float, height: Float) {
        if (world.mode != GameMode.Boss) {
            return
        }

        drawBossTelegraphs(canvas)

        world.playerProjectiles.forEach { projectile ->
            canvas.drawRoundRect(projectile.bounds, 6f, 6f, playerProjectilePaint)
        }

        world.bossProjectiles.forEach { projectile ->
            canvas.drawRect(projectile.bounds, bossProjectilePaint)
        }

        val bossBounds = world.boss.bounds
        bossPaint.color = bossColorForLevel(world.currentBossLevel, world.bossEnraged)
        bossCorePaint.color = bossCoreColorForLevel(world.currentBossLevel, world.bossEnraged)
        canvas.drawRect(bossBounds, bossPaint)

        val coreInsetX = bossBounds.width() * 0.28f
        val coreInsetY = bossBounds.height() * 0.3f
        bossCoreRect.set(
            bossBounds.left + coreInsetX,
            bossBounds.top + coreInsetY,
            bossBounds.right - coreInsetX,
            bossBounds.bottom - coreInsetY
        )
        canvas.drawRect(bossCoreRect, bossCorePaint)
        drawBossHealth(canvas, width, height)
    }

    private fun drawBossTelegraphs(canvas: Canvas) {
        world.bossTelegraphs.forEach { telegraph ->
            val fillAlpha = (72 + telegraph.progress * 94f).toInt().coerceIn(72, 166)
            val edgeAlpha = (126 + telegraph.progress * 110f).toInt().coerceIn(126, 236)
            bossTelegraphPaint.color = Color.argb(fillAlpha, 255, 82, 92)
            bossTelegraphEdgePaint.color = Color.argb(edgeAlpha, 255, 210, 92)
            canvas.drawRect(telegraph.bounds, bossTelegraphPaint)
            canvas.drawRect(telegraph.bounds, bossTelegraphEdgePaint)
        }
    }

    private fun drawPlayer(canvas: Canvas, height: Float) {
        playerPaint.color = world.selectedCharacterColor
        if (world.shieldCharges > 0) {
            shieldOutlinePaint.color = shieldColorForCharges(world.shieldCharges)
            shieldOutlinePaint.strokeWidth = (height * 0.0055f).coerceIn(5f, 12f)

            val outset = shieldOutlinePaint.strokeWidth * 1.35f
            playerShieldRect.set(world.player.bounds)
            playerShieldRect.inset(-outset, -outset)
            canvas.drawRoundRect(playerShieldRect, 6f, 6f, shieldOutlinePaint)
        }

        canvas.drawRect(world.player.bounds, playerPaint)
    }

    private fun drawHud(canvas: Canvas, width: Float, height: Float) {
        if (world.state == GameState.Running || world.state == GameState.BossWarning || world.state == GameState.BossDefeated || world.state == GameState.ChoosingUpgrade || world.state == GameState.GameOver) {
            textPaint.textSize = (height * 0.06f).coerceIn(42f, 76f)
            canvas.drawText(world.score.toString(), width / 2f, height * 0.11f, textPaint)
            drawRunSummary(canvas, width, height)
        }

        if (world.mode == GameMode.Boss) {
            smallTextPaint.textSize = (height * 0.02f).coerceIn(20f, 30f)
            canvas.drawText(
                "BOSS LV ${world.currentBossLevel}  ${world.bossPhaseLabel}",
                width / 2f,
                height * 0.155f,
                smallTextPaint
            )
        }

        when (world.state) {
            GameState.Title -> Unit
            GameState.BossWarning -> drawBossWarning(canvas, width, height)
            GameState.BossDefeated -> drawBossDefeated(canvas, width, height)
            GameState.GameOver -> drawGameOver(canvas, width, height)
            GameState.Running,
            GameState.ChoosingUpgrade -> Unit
            GameState.UpgradeShop,
            GameState.CharacterShop -> Unit
        }
    }

    private fun drawRunSummary(canvas: Canvas, width: Float, height: Float) {
        if (world.activeUpgrades.isEmpty() && world.shieldCharges == 0) {
            return
        }

        leftBodyPaint.textSize = (height * 0.018f).coerceIn(18f, 28f)
        val shieldText = if (world.shieldCharges > 0) "  Shields ${world.shieldCharges}" else ""
        val stackText = world.upgradeStacks
            .take(3)
            .joinToString("  ") { stack -> "${stack.type.title} x${stack.count}" }
        val upgradeText = if (stackText.isEmpty()) {
            "Upgrades ${world.activeUpgrades.size}"
        } else {
            stackText
        }
        canvas.drawText(
            "$upgradeText$shieldText",
            width * 0.05f,
            height * 0.065f,
            leftBodyPaint
        )
    }

    private fun drawUpgradeChoices(canvas: Canvas, width: Float, height: Float) {
        canvas.drawRect(0f, 0f, width, height, overlayPaint)

        textPaint.textSize = (height * 0.043f).coerceIn(32f, 56f)
        smallTextPaint.textSize = (height * 0.021f).coerceIn(20f, 30f)
        canvas.drawText(world.currentUpgradeOfferType.title, width / 2f, height * 0.2f, textPaint)
        val subtitle = when (world.currentUpgradeOfferType.subtitle) {
            "Boss arrives at score" -> "Boss arrives at score ${world.nextBossScore}"
            else -> world.currentUpgradeOfferType.subtitle
        }
        canvas.drawText(subtitle, width / 2f, height * 0.245f, smallTextPaint)

        val choices = world.upgradeChoices
        choices.forEachIndexed { index, upgrade ->
            upgradeCardRect(index, choices.size, width, height, cardRect)
            cardBorderPaint.color = rarityColor(upgrade.rarity)
            canvas.drawRoundRect(cardRect, CARD_RADIUS, CARD_RADIUS, cardPaint)
            canvas.drawRoundRect(cardRect, CARD_RADIUS, CARD_RADIUS, cardBorderPaint)

            val accentLeft = cardRect.left + width * 0.035f
            val accentTop = cardRect.top + cardRect.height() * 0.24f
            accentPaint.color = rarityColor(upgrade.rarity)
            canvas.drawCircle(accentLeft, accentTop, height * 0.012f, accentPaint)

            leftTitlePaint.textSize = (height * 0.025f).coerceIn(24f, 36f)
            leftBodyPaint.textSize = (height * 0.019f).coerceIn(18f, 28f)
            smallTextPaint.textSize = (height * 0.016f).coerceIn(16f, 24f)

            val textLeft = cardRect.left + width * 0.075f
            val textRightPadding = width * 0.045f
            val textWidth = cardRect.width() - width * 0.12f - textRightPadding
            val titleBaseLine = cardRect.top + cardRect.height() * 0.34f
            canvas.drawText(upgrade.title, textLeft, titleBaseLine, leftTitlePaint)
            val currentStack = world.upgradeStackCount(upgrade)
            smallTextPaint.color = rarityColor(upgrade.rarity)
            canvas.drawText(
                "${upgrade.rarity.label}  Lv ${currentStack + 1}/${upgrade.maxStacks}",
                cardRect.right - width * 0.12f,
                titleBaseLine,
                smallTextPaint
            )
            smallTextPaint.color = Color.rgb(218, 226, 235)
            drawWrappedText(
                canvas = canvas,
                text = upgrade.description,
                x = textLeft,
                y = titleBaseLine + cardRect.height() * 0.22f,
                maxWidth = textWidth,
                paint = leftBodyPaint,
                lineHeight = leftBodyPaint.textSize * 1.22f,
                maxLines = 2
            )
        }
        accentPaint.color = Color.rgb(255, 205, 66)
        cardBorderPaint.color = Color.rgb(105, 130, 153)
    }

    private fun drawMainMenu(canvas: Canvas, width: Float, height: Float) {
        canvas.drawRect(0f, 0f, width, height, overlayPaint)

        textPaint.textSize = (height * 0.058f).coerceIn(42f, 78f)
        smallTextPaint.textSize = (height * 0.024f).coerceIn(22f, 34f)
        canvas.drawText("Flappy Rogue", width / 2f, height * 0.24f, textPaint)
        canvas.drawText("Best ${world.bestScore}  Coins ${world.coinCount}", width / 2f, height * 0.3f, smallTextPaint)
        canvas.drawText("Opening slots ${world.startingUpgradeSlots}", width / 2f, height * 0.34f, smallTextPaint)

        val actions = listOf(
            "Start Run",
            "Upgrade Shop",
            "Character Shop"
        )

        actions.forEachIndexed { index, label ->
            mainMenuButtonRect(index, actions.size, width, height, menuButtonRect)
            canvas.drawRoundRect(menuButtonRect, CARD_RADIUS, CARD_RADIUS, cardPaint)
            canvas.drawRoundRect(menuButtonRect, CARD_RADIUS, CARD_RADIUS, cardBorderPaint)

            textPaint.textSize = (height * 0.028f).coerceIn(24f, 36f)
            canvas.drawText(label, menuButtonRect.centerX(), menuButtonRect.centerY() + textPaint.textSize * 0.35f, textPaint)
        }

        smallTextPaint.textSize = (height * 0.018f).coerceIn(16f, 24f)
        canvas.drawText(
            "Earn coins during runs, then spend them in the shops",
            width / 2f,
            height * 0.83f,
            smallTextPaint
        )

        cardPaint.color = Color.rgb(35, 45, 57)
        cardBorderPaint.color = Color.rgb(105, 130, 153)
    }

    private fun drawUpgradeShop(canvas: Canvas, width: Float, height: Float) {
        canvas.drawRect(0f, 0f, width, height, overlayPaint)
        drawShopHeader(
            canvas = canvas,
            width = width,
            height = height,
            title = "Upgrade Shop",
            subtitle = "Buy permanent meta upgrades in order"
        )

        val upgrades = world.metaUpgrades
        upgrades.forEachIndexed { index, upgrade ->
            shopListCardRect(index, upgrades.size, width, height, shopItemRect)
            val owned = index < world.nextMetaUpgradeIndex
            val nextAvailable = index == world.nextMetaUpgradeIndex
            cardPaint.color = if (owned) Color.rgb(40, 62, 54) else Color.rgb(35, 45, 57)
            cardBorderPaint.color = when {
                owned -> Color.rgb(86, 224, 216)
                nextAvailable -> Color.rgb(255, 205, 66)
                else -> Color.rgb(105, 130, 153)
            }
            canvas.drawRoundRect(shopItemRect, CARD_RADIUS, CARD_RADIUS, cardPaint)
            canvas.drawRoundRect(shopItemRect, CARD_RADIUS, CARD_RADIUS, cardBorderPaint)

            leftTitlePaint.textSize = (height * 0.02f).coerceIn(18f, 28f)
            leftBodyPaint.textSize = (height * 0.014f).coerceIn(13f, 20f)
            smallTextPaint.textSize = (height * 0.014f).coerceIn(13f, 20f)
            val textLeft = shopItemRect.left + width * 0.04f
            val lineY = shopItemRect.top + shopItemRect.height() * 0.3f
            canvas.drawText(upgrade.title, textLeft, lineY, leftTitlePaint)

            val statusText = when {
                owned -> "Owned"
                nextAvailable -> "Cost ${upgrade.cost}"
                else -> "Locked"
            }
            canvas.drawText(statusText, shopItemRect.right - width * 0.04f, lineY, smallTextPaint)

            drawWrappedText(
                canvas = canvas,
                text = upgrade.description,
                x = textLeft,
                y = lineY + shopItemRect.height() * 0.18f,
                maxWidth = shopItemRect.width() - width * 0.08f,
                paint = leftBodyPaint,
                lineHeight = leftBodyPaint.textSize * 1.2f,
                maxLines = 1
            )
        }

        smallTextPaint.textSize = (height * 0.017f).coerceIn(16f, 24f)
        canvas.drawText(
            if (world.nextMetaUpgrade != null) {
                "Tap the highlighted upgrade to buy it"
            } else {
                "All upgrades purchased"
            },
            width / 2f,
            height * 0.93f,
            smallTextPaint
        )
        canvas.drawText(
            "Permanent upgrades carry into every future run",
            width / 2f,
            height * 0.955f,
            smallTextPaint
        )

        cardPaint.color = Color.rgb(35, 45, 57)
        cardBorderPaint.color = Color.rgb(105, 130, 153)
    }

    private fun drawCharacterShop(canvas: Canvas, width: Float, height: Float) {
        canvas.drawRect(0f, 0f, width, height, overlayPaint)
        drawShopHeader(
            canvas = canvas,
            width = width,
            height = height,
            title = "Character Shop",
            subtitle = "Buy 6 color-only placeholder characters"
        )

        val skins = world.characterSkins
        skins.forEachIndexed { index, skin ->
            characterShopCardRect(index, skins.size, width, height, shopItemRect)
            val unlocked = index < world.nextCharacterIndex
            val selected = index == world.selectedCharacterIndex
            cardPaint.color = if (selected) Color.rgb(45, 58, 76) else Color.rgb(35, 45, 57)
            cardBorderPaint.color = when {
                selected -> Color.rgb(255, 205, 66)
                unlocked -> Color.rgb(86, 224, 216)
                else -> Color.rgb(105, 130, 153)
            }
            canvas.drawRoundRect(shopItemRect, CARD_RADIUS, CARD_RADIUS, cardPaint)
            canvas.drawRoundRect(shopItemRect, CARD_RADIUS, CARD_RADIUS, cardBorderPaint)

            val colorSwatchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = skin.color
                style = Paint.Style.FILL
            }
            val swatchSize = shopItemRect.height() * 0.34f
            val swatchLeft = shopItemRect.left + width * 0.04f
            val swatchTop = shopItemRect.centerY() - swatchSize / 2f
            canvas.drawRoundRect(
                swatchLeft,
                swatchTop,
                swatchLeft + swatchSize,
                swatchTop + swatchSize,
                swatchSize * 0.22f,
                swatchSize * 0.22f,
                colorSwatchPaint
            )

            leftTitlePaint.textSize = (height * 0.02f).coerceIn(18f, 28f)
            leftBodyPaint.textSize = (height * 0.014f).coerceIn(13f, 20f)
            smallTextPaint.textSize = (height * 0.014f).coerceIn(13f, 20f)
            val textLeft = swatchLeft + swatchSize + width * 0.03f
            val lineY = shopItemRect.top + shopItemRect.height() * 0.3f
            canvas.drawText(skin.title, textLeft, lineY, leftTitlePaint)
            canvas.drawText("Price ${skin.price}", shopItemRect.right - width * 0.04f, lineY, smallTextPaint)
            canvas.drawText(
                when {
                    selected -> "Selected"
                    unlocked -> "Unlocked"
                    else -> "Locked"
                },
                textLeft,
                shopItemRect.bottom - shopItemRect.height() * 0.18f,
                smallTextPaint
            )
        }

        smallTextPaint.textSize = (height * 0.017f).coerceIn(16f, 24f)
        canvas.drawText(
            if (world.selectedCharacterIndex >= 0) {
                "Tap a color to select it"
            } else {
                "Buy the first color to begin"
            },
            width / 2f,
            height * 0.93f,
            smallTextPaint
        )
        canvas.drawText(
            "Characters are placeholders for future art and animations",
            width / 2f,
            height * 0.955f,
            smallTextPaint
        )

        cardPaint.color = Color.rgb(35, 45, 57)
        cardBorderPaint.color = Color.rgb(105, 130, 153)
    }

    private fun drawShopHeader(canvas: Canvas, width: Float, height: Float, title: String, subtitle: String) {
        textPaint.textSize = (height * 0.042f).coerceIn(30f, 54f)
        smallTextPaint.textSize = (height * 0.019f).coerceIn(18f, 26f)
        canvas.drawText(title, width / 2f, height * 0.105f, textPaint)
        canvas.drawText("Coins ${world.coinCount}  Best ${world.bestScore}", width / 2f, height * 0.145f, smallTextPaint)
        canvas.drawText(subtitle, width / 2f, height * 0.177f, smallTextPaint)

        shopBackButtonRect(width, height, backButtonRect)
        canvas.drawRoundRect(backButtonRect, CARD_RADIUS, CARD_RADIUS, cardPaint)
        canvas.drawRoundRect(backButtonRect, CARD_RADIUS, CARD_RADIUS, cardBorderPaint)
        textPaint.textSize = (height * 0.02f).coerceIn(18f, 26f)
        canvas.drawText("Back", backButtonRect.centerX(), backButtonRect.centerY() + textPaint.textSize * 0.35f, textPaint)
    }

    private fun drawBossWarning(canvas: Canvas, width: Float, height: Float) {
        canvas.drawRect(0f, 0f, width, height, overlayPaint)

        val remaining = (world.transitionProgress * com.example.flappy.game.GameConfig.BOSS_WARNING_SECONDS)
            .coerceAtLeast(0f)
        textPaint.textSize = (height * 0.052f).coerceIn(38f, 68f)
        smallTextPaint.textSize = (height * 0.024f).coerceIn(22f, 34f)
        canvas.drawText("Boss Incoming", width / 2f, height * 0.39f, textPaint)
        canvas.drawText("Level ${world.currentBossLevel} starts in ${formatOneDecimal(remaining)}", width / 2f, height * 0.45f, smallTextPaint)
    }

    private fun drawBossDefeated(canvas: Canvas, width: Float, height: Float) {
        canvas.drawRect(0f, 0f, width, height, overlayPaint)

        textPaint.textSize = (height * 0.052f).coerceIn(38f, 68f)
        smallTextPaint.textSize = (height * 0.024f).coerceIn(22f, 34f)
        canvas.drawText("Boss ${world.currentBossLevel} Defeated", width / 2f, height * 0.39f, textPaint)
        canvas.drawText("+${com.example.flappy.game.GameConfig.BOSS_DEFEAT_SCORE_BONUS} score  Next boss at ${world.nextBossScore}", width / 2f, height * 0.45f, smallTextPaint)
    }

    private fun drawGameOver(canvas: Canvas, width: Float, height: Float) {
        canvas.drawRect(0f, 0f, width, height, overlayPaint)

        val summary = world.runSummary
        textPaint.textSize = (height * 0.052f).coerceIn(38f, 68f)
        smallTextPaint.textSize = (height * 0.022f).coerceIn(20f, 32f)
        canvas.drawText("Run Over", width / 2f, height * 0.29f, textPaint)
        canvas.drawText("Score ${summary.score}  Best ${summary.bestScore}", width / 2f, height * 0.36f, smallTextPaint)
        canvas.drawText("Bosses ${summary.bossesDefeated}  Upgrades ${summary.upgrades}", width / 2f, height * 0.405f, smallTextPaint)
        canvas.drawText("Time ${formatRunTime(summary.survivalSeconds)}", width / 2f, height * 0.45f, smallTextPaint)

        drawCenteredMessage(
            canvas = canvas,
            width = width,
            height = height,
            title = "Tap to Retry",
            subtitle = "A fresh build starts immediately"
        )
    }

    private fun drawCenteredMessage(
        canvas: Canvas,
        width: Float,
        height: Float,
        title: String,
        subtitle: String
    ) {
        textPaint.textSize = (height * 0.052f).coerceIn(36f, 64f)
        smallTextPaint.textSize = (height * 0.025f).coerceIn(22f, 34f)

        textPaint.getTextBounds(title, 0, title.length, textBounds)
        canvas.drawText(title, width / 2f, height * 0.43f, textPaint)
        canvas.drawText(subtitle, width / 2f, height * 0.48f + textBounds.height(), smallTextPaint)
    }

    private fun drawBossHealth(canvas: Canvas, width: Float, height: Float) {
        val barWidth = width * 0.62f
        val barHeight = (height * 0.014f).coerceIn(16f, 26f)
        val left = (width - barWidth) / 2f
        val top = height * 0.17f
        healthRect.set(left, top, left + barWidth, top + barHeight)
        healthFillRect.set(
            healthRect.left,
            healthRect.top,
            healthRect.left + healthRect.width() * world.bossHealthFraction,
            healthRect.bottom
        )

        healthFillPaint.color = if (world.bossEnraged) {
            Color.rgb(255, 92, 92)
        } else {
            Color.rgb(255, 205, 66)
        }
        canvas.drawRoundRect(healthRect, barHeight / 2f, barHeight / 2f, healthTrackPaint)
        canvas.drawRoundRect(healthFillRect, barHeight / 2f, barHeight / 2f, healthFillPaint)
    }

    private fun drawHitboxes(canvas: Canvas) {
        hitboxPaint.color = Color.WHITE
        canvas.drawRect(world.player.bounds, hitboxPaint)

        hitboxPaint.color = Color.rgb(106, 255, 140)
        world.activeObstacles.forEach { obstacle ->
            canvas.drawRect(obstacle.topRect, hitboxPaint)
            canvas.drawRect(obstacle.bottomRect, hitboxPaint)
        }

        if (world.mode == GameMode.Boss) {
            hitboxPaint.color = Color.rgb(255, 128, 128)
            canvas.drawRect(world.boss.bounds, hitboxPaint)
            world.bossProjectiles.forEach { projectile ->
                canvas.drawRect(projectile.bounds, hitboxPaint)
            }

            hitboxPaint.color = Color.rgb(86, 224, 216)
            world.playerProjectiles.forEach { projectile ->
                canvas.drawRect(projectile.bounds, hitboxPaint)
            }
        }
    }

    private fun drawDebugTools(canvas: Canvas, width: Float, height: Float) {
        if (world.debugPanelOpen) {
            canvas.drawRect(0f, 0f, width, height, overlayPaint)
            textPaint.textSize = (height * 0.033f).coerceIn(28f, 44f)
            smallTextPaint.textSize = (height * 0.018f).coerceIn(18f, 26f)
            canvas.drawText("Debug Tools", width / 2f, height * 0.16f, textPaint)
            canvas.drawText(
                "Paused  Time x${formatOneDecimal(world.debugTimeScale)}",
                width / 2f,
                height * 0.2f,
                smallTextPaint
            )

            debugPanelActions.forEachIndexed { index, action ->
                debugControlRect(index, width, height, debugControlRect)
                val active = (action == DebugAction.ToggleHitboxes && world.debugShowHitboxes) ||
                    (action == DebugAction.ToggleSlowMotion && world.debugSlowMotion)
                val fillPaint = if (active) debugActivePaint else cardPaint
                canvas.drawRoundRect(debugControlRect, CARD_RADIUS, CARD_RADIUS, fillPaint)
                canvas.drawRoundRect(debugControlRect, CARD_RADIUS, CARD_RADIUS, cardBorderPaint)

                val label = when (action) {
                    DebugAction.ToggleHitboxes -> "Hitboxes: ${if (world.debugShowHitboxes) "On" else "Off"}"
                    DebugAction.ToggleSlowMotion -> "Slow Motion: ${if (world.debugSlowMotion) "On" else "Off"}"
                    else -> action.label
                }
                textPaint.textSize = (height * 0.021f).coerceIn(20f, 30f)
                canvas.drawText(
                    label,
                    debugControlRect.centerX(),
                    debugControlRect.centerY() + textPaint.textSize * 0.35f,
                    textPaint
                )
            }
        }

        debugButtonRect(width, height, debugRect)
        canvas.drawRoundRect(
            debugRect,
            CARD_RADIUS,
            CARD_RADIUS,
            if (world.debugPanelOpen) debugActivePaint else debugButtonPaint
        )
        canvas.drawRoundRect(debugRect, CARD_RADIUS, CARD_RADIUS, debugOutlinePaint)

        textPaint.textSize = (height * 0.016f).coerceIn(17f, 24f)
        canvas.drawText("DBG", debugRect.centerX(), debugRect.centerY() + textPaint.textSize * 0.35f, textPaint)
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        paint: Paint,
        lineHeight: Float,
        maxLines: Int
    ) {
        val words = text.split(" ")
        var line = ""
        var currentY = y
        var linesDrawn = 0

        words.forEach { word ->
            val nextLine = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(nextLine) <= maxWidth) {
                line = nextLine
            } else {
                canvas.drawText(line, x, currentY, paint)
                currentY += lineHeight
                linesDrawn += 1
                line = word
            }

            if (linesDrawn >= maxLines) {
                return
            }
        }

        if (line.isNotEmpty() && linesDrawn < maxLines) {
            canvas.drawText(line, x, currentY, paint)
        }
    }

    private fun mainMenuButtonRect(
        index: Int,
        count: Int,
        width: Float,
        height: Float,
        outRect: RectF
    ): RectF {
        val buttonWidth = (width * 0.62f).coerceIn(250f, 520f)
        val buttonHeight = (height * 0.072f).coerceIn(68f, 104f)
        val gap = height * 0.028f
        val totalHeight = count * buttonHeight + (count - 1) * gap
        val top = height * 0.49f - totalHeight / 2f + index * (buttonHeight + gap)
        val left = (width - buttonWidth) / 2f
        outRect.set(left, top, left + buttonWidth, top + buttonHeight)
        return outRect
    }

    private fun shopListCardRect(
        index: Int,
        count: Int,
        width: Float,
        height: Float,
        outRect: RectF
    ): RectF {
        val cardWidth = width * 0.86f
        val cardHeight = (height * 0.062f).coerceIn(60f, 74f)
        val gap = height * 0.008f
        val totalHeight = count * cardHeight + (count - 1) * gap
        val contentTop = height * 0.205f
        val contentBottom = height * 0.845f
        val availableHeight = (contentBottom - contentTop).coerceAtLeast(cardHeight)
        val startTop = contentTop + ((availableHeight - totalHeight).coerceAtLeast(0f) / 2f)
        val top = startTop + index * (cardHeight + gap)
        val left = (width - cardWidth) / 2f
        outRect.set(left, top, left + cardWidth, top + cardHeight)
        return outRect
    }

    private fun upgradeShopCardRect(width: Float, height: Float, outRect: RectF): RectF {
        if (world.nextMetaUpgrade == null) {
            outRect.setEmpty()
            return outRect
        }

        val index = world.nextMetaUpgradeIndex
        val total = maxOf(1, world.metaUpgrades.size)
        shopListCardRect(index, total, width, height, outRect)
        return outRect
    }

    private fun characterShopCardRect(
        index: Int,
        count: Int,
        width: Float,
        height: Float,
        outRect: RectF
    ): RectF {
        val cardWidth = width * 0.86f
        val cardHeight = (height * 0.066f).coerceIn(62f, 78f)
        val gap = height * 0.008f
        val totalHeight = count * cardHeight + (count - 1) * gap
        val contentTop = height * 0.205f
        val contentBottom = height * 0.845f
        val availableHeight = (contentBottom - contentTop).coerceAtLeast(cardHeight)
        val startTop = contentTop + ((availableHeight - totalHeight).coerceAtLeast(0f) / 2f)
        val top = startTop + index * (cardHeight + gap)
        val left = (width - cardWidth) / 2f
        outRect.set(left, top, left + cardWidth, top + cardHeight)
        return outRect
    }

    private fun shopBackButtonRect(width: Float, height: Float, outRect: RectF): RectF {
        val buttonWidth = (width * 0.18f).coerceIn(92f, 160f)
        val buttonHeight = (height * 0.045f).coerceIn(46f, 68f)
        val margin = width * 0.04f
        outRect.set(margin, height * 0.03f, margin + buttonWidth, height * 0.03f + buttonHeight)
        return outRect
    }

    private fun upgradeCardRect(
        index: Int,
        count: Int,
        width: Float,
        height: Float,
        outRect: RectF
    ): RectF {
        val cardWidth = width * 0.84f
        val cardHeight = (height * 0.12f).coerceIn(142f, 230f)
        val gap = height * 0.022f
        val totalHeight = count * cardHeight + (count - 1) * gap
        val top = height * 0.55f - totalHeight / 2f + index * (cardHeight + gap)
        val left = (width - cardWidth) / 2f
        outRect.set(left, top, left + cardWidth, top + cardHeight)
        return outRect
    }

    private fun debugButtonRect(width: Float, height: Float, outRect: RectF): RectF {
        val buttonWidth = (width * 0.14f).coerceIn(86f, 132f)
        val buttonHeight = (height * 0.04f).coerceIn(46f, 64f)
        val margin = width * 0.035f
        outRect.set(
            width - margin - buttonWidth,
            height * 0.025f,
            width - margin,
            height * 0.025f + buttonHeight
        )
        return outRect
    }

    private fun debugControlRect(
        index: Int,
        width: Float,
        height: Float,
        outRect: RectF
    ): RectF {
        val columns = 2
        val column = index % columns
        val row = index / columns
        val panelWidth = width * 0.82f
        val controlGap = width * 0.025f
        val controlWidth = (panelWidth - controlGap) / columns
        val controlHeight = (height * 0.07f).coerceIn(72f, 104f)
        val startLeft = (width - panelWidth) / 2f
        val startTop = height * 0.26f
        val left = startLeft + column * (controlWidth + controlGap)
        val top = startTop + row * (controlHeight + height * 0.018f)
        outRect.set(left, top, left + controlWidth, top + controlHeight)
        return outRect
    }

    private fun formatRunTime(seconds: Float): String {
        val totalSeconds = seconds.toInt().coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val remainder = totalSeconds % 60
        return if (minutes > 0) {
            "${minutes}m ${remainder}s"
        } else {
            "${remainder}s"
        }
    }

    private fun formatOneDecimal(value: Float): String {
        val clamped = value.coerceAtLeast(0f)
        val whole = clamped.toInt()
        val tenths = ((clamped - whole) * 10f).toInt().coerceIn(0, 9)
        return "$whole.$tenths"
    }

    private fun shieldColorForCharges(charges: Int): Int {
        return when (charges) {
            1 -> Color.rgb(75, 155, 255)
            2 -> Color.rgb(68, 220, 112)
            3 -> Color.rgb(255, 226, 82)
            4 -> Color.rgb(255, 154, 70)
            5 -> Color.rgb(255, 92, 116)
            else -> Color.rgb(192, 118, 255)
        }
    }

    private fun rarityColor(rarity: UpgradeRarity): Int {
        return when (rarity) {
            UpgradeRarity.Common -> Color.rgb(218, 226, 235)
            UpgradeRarity.Rare -> Color.rgb(86, 224, 216)
            UpgradeRarity.Epic -> Color.rgb(192, 118, 255)
        }
    }

    private fun bossColorForLevel(level: Int, enraged: Boolean): Int {
        if (enraged) {
            return Color.rgb(226, 54, 72)
        }

        return when ((level - 1).floorMod(BOSS_PALETTE_SIZE)) {
            0 -> Color.rgb(198, 78, 96)
            1 -> Color.rgb(156, 100, 220)
            2 -> Color.rgb(70, 140, 220)
            3 -> Color.rgb(58, 174, 132)
            else -> Color.rgb(222, 134, 66)
        }
    }

    private fun bossCoreColorForLevel(level: Int, enraged: Boolean): Int {
        if (enraged) {
            return Color.rgb(255, 142, 86)
        }

        return when ((level - 1).floorMod(BOSS_PALETTE_SIZE)) {
            0 -> Color.rgb(255, 140, 112)
            1 -> Color.rgb(218, 160, 255)
            2 -> Color.rgb(130, 206, 255)
            3 -> Color.rgb(126, 236, 172)
            else -> Color.rgb(255, 196, 92)
        }
    }

    private fun Int.floorMod(other: Int): Int {
        return ((this % other) + other) % other
    }

    private companion object {
        const val CARD_RADIUS = 8f
        const val BOSS_PALETTE_SIZE = 5
    }
}
