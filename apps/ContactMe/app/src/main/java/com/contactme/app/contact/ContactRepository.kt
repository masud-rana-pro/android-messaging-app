package com.contactme.app.contact

import com.contactme.app.profile.UserProfile

interface ContactRepository {
    suspend fun saveContact(
        ownerUserId: String,
        contact: UserProfile
    ): ContactResult
}
