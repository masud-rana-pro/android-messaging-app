package com.contactme.app.contact

import com.contactme.app.profile.UserProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class FirebaseContactRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ContactRepository {
    override suspend fun saveContact(
        ownerUserId: String,
        contact: UserProfile
    ): ContactResult {
        if (ownerUserId == contact.userId) {
            return ContactResult.Error
        }

        return runCatching {
            val contactData = mapOf(
                "userId" to contact.userId,
                "displayName" to contact.displayName,
                "username" to contact.username,
                "phoneNumber" to contact.phoneNumber,
                "photoUrl" to contact.photoUrl,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            firestore.collection(CONTACTS_COLLECTION)
                .document(ownerUserId)
                .collection(CONTACT_ITEMS_COLLECTION)
                .document(contact.userId)
                .set(contactData, SetOptions.merge())
                .await()
        }.fold(
            onSuccess = { ContactResult.Success },
            onFailure = { ContactResult.Error }
        )
    }

    private companion object {
        const val CONTACTS_COLLECTION = "contacts"
        const val CONTACT_ITEMS_COLLECTION = "items"
    }
}
