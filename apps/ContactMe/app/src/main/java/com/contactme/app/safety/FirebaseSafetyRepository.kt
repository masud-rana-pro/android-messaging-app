package com.contactme.app.safety

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class FirebaseSafetyRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : SafetyRepository {
    override suspend fun blockUser(
        currentUserId: String,
        blockedUserId: String
    ): SafetyResult {
        SafetyInputValidator.blockError(currentUserId, blockedUserId)?.let { message ->
            return SafetyResult.Error(message)
        }

        return runCatching {
            firestore.collection(BLOCKED_USERS_COLLECTION)
                .document(currentUserId)
                .collection(ITEMS_COLLECTION)
                .document(blockedUserId)
                .set(
                    mapOf(
                        "userId" to blockedUserId,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
        }.fold(
            onSuccess = { SafetyResult.Success },
            onFailure = { SafetyResult.Error("We could not block this user. Please try again.") }
        )
    }

    override suspend fun unblockUser(
        currentUserId: String,
        blockedUserId: String
    ): SafetyResult {
        SafetyInputValidator.unblockError(currentUserId, blockedUserId)?.let { message ->
            return SafetyResult.Error(message)
        }

        return runCatching {
            firestore.collection(BLOCKED_USERS_COLLECTION)
                .document(currentUserId)
                .collection(ITEMS_COLLECTION)
                .document(blockedUserId)
                .delete()
                .await()
        }.fold(
            onSuccess = { SafetyResult.Success },
            onFailure = { SafetyResult.Error("We could not unblock this user. Please try again.") }
        )
    }

    override suspend fun reportUser(
        reporterUserId: String,
        reportedUserId: String,
        conversationId: String,
        reason: ReportReason
    ): SafetyResult {
        SafetyInputValidator.reportError(reporterUserId, reportedUserId)?.let { message ->
            return SafetyResult.Error(message)
        }

        return runCatching {
            firestore.collection(REPORTS_COLLECTION)
                .document()
                .set(
                    mapOf(
                        "reporterUserId" to reporterUserId,
                        "reportedUserId" to reportedUserId,
                        "conversationId" to conversationId,
                        "reason" to reason.firestoreValue,
                        "status" to OPEN_STATUS,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
        }.fold(
            onSuccess = { SafetyResult.Success },
            onFailure = { SafetyResult.Error("We could not send this report. Please try again.") }
        )
    }

    override suspend fun blockConversationPeer(
        currentUserId: String,
        conversationId: String
    ): SafetyResult {
        val peerUserId = peerUserIdForConversation(
            currentUserId = currentUserId,
            conversationId = conversationId
        ) ?: return SafetyResult.Error("We could not update this chat. Please try again.")

        return blockUser(
            currentUserId = currentUserId,
            blockedUserId = peerUserId
        )
    }

    override suspend fun unblockConversationPeer(
        currentUserId: String,
        conversationId: String
    ): SafetyResult {
        val peerUserId = peerUserIdForConversation(
            currentUserId = currentUserId,
            conversationId = conversationId
        ) ?: return SafetyResult.Error("We could not update this chat. Please try again.")

        return unblockUser(
            currentUserId = currentUserId,
            blockedUserId = peerUserId
        )
    }

    override suspend fun reportConversationPeer(
        reporterUserId: String,
        conversationId: String,
        reason: ReportReason
    ): SafetyResult {
        val peerUserId = peerUserIdForConversation(
            currentUserId = reporterUserId,
            conversationId = conversationId
        ) ?: return SafetyResult.Error("We could not send this report. Please try again.")

        return reportUser(
            reporterUserId = reporterUserId,
            reportedUserId = peerUserId,
            conversationId = conversationId,
            reason = reason
        )
    }

    override suspend fun hasBlockBetween(
        currentUserId: String,
        otherUserId: String
    ): Boolean {
        if (currentUserId.isBlank() || otherUserId.isBlank()) return false

        return runCatching {
            val currentUserBlockedOther = firestore.collection(BLOCKED_USERS_COLLECTION)
                .document(currentUserId)
                .collection(ITEMS_COLLECTION)
                .document(otherUserId)
                .get()
                .await()
                .exists()
            val otherUserBlockedCurrent = firestore.collection(BLOCKED_USERS_COLLECTION)
                .document(otherUserId)
                .collection(ITEMS_COLLECTION)
                .document(currentUserId)
                .get()
                .await()
                .exists()

            currentUserBlockedOther || otherUserBlockedCurrent
        }.getOrDefault(false)
    }

    override suspend fun hasCurrentUserBlockedConversationPeer(
        currentUserId: String,
        conversationId: String
    ): Boolean {
        val peerUserId = peerUserIdForConversation(
            currentUserId = currentUserId,
            conversationId = conversationId
        ) ?: return false

        return hasCurrentUserBlocked(
            currentUserId = currentUserId,
            blockedUserId = peerUserId
        )
    }

    override suspend fun hasBlockInConversation(
        currentUserId: String,
        conversationId: String
    ): Boolean {
        val peerUserId = peerUserIdForConversation(
            currentUserId = currentUserId,
            conversationId = conversationId
        ) ?: return false

        return hasBlockBetween(
            currentUserId = currentUserId,
            otherUserId = peerUserId
        )
    }

    private companion object {
        const val CONVERSATIONS_COLLECTION = "conversations"
        const val BLOCKED_USERS_COLLECTION = "blocked_users"
        const val ITEMS_COLLECTION = "items"
        const val REPORTS_COLLECTION = "reports"
        const val OPEN_STATUS = "open"
    }

    private suspend fun peerUserIdForConversation(
        currentUserId: String,
        conversationId: String
    ): String? {
        val participantIds = firestore.collection(CONVERSATIONS_COLLECTION)
            .document(conversationId)
            .get()
            .await()
            .get("participantIds") as? List<*>

        return participantIds
            .orEmpty()
            .filterIsInstance<String>()
            .firstOrNull { userId -> userId != currentUserId }
    }

    private suspend fun hasCurrentUserBlocked(
        currentUserId: String,
        blockedUserId: String
    ): Boolean {
        return runCatching {
            firestore.collection(BLOCKED_USERS_COLLECTION)
                .document(currentUserId)
                .collection(ITEMS_COLLECTION)
                .document(blockedUserId)
                .get()
                .await()
                .exists()
        }.getOrDefault(false)
    }
}
