package com.contactme.app.call

import kotlinx.coroutines.flow.Flow

interface CallSignalingRepository {
    fun observeCall(callId: String): Flow<CallSession?>

    fun observeRemoteIceCandidates(
        callId: String,
        currentUserId: String
    ): Flow<List<CallIceCandidate>>

    suspend fun createCall(
        callerId: String,
        receiverId: String,
        type: CallType
    ): CallResult

    suspend fun setOffer(callId: String, callerId: String, offer: String): CallResult

    suspend fun setAnswer(callId: String, receiverId: String, answer: String): CallResult

    suspend fun addIceCandidate(
        callId: String,
        currentUserId: String,
        candidate: CallIceCandidate
    ): CallResult

    suspend fun updateStatus(
        callId: String,
        currentUserId: String,
        status: CallStatus
    ): CallResult
}
