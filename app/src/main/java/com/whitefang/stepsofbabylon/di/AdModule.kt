package com.whitefang.stepsofbabylon.di

import com.whitefang.stepsofbabylon.data.ads.StubRewardAdManager
import com.whitefang.stepsofbabylon.domain.repository.RewardAdManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdModule {
    @Binds @Singleton
    abstract fun bindRewardAdManager(impl: StubRewardAdManager): RewardAdManager
}
