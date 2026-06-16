package com.contactme.app.di

import com.contactme.app.message.FirebaseMessageRepository
import com.contactme.app.message.MessageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MessageModule {
    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        firebaseMessageRepository: FirebaseMessageRepository
    ): MessageRepository
}
