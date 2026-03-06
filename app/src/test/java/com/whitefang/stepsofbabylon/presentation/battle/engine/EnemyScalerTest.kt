package com.whitefang.stepsofbabylon.presentation.battle.engine

import com.whitefang.stepsofbabylon.domain.model.EnemyType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.pow

class EnemyScalerTest {

    private val eps = 0.001

    @Test
    fun `wave 1 BASIC health`() {
        val expected = 50.0 * 1.0 * 1.05.pow(1)
        assertEquals(expected, EnemyScaler.scaleHealth(EnemyType.BASIC, 1), eps)
    }

    @Test
    fun `wave 10 BOSS health`() {
        val expected = 50.0 * 20.0 * 1.05.pow(10)
        assertEquals(expected, EnemyScaler.scaleHealth(EnemyType.BOSS, 10), eps)
    }

    @Test
    fun `FAST speed`() {
        assertEquals(160f, EnemyScaler.scaleSpeed(EnemyType.FAST))
    }

    @Test
    fun `cash rewards for all types`() {
        assertEquals(5L, EnemyScaler.cashReward(EnemyType.BASIC))
        assertEquals(3L, EnemyScaler.cashReward(EnemyType.FAST))
        assertEquals(15L, EnemyScaler.cashReward(EnemyType.TANK))
        assertEquals(8L, EnemyScaler.cashReward(EnemyType.RANGED))
        assertEquals(100L, EnemyScaler.cashReward(EnemyType.BOSS))
        assertEquals(6L, EnemyScaler.cashReward(EnemyType.SCATTER))
    }
}
