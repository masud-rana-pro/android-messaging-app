package com.contactme.app.safety

interface SafetyRepository {
    suspend fun blockUser(
        currentUserId: String,
        blockedUserId: String
    ): SafetyResult

    suspend fun unblockUser(
        currentUserId: String,
        blockedUserId: String
    ): SafetyResult

    suspend fun reportUser(
        reporterUserId: String,
        reportedUserId: String,
        conversationId: String,
        reason: ReportReason
    ): SafetyResult

    suspend fun blockConversationPeer(
        currentUserId: String,
        conversationId: String
    ): SafetyResult

    suspend fun reportConversationPeer(
        reporterUserId: String,
        conversationId: String,
        reason: ReportReason
    ): SafetyResult

    suspend fun hasBlockBetween(
        currentUserId: String,
        otherUserId: String
    ): Boolean
}
