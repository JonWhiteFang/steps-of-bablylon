package com.whitefang.stepsofbabylon.presentation.battle.effects

import android.graphics.Canvas
import android.graphics.Paint

interface Effect {
    val isFinished: Boolean
    fun update(dt: Float)
    fun render(canvas: Canvas)
}

class EffectEngine(val reducedMotion: Boolean = false) {
    val pool = ParticlePool(200)
    private val effects = mutableListOf<Effect>()
    private val pendingEffects = mutableListOf<Effect>()
    val screenShake = ScreenShake()
    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun addEffect(effect: Effect) { pendingEffects.add(effect) }

    fun update(dt: Float) {
        effects.addAll(pendingEffects); pendingEffects.clear()
        pool.updateAll(dt)
        effects.forEach { it.update(dt) }
        effects.removeAll { it.isFinished }
        if (!reducedMotion) screenShake.update(dt)
    }

    fun render(canvas: Canvas) {
        pool.renderAll(canvas, particlePaint)
        effects.forEach { it.render(canvas) }
    }

    fun clear() { effects.clear(); pendingEffects.clear(); pool.clear(); screenShake.reset() }
}
