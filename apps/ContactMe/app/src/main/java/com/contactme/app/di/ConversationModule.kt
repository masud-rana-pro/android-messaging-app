package com.contactme.app.di

import com.contactme.app.conversation.ConversationRepository
import com.contactme.app.conversation.FirebaseConversationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ConversationModule {
    @Binds
    @Singleton
    abstract fun bindConversationRepository(
        firebaseConversationRepository: FirebaseConversationRepository
    ): ConversationRepository
}
