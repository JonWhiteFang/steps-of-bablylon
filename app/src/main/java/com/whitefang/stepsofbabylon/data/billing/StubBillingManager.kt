package com.whitefang.stepsofbabylon.data.billing

import com.whitefang.stepsofbabylon.domain.model.BillingProduct
import com.whitefang.stepsofbabylon.domain.model.PurchaseResult
import com.whitefang.stepsofbabylon.domain.repository.BillingManager
import com.whitefang.stepsofbabylon.domain.repository.PlayerRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class StubBillingManager @Inject constructor(
    private val playerRepository: PlayerRepository,
) : BillingManager {

    override suspend fun purchase(product: BillingProduct): PurchaseResult {
        delay(500) // simulate network
        when (product) {
            BillingProduct.GEM_PACK_SMALL,
            BillingProduct.GEM_PACK_MEDIUM,
            BillingProduct.GEM_PACK_LARGE -> playerRepository.addGems(product.gemAmount)
            BillingProduct.AD_REMOVAL -> playerRepository.updateAdRemoved(true)
            BillingProduct.SEASON_PASS -> {
                val expiry = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000
                playerRepository.updateSeasonPass(true, expiry)
            }
        }
        return PurchaseResult.Success
    }

    override suspend fun isAdRemoved(): Boolean =
        playerRepository.observeProfile().first().adRemoved

    override suspend fun isSeasonPassActive(): Boolean {
        val profile = playerRepository.observeProfile().first()
        if (profile.seasonPassActive && profile.seasonPassExpiry < System.currentTimeMillis()) {
            playerRepository.updateSeasonPass(false, 0)
            return false
        }
        return profile.seasonPassActive
    }
}
