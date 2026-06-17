package com.contactme.app.message

import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseMessageRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
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
                        type = MessageType.fromFirestore(document.getString("type")),
                        text = document.getString("text").orEmpty(),
                        mediaUrl = document.getString("mediaUrl").orEmpty(),
                        sentAtMillis = document.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                        status = MessageStatus.fromFirestore(document.getString("status"))
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
            "type" to MessageType.Text.firestoreValue,
            "status" to MessageStatus.Sent.firestoreValue,
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

    override suspend fun sendImageMessage(
        conversationId: String,
        senderId: String,
        imageUri: Uri
    ): MessageResult {
        val conversationDocument = firestore.collection(CONVERSATIONS_COLLECTION)
            .document(conversationId)
        val messageDocument = conversationDocument
            .collection(MESSAGES_COLLECTION)
            .document()
        val mediaReference = storage.reference
            .child(CHAT_MEDIA_PATH)
            .child(conversationId)
            .child(messageDocument.id)
            .child(IMAGE_FILE_NAME)

        return runCatching {
            mediaReference.putFile(imageUri).await()
            val mediaUrl = mediaReference.downloadUrl.await().toString()
            val messageData = mapOf(
                "senderId" to senderId,
                "text" to "",
                "type" to MessageType.Image.firestoreValue,
                "mediaUrl" to mediaUrl,
                "status" to MessageStatus.Sent.firestoreValue,
                "createdAt" to FieldValue.serverTimestamp()
            )

            firestore.runBatch { batch ->
                batch.set(messageDocument, messageData)
                batch.update(
                    conversationDocument,
                    mapOf(
                        "lastMessageText" to IMAGE_LAST_MESSAGE,
                        "lastMessageSenderId" to senderId,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
        }.fold(
            onSuccess = { MessageResult.Success },
            onFailure = {
                MessageResult.Error("We could not send this photo. Please try again.")
            }
        )
    }

    private companion object {
        const val CONVERSATIONS_COLLECTION = "conversations"
        const val MESSAGES_COLLECTION = "messages"
        const val CHAT_MEDIA_PATH = "chat_media"
        const val IMAGE_FILE_NAME = "image.jpg"
        const val IMAGE_LAST_MESSAGE = "Photo"
    }
}
