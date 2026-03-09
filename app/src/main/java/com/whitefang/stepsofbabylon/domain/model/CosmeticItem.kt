package com.whitefang.stepsofbabylon.domain.model

data class CosmeticItem(
    val cosmeticId: String,
    val category: CosmeticCategory,
    val name: String,
    val description: String,
    val priceGems: Long,
    val isOwned: Boolean = false,
    val isEquipped: Boolean = false,
)
