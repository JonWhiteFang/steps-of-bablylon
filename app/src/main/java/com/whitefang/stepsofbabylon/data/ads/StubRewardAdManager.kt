package com.whitefang.stepsofbabylon.data.ads

import com.whitefang.stepsofbabylon.domain.model.AdPlacement
import com.whitefang.stepsofbabylon.domain.model.AdResult
import com.whitefang.stepsofbabylon.domain.repository.RewardAdManager
import kotlinx.coroutines.delay
import javax.inject.Inject

class StubRewardAdManager @Inject constructor() : RewardAdManager {

    override suspend fun showRewardAd(placement: AdPlacement): AdResult {
        delay(1000) // simulate ad view
        return AdResult.Rewarded
    }

    override fun isAdAvailable(placement: AdPlacement): Boolean = true
}
