package com.contactme.app.message

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseMessageRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : MessageRepository {
    override fun observeMessages(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        val registration = firestore.collection(CONVERSATIONS_COLLECTION)
            .document(conversationId)
            .collection(MESSAGES_COLLECTION)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents.orEmpty().map { document ->
                    ChatMessage(
                        id = document.id,
                        senderId = document.getString("senderId").orEmpty(),
                        text = document.getString("text").orEmpty(),
                        sentAtMillis = document.getTimestamp("createdAt")?.toDate()?.time ?: 0L
                    )
                }

                trySend(messages)
            }

        awaitClose { registration.remove() }
    }

    override suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        text: String
    ): MessageResult {
        val trimmedText = text.trim()

        if (trimmedText.isBlank()) {
            return MessageResult.Error("Type a message first.")
        }

        val conversationDocument = firestore.collection(CONVERSATIONS_COLLECTION)
            .document(conversationId)

        val messageData = mapOf(
            "senderId" to senderId,
            "text" to trimmedText,
            "type" to "text",
            "createdAt" to FieldValue.serverTimestamp()
        )

        return runCatching {
            val messageDocument = conversationDocument
                .collection(MESSAGES_COLLECTION)
                .document()

            firestore.runBatch { batch ->
                batch.set(messageDocument, messageData)
                batch.update(
                    conversationDocument,
                    mapOf(
                        "lastMessageText" to trimmedText,
                        "lastMessageSenderId" to senderId,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
        }.fold(
            onSuccess = { MessageResult.Success },
            onFailure = {
                MessageResult.Error("We could not send this message. Please try again.")
            }
        )
    }

    private companion object {
        const val CONVERSATIONS_COLLECTION = "conversations"
        const val MESSAGES_COLLECTION = "messages"
    }
}
