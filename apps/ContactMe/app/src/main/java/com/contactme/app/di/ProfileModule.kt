package com.contactme.app.di

import com.contactme.app.profile.FirebaseProfileRepository
import com.contactme.app.profile.FirebaseProfilePhotoRepository
import com.contactme.app.profile.ProfilePhotoRepository
import com.contactme.app.profile.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {
    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        firebaseProfileRepository: FirebaseProfileRepository
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindProfilePhotoRepository(
        firebaseProfilePhotoRepository: FirebaseProfilePhotoRepository
    ): ProfilePhotoRepository
}
