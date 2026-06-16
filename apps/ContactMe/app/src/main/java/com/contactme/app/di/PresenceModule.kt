package com.contactme.app.di

import com.contactme.app.presence.FirebasePresenceRepository
import com.contactme.app.presence.PresenceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PresenceModule {
    @Binds
    @Singleton
    abstract fun bindPresenceRepository(
        firebasePresenceRepository: FirebasePresenceRepository
    ): PresenceRepository
}
