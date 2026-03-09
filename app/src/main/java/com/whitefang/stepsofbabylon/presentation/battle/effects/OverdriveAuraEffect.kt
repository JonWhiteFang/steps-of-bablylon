package com.whitefang.stepsofbabylon.presentation.battle.effects

import android.graphics.Canvas
import android.graphics.Paint
import com.whitefang.stepsofbabylon.domain.model.OverdriveType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class OverdriveAuraEffect(
    private val pool: ParticlePool,
    private val type: OverdriveType,
    private val getCenterX: () -> Float,
    private val getCenterY: () -> Float,
    private val getProgress: () -> Float,
    private val reducedMotion: Boolean = false,
) : Effect {
    private var spawnTimer = 0f
    private val color = when (type) {
        OverdriveType.ASSAULT -> 0xFFE53935.toInt()
        OverdriveType.FORTRESS -> 0xFF2196F3.toInt()
        OverdriveType.FORTUNE -> 0xFFFFD700.toInt()
        OverdriveType.SURGE -> 0xFF9C27B0.toInt()
    }
    private val fallbackPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    override val isFinished: Boolean get() = getProgress() <= 0f

    override fun update(dt: Float) {
        if (reducedMotion || isFinished) return
        val rate = 0.05f * getProgress() // Slower emission as timer fades
        spawnTimer += dt
        while (spawnTimer >= rate && rate > 0f) {
            spawnTimer -= rate
            val cx = getCenterX(); val cy = getCenterY()
            val p = pool.acquire()
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val dist = 30f + Random.nextFloat() * 20f
            p.x = cx + cos(angle) * dist; p.y = cy + sin(angle) * dist
            p.color = color; p.size = 2f + Random.nextFloat() * 3f; p.lifetime = 0.5f
            when (type) {
                OverdriveType.ASSAULT -> { p.vx = cos(angle) * 60f; p.vy = sin(angle) * 60f }
                OverdriveType.FORTRESS -> { p.vx = -sin(angle) * 30f; p.vy = cos(angle) * 30f }
                OverdriveType.FORTUNE -> { p.vx = Random.nextFloat() * 10f - 5f; p.vy = -30f - Random.nextFloat() * 20f }
                OverdriveType.SURGE -> { p.vx = Random.nextFloat() * 80f - 40f; p.vy = Random.nextFloat() * 80f - 40f }
            }
        }
    }

    override fun render(canvas: Canvas) {
        if (!reducedMotion) return
        // Reduced motion fallback: simple circle aura
        val progress = getProgress()
        if (progress <= 0f) return
        fallbackPaint.color = color; fallbackPaint.alpha = (80 * progress).toInt()
        canvas.drawCircle(getCenterX(), getCenterY(), 60f, fallbackPaint)
    }
}
