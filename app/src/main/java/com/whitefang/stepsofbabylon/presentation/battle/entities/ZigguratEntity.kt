package com.whitefang.stepsofbabylon.presentation.battle.entities

import android.graphics.Canvas
import android.graphics.Paint
import com.whitefang.stepsofbabylon.domain.model.ZigguratBaseStats
import com.whitefang.stepsofbabylon.presentation.battle.engine.Entity
import kotlin.math.hypot

class ZigguratEntity(
    private val screenWidth: Float,
    private val screenHeight: Float,
    private val findNearestEnemy: () -> EnemyEntity?,
    private val onFireProjectile: (startX: Float, startY: Float, targetX: Float, targetY: Float) -> Unit,
) : Entity() {

    var currentHp: Double = ZigguratBaseStats.BASE_HEALTH
    var maxHp: Double = ZigguratBaseStats.BASE_HEALTH
    val attackRange: Float = ZigguratBaseStats.BASE_RANGE

    private var attackCooldown: Float = 0f
    private val attackInterval: Float = (1.0 / ZigguratBaseStats.BASE_ATTACK_SPEED).toFloat()

    private val layerCount = 5
    private val totalHeight: Float = screenHeight * 0.25f
    private val baseWidth: Float = screenWidth * 0.35f
    private val layerHeight: Float = totalHeight / layerCount

    private val layerPaints = arrayOf(
        paint(0xFF8B7355.toInt()),
        paint(0xFF9C8565.toInt()),
        paint(0xFFC2B280.toInt()),
        paint(0xFFCDBFA0.toInt()),
        paint(0xFFD4A843.toInt()),
    )
    private val originPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFD700.toInt() }

    init {
        width = baseWidth
        height = totalHeight
        x = screenWidth / 2f
        y = screenHeight - totalHeight * 0.1f
    }

    val originX: Float get() = x
    val originY: Float get() = y - totalHeight

    override fun update(deltaTime: Float) {
        attackCooldown -= deltaTime
        if (attackCooldown <= 0f) {
            val target = findNearestEnemy()
            if (target != null && hypot(target.x - x, target.y - y) <= attackRange) {
                attackCooldown = attackInterval
                onFireProjectile(originX, originY, target.x, target.y)
            } else {
                attackCooldown = 0f // ready to fire immediately when enemy enters range
            }
        }
    }

    override fun render(canvas: Canvas) {
        for (i in 0 until layerCount) {
            val widthFraction = 1f - (i.toFloat() / layerCount) * 0.6f
            val lw = baseWidth * widthFraction
            val ly = y - (i + 1) * layerHeight
            canvas.drawRect(x - lw / 2f, ly, x + lw / 2f, ly + layerHeight, layerPaints[i])
        }
        canvas.drawCircle(originX, originY, 6f, originPaint)
    }

    private fun paint(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
}
