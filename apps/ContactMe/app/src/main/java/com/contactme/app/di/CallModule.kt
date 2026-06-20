package com.contactme.app.di

import com.contactme.app.call.CallSignalingRepository
import com.contactme.app.call.FirebaseCallSignalingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CallModule {
    @Binds
    @Singleton
    abstract fun bindCallSignalingRepository(
        firebaseCallSignalingRepository: FirebaseCallSignalingRepository
    ): CallSignalingRepository
}
