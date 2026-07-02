package com.example.flappy.view

import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.flappy.core.GameLoop
import com.example.flappy.game.GameSaveStore
import com.example.flappy.game.GameState
import com.example.flappy.game.GameWorld

class FlappyGameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    private val world = GameWorld(GameSaveStore(context))
    private val renderer = GameRenderer(world)
    private var gameLoop: GameLoop? = null

    init {
        holder.addCallback(this)
        isFocusable = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        world.resize(width, height)
        startLoopIfNeeded()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        world.resize(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopLoop()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val debugAction = renderer.debugActionAt(event.x, event.y, width, height)
            if (debugAction != null) {
                world.handleDebugAction(debugAction)
            } else if (world.state == GameState.Title) {
                val menuAction = renderer.mainMenuActionAt(event.x, event.y, width, height)
                if (menuAction != null) {
                    world.handleMainMenuAction(menuAction)
                }
            } else if (world.state == GameState.UpgradeShop) {
                val backPressed = renderer.shopBackActionAt(event.x, event.y, width, height)
                when {
                    backPressed -> world.returnToTitle()
                    renderer.upgradeShopActionAt(event.x, event.y, width, height) -> world.purchaseNextMetaUpgrade()
                    else -> Unit
                }
            } else if (world.state == GameState.CharacterShop) {
                val backPressed = renderer.shopBackActionAt(event.x, event.y, width, height)
                when {
                    backPressed -> world.returnToTitle()
                    else -> {
                        val characterIndex = renderer.characterShopActionAt(event.x, event.y, width, height)
                        if (characterIndex != null) {
                            world.chooseCharacter(characterIndex)
                        }
                    }
                }
            } else if (world.debugPanelOpen) {
                return true
            } else if (world.state == GameState.ChoosingUpgrade) {
                val upgradeIndex = renderer.upgradeChoiceIndexAt(event.x, event.y, width, height)
                if (upgradeIndex != null) {
                    world.chooseUpgrade(upgradeIndex)
                }
            } else {
                world.handleTap()
            }
            return true
        }

        return true
    }

    fun resume() {
        if (holder.surface.isValid) {
            startLoopIfNeeded()
        }
    }

    fun pause() {
        stopLoop()
    }

    fun update(deltaSeconds: Float) {
        world.update(deltaSeconds)
    }

    fun render(canvas: Canvas) {
        renderer.render(canvas)
    }

    private fun startLoopIfNeeded() {
        if (gameLoop?.isAlive == true) {
            return
        }

        gameLoop = GameLoop(holder, this).also { loop ->
            loop.startLoop()
        }
    }

    private fun stopLoop() {
        val loop = gameLoop ?: return
        loop.requestStop()

        var retry = true
        while (retry) {
            try {
                loop.join()
                retry = false
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                retry = false
            }
        }

        gameLoop = null
    }
}
