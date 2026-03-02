package com.whitefang.stepsofbabylon.domain.model

enum class CardType(
    val rarity: CardRarity,
    val effectLv1: String,
    val effectLv5: String,
    val maxLevel: Int = 5,
) {
    IRON_SKIN(CardRarity.COMMON, "+10% Defense Absolute", "+30% Defense Absolute"),
    SHARP_SHOOTER(CardRarity.COMMON, "+15% Critical Chance", "+35% Critical Chance"),
    CASH_GRAB(CardRarity.COMMON, "+20% Cash from kills", "+50% Cash from kills"),
    VAMPIRIC_TOUCH(CardRarity.RARE, "+5% Lifesteal", "+15% Lifesteal"),
    CHAIN_REACTION(CardRarity.RARE, "+2 Bounce Shot targets", "+4 Bounce Shot targets"),
    SECOND_WIND(CardRarity.RARE, "Revive once at 50% HP", "Revive once at 100% HP"),
    WALKING_FORTRESS(CardRarity.EPIC, "+50% Health, -20% Attack Speed", "+100% Health, -10% Attack Speed"),
    GLASS_CANNON(CardRarity.EPIC, "+80% Damage, -40% Health", "+120% Damage, -20% Health"),
    STEP_SURGE(CardRarity.EPIC, "Earn 2x Gems this round", "Earn 4x Gems this round"),
}
