package com.whitefang.stepsofbabylon.domain.model

enum class BillingProduct(val gemAmount: Long, val priceDisplay: String) {
    GEM_PACK_SMALL(50, "$0.99"),
    GEM_PACK_MEDIUM(300, "$4.99"),
    GEM_PACK_LARGE(700, "$9.99"),
    AD_REMOVAL(0, "$3.99"),
    SEASON_PASS(0, "$4.99/mo"),
    ;

    /**
     * Opt-in Companion so extension functions can attach reverse-lookup helpers (e.g.
     * [com.whitefang.stepsofbabylon.data.billing.fromSkuIdOrNull] in the data layer, which
     * maps Play Billing `productId` strings back to enum entries during reconciliation).
     * Empty on purpose — lookup tables live where they are used.
     */
    companion object
}

sealed class PurchaseResult {
    data object Success : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}
