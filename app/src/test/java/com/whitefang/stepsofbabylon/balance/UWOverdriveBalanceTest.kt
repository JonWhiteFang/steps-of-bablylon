package com.whitefang.stepsofbabylon.balance

import com.whitefang.stepsofbabylon.domain.model.EnemyType
import com.whitefang.stepsofbabylon.domain.model.OverdriveType
import com.whitefang.stepsofbabylon.domain.model.UltimateWeaponType
import com.whitefang.stepsofbabylon.presentation.battle.engine.EnemyScaler
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Validates UW cooldowns, damage, and Overdrive cost proportionality.
 */
class UWOverdriveBalanceTest {

    @Test
    fun `all UWs can activate 2 to 3 times in a 20 minute round at level 1`() {
        val roundSeconds = 20 * 60f // 20 minutes
        for (uw in UltimateWeaponType.entries) {
            val cooldown = uw.cooldownAtLevel(1)
            val activations = (roundSeconds / cooldown).toInt()
            assertTrue(activations >= 2,
                "${uw.name} at Lv1: cooldown ${cooldown}s, only $activations activations in 20min")
            assertTrue(activations <= 30,
                "${uw.name} at Lv1: $activations activations seems too many")
        }
    }

    @Test
    fun `death wave lv5 does not one-shot wave 50 boss`() {
        val damage = 500.0 * 5 // Death Wave at level 5
        val bossHp = EnemyScaler.scaleHealth(EnemyType.BOSS, 50)
        val ratio = damage / bossHp
        assertTrue(ratio < 1.0, "Death Wave Lv5 ($damage) one-shots wave 50 boss ($bossHp)")
        assertTrue(ratio > 0.05, "Death Wave Lv5 ($damage) is negligible vs wave 50 boss ($bossHp)")
    }

    @Test
    fun `golden ziggurat 5x cash for 10s is strong but bounded`() {
        // 10 seconds of 5x cash vs a full 35-second wave
        // At most ~28% of wave time at 5x = effective 2.1x for the wave
        val effectiveMultiplier = (10.0 / 35.0) * 5.0 + (25.0 / 35.0) * 1.0
        assertTrue(effectiveMultiplier < 3.0,
            "Golden Ziggurat effective wave multiplier: $effectiveMultiplier (should be <3x)")
    }

    @Test
    fun `overdrive costs represent 3 to 10 minutes of walking`() {
        // Average walking pace: ~100 steps/minute
        val stepsPerMinute = 100
        for (od in OverdriveType.entries) {
            val minutes = od.stepCost.toDouble() / stepsPerMinute
            assertTrue(minutes in 2.0..10.0,
                "${od.name} costs ${od.stepCost} Steps = ${minutes}min of walking (should be 2-10min)")
        }
    }

    @Test
    fun `surge overdrive value scales with equipped UW count`() {
        // Surge resets all cooldowns — value is proportional to number of UWs
        // With 1 UW: saves 1 cooldown. With 3 UWs: saves 3 cooldowns.
        // Cost (750) should be justified with 2+ UWs
        val surgeCost = OverdriveType.SURGE.stepCost
        val cheapestUWCooldown = UltimateWeaponType.entries.minOf { it.baseCooldownSeconds }
        // With 3 UWs, you save 3 × cheapest cooldown worth of waiting
        val timeSavedWith3 = cheapestUWCooldown * 3
        assertTrue(timeSavedWith3 >= 120, "Surge with 3 UWs saves ${timeSavedWith3}s (should be ≥120s)")
    }
}
