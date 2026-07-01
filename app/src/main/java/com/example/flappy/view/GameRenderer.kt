package com.example.flappy.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import com.example.flappy.game.GameMode
import com.example.flappy.game.GameState
import com.example.flappy.game.GameWorld

class GameRenderer(private val world: GameWorld) {
    private val backgroundPaint = Paint().apply {
        color = Color.rgb(24, 32, 42)
        style = Paint.Style.FILL
    }

    private val playerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 205, 66)
        style = Paint.Style.FILL
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

    fun render(canvas: Canvas) {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        canvas.drawRect(0f, 0f, width, height, backgroundPaint)
        drawObstacles(canvas)
        drawBossEncounter(canvas, width, height)
        canvas.drawRect(world.player.bounds, playerPaint)
        drawHud(canvas, width, height)

        if (world.state == GameState.ChoosingUpgrade) {
            drawUpgradeChoices(canvas, width, height)
        }
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

        world.playerProjectiles.forEach { projectile ->
            canvas.drawRoundRect(projectile.bounds, 6f, 6f, playerProjectilePaint)
        }

        world.bossProjectiles.forEach { projectile ->
            canvas.drawRect(projectile.bounds, bossProjectilePaint)
        }

        val bossBounds = world.boss.bounds
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

    private fun drawHud(canvas: Canvas, width: Float, height: Float) {
        textPaint.textSize = (height * 0.06f).coerceIn(42f, 76f)
        canvas.drawText(world.score.toString(), width / 2f, height * 0.11f, textPaint)
        drawRunSummary(canvas, width, height)
        if (world.mode == GameMode.Boss) {
            smallTextPaint.textSize = (height * 0.02f).coerceIn(20f, 30f)
            canvas.drawText("BOSS", width / 2f, height * 0.155f, smallTextPaint)
        }

        when (world.state) {
            GameState.Ready -> drawCenteredMessage(
                canvas = canvas,
                width = width,
                height = height,
                title = "Tap to Flap",
                subtitle = "Pass the green blocks"
            )
            GameState.GameOver -> drawCenteredMessage(
                canvas = canvas,
                width = width,
                height = height,
                title = "Game Over",
                subtitle = "Score ${world.score}  Best ${world.bestScore}"
            )
            GameState.Running,
            GameState.ChoosingUpgrade -> Unit
        }
    }

    private fun drawRunSummary(canvas: Canvas, width: Float, height: Float) {
        if (world.activeUpgrades.isEmpty() && world.shieldCharges == 0) {
            return
        }

        leftBodyPaint.textSize = (height * 0.018f).coerceIn(18f, 28f)
        val shieldText = if (world.shieldCharges > 0) "  Shields ${world.shieldCharges}" else ""
        canvas.drawText(
            "Upgrades ${world.activeUpgrades.size}$shieldText",
            width * 0.05f,
            height * 0.065f,
            leftBodyPaint
        )
    }

    private fun drawUpgradeChoices(canvas: Canvas, width: Float, height: Float) {
        canvas.drawRect(0f, 0f, width, height, overlayPaint)

        textPaint.textSize = (height * 0.043f).coerceIn(32f, 56f)
        smallTextPaint.textSize = (height * 0.021f).coerceIn(20f, 30f)
        canvas.drawText("Choose Your Upgrade", width / 2f, height * 0.2f, textPaint)
        canvas.drawText("Boss arrives at score ${world.nextBossScore}", width / 2f, height * 0.245f, smallTextPaint)

        val choices = world.upgradeChoices
        choices.forEachIndexed { index, upgrade ->
            upgradeCardRect(index, choices.size, width, height, cardRect)
            canvas.drawRoundRect(cardRect, CARD_RADIUS, CARD_RADIUS, cardPaint)
            canvas.drawRoundRect(cardRect, CARD_RADIUS, CARD_RADIUS, cardBorderPaint)

            val accentLeft = cardRect.left + width * 0.035f
            val accentTop = cardRect.top + cardRect.height() * 0.24f
            canvas.drawCircle(accentLeft, accentTop, height * 0.012f, accentPaint)

            leftTitlePaint.textSize = (height * 0.025f).coerceIn(24f, 36f)
            leftBodyPaint.textSize = (height * 0.019f).coerceIn(18f, 28f)

            val textLeft = cardRect.left + width * 0.075f
            val textRightPadding = width * 0.045f
            val textWidth = cardRect.width() - width * 0.12f - textRightPadding
            val titleBaseLine = cardRect.top + cardRect.height() * 0.34f
            canvas.drawText(upgrade.title, textLeft, titleBaseLine, leftTitlePaint)
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

        canvas.drawRoundRect(healthRect, barHeight / 2f, barHeight / 2f, healthTrackPaint)
        canvas.drawRoundRect(healthFillRect, barHeight / 2f, barHeight / 2f, healthFillPaint)
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

    private companion object {
        const val CARD_RADIUS = 8f
    }
}
