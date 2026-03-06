package com.whitefang.stepsofbabylon.presentation.battle.entities

import android.graphics.Canvas
import android.graphics.Paint
import com.whitefang.stepsofbabylon.presentation.battle.engine.Entity
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class OrbEntity(
    private val zigX: Float,
    private val zigY: Float,
    private val orbitRadius: Float,
    private var angle: Float,
    private val angularSpeed: Float = 2f,
    private val damage: Double,
    private val getEnemies: () -> List<EnemyEntity>,
    private val onHitEnemy: (EnemyEntity, Double) -> Unit,
) : Entity(width = 10f, height = 10f) {

    private val hitCooldowns = mutableMapOf<EnemyEntity, Float>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF00BCD4.toInt() }

    companion object {
        private const val HIT_COOLDOWN = 0.5f
        private const val HIT_RANGE = 25f
    }

    override fun update(deltaTime: Float) {
        angle += angularSpeed * deltaTime
        x = zigX + cos(angle) * orbitRadius
        y = zigY + sin(angle) * orbitRadius

        // Decrement cooldowns, remove dead
        val iter = hitCooldowns.iterator()
        while (iter.hasNext()) {
            val entry = iter.next()
            if (!entry.key.isAlive) { iter.remove(); continue }
            entry.setValue(entry.value - deltaTime)
            if (entry.value <= 0f) iter.remove()
        }

        // Check proximity to enemies
        for (enemy in getEnemies()) {
            if (!enemy.isAlive || hitCooldowns.containsKey(enemy)) continue
            if (hypot(x - enemy.x, y - enemy.y) < HIT_RANGE) {
                onHitEnemy(enemy, damage)
                hitCooldowns[enemy] = HIT_COOLDOWN
            }
        }
    }

    override fun render(canvas: Canvas) {
        canvas.drawCircle(x, y, width / 2f, paint)
    }
}
