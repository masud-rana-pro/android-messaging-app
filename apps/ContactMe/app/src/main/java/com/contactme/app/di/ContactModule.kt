package com.contactme.app.di

import com.contactme.app.contact.ContactRepository
import com.contactme.app.contact.FirebaseContactRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ContactModule {
    @Binds
    @Singleton
    abstract fun bindContactRepository(
        firebaseContactRepository: FirebaseContactRepository
    ): ContactRepository
}
