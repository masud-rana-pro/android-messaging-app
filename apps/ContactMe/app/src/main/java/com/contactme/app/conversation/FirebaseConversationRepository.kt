package com.contactme.app.conversation

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseConversationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
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
                    val previews = conversations.mapNotNull { document ->
                        val participantIds = document.get("participantIds") as? List<*> ?: return@mapNotNull null
                        val otherUserId = participantIds
                            .filterIsInstance<String>()
                            .firstOrNull { userId -> userId != currentUserId }
                            ?: return@mapNotNull null

                        val otherUser = firestore.collection(USERS_COLLECTION)
                            .document(otherUserId)
                            .get()
                            .await()

                        ConversationPreview(
                            conversationId = document.id,
                            otherUserId = otherUserId,
                            title = otherUser.getString("displayName").orEmpty().ifBlank {
                                otherUser.getString("username").orEmpty().ifBlank { "ContactMe User" }
                            },
                            subtitle = document.getString("lastMessageText").orEmpty().ifBlank {
                                "No messages yet."
                            },
                            updatedAtMillis = document.getTimestamp("updatedAt")?.toDate()?.time ?: 0L
                        )
                    }

                    trySend(previews.sortedByDescending { preview -> preview.updatedAtMillis })
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

    private companion object {
        const val CONVERSATIONS_COLLECTION = "conversations"
        const val USERS_COLLECTION = "users"
        const val DIRECT_TYPE = "direct"
        const val DIRECT_ID_SEPARATOR = "__"
    }
}
