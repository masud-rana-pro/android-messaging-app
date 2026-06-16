package com.contactme.app.di

import com.contactme.app.typing.FirebaseTypingRepository
import com.contactme.app.typing.TypingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TypingModule {
    @Binds
    @Singleton
    abstract fun bindTypingRepository(
        firebaseTypingRepository: FirebaseTypingRepository
    ): TypingRepository
}
