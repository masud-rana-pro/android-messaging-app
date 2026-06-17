package com.contactme.app.di

import com.contactme.app.notification.DeviceTokenRepository
import com.contactme.app.notification.FirebaseDeviceTokenRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {
    @Binds
    @Singleton
    abstract fun bindDeviceTokenRepository(
        firebaseDeviceTokenRepository: FirebaseDeviceTokenRepository
    ): DeviceTokenRepository
}
