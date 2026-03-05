package com.whitefang.stepsofbabylon.presentation.battle.engine

import android.graphics.Canvas
import com.whitefang.stepsofbabylon.domain.model.EnemyType
import com.whitefang.stepsofbabylon.domain.model.ResolvedStats
import com.whitefang.stepsofbabylon.domain.model.ZigguratBaseStats
import com.whitefang.stepsofbabylon.domain.usecase.CalculateDamage
import com.whitefang.stepsofbabylon.domain.usecase.CalculateDefense
import com.whitefang.stepsofbabylon.presentation.battle.entities.EnemyEntity
import com.whitefang.stepsofbabylon.presentation.battle.entities.EnemyProjectileEntity
import com.whitefang.stepsofbabylon.presentation.battle.entities.ProjectileEntity
import com.whitefang.stepsofbabylon.presentation.battle.entities.ZigguratEntity
import com.whitefang.stepsofbabylon.presentation.battle.ui.HealthBarRenderer
import kotlin.math.hypot
import kotlin.math.min
import kotlin.random.Random

class GameEngine {

    private val entities = mutableListOf<Entity>()
    private val pendingAdd = mutableListOf<Entity>()
    private val healthBarRenderer = HealthBarRenderer()
    private val calculateDamage = CalculateDamage()
    private val calculateDefense = CalculateDefense()

    var screenWidth: Float = 0f; private set
    var screenHeight: Float = 0f; private set
    var ziggurat: ZigguratEntity? = null; private set
    var waveSpawner: WaveSpawner? = null; private set
    private var stats: ResolvedStats = ResolvedStats()

    @Volatile var cash: Long = 0L; private set
    @Volatile var roundOver: Boolean = false; private set

    private val bgColor = 0xFF6B3A2A.toInt()

    fun init(width: Float, height: Float, resolvedStats: ResolvedStats = ResolvedStats()) {
        screenWidth = width; screenHeight = height
        entities.clear(); pendingAdd.clear()
        cash = 0L; roundOver = false
        stats = resolvedStats

        val zig = ZigguratEntity(width, height, stats, ::findNearestEnemy) { sx, sy, tx, ty ->
            pendingAdd.add(ProjectileEntity(sx, sy, tx, ty, ZigguratBaseStats.PROJECTILE_SPEED))
        }
        ziggurat = zig
        entities.add(zig)

        waveSpawner = WaveSpawner(
            onSpawnEnemy = { pendingAdd.add(it) },
            zigguratX = zig.x, zigguratY = zig.y,
            onEnemyDeath = ::handleEnemyDeath,
            onMeleeHit = { dmg -> applyDamageToZiggurat(dmg, null) },
            onEnemyFireProjectile = { sx, sy, tx, ty, dmg ->
                pendingAdd.add(EnemyProjectileEntity(sx, sy, tx, ty, damage = dmg))
            },
        )
    }

    fun setStats(resolvedStats: ResolvedStats) { stats = resolvedStats }

    fun update(deltaTime: Float) {
        if (roundOver) return
        val zig = ziggurat ?: return

        waveSpawner?.update(deltaTime, screenWidth, screenHeight)
        entities.addAll(pendingAdd); pendingAdd.clear()
        entities.forEach { it.update(deltaTime) }

        CollisionSystem.checkCollisions(
            entities, zig.x, zig.y, zig.width,
            onProjectileHitEnemy = ::onProjectileHitEnemy,
            onEnemyProjectileHitZiggurat = { proj ->
                applyDamageToZiggurat(proj.damage, null)
                proj.isAlive = false
            },
        )
        entities.removeAll { !it.isAlive }

        if (zig.currentHp <= 0.0) roundOver = true
    }

    fun render(canvas: Canvas) {
        canvas.drawColor(bgColor)
        entities.forEach { it.render(canvas) }
        ziggurat?.let { healthBarRenderer.render(canvas, it.currentHp, it.maxHp, screenWidth) }
    }

    fun addEntity(entity: Entity) { pendingAdd.add(entity) }

    // --- Combat mechanics ---

    private fun onProjectileHitEnemy(proj: ProjectileEntity, enemy: EnemyEntity) {
        val zig = ziggurat ?: return
        val dist = hypot(zig.originX - enemy.x, zig.originY - enemy.y)
        val result = calculateDamage(stats, dist)

        enemy.takeDamage(result.amount)
        proj.isAlive = false

        // Knockback
        if (stats.knockbackForce > 0f) {
            val dx = enemy.x - zig.x; val dy = enemy.y - zig.y
            val d = hypot(dx, dy).coerceAtLeast(1f)
            enemy.applyKnockback(dx / d * stats.knockbackForce, dy / d * stats.knockbackForce)
        }

        // Lifesteal
        if (stats.lifestealPercent > 0) {
            val heal = result.amount * stats.lifestealPercent
            zig.currentHp = min(zig.currentHp + heal, zig.maxHp)
        }
    }

    private fun applyDamageToZiggurat(rawDamage: Double, attacker: EnemyEntity?) {
        val zig = ziggurat ?: return
        val mitigated = calculateDefense(rawDamage, stats)

        if (zig.currentHp - mitigated <= 0.0 && stats.deathDefyChance > 0) {
            if (Random.nextDouble() < stats.deathDefyChance) {
                zig.currentHp = 1.0
                // Thorn still applies
                applyThorn(rawDamage, attacker)
                return
            }
        }

        zig.currentHp = (zig.currentHp - mitigated).coerceAtLeast(0.0)
        applyThorn(rawDamage, attacker)
    }

    private fun applyThorn(rawDamage: Double, attacker: EnemyEntity?) {
        if (attacker != null && attacker.isAlive && stats.thornPercent > 0) {
            attacker.takeDamage(rawDamage * stats.thornPercent)
        }
    }

    // --- Targeting & death ---

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
                    onMeleeHit = { dmg -> applyDamageToZiggurat(dmg, null) },
                ).apply {
                    x = enemy.x + (i - childCount / 2f) * 15f; y = enemy.y; initDistance()
                }
                pendingAdd.add(child)
            }
        }
    }
}
