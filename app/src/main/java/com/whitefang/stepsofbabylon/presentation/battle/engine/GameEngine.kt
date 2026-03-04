package com.whitefang.stepsofbabylon.presentation.battle.engine

import android.graphics.Canvas
import com.whitefang.stepsofbabylon.domain.model.ZigguratBaseStats
import com.whitefang.stepsofbabylon.presentation.battle.entities.ProjectileEntity
import com.whitefang.stepsofbabylon.presentation.battle.entities.ZigguratEntity
import com.whitefang.stepsofbabylon.presentation.battle.ui.HealthBarRenderer

class GameEngine {

    private val entities = mutableListOf<Entity>()
    private val pendingAdd = mutableListOf<Entity>()
    private val healthBarRenderer = HealthBarRenderer()

    var screenWidth: Float = 0f
        private set
    var screenHeight: Float = 0f
        private set
    var ziggurat: ZigguratEntity? = null
        private set

    private val bgColor = 0xFF6B3A2A.toInt() // DeepBronze

    fun init(width: Float, height: Float) {
        screenWidth = width
        screenHeight = height
        entities.clear()

        val zig = ZigguratEntity(width, height) { sx, sy, tx, ty ->
            pendingAdd.add(
                ProjectileEntity(sx, sy, tx, ty, ZigguratBaseStats.PROJECTILE_SPEED, ZigguratBaseStats.BASE_DAMAGE)
            )
        }
        ziggurat = zig
        entities.add(zig)
    }

    fun update(deltaTime: Float) {
        entities.addAll(pendingAdd)
        pendingAdd.clear()
        entities.forEach { it.update(deltaTime) }
        entities.removeAll { !it.isAlive }
    }

    fun render(canvas: Canvas) {
        canvas.drawColor(bgColor)
        entities.forEach { it.render(canvas) }
        ziggurat?.let { healthBarRenderer.render(canvas, it.currentHp, it.maxHp, screenWidth) }
    }

    fun addEntity(entity: Entity) { pendingAdd.add(entity) }
}
