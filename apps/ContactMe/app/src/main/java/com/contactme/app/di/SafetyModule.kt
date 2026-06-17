package com.contactme.app.di

import com.contactme.app.safety.FirebaseSafetyRepository
import com.contactme.app.safety.SafetyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SafetyModule {
    @Binds
    @Singleton
    abstract fun bindSafetyRepository(
        firebaseSafetyRepository: FirebaseSafetyRepository
    ): SafetyRepository
}
