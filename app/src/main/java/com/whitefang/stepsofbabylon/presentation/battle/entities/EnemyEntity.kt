package com.whitefang.stepsofbabylon.presentation.battle.entities

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.whitefang.stepsofbabylon.domain.model.EnemyType
import com.whitefang.stepsofbabylon.presentation.battle.engine.Entity
import kotlin.math.hypot

class EnemyEntity(
    val enemyType: EnemyType,
    var currentHp: Double,
    val maxHp: Double,
    val speed: Float,
    val damage: Double,
    private val targetX: Float,
    private val targetY: Float,
    private val onDeath: (EnemyEntity) -> Unit,
    private val onMeleeHit: ((Double) -> Unit)? = null,
    private val onFireProjectile: ((Float, Float, Float, Float, Double) -> Unit)? = null,
) : Entity() {

    private var attackCooldown = 0f
    private val attackInterval = 1f
    private val meleeRange = 40f
    private var initialDist = 0f

    init {
        val size = when (enemyType) {
            EnemyType.BOSS -> 40f
            EnemyType.TANK -> 28f
            EnemyType.FAST -> 16f
            else -> 20f
        }
        width = size; height = size
    }

    fun initDistance() { initialDist = hypot(targetX - x, targetY - y) }

    override fun update(deltaTime: Float) {
        val dx = targetX - x
        val dy = targetY - y
        val dist = hypot(dx, dy)

        val stopDist = if (enemyType == EnemyType.RANGED) initialDist * 0.4f else meleeRange
        if (dist > stopDist) {
            val ratio = speed * deltaTime / dist
            x += dx * ratio
            y += dy * ratio
        } else {
            attackCooldown -= deltaTime
            if (attackCooldown <= 0f) {
                attackCooldown = attackInterval
                if (enemyType == EnemyType.RANGED) {
                    onFireProjectile?.invoke(x, y, targetX, targetY, damage)
                } else {
                    onMeleeHit?.invoke(damage)
                }
            }
        }
    }

    fun takeDamage(amount: Double) {
        currentHp -= amount
        if (currentHp <= 0.0) { isAlive = false; onDeath(this) }
    }

    fun applyKnockback(forceX: Float, forceY: Float) {
        x += forceX; y += forceY
    }

    override fun render(canvas: Canvas) {
        val r = width / 2f
        when (enemyType) {
            EnemyType.BASIC -> canvas.drawCircle(x, y, r, BASIC_PAINT)
            EnemyType.FAST -> {
                TRIANGLE_PATH.reset()
                TRIANGLE_PATH.moveTo(x, y - r); TRIANGLE_PATH.lineTo(x - r, y + r); TRIANGLE_PATH.lineTo(x + r, y + r); TRIANGLE_PATH.close()
                canvas.drawPath(TRIANGLE_PATH, FAST_PAINT)
            }
            EnemyType.TANK -> canvas.drawRect(x - r, y - r, x + r, y + r, TANK_PAINT)
            EnemyType.RANGED -> {
                TRIANGLE_PATH.reset()
                TRIANGLE_PATH.moveTo(x, y - r); TRIANGLE_PATH.lineTo(x + r, y); TRIANGLE_PATH.lineTo(x, y + r); TRIANGLE_PATH.lineTo(x - r, y); TRIANGLE_PATH.close()
                canvas.drawPath(TRIANGLE_PATH, RANGED_PAINT)
            }
            EnemyType.BOSS -> canvas.drawCircle(x, y, r, BOSS_PAINT)
            EnemyType.SCATTER -> canvas.drawCircle(x, y, r, SCATTER_PAINT)
        }
        // Mini HP bar
        val barW = width * 1.2f; val barH = 4f; val barY = y - r - 8f
        canvas.drawRect(x - barW / 2, barY, x + barW / 2, barY + barH, HP_BG)
        val ratio = (currentHp / maxHp).coerceIn(0.0, 1.0).toFloat()
        HP_FILL.color = when { ratio > 0.6f -> 0xFF4CAF50.toInt(); ratio > 0.3f -> 0xFFFFEB3B.toInt(); else -> 0xFFF44336.toInt() }
        canvas.drawRect(x - barW / 2, barY, x - barW / 2 + barW * ratio, barY + barH, HP_FILL)
    }

    companion object {
        private val BASIC_PAINT = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFE53935.toInt() }
        private val FAST_PAINT = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFF9800.toInt() }
        private val TANK_PAINT = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF8B0000.toInt() }
        private val RANGED_PAINT = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF9C27B0.toInt() }
        private val BOSS_PAINT = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF4A0000.toInt() }
        private val SCATTER_PAINT = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF4CAF50.toInt() }
        private val HP_BG = Paint().apply { color = 0xFF2A1A10.toInt() }
        private val HP_FILL = Paint()
        private val TRIANGLE_PATH = Path()
    }
}
