package com.contactme.app.message

import android.net.Uri
import com.contactme.app.media.CloudinaryUploadClient
import com.contactme.app.media.MediaUploadException
import com.contactme.app.safety.SafetyRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseMessageRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val cloudinaryUploadClient: CloudinaryUploadClient,
    private val safetyRepository: SafetyRepository
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
                        mediaProvider = document.getString("mediaProvider").orEmpty(),
                        mediaPublicId = document.getString("mediaPublicId").orEmpty(),
                        mimeType = document.getString("mimeType").orEmpty(),
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

        if (isBlockedConversation(conversationId, senderId)) {
            return MessageResult.Error("This chat is not available.")
        }

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

        if (isBlockedConversation(conversationId, senderId)) {
            return MessageResult.Error("This chat is not available.")
        }

        return runCatching {
            val uploadResult = cloudinaryUploadClient.upload(
                uri = imageUri,
                fileName = IMAGE_FILE_NAME
            )
            val messageData = mapOf(
                "senderId" to senderId,
                "text" to "",
                "type" to MessageType.Image.firestoreValue,
                "mediaProvider" to CLOUDINARY_PROVIDER,
                "mediaUrl" to uploadResult.secureUrl,
                "mediaPublicId" to uploadResult.publicId,
                "mimeType" to uploadResult.mimeType,
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
            onFailure = { error ->
                MessageResult.Error(
                    (error as? MediaUploadException)?.userMessage
                        ?: "We could not send this photo. Please try again."
                )
            }
        )
    }

    private companion object {
        const val CONVERSATIONS_COLLECTION = "conversations"
        const val MESSAGES_COLLECTION = "messages"
        const val IMAGE_FILE_NAME = "image.jpg"
        const val IMAGE_LAST_MESSAGE = "Photo"
        const val CLOUDINARY_PROVIDER = "cloudinary"
    }

    private suspend fun isBlockedConversation(
        conversationId: String,
        senderId: String
    ): Boolean {
        val conversation = firestore.collection(CONVERSATIONS_COLLECTION)
            .document(conversationId)
            .get()
            .await()
        if (conversation.getString("type") == "group") return false

        val participantIds = conversation.get("participantIds") as? List<*>
        val peerUserId = participantIds
            .orEmpty()
            .filterIsInstance<String>()
            .firstOrNull { userId -> userId != senderId }
            ?: return false

        return safetyRepository.hasBlockBetween(senderId, peerUserId)
    }
}
