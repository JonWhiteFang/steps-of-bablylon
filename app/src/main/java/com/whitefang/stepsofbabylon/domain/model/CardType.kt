package com.whitefang.stepsofbabylon.domain.model

enum class CardType(
    val rarity: CardRarity,
    val effectLv1: String,
    val effectLv5: String,
    val valueLv1: Double,
    val valueLv5: Double,
    val secondaryLv1: Double = 0.0,
    val secondaryLv5: Double = 0.0,
    val maxLevel: Int = 5,
) {
    IRON_SKIN(CardRarity.COMMON, "+10% Defense Absolute", "+30% Defense Absolute", 10.0, 30.0),
    SHARP_SHOOTER(CardRarity.COMMON, "+15% Critical Chance", "+35% Critical Chance", 15.0, 35.0),
    CASH_GRAB(CardRarity.COMMON, "+20% Cash from kills", "+50% Cash from kills", 20.0, 50.0),
    VAMPIRIC_TOUCH(CardRarity.RARE, "+5% Lifesteal", "+15% Lifesteal", 5.0, 15.0),
    CHAIN_REACTION(CardRarity.RARE, "+2 Bounce Shot targets", "+4 Bounce Shot targets", 2.0, 4.0),
    SECOND_WIND(CardRarity.RARE, "Revive once at 50% HP", "Revive once at 100% HP", 50.0, 100.0),
    WALKING_FORTRESS(CardRarity.EPIC, "+50% Health, -20% Attack Speed", "+100% Health, -10% Attack Speed", 50.0, 100.0, 20.0, 10.0),
    GLASS_CANNON(CardRarity.EPIC, "+80% Damage, -40% Health", "+120% Damage, -20% Health", 80.0, 120.0, 40.0, 20.0),
    STEP_SURGE(CardRarity.EPIC, "Earn 2x Gems this round", "Earn 4x Gems this round", 2.0, 4.0);

    fun effectAtLevel(level: Int): Double =
        valueLv1 + (valueLv5 - valueLv1) * (level - 1) / 4.0

    fun secondaryAtLevel(level: Int): Double =
        secondaryLv1 + (secondaryLv5 - secondaryLv1) * (level - 1) / 4.0
}
