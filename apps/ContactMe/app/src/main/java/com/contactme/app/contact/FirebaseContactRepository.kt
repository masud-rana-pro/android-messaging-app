package com.contactme.app.contact

import com.contactme.app.profile.PrivacyVisibility
import com.contactme.app.profile.UserProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseContactRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ContactRepository {
    override fun observeContacts(ownerUserId: String): Flow<List<UserProfile>> = callbackFlow {
        val registration = firestore.collection(CONTACTS_COLLECTION)
            .document(ownerUserId)
            .collection(CONTACT_ITEMS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                launch {
                    val contacts = snapshot
                        ?.documents
                        .orEmpty()
                        .mapNotNull { contactDocument ->
                            contactDocument.toVisibleContactFor(ownerUserId)
                        }
                        .sortedBy { contact ->
                            contact.displayName.ifBlank { contact.username }.lowercase()
                        }

                    trySend(contacts)
                }
            }

        awaitClose { registration.remove() }
    }

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

    private suspend fun DocumentSnapshot.toVisibleContactFor(viewerUserId: String): UserProfile? {
        val contactUserId = getString("userId").orEmpty().ifBlank { id }
        if (contactUserId.isBlank()) return null

        val profileDocument = firestore.collection(USERS_COLLECTION)
            .document(contactUserId)
            .get()
            .await()
        val sourceDocument = if (profileDocument.exists()) profileDocument else this

        return UserProfile(
            userId = contactUserId,
            displayName = sourceDocument.getString("displayName").orEmpty(),
            username = sourceDocument.getString("username").orEmpty(),
            phoneNumber = sourceDocument.getString("phoneNumber").orEmpty(),
            photoUrl = if (profileDocument.exists()) {
                profileDocument.visibleProfilePhotoUrlFor(viewerUserId)
            } else {
                getString("photoUrl").orEmpty()
            }
        )
    }

    private suspend fun DocumentSnapshot.visibleProfilePhotoUrlFor(viewerUserId: String): String {
        val visibility = PrivacyVisibility.fromFirestore(getString("profilePhotoVisibility"))
        return when (visibility) {
            PrivacyVisibility.Everyone -> getString("photoUrl").orEmpty()
            PrivacyVisibility.Contacts -> {
                if (isContact(ownerUserId = id, viewerUserId = viewerUserId)) {
                    getString("photoUrl").orEmpty()
                } else {
                    ""
                }
            }
            PrivacyVisibility.Nobody -> ""
        }
    }

    private suspend fun isContact(ownerUserId: String, viewerUserId: String): Boolean {
        return firestore.collection(CONTACTS_COLLECTION)
            .document(ownerUserId)
            .collection(CONTACT_ITEMS_COLLECTION)
            .document(viewerUserId)
            .get()
            .await()
            .exists()
    }

    private companion object {
        const val USERS_COLLECTION = "users"
        const val CONTACTS_COLLECTION = "contacts"
        const val CONTACT_ITEMS_COLLECTION = "items"
    }
}
