package com.whitefang.stepsofbabylon.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Health Connect dependencies are auto-provided via @Inject constructors.
 * This module exists as an organizational placeholder.
 */
@Module
@InstallIn(SingletonComponent::class)
object HealthConnectModule
