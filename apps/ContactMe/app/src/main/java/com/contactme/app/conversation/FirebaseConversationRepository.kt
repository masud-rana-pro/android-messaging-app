package com.contactme.app.conversation

import com.contactme.app.profile.PrivacyVisibility
import com.contactme.app.safety.SafetyRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseConversationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val safetyRepository: SafetyRepository
) : ConversationRepository {
    override fun observeConversationPreviews(
        currentUserId: String
    ): Flow<List<ConversationPreview>> = callbackFlow {
        val registration = firestore.collection(CONVERSATIONS_COLLECTION)
            .whereArrayContains("participantIds", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val conversations = snapshot?.documents.orEmpty()

                launch {
                    runCatching {
                        val previews = conversations.mapNotNull { document ->
                            val participantIds = document.get("participantIds") as? List<*> ?: return@mapNotNull null
                            val conversationType = ConversationType.fromFirestore(document.getString("type"))
                            val otherUserId = participantIds.filterIsInstance<String>()
                                .firstOrNull { userId -> userId != currentUserId }.orEmpty()
                            val otherUser = otherUserId.takeIf { conversationType == ConversationType.Direct }
                                ?.let { firestore.collection(USERS_COLLECTION).document(it).get().await() }
                            val updatedAtMillis = document.getTimestamp("updatedAt")?.toDate()?.time ?: 0L
                            val lastMessageSenderId = document.getString("lastMessageSenderId").orEmpty()
                            val readAtMillis = document.getTimestamp("readAtByUser.$currentUserId")
                                ?.toDate()
                                ?.time
                                ?: 0L

                            ConversationPreview(
                                conversationId = document.id,
                                otherUserId = otherUserId,
                                title = if (conversationType == ConversationType.Group) {
                                    document.getString("title").orEmpty().ifBlank { "Group" }
                                } else {
                                    otherUser?.getString("displayName").orEmpty().ifBlank {
                                        otherUser?.getString("username").orEmpty().ifBlank { "ContactMe User" }
                                    }
                                },
                                photoUrl = if (conversationType == ConversationType.Group) {
                                    document.getString("photoUrl").orEmpty()
                                } else {
                                    otherUser?.visibleProfilePhotoUrlFor(currentUserId).orEmpty()
                                },
                                subtitle = document.getString("lastMessageText").orEmpty().ifBlank {
                                    "No messages yet."
                                },
                                updatedAtMillis = updatedAtMillis,
                                hasUnreadMessages = lastMessageSenderId.isNotBlank() &&
                                    lastMessageSenderId != currentUserId &&
                                    updatedAtMillis > readAtMillis,
                                type = conversationType
                            )
                        }

                        trySend(previews.sortedByDescending { preview -> preview.updatedAtMillis })
                    }.onFailure {
                        trySend(emptyList())
                    }
                }
            }

        awaitClose { registration.remove() }
    }

    override fun observeReadReceiptState(
        conversationId: String,
        currentUserId: String
    ): Flow<ReadReceiptState> = callbackFlow {
        val registration = firestore.collection(CONVERSATIONS_COLLECTION)
            .document(conversationId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(ReadReceiptState())
                    return@addSnapshotListener
                }

                launch {
                    runCatching {
                        val participantIds = snapshot
                            ?.get("participantIds") as? List<*>
                        val peerUserId = participantIds
                            .orEmpty()
                            .filterIsInstance<String>()
                            .firstOrNull { userId -> userId != currentUserId }

                        if (peerUserId == null) {
                            trySend(ReadReceiptState())
                            return@runCatching
                        }

                        val peerProfile = firestore.collection(USERS_COLLECTION)
                            .document(peerUserId)
                            .get()
                            .await()
                        val peerAllowsReadReceipts = peerProfile.getBoolean("readReceiptsEnabled") ?: true
                        val peerReadAtMillis = snapshot
                            ?.getTimestamp("readAtByUser.$peerUserId")
                            ?.toDate()
                            ?.time
                            ?: 0L

                        trySend(
                            ReadReceiptState(
                                peerReadAtMillis = peerReadAtMillis,
                                canShowPeerReadReceipt = peerAllowsReadReceipts
                            )
                        )
                    }.onFailure {
                        trySend(ReadReceiptState())
                    }
                }
            }

        awaitClose { registration.remove() }
    }

    override suspend fun getOrCreateDirectConversation(
        currentUserId: String,
        otherUserId: String
    ): ConversationResult {
        if (currentUserId == otherUserId) {
            return ConversationResult.Error("You cannot open a chat with yourself.")
        }

        if (safetyRepository.hasBlockBetween(currentUserId, otherUserId)) {
            return ConversationResult.Error("This chat is not available.")
        }

        val participantIds = listOf(currentUserId, otherUserId).sorted()
        val conversationId = participantIds.joinToString(separator = DIRECT_ID_SEPARATOR)
        val conversationDocument = firestore.collection(CONVERSATIONS_COLLECTION)
            .document(conversationId)

        return runCatching {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(conversationDocument)

                if (!snapshot.exists()) {
                    val conversationData = mapOf(
                        "type" to DIRECT_TYPE,
                        "participantIds" to participantIds,
                        "participantKey" to conversationId,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )

                    transaction.set(conversationDocument, conversationData)
                } else {
                    transaction.update(
                        conversationDocument,
                        "updatedAt",
                        FieldValue.serverTimestamp()
                    )
                }

                conversationId
            }.await()
        }.fold(
            onSuccess = { id -> ConversationResult.Success(id) },
            onFailure = {
                ConversationResult.Error("We could not open this chat. Please try again.")
            }
        )
    }

    override suspend fun createGroupConversation(
        currentUserId: String,
        title: String,
        memberUserIds: List<String>
    ): ConversationResult {
        GroupConversationValidator.error(currentUserId, title, memberUserIds)?.let { message ->
            return ConversationResult.Error(message)
        }

        val participantIds = (memberUserIds + currentUserId).distinct()
        return runCatching {
            val document = firestore.collection(CONVERSATIONS_COLLECTION).document()
            document.set(
                mapOf(
                    "type" to ConversationType.Group.firestoreValue,
                    "title" to title.trim(),
                    "photoUrl" to "",
                    "participantIds" to participantIds,
                    "adminIds" to listOf(currentUserId),
                    "createdBy" to currentUserId,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            document.id
        }.fold(
            onSuccess = { ConversationResult.Success(it) },
            onFailure = { ConversationResult.Error("We could not create this group. Please try again.") }
        )
    }

    override suspend fun markConversationRead(
        conversationId: String,
        userId: String
    ) {
        runCatching {
            firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .update("readAtByUser.$userId", FieldValue.serverTimestamp())
                .await()
        }
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
        return runCatching {
            firestore.collection(CONTACTS_COLLECTION)
                .document(ownerUserId)
                .collection(CONTACT_ITEMS_COLLECTION)
                .document(viewerUserId)
                .get()
                .await()
                .exists()
        }.getOrDefault(false)
    }

    private companion object {
        const val CONVERSATIONS_COLLECTION = "conversations"
        const val USERS_COLLECTION = "users"
        const val CONTACTS_COLLECTION = "contacts"
        const val CONTACT_ITEMS_COLLECTION = "items"
        const val DIRECT_TYPE = "direct"
        const val DIRECT_ID_SEPARATOR = "__"
    }
}
