package com.whitefang.stepsofbabylon.domain.model

data class BattleConditionEffects(
    val enemySpeedMultiplier: Float = 1f,
    val enemyAttackSpeedMultiplier: Float = 1f,
    val orbDamageMultiplier: Float = 1f,
    val knockbackMultiplier: Float = 1f,
    val thornMultiplier: Float = 1f,
    val armorHits: Int = 0,
    val bossWaveInterval: Int = 10,
) {
    companion object {
        fun fromTier(tier: Int): BattleConditionEffects {
            val conditions = TierConfig.forTier(tier).battleConditions
            if (conditions.isEmpty()) return BattleConditionEffects()
            return BattleConditionEffects(
                enemySpeedMultiplier = 1f + (conditions[BattleCondition.ENEMY_SPEED] ?: 0) / 100f,
                enemyAttackSpeedMultiplier = 1f + (conditions[BattleCondition.ENEMY_ATTACK_SPEED] ?: 0) / 100f,
                orbDamageMultiplier = 1f - (conditions[BattleCondition.ORB_RESISTANCE] ?: 0) / 100f,
                knockbackMultiplier = 1f - (conditions[BattleCondition.KNOCKBACK_RESISTANCE] ?: 0) / 100f,
                thornMultiplier = 1f - (conditions[BattleCondition.THORN_RESISTANCE] ?: 0) / 100f,
                armorHits = conditions[BattleCondition.ARMORED_ENEMIES] ?: 0,
                bossWaveInterval = conditions[BattleCondition.MORE_BOSSES] ?: 10,
            )
        }
    }
}
