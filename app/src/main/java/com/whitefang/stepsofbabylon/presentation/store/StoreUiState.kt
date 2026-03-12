package com.whitefang.stepsofbabylon.presentation.store

import com.whitefang.stepsofbabylon.domain.model.BillingProduct

data class StoreUiState(
    val gems: Long = 0,
    val adRemoved: Boolean = false,
    val seasonPassActive: Boolean = false,
    val seasonPassExpiry: Long = 0,
    val cosmetics: List<CosmeticDisplayInfo> = emptyList(),
    val isPurchasing: Boolean = false,
    val userMessage: String? = null,
)

data class CosmeticDisplayInfo(
    val cosmeticId: String,
    val category: String,
    val name: String,
    val description: String,
    val priceGems: Long,
    val isOwned: Boolean,
    val isEquipped: Boolean,
)
