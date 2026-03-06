package com.whitefang.stepsofbabylon.presentation.battle.engine

import android.graphics.Canvas
import com.whitefang.stepsofbabylon.domain.model.BattleConditionEffects
import com.whitefang.stepsofbabylon.domain.model.EnemyType
import com.whitefang.stepsofbabylon.domain.model.ResolvedStats
import com.whitefang.stepsofbabylon.domain.model.TierConfig
import com.whitefang.stepsofbabylon.domain.model.UpgradeType
import com.whitefang.stepsofbabylon.domain.model.ZigguratBaseStats
import com.whitefang.stepsofbabylon.domain.usecase.CalculateDamage
import com.whitefang.stepsofbabylon.domain.usecase.CalculateDefense
import com.whitefang.stepsofbabylon.presentation.battle.entities.EnemyEntity
import com.whitefang.stepsofbabylon.presentation.battle.entities.EnemyProjectileEntity
import com.whitefang.stepsofbabylon.presentation.battle.entities.OrbEntity
import com.whitefang.stepsofbabylon.presentation.battle.entities.ProjectileEntity
import com.whitefang.stepsofbabylon.presentation.battle.entities.ZigguratEntity
import com.whitefang.stepsofbabylon.presentation.battle.ui.HealthBarRenderer
import kotlin.math.PI
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
    private var tier: Int = 1
    private var conditions: BattleConditionEffects = BattleConditionEffects()
    private var workshopLevels: Map<UpgradeType, Int> = emptyMap()

    @Volatile var cash: Long = 0L; private set
    @Volatile var totalCashEarned: Long = 0L; private set
    @Volatile var roundOver: Boolean = false
    @Volatile var totalEnemiesKilled: Int = 0; private set
    @Volatile var elapsedTimeSeconds: Float = 0f; private set

    private val bgColor = 0xFF6B3A2A.toInt()

    companion object {
        const val BASE_CASH_PER_WAVE = 20L
        const val FLAT_BONUS_PER_WAVE_LEVEL = 5L
    }

    fun init(
        width: Float, height: Float,
        resolvedStats: ResolvedStats = ResolvedStats(),
        playerTier: Int = 1,
        wsLevels: Map<UpgradeType, Int> = emptyMap(),
    ) {
        screenWidth = width; screenHeight = height
        entities.clear(); pendingAdd.clear()
        cash = 0L; totalCashEarned = 0L; roundOver = false
        totalEnemiesKilled = 0; elapsedTimeSeconds = 0f
        stats = resolvedStats; tier = playerTier; workshopLevels = wsLevels
        conditions = BattleConditionEffects.fromTier(tier)

        val zig = ZigguratEntity(width, height, stats, ::findNearestEnemies) { sx, sy, tx, ty ->
            pendingAdd.add(ProjectileEntity(sx, sy, tx, ty, ZigguratBaseStats.PROJECTILE_SPEED, bouncesRemaining = stats.bounceCount))
        }
        ziggurat = zig
        entities.add(zig)

        spawnOrbs()

        waveSpawner = WaveSpawner(
            onSpawnEnemy = { pendingAdd.add(it) },
            zigguratX = zig.x, zigguratY = zig.y,
            onEnemyDeath = ::handleEnemyDeath,
            onMeleeHit = { dmg -> applyDamageToZiggurat(dmg, null) },
            onEnemyFireProjectile = { sx, sy, tx, ty, dmg ->
                pendingAdd.add(EnemyProjectileEntity(sx, sy, tx, ty, damage = dmg))
            },
            onWaveComplete = ::handleWaveComplete,
            conditions = conditions,
        )
    }

    fun setStats(resolvedStats: ResolvedStats) { stats = resolvedStats }

    fun updateZigguratStats(newStats: ResolvedStats) {
        val oldOrbCount = stats.orbCount
        stats = newStats
        val zig = ziggurat ?: return
        val hpRatio = if (zig.maxHp > 0) zig.currentHp / zig.maxHp else 1.0
        zig.maxHp = newStats.maxHealth
        zig.currentHp = newStats.maxHealth * hpRatio
        if (newStats.orbCount != oldOrbCount) {
            entities.removeAll { it is OrbEntity }
            spawnOrbs()
        }
    }

    fun spendCash(amount: Long): Boolean {
        if (cash < amount) return false
        cash -= amount; return true
    }

    fun update(deltaTime: Float) {
        if (roundOver) return
        val zig = ziggurat ?: return
        elapsedTimeSeconds += deltaTime

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

    // --- Orb management ---

    private fun spawnOrbs() {
        val zig = ziggurat ?: return
        val count = stats.orbCount
        if (count <= 0) return
        val radius = stats.range * 0.4f
        val damage = stats.damage * 0.5 * conditions.orbDamageMultiplier
        for (i in 0 until count) {
            val angle = (2.0 * PI / count * i).toFloat()
            entities.add(OrbEntity(
                zigX = zig.x, zigY = zig.y, orbitRadius = radius,
                angle = angle, damage = damage,
                getEnemies = ::getAliveEnemies,
                onHitEnemy = ::onOrbHitEnemy,
            ))
        }
    }

    private fun getAliveEnemies(): List<EnemyEntity> =
        entities.filterIsInstance<EnemyEntity>().filter { it.isAlive }

    private fun onOrbHitEnemy(enemy: EnemyEntity, damage: Double) {
        enemy.takeDamage(damage)
        val zig = ziggurat ?: return
        if (stats.knockbackForce > 0f) {
            val dx = enemy.x - zig.x; val dy = enemy.y - zig.y
            val d = hypot(dx, dy).coerceAtLeast(1f)
            val kb = stats.knockbackForce * 0.5f * conditions.knockbackMultiplier
            enemy.applyKnockback(dx / d * kb, dy / d * kb)
        }
        if (stats.lifestealPercent > 0) {
            zig.currentHp = min(zig.currentHp + damage * stats.lifestealPercent, zig.maxHp)
        }
    }

    // --- Cash economy ---

    private fun wsLevel(type: UpgradeType): Int = workshopLevels[type] ?: 0

    private fun handleWaveComplete(wave: Int) {
        val waveCash = BASE_CASH_PER_WAVE + wsLevel(UpgradeType.CASH_PER_WAVE) * FLAT_BONUS_PER_WAVE_LEVEL
        cash += waveCash
        totalCashEarned += waveCash
        val interestLevel = wsLevel(UpgradeType.INTEREST)
        if (interestLevel > 0) {
            cash += (cash * min(interestLevel * 0.005, 0.10)).toLong()
        }
    }

    // --- Combat mechanics ---

    private fun onProjectileHitEnemy(proj: ProjectileEntity, enemy: EnemyEntity) {
        val zig = ziggurat ?: return
        val dist = hypot(zig.originX - enemy.x, zig.originY - enemy.y)
        val result = calculateDamage(stats, dist)

        enemy.takeDamage(result.amount)
        proj.hitEnemies.add(enemy)
        proj.isAlive = false

        if (stats.knockbackForce > 0f) {
            val dx = enemy.x - zig.x; val dy = enemy.y - zig.y
            val d = hypot(dx, dy).coerceAtLeast(1f)
            val kb = stats.knockbackForce * conditions.knockbackMultiplier
            enemy.applyKnockback(dx / d * kb, dy / d * kb)
        }
        if (stats.lifestealPercent > 0) {
            zig.currentHp = min(zig.currentHp + result.amount * stats.lifestealPercent, zig.maxHp)
        }

        // Bounce shot
        if (proj.bouncesRemaining > 0) {
            val nextTarget = entities.asSequence()
                .filterIsInstance<EnemyEntity>()
                .filter { it.isAlive && it !in proj.hitEnemies }
                .minByOrNull { hypot(it.x - enemy.x, it.y - enemy.y) }
            if (nextTarget != null) {
                pendingAdd.add(ProjectileEntity(
                    startX = enemy.x, startY = enemy.y,
                    targetX = nextTarget.x, targetY = nextTarget.y,
                    speed = ZigguratBaseStats.PROJECTILE_SPEED,
                    bouncesRemaining = proj.bouncesRemaining - 1,
                    hitEnemies = proj.hitEnemies,
                ))
            }
        }
    }

    private fun applyDamageToZiggurat(rawDamage: Double, attacker: EnemyEntity?) {
        val zig = ziggurat ?: return
        val mitigated = calculateDefense(rawDamage, stats)
        if (zig.currentHp - mitigated <= 0.0 && stats.deathDefyChance > 0) {
            if (Random.nextDouble() < stats.deathDefyChance) {
                zig.currentHp = 1.0; applyThorn(rawDamage, attacker); return
            }
        }
        zig.currentHp = (zig.currentHp - mitigated).coerceAtLeast(0.0)
        applyThorn(rawDamage, attacker)
    }

    private fun applyThorn(rawDamage: Double, attacker: EnemyEntity?) {
        if (attacker != null && attacker.isAlive && stats.thornPercent > 0)
            attacker.takeDamage(rawDamage * stats.thornPercent * conditions.thornMultiplier)
    }

    // --- Targeting & death ---

    private fun findNearestEnemies(n: Int): List<EnemyEntity> {
        val zig = ziggurat ?: return emptyList()
        return entities.asSequence()
            .filterIsInstance<EnemyEntity>()
            .filter { it.isAlive && hypot(it.x - zig.x, it.y - zig.y) <= zig.attackRange }
            .sortedBy { hypot(it.x - zig.x, it.y - zig.y) }
            .take(n)
            .toList()
    }

    private fun handleEnemyDeath(enemy: EnemyEntity) {
        totalEnemiesKilled++
        val baseCash = EnemyScaler.cashReward(enemy.enemyType)
        val tierMult = TierConfig.forTier(tier).cashMultiplier
        val cashBonus = 1.0 + wsLevel(UpgradeType.CASH_BONUS) * 0.03
        cash += (baseCash * tierMult * cashBonus).toLong()
        totalCashEarned += (baseCash * tierMult * cashBonus).toLong()
        waveSpawner?.onEnemyKilled()

        if (enemy.enemyType == EnemyType.SCATTER) {
            val zig = ziggurat ?: return
            val childCount = (2..3).random()
            repeat(childCount) { i ->
                val child = EnemyEntity(
                    enemyType = EnemyType.BASIC,
                    currentHp = enemy.maxHp * 0.5, maxHp = enemy.maxHp * 0.5,
                    speed = EnemyScaler.scaleSpeed(EnemyType.SCATTER) * conditions.enemySpeedMultiplier,
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
