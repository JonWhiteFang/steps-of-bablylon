package com.whitefang.stepsofbabylon.presentation.battle.engine

import com.whitefang.stepsofbabylon.domain.model.EnemyType
import kotlin.math.pow

object EnemyScaler {
    const val BASE_HEALTH = 50.0
    const val BASE_DAMAGE = 5.0
    const val BASE_SPEED = 80f
    const val SCALING_FACTOR = 1.05

    fun scaleHealth(type: EnemyType, wave: Int): Double =
        BASE_HEALTH * type.healthMultiplier * SCALING_FACTOR.pow(wave)

    fun scaleDamage(type: EnemyType, wave: Int): Double =
        BASE_DAMAGE * type.damageMultiplier * SCALING_FACTOR.pow(wave)

    fun scaleSpeed(type: EnemyType): Float =
        BASE_SPEED * type.speedMultiplier.toFloat()

    fun cashReward(type: EnemyType): Long = when (type) {
        EnemyType.BASIC -> 5
        EnemyType.FAST -> 3
        EnemyType.TANK -> 15
        EnemyType.RANGED -> 8
        EnemyType.BOSS -> 100
        EnemyType.SCATTER -> 6
    }
}
