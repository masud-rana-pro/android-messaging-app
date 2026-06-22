package com.contactme.app.message

import android.net.Uri
import android.util.Log
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

class FirebaseMessageRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val cloudinaryUploadClient: CloudinaryUploadClient,
    private val safetyRepository: SafetyRepository
) : MessageRepository {
    private val senderNameCache = ConcurrentHashMap<String, String>()

    override fun observeMessages(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        val registration = firestore.collection(CONVERSATIONS_COLLECTION)
            .document(conversationId)
            .collection(MESSAGES_COLLECTION)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                launch {
                    runCatching {
                        val documents = snapshot?.documents.orEmpty()
                        val senderIds = documents.mapNotNull { it.getString("senderId") }
                            .filter(String::isNotBlank)
                            .distinct()
                        resolveSenderNames(senderIds)

                        val messages = documents.map { document ->
                            val senderId = document.getString("senderId").orEmpty()
                            ChatMessage(
                                id = document.id,
                                senderId = senderId,
                                senderDisplayName = senderNameCache[senderId].orEmpty(),
                                type = MessageType.fromFirestore(document.getString("type")),
                                text = document.getString("text").orEmpty(),
                                mediaUrl = document.getString("mediaUrl").orEmpty(),
                                mediaProvider = document.getString("mediaProvider").orEmpty(),
                                mediaPublicId = document.getString("mediaPublicId").orEmpty(),
                                mimeType = document.getString("mimeType").orEmpty(),
                                fileName = document.getString("fileName").orEmpty(),
                                fileSizeBytes = document.getLong("fileSizeBytes") ?: 0L,
                                durationMillis = document.getLong("durationMillis") ?: 0L,
                                replyTo = document.getString("replyToMessageId")
                                    ?.takeIf(String::isNotBlank)
                                    ?.let { replyMessageId ->
                                        MessageReply(
                                            messageId = replyMessageId,
                                            senderName = document.getString("replyToSenderName").orEmpty(),
                                            preview = document.getString("replyPreview").orEmpty(),
                                            type = MessageType.fromFirestore(document.getString("replyType"))
                                        )
                                    },
                                isDeleted = document.getBoolean("isDeleted") ?: false,
                                editedAtMillis = document.getTimestamp("editedAt")?.toDate()?.time ?: 0L,
                                sentAtMillis = document.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                                status = MessageStatus.fromFirestore(document.getString("status"))
                            )
                        }

                        trySend(messages)
                    }.onFailure {
                        trySend(emptyList())
                    }
                }
            }

        awaitClose { registration.remove() }
    }

    override suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        text: String,
        replyTo: MessageReply?
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

        val messageData = mutableMapOf<String, Any>(
            "senderId" to senderId,
            "text" to trimmedText,
            "type" to MessageType.Text.firestoreValue,
            "status" to MessageStatus.Sent.firestoreValue,
            "createdAt" to FieldValue.serverTimestamp()
        )
        replyTo?.let { reply ->
            messageData["replyToMessageId"] = reply.messageId
            messageData["replyToSenderName"] = reply.senderName
            messageData["replyPreview"] = reply.preview
            messageData["replyType"] = reply.type.firestoreValue
        }

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
                        "lastMessageId" to messageDocument.id,
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
        Log.d(TAG, "sendImageMessage start for conversation: $conversationId")
        val conversationDocument = firestore.collection(CONVERSATIONS_COLLECTION)
            .document(conversationId)
        val messageDocument = conversationDocument
            .collection(MESSAGES_COLLECTION)
            .document()

        if (isBlockedConversation(conversationId, senderId)) {
            Log.w(TAG, "sendImageMessage failed: blocked conversation")
            return MessageResult.Error("This chat is not available.")
        }

        return runCatching {
            Log.d(TAG, "Uploading image to Cloudinary...")
            val uploadResult = cloudinaryUploadClient.upload(
                uri = imageUri,
                fileName = IMAGE_FILE_NAME
            )
            Log.d(TAG, "Image upload successful. URL: ${uploadResult.secureUrl}")

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

            Log.d(TAG, "Creating Firestore message...")
            firestore.runBatch { batch ->
                batch.set(messageDocument, messageData)
                batch.update(
                    conversationDocument,
                    mapOf(
                        "lastMessageText" to IMAGE_LAST_MESSAGE,
                        "lastMessageId" to messageDocument.id,
                        "lastMessageSenderId" to senderId,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Log.d(TAG, "Firestore message created successfully")
        }.fold(
            onSuccess = { MessageResult.Success },
            onFailure = { error ->
                Log.e(TAG, "sendImageMessage failed", error)
                MessageResult.Error(
                    (error as? MediaUploadException)?.userMessage
                        ?: "We could not send this photo. Please try again."
                )
            }
        )
    }

    override suspend fun sendDocumentMessage(
        conversationId: String,
        senderId: String,
        documentUri: Uri,
        fileName: String,
        mimeType: String,
        fileSizeBytes: Long
    ): MessageResult {
        Log.d(TAG, "sendDocumentMessage start for: $fileName")
        if (isBlockedConversation(conversationId, senderId)) {
            return MessageResult.Error("This chat is not available.")
        }

        return runCatching {
            Log.d(TAG, "Uploading document to Cloudinary...")
            val upload = cloudinaryUploadClient.uploadDocument(documentUri, fileName, mimeType)
            Log.d(TAG, "Document upload successful")
            val conversationDocument = firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
            val messageDocument = conversationDocument.collection(MESSAGES_COLLECTION).document()
            val messageData = mapOf(
                "senderId" to senderId,
                "text" to "",
                "type" to MessageType.Document.firestoreValue,
                "mediaProvider" to CLOUDINARY_PROVIDER,
                "mediaUrl" to upload.secureUrl,
                "mediaPublicId" to upload.publicId,
                "mimeType" to upload.mimeType,
                "fileName" to fileName,
                "fileSizeBytes" to fileSizeBytes,
                "status" to MessageStatus.Sent.firestoreValue,
                "createdAt" to FieldValue.serverTimestamp()
            )
            Log.d(TAG, "Creating Firestore document message...")
            firestore.runBatch { batch ->
                batch.set(messageDocument, messageData)
                batch.update(
                    conversationDocument,
                    mapOf(
                        "lastMessageText" to fileName,
                        "lastMessageId" to messageDocument.id,
                        "lastMessageSenderId" to senderId,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Log.d(TAG, "Firestore document message created")
        }.fold(
            onSuccess = { MessageResult.Success },
            onFailure = { error ->
                Log.e(TAG, "sendDocumentMessage failed", error)
                MessageResult.Error(
                    (error as? MediaUploadException)?.userMessage
                        ?: "We could not send this document. Please try again."
                )
            }
        )
    }

    override suspend fun sendVoiceMessage(
        conversationId: String,
        senderId: String,
        audioUri: Uri,
        durationMillis: Long,
        fileSizeBytes: Long
    ): MessageResult {
        Log.d(TAG, "sendVoiceMessage start for conversation: $conversationId")
        if (isBlockedConversation(conversationId, senderId)) {
            return MessageResult.Error("This chat is not available.")
        }

        return runCatching {
            val fileName = "voice_message.m4a"
            Log.d(TAG, "Uploading voice message to Cloudinary...")
            val upload = cloudinaryUploadClient.uploadDocument(audioUri, fileName, "audio/mp4")
            Log.d(TAG, "Voice upload successful")

            val conversationDocument = firestore.collection(CONVERSATIONS_COLLECTION)
                .document(conversationId)
            val messageDocument = conversationDocument.collection(MESSAGES_COLLECTION).document()
            val messageData = mapOf(
                "senderId" to senderId,
                "text" to "",
                "type" to MessageType.Voice.firestoreValue,
                "mediaProvider" to CLOUDINARY_PROVIDER,
                "mediaUrl" to upload.secureUrl,
                "mediaPublicId" to upload.publicId,
                "mimeType" to upload.mimeType,
                "fileName" to fileName,
                "fileSizeBytes" to fileSizeBytes,
                "durationMillis" to durationMillis,
                "status" to MessageStatus.Sent.firestoreValue,
                "createdAt" to FieldValue.serverTimestamp()
            )
            Log.d(TAG, "Creating Firestore voice message...")
            firestore.runBatch { batch ->
                batch.set(messageDocument, messageData)
                batch.update(
                    conversationDocument,
                    mapOf(
                        "lastMessageText" to "Voice message",
                        "lastMessageId" to messageDocument.id,
                        "lastMessageSenderId" to senderId,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Log.d(TAG, "Firestore voice message created")
        }.fold(
            onSuccess = { MessageResult.Success },
            onFailure = { error ->
                Log.e(TAG, "sendVoiceMessage failed", error)
                MessageResult.Error(
                    (error as? MediaUploadException)?.userMessage
                        ?: "We could not send this voice message. Please try again."
                )
            }
        )
    }

    override suspend fun deleteMessage(
        conversationId: String,
        messageId: String,
        currentUserId: String
    ): MessageResult {
        if (conversationId.isBlank() || messageId.isBlank() || currentUserId.isBlank()) {
            return MessageResult.Error("We could not delete this message.")
        }

        return runCatching {
            val conversation = firestore.collection(CONVERSATIONS_COLLECTION).document(conversationId)
            val message = conversation.collection(MESSAGES_COLLECTION).document(messageId)
            firestore.runTransaction { transaction ->
                val messageSnapshot = transaction.get(message)
                val conversationSnapshot = transaction.get(conversation)
                check(messageSnapshot.getString("senderId") == currentUserId)
                transaction.update(
                    message,
                    mapOf(
                        "isDeleted" to true,
                        "deletedAt" to FieldValue.serverTimestamp(),
                        "text" to "",
                        "mediaUrl" to "",
                        "mediaPublicId" to "",
                        "mediaProvider" to "",
                        "mimeType" to "",
                        "fileName" to "",
                        "fileSizeBytes" to 0L,
                        "durationMillis" to 0L
                    )
                )
                if (conversationSnapshot.getString("lastMessageId") == messageId) {
                    transaction.update(conversation, "lastMessageText", DELETED_LAST_MESSAGE)
                }
            }.await()
        }.fold(
            onSuccess = { MessageResult.Success },
            onFailure = { MessageResult.Error("We could not delete this message. Please try again.") }
        )
    }

    override suspend fun editMessage(
        conversationId: String,
        messageId: String,
        currentUserId: String,
        text: String
    ): MessageResult {
        val trimmedText = text.trim()
        if (conversationId.isBlank() || messageId.isBlank() || currentUserId.isBlank() ||
            trimmedText.isBlank() || trimmedText.length > 4000
        ) {
            return MessageResult.Error("Enter a valid message.")
        }

        return runCatching {
            val conversation = firestore.collection(CONVERSATIONS_COLLECTION).document(conversationId)
            val message = conversation.collection(MESSAGES_COLLECTION).document(messageId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(message)
                val conversationSnapshot = transaction.get(conversation)
                check(snapshot.getString("senderId") == currentUserId)
                check(snapshot.getString("type") == MessageType.Text.firestoreValue)
                check(snapshot.getBoolean("isDeleted") != true)
                transaction.update(
                    message,
                    mapOf("text" to trimmedText, "editedAt" to FieldValue.serverTimestamp())
                )
                if (conversationSnapshot.getString("lastMessageId") == messageId) {
                    transaction.update(conversation, "lastMessageText", trimmedText)
                }
            }.await()
        }.fold(
            onSuccess = { MessageResult.Success },
            onFailure = { MessageResult.Error("We could not edit this message. Please try again.") }
        )
    }

    private companion object {
        const val TAG = "FirebaseMessageRepo"
        const val CONVERSATIONS_COLLECTION = "conversations"
        const val MESSAGES_COLLECTION = "messages"
        const val USERS_COLLECTION = "users"
        const val IMAGE_FILE_NAME = "image.jpg"
        const val IMAGE_LAST_MESSAGE = "Photo"
        const val CLOUDINARY_PROVIDER = "cloudinary"
        const val DELETED_LAST_MESSAGE = "Message deleted"
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

    private suspend fun resolveSenderNames(senderIds: List<String>) {
        senderIds.filterNot(senderNameCache::containsKey).forEach { senderId ->
            runCatching {
                firestore.collection(USERS_COLLECTION).document(senderId).get().await()
            }.getOrNull()?.let { profile ->
                senderNameCache[senderId] = profile.getString("displayName").orEmpty()
                    .ifBlank { profile.getString("username").orEmpty() }
                    .ifBlank { "Group member" }
            }
        }
    }
}
