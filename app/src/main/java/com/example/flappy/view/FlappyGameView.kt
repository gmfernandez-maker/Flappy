package com.example.flappy.view

import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.flappy.core.GameLoop
import com.example.flappy.game.GameState
import com.example.flappy.game.GameWorld

class FlappyGameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    private val world = GameWorld()
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
            if (world.state == GameState.ChoosingUpgrade) {
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
