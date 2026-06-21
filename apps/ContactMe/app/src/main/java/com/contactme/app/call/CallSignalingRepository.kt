package com.contactme.app.call

import kotlinx.coroutines.flow.Flow

interface CallSignalingRepository {
    suspend fun createCallOffer(
        callerId: String,
        receiverId: String,
        type: CallType,
        offer: String
    ): CallResult

    fun listenForIncomingCalls(receiverId: String): Flow<List<CallSession>>

    fun listenToAllCalls(userId: String): Flow<List<CallSession>>

    fun listenToCall(callId: String): Flow<CallSession?>

    suspend fun acceptCallWithAnswer(
        callId: String,
        receiverId: String,
        answer: String
    ): CallResult

    suspend fun rejectCall(callId: String, receiverId: String): CallResult

    suspend fun endCall(callId: String, currentUserId: String): CallResult

    suspend fun addCallerIceCandidate(
        callId: String,
        callerId: String,
        candidate: CallIceCandidate
    ): CallResult

    suspend fun addReceiverIceCandidate(
        callId: String,
        receiverId: String,
        candidate: CallIceCandidate
    ): CallResult

    fun listenCallerIceCandidates(callId: String): Flow<List<CallIceCandidate>>

    fun listenReceiverIceCandidates(callId: String): Flow<List<CallIceCandidate>>
}
