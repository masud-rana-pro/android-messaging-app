package com.contactme.app.typing

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseTypingRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : TypingRepository {
    override fun observeOtherTyping(
        conversationId: String,
        currentUserId: String
    ): Flow<Boolean> = callbackFlow {
        val registration = firestore.collection(CONVERSATIONS_COLLECTION)
            .document(conversationId)
            .collection(TYPING_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(false)
                    return@addSnapshotListener
                }

                val now = System.currentTimeMillis()
                val isOtherUserTyping = snapshot?.documents.orEmpty().any { document ->
                    val userId = document.getString("userId").orEmpty()
                    val isTyping = document.getBoolean("isTyping") ?: false
                    val updatedAtMillis = document.getTimestamp("updatedAt")?.toDate()?.time
                    val isFresh = updatedAtMillis == null || now - updatedAtMillis <= TYPING_STALE_AFTER_MILLIS

                    userId != currentUserId && isTyping && isFresh
                }

                trySend(isOtherUserTyping)
            }

        awaitClose { registration.remove() }
    }

    override suspend fun setTyping(
        conversationId: String,
        userId: String,
        isTyping: Boolean
    ) {
        runCatching {
            firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
                .collection(TYPING_COLLECTION)
                .document(userId)
                .set(
                    mapOf(
                        "userId" to userId,
                        "isTyping" to isTyping,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
        }
    }

    private companion object {
        const val CONVERSATIONS_COLLECTION = "conversations"
        const val TYPING_COLLECTION = "typing"
        const val TYPING_STALE_AFTER_MILLIS = 15_000L
    }
}
