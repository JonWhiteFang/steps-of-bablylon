package com.whitefang.stepsofbabylon.presentation.battle

import android.content.Context
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.whitefang.stepsofbabylon.domain.model.ResolvedStats
import com.whitefang.stepsofbabylon.domain.model.UpgradeType
import com.whitefang.stepsofbabylon.presentation.battle.engine.GameEngine

class GameSurfaceView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    val engine = GameEngine()
    private var gameThread: GameLoopThread? = null
    private var currentStats: ResolvedStats = ResolvedStats()
    private var currentTier: Int = 1
    private var currentWsLevels: Map<UpgradeType, Int> = emptyMap()
    private var surfaceReady = false

    init { holder.addCallback(this) }

    fun configure(stats: ResolvedStats, tier: Int, wsLevels: Map<UpgradeType, Int>) {
        currentStats = stats; currentTier = tier; currentWsLevels = wsLevels
        if (surfaceReady) engine.init(width.toFloat(), height.toFloat(), stats, tier, wsLevels)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        surfaceReady = true
        engine.init(width.toFloat(), height.toFloat(), currentStats, currentTier, currentWsLevels)
        val thread = GameLoopThread(holder, engine)
        thread.isRunning = true; thread.start(); gameThread = thread
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        engine.init(width.toFloat(), height.toFloat(), currentStats, currentTier, currentWsLevels)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        surfaceReady = false
        val thread = gameThread ?: return
        thread.isRunning = false
        try { thread.join(1000) } catch (_: InterruptedException) {}
        gameThread = null
    }

    fun setSpeedMultiplier(speed: Float) { gameThread?.speedMultiplier = speed }
    fun setPaused(paused: Boolean) { gameThread?.isPaused = paused }
}
