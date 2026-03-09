package com.whitefang.stepsofbabylon.domain.repository

import com.whitefang.stepsofbabylon.domain.model.BillingProduct
import com.whitefang.stepsofbabylon.domain.model.PurchaseResult

interface BillingManager {
    suspend fun purchase(product: BillingProduct): PurchaseResult
    suspend fun isAdRemoved(): Boolean
    suspend fun isSeasonPassActive(): Boolean
}
