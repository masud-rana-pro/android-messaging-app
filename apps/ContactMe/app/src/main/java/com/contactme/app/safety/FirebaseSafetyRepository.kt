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
        if (currentUserId.isBlank() || blockedUserId.isBlank()) {
            return SafetyResult.Error("We could not update this setting. Please try again.")
        }

        if (currentUserId == blockedUserId) {
            return SafetyResult.Error("You cannot block yourself.")
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
        if (currentUserId.isBlank() || blockedUserId.isBlank()) {
            return SafetyResult.Error("We could not update this setting. Please try again.")
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
        if (reporterUserId.isBlank() || reportedUserId.isBlank()) {
            return SafetyResult.Error("We could not send this report. Please try again.")
        }

        if (reporterUserId == reportedUserId) {
            return SafetyResult.Error("You cannot report yourself.")
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

    private companion object {
        const val BLOCKED_USERS_COLLECTION = "blocked_users"
        const val ITEMS_COLLECTION = "items"
        const val REPORTS_COLLECTION = "reports"
        const val OPEN_STATUS = "open"
    }
}
