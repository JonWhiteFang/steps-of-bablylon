package com.whitefang.stepsofbabylon.presentation.battle.engine

import android.graphics.Canvas
import com.whitefang.stepsofbabylon.domain.model.EnemyType
import com.whitefang.stepsofbabylon.domain.model.ZigguratBaseStats
import com.whitefang.stepsofbabylon.presentation.battle.entities.EnemyEntity
import com.whitefang.stepsofbabylon.presentation.battle.entities.EnemyProjectileEntity
import com.whitefang.stepsofbabylon.presentation.battle.entities.ProjectileEntity
import com.whitefang.stepsofbabylon.presentation.battle.entities.ZigguratEntity
import com.whitefang.stepsofbabylon.presentation.battle.ui.HealthBarRenderer
import kotlin.math.hypot

class GameEngine {

    private val entities = mutableListOf<Entity>()
    private val pendingAdd = mutableListOf<Entity>()
    private val healthBarRenderer = HealthBarRenderer()

    var screenWidth: Float = 0f; private set
    var screenHeight: Float = 0f; private set
    var ziggurat: ZigguratEntity? = null; private set
    var waveSpawner: WaveSpawner? = null; private set

    @Volatile var cash: Long = 0L; private set
    @Volatile var roundOver: Boolean = false; private set

    private val bgColor = 0xFF6B3A2A.toInt()

    private fun applyMeleeDamage(dmg: Double) {
        ziggurat?.let { it.currentHp = (it.currentHp - dmg).coerceAtLeast(0.0) }
    }

    fun init(width: Float, height: Float) {
        screenWidth = width
        screenHeight = height
        entities.clear()
        pendingAdd.clear()
        cash = 0L
        roundOver = false

        val zig = ZigguratEntity(width, height, ::findNearestEnemy) { sx, sy, tx, ty ->
            pendingAdd.add(ProjectileEntity(sx, sy, tx, ty, ZigguratBaseStats.PROJECTILE_SPEED, ZigguratBaseStats.BASE_DAMAGE))
        }
        ziggurat = zig
        entities.add(zig)

        waveSpawner = WaveSpawner(
            onSpawnEnemy = { pendingAdd.add(it) },
            zigguratX = zig.x, zigguratY = zig.y,
            onEnemyDeath = ::handleEnemyDeath,
            onMeleeHit = ::applyMeleeDamage,
            onEnemyFireProjectile = { sx, sy, tx, ty, dmg ->
                pendingAdd.add(EnemyProjectileEntity(sx, sy, tx, ty, damage = dmg))
            },
        )
    }

    fun update(deltaTime: Float) {
        if (roundOver) return
        val zig = ziggurat ?: return

        waveSpawner?.update(deltaTime, screenWidth, screenHeight)
        entities.addAll(pendingAdd)
        pendingAdd.clear()
        entities.forEach { it.update(deltaTime) }
        CollisionSystem.checkCollisions(entities, zig)
        entities.removeAll { !it.isAlive }

        if (zig.currentHp <= 0.0) roundOver = true
    }

    fun render(canvas: Canvas) {
        canvas.drawColor(bgColor)
        entities.forEach { it.render(canvas) }
        ziggurat?.let { healthBarRenderer.render(canvas, it.currentHp, it.maxHp, screenWidth) }
    }

    fun addEntity(entity: Entity) { pendingAdd.add(entity) }

    private fun findNearestEnemy(): EnemyEntity? {
        val zig = ziggurat ?: return null
        return entities.asSequence()
            .filterIsInstance<EnemyEntity>()
            .filter { it.isAlive }
            .minByOrNull { hypot(it.x - zig.x, it.y - zig.y) }
    }

    private fun handleEnemyDeath(enemy: EnemyEntity) {
        cash += EnemyScaler.cashReward(enemy.enemyType)
        waveSpawner?.onEnemyKilled()

        if (enemy.enemyType == EnemyType.SCATTER) {
            val zig = ziggurat ?: return
            val childCount = (2..3).random()
            repeat(childCount) { i ->
                val child = EnemyEntity(
                    enemyType = EnemyType.BASIC,
                    currentHp = enemy.maxHp * 0.5, maxHp = enemy.maxHp * 0.5,
                    speed = EnemyScaler.scaleSpeed(EnemyType.SCATTER),
                    damage = enemy.damage * 0.5,
                    targetX = zig.x, targetY = zig.y,
                    onDeath = ::handleEnemyDeath,
                    onMeleeHit = ::applyMeleeDamage,
                ).apply {
                    x = enemy.x + (i - childCount / 2f) * 15f
                    y = enemy.y
                    initDistance()
                }
                pendingAdd.add(child)
            }
        }
    }
}
