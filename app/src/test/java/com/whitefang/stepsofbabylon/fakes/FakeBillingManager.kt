package com.whitefang.stepsofbabylon.fakes

import com.whitefang.stepsofbabylon.domain.model.BillingProduct
import com.whitefang.stepsofbabylon.domain.model.PurchaseResult
import com.whitefang.stepsofbabylon.domain.repository.BillingManager

class FakeBillingManager : BillingManager {
    var nextResult: PurchaseResult = PurchaseResult.Success
    val purchases = mutableListOf<BillingProduct>()

    override suspend fun purchase(product: BillingProduct): PurchaseResult {
        purchases += product
        return nextResult
    }
    override suspend fun isAdRemoved(): Boolean = false
    override suspend fun isSeasonPassActive(): Boolean = false
}
