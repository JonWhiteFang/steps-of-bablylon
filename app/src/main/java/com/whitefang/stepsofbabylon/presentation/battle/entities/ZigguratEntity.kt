package com.whitefang.stepsofbabylon.presentation.battle.entities

import android.graphics.Canvas
import android.graphics.Paint
import com.whitefang.stepsofbabylon.domain.model.ResolvedStats
import com.whitefang.stepsofbabylon.presentation.battle.engine.Entity
import kotlin.math.min

class ZigguratEntity(
    private val screenWidth: Float,
    private val screenHeight: Float,
    val stats: ResolvedStats,
    private val findNearestEnemies: (Int) -> List<EnemyEntity>,
    layerColors: List<Int> = DEFAULT_COLORS,
    private val onFireProjectile: (startX: Float, startY: Float, targetX: Float, targetY: Float) -> Unit,
) : Entity() {

    var currentHp: Double = stats.maxHealth
    var maxHp: Double = stats.maxHealth
    val attackRange: Float = stats.range

    private var attackCooldown: Float = 0f
    private val attackInterval: Float = (1.0 / stats.attackSpeed).toFloat()

    private val layerCount = 5
    private val totalHeight: Float = screenHeight * 0.25f
    private val baseWidth: Float = screenWidth * 0.35f
    private val layerHeight: Float = totalHeight / layerCount

    private val layerPaints = layerColors.map { Paint(Paint.ANTI_ALIAS_FLAG).apply { color = it } }.toTypedArray()
    private val originPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFD700.toInt() }

    init {
        width = baseWidth; height = totalHeight
        x = screenWidth / 2f; y = screenHeight - totalHeight * 0.1f
    }

    val originX: Float get() = x
    val originY: Float get() = y - totalHeight

    override fun update(deltaTime: Float) {
        currentHp = min(currentHp + stats.healthRegen * deltaTime, maxHp)
        attackCooldown -= deltaTime
        if (attackCooldown <= 0f) {
            val targets = findNearestEnemies(stats.multishotTargets)
            if (targets.isNotEmpty()) {
                attackCooldown = attackInterval
                for (target in targets) onFireProjectile(originX, originY, target.x, target.y)
            } else {
                attackCooldown = 0f
            }
        }
    }

    override fun render(canvas: Canvas) {
        for (i in 0 until layerCount) {
            val wf = 1f - (i.toFloat() / layerCount) * 0.6f
            val lw = baseWidth * wf
            val ly = y - (i + 1) * layerHeight
            canvas.drawRect(x - lw / 2f, ly, x + lw / 2f, ly + layerHeight, layerPaints[i])
        }
        canvas.drawCircle(originX, originY, 6f, originPaint)
    }

    companion object {
        val DEFAULT_COLORS = listOf(0xFF8B7355.toInt(), 0xFF9C8565.toInt(), 0xFFC2B280.toInt(), 0xFFCDBFA0.toInt(), 0xFFD4A843.toInt())
    }
}
