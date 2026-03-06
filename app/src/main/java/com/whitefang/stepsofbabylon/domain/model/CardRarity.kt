package com.whitefang.stepsofbabylon.domain.model

enum class CardRarity(val dustValue: Long, val upgradeDustPerLevel: Long) {
    COMMON(5, 10),
    RARE(15, 25),
    EPIC(50, 50),
}
