package com.whitefang.stepsofbabylon.fakes

import com.whitefang.stepsofbabylon.domain.model.AdPlacement
import com.whitefang.stepsofbabylon.domain.model.AdResult
import com.whitefang.stepsofbabylon.domain.repository.RewardAdManager

class FakeRewardAdManager : RewardAdManager {
    var nextResult: AdResult = AdResult.Rewarded
    override suspend fun showRewardAd(placement: AdPlacement): AdResult = nextResult
    override fun isAdAvailable(placement: AdPlacement): Boolean = true
}
