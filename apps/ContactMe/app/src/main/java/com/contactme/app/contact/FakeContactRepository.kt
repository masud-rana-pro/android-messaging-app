package com.contactme.app.contact

import com.contactme.app.profile.UserProfile
import javax.inject.Inject

class FakeContactRepository @Inject constructor() : ContactRepository {
    private val contacts = mutableMapOf<String, MutableMap<String, UserProfile>>()

    override suspend fun saveContact(
        ownerUserId: String,
        contact: UserProfile
    ): ContactResult {
        if (ownerUserId == contact.userId) {
            return ContactResult.Error
        }

        contacts.getOrPut(ownerUserId) { mutableMapOf() }[contact.userId] = contact
        return ContactResult.Success
    }
}
