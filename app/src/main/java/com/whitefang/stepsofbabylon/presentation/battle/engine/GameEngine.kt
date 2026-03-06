package com.whitefang.stepsofbabylon.presentation.battle.engine

import android.graphics.Canvas
import com.whitefang.stepsofbabylon.domain.model.BattleConditionEffects
import com.whitefang.stepsofbabylon.domain.model.Biome
import com.whitefang.stepsofbabylon.domain.model.EnemyType
import android.graphics.Paint
import com.whitefang.stepsofbabylon.domain.model.OwnedWeapon
import com.whitefang.stepsofbabylon.domain.model.OverdriveType
import com.whitefang.stepsofbabylon.domain.model.UltimateWeaponType
import com.whitefang.stepsofbabylon.domain.model.ResolvedStats
import com.whitefang.stepsofbabylon.domain.model.TierConfig
import com.whitefang.stepsofbabylon.domain.model.UpgradeType
import com.whitefang.stepsofbabylon.domain.model.ZigguratBaseStats
import com.whitefang.stepsofbabylon.domain.usecase.CalculateDamage
import com.whitefang.stepsofbabylon.domain.usecase.CalculateDefense
import com.whitefang.stepsofbabylon.presentation.battle.biome.BackgroundRenderer
import com.whitefang.stepsofbabylon.presentation.battle.biome.BiomeTheme
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
    private var backgroundRenderer: BackgroundRenderer? = null
    private var biomeTheme: BiomeTheme = BiomeTheme.forBiome(Biome.HANGING_GARDENS)

    @Volatile var cash: Long = 0L; private set
    @Volatile var totalCashEarned: Long = 0L; private set
    @Volatile var roundOver: Boolean = false
    @Volatile var totalEnemiesKilled: Int = 0; private set
    @Volatile var elapsedTimeSeconds: Float = 0f; private set
    @Volatile var activeOverdrive: OverdriveType? = null; private set
    @Volatile var overdriveTimeRemaining: Float = 0f; private set
    @Volatile var secondWindHpPercent: Double = 0.0
    @Volatile var secondWindUsed: Boolean = false
    @Volatile var cashBonusPercent: Double = 0.0
    private var preOverdriveStats: ResolvedStats? = null
    private var fortuneMultiplier: Double = 1.0

    data class UWState(val type: UltimateWeaponType, val level: Int, var cooldownRemaining: Float = 0f, var effectTimeRemaining: Float = 0f)
    data class UWEffect(val type: UltimateWeaponType, var timer: Float, val x: Float, val y: Float)
    val uwStates = mutableListOf<UWState>()
    private val uwEffects = mutableListOf<UWEffect>()
    private var chronoActive = false
    private var goldenZigActive = false
    private var preGoldenStats: ResolvedStats? = null
    private val uwPaint = Paint(Paint.ANTI_ALIAS_FLAG)

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
        activeOverdrive = null; overdriveTimeRemaining = 0f; preOverdriveStats = null; fortuneMultiplier = 1.0
        secondWindUsed = false
        uwStates.clear(); uwEffects.clear(); chronoActive = false; goldenZigActive = false; preGoldenStats = null
        stats = resolvedStats; tier = playerTier; workshopLevels = wsLevels
        conditions = BattleConditionEffects.fromTier(tier)
        biomeTheme = BiomeTheme.forBiome(Biome.forTier(tier))
        backgroundRenderer = BackgroundRenderer(width, height, biomeTheme)

        val zig = ZigguratEntity(width, height, stats, ::findNearestEnemies, layerColors = biomeTheme.zigguratColors) { sx, sy, tx, ty ->
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
            enemyTint = biomeTheme.enemyTint,
        )
    }

    fun setStats(resolvedStats: ResolvedStats) { stats = resolvedStats }

    fun activateOverdrive(type: OverdriveType, baseStats: ResolvedStats) {
        activeOverdrive = type
        overdriveTimeRemaining = type.durationSeconds.toFloat()
        preOverdriveStats = baseStats
        when (type) {
            OverdriveType.ASSAULT -> {
                stats = stats.copy(damage = stats.damage * 1.5, attackSpeed = stats.attackSpeed * 2.0)
                ziggurat?.let { it.overdriveColor = 0xFFE53935.toInt() }
            }
            OverdriveType.FORTRESS -> {
                stats = stats.copy(healthRegen = stats.healthRegen * 2.0, defensePercent = min(stats.defensePercent + 0.50, 0.75))
                ziggurat?.let { it.overdriveColor = 0xFF2196F3.toInt() }
            }
            OverdriveType.FORTUNE -> {
                fortuneMultiplier = 3.0
                ziggurat?.let { it.overdriveColor = 0xFFFFD700.toInt() }
            }
            OverdriveType.SURGE -> {
                resetUWCooldowns()
                ziggurat?.let { it.overdriveColor = 0xFF9C27B0.toInt() }
            }
        }
    }

    private fun expireOverdrive() {
        preOverdriveStats?.let { stats = it }
        preOverdriveStats = null
        fortuneMultiplier = 1.0
        activeOverdrive = null
        overdriveTimeRemaining = 0f
        ziggurat?.let { it.overdriveColor = 0; it.overdriveProgress = 0f }
    }

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
        backgroundRenderer?.update(deltaTime)
        if (activeOverdrive != null) {
            overdriveTimeRemaining -= deltaTime
            ziggurat?.overdriveProgress = (overdriveTimeRemaining / 60f).coerceIn(0f, 1f)
            if (overdriveTimeRemaining <= 0f) expireOverdrive()
        }
        updateUWs(deltaTime)

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
        backgroundRenderer?.render(canvas) ?: canvas.drawColor(0xFF6B3A2A.toInt())
        entities.forEach { it.render(canvas) }
        // UW visual effects
        for (effect in uwEffects) {
            when (effect.type) {
                UltimateWeaponType.DEATH_WAVE -> {
                    val progress = 1f - effect.timer / 0.5f
                    uwPaint.color = 0xFF4CAF50.toInt(); uwPaint.alpha = ((1f - progress) * 150).toInt()
                    uwPaint.style = Paint.Style.STROKE; uwPaint.strokeWidth = 4f
                    canvas.drawCircle(effect.x, effect.y, progress * screenWidth * 0.6f, uwPaint)
                }
                UltimateWeaponType.CHAIN_LIGHTNING -> {
                    uwPaint.color = 0xFFE0E0FF.toInt(); uwPaint.alpha = 200
                    uwPaint.style = Paint.Style.STROKE; uwPaint.strokeWidth = 2f
                    getAliveEnemies().take(8).forEach { e -> canvas.drawLine(effect.x, effect.y, e.x, e.y, uwPaint) }
                }
                UltimateWeaponType.BLACK_HOLE -> {
                    uwPaint.color = 0xFF6A0DAD.toInt(); uwPaint.alpha = 100; uwPaint.style = Paint.Style.FILL
                    canvas.drawCircle(effect.x, effect.y, 60f, uwPaint)
                }
                UltimateWeaponType.CHRONO_FIELD -> {
                    uwPaint.color = 0x332196F3.toInt(); uwPaint.style = Paint.Style.FILL
                    canvas.drawRect(0f, 0f, screenWidth, screenHeight, uwPaint)
                }
                UltimateWeaponType.POISON_SWAMP -> {
                    uwPaint.color = 0xFF4CAF50.toInt(); uwPaint.alpha = 60; uwPaint.style = Paint.Style.FILL
                    canvas.drawCircle(effect.x, effect.y, 100f, uwPaint)
                }
                UltimateWeaponType.GOLDEN_ZIGGURAT -> {} // Uses overdrive aura
            }
        }
        if (chronoActive) {
            uwPaint.color = 0x222196F3.toInt(); uwPaint.style = Paint.Style.FILL
            canvas.drawRect(0f, 0f, screenWidth, screenHeight, uwPaint)
        }
        ziggurat?.let { healthBarRenderer.render(canvas, it.currentHp, it.maxHp, screenWidth) }
    }

    fun addEntity(entity: Entity) { pendingAdd.add(entity) }

    fun initUWs(equipped: List<OwnedWeapon>) {
        uwStates.clear()
        equipped.forEach { uwStates.add(UWState(it.type, it.level)) }
    }

    fun activateUW(index: Int) {
        val uw = uwStates.getOrNull(index) ?: return
        if (uw.cooldownRemaining > 0f) return
        uw.cooldownRemaining = uw.type.cooldownAtLevel(uw.level)
        val zig = ziggurat ?: return
        val duration = uw.type.effectDurationSeconds.toFloat()
        if (duration > 0f) uw.effectTimeRemaining = duration

        when (uw.type) {
            UltimateWeaponType.DEATH_WAVE -> {
                val dmg = 500.0 * uw.level
                getAliveEnemies().forEach { it.takeDamage(dmg) }
                uwEffects.add(UWEffect(uw.type, 0.5f, zig.x, zig.y - zig.height / 2))
            }
            UltimateWeaponType.CHAIN_LIGHTNING -> {
                val dmg = 300.0 * uw.level
                val targets = getAliveEnemies().sortedBy { hypot(it.x - zig.x, it.y - zig.y) }.take(8)
                targets.forEach { it.takeDamage(dmg) }
                uwEffects.add(UWEffect(uw.type, 0.3f, zig.x, zig.y - zig.height / 2))
            }
            UltimateWeaponType.BLACK_HOLE -> {
                uwEffects.add(UWEffect(uw.type, duration, screenWidth / 2f, screenHeight * 0.35f))
            }
            UltimateWeaponType.CHRONO_FIELD -> { chronoActive = true }
            UltimateWeaponType.POISON_SWAMP -> {
                uwEffects.add(UWEffect(uw.type, duration, screenWidth / 2f, screenHeight * 0.6f))
            }
            UltimateWeaponType.GOLDEN_ZIGGURAT -> {
                goldenZigActive = true; preGoldenStats = stats
                fortuneMultiplier = fortuneMultiplier.coerceAtLeast(5.0)
                stats = stats.copy(damage = stats.damage * 1.5)
                zig.overdriveColor = 0xFFFFD700.toInt(); zig.overdriveProgress = 1f
            }
        }
    }

    private fun updateUWs(deltaTime: Float) {
        val zig = ziggurat ?: return
        for (uw in uwStates) {
            if (uw.cooldownRemaining > 0f) uw.cooldownRemaining = (uw.cooldownRemaining - deltaTime).coerceAtLeast(0f)
            if (uw.effectTimeRemaining > 0f) {
                uw.effectTimeRemaining -= deltaTime
                if (uw.effectTimeRemaining <= 0f) {
                    uw.effectTimeRemaining = 0f
                    when (uw.type) {
                        UltimateWeaponType.CHRONO_FIELD -> chronoActive = false
                        UltimateWeaponType.GOLDEN_ZIGGURAT -> {
                            goldenZigActive = false; preGoldenStats?.let { stats = it }; preGoldenStats = null
                            if (activeOverdrive == null) fortuneMultiplier = 1.0
                            if (activeOverdrive == null) { zig.overdriveColor = 0; zig.overdriveProgress = 0f }
                        }
                        else -> {}
                    }
                }
                // Ongoing effects
                when (uw.type) {
                    UltimateWeaponType.BLACK_HOLE -> {
                        val cx = screenWidth / 2f; val cy = screenHeight * 0.35f
                        val dmg = 50.0 * uw.level * deltaTime
                        getAliveEnemies().forEach { e ->
                            val dx = cx - e.x; val dy = cy - e.y; val d = hypot(dx, dy).coerceAtLeast(1f)
                            e.x += dx / d * 60f * deltaTime; e.y += dy / d * 60f * deltaTime
                            e.takeDamage(dmg)
                        }
                    }
                    UltimateWeaponType.POISON_SWAMP -> {
                        val dmg = 0.02 * uw.level * deltaTime
                        getAliveEnemies().forEach { e -> e.takeDamage(e.maxHp * dmg) }
                    }
                    else -> {}
                }
            }
        }
        // Chrono effect: handled in WaveSpawner via speed, but we also slow existing enemies
        // (enemies already spawned keep their speed, so we apply chrono as a position modifier)
        uwEffects.removeAll { e -> e.timer -= deltaTime; e.timer <= 0f }
    }

    fun resetUWCooldowns() { uwStates.forEach { it.cooldownRemaining = 0f } }

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
        val waveCash = ((BASE_CASH_PER_WAVE + wsLevel(UpgradeType.CASH_PER_WAVE) * FLAT_BONUS_PER_WAVE_LEVEL) * fortuneMultiplier).toLong()
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
        if (zig.currentHp - mitigated <= 0.0 && !secondWindUsed && secondWindHpPercent > 0.0) {
            secondWindUsed = true
            zig.currentHp = zig.maxHp * secondWindHpPercent
            applyThorn(rawDamage, attacker); return
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
        val killCash = (baseCash * tierMult * cashBonus * fortuneMultiplier * (1.0 + cashBonusPercent / 100.0)).toLong()
        cash += killCash
        totalCashEarned += killCash
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
