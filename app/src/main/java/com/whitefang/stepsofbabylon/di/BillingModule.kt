package com.whitefang.stepsofbabylon.di

import com.whitefang.stepsofbabylon.data.billing.StubBillingManager
import com.whitefang.stepsofbabylon.domain.repository.BillingManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BillingModule {
    @Binds @Singleton
    abstract fun bindBillingManager(impl: StubBillingManager): BillingManager
}
