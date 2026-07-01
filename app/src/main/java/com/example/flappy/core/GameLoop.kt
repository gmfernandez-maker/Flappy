package com.example.flappy.core

import android.graphics.Canvas
import android.view.SurfaceHolder
import com.example.flappy.view.FlappyGameView
import kotlin.math.min

class GameLoop(
    private val surfaceHolder: SurfaceHolder,
    private val gameView: FlappyGameView
) : Thread("FlappyGameLoop") {
    @Volatile
    private var running = false

    fun startLoop() {
        running = true
        start()
    }

    fun requestStop() {
        running = false
    }

    override fun run() {
        var previousTime = System.nanoTime()
        val targetFrameTimeMillis = 1000L / TARGET_FPS

        while (running) {
            val frameStartedMillis = System.currentTimeMillis()
            val now = System.nanoTime()
            val deltaSeconds = min((now - previousTime) / NANOS_PER_SECOND, MAX_DELTA_SECONDS)
            previousTime = now

            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    synchronized(surfaceHolder) {
                        gameView.update(deltaSeconds)
                        gameView.render(canvas)
                    }
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas)
                }
            }

            val frameDurationMillis = System.currentTimeMillis() - frameStartedMillis
            val sleepMillis = targetFrameTimeMillis - frameDurationMillis
            if (sleepMillis > 0L) {
                try {
                    sleep(sleepMillis)
                } catch (_: InterruptedException) {
                    running = false
                }
            }
        }
    }

    private companion object {
        const val TARGET_FPS = 60L
        const val NANOS_PER_SECOND = 1_000_000_000f
        const val MAX_DELTA_SECONDS = 0.033f
    }
}
