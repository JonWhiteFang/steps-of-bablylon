package com.whitefang.stepsofbabylon.di

import com.whitefang.stepsofbabylon.data.repository.*
import com.whitefang.stepsofbabylon.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindPlayerRepository(impl: PlayerRepositoryImpl): PlayerRepository

    @Binds @Singleton
    abstract fun bindWorkshopRepository(impl: WorkshopRepositoryImpl): WorkshopRepository

    @Binds @Singleton
    abstract fun bindLabRepository(impl: LabRepositoryImpl): LabRepository

    @Binds @Singleton
    abstract fun bindCardRepository(impl: CardRepositoryImpl): CardRepository

    @Binds @Singleton
    abstract fun bindUltimateWeaponRepository(impl: UltimateWeaponRepositoryImpl): UltimateWeaponRepository

    @Binds @Singleton
    abstract fun bindStepRepository(impl: StepRepositoryImpl): StepRepository

    @Binds @Singleton
    abstract fun bindWalkingEncounterRepository(impl: WalkingEncounterRepositoryImpl): WalkingEncounterRepository

    @Binds @Singleton
    abstract fun bindCosmeticRepository(impl: CosmeticRepositoryImpl): CosmeticRepository
}
