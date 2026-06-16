package com.contactme.app.contact

import com.contactme.app.profile.UserProfile
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun observeContacts(ownerUserId: String): Flow<List<UserProfile>>

    suspend fun saveContact(
        ownerUserId: String,
        contact: UserProfile
    ): ContactResult
}
