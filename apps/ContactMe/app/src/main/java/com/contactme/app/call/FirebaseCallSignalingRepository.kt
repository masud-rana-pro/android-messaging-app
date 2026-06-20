package com.contactme.app.call

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseCallSignalingRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : CallSignalingRepository {
    override suspend fun createCallOffer(
        callerId: String,
        receiverId: String,
        type: CallType,
        offer: String
    ): CallResult {
        if (callerId.isBlank() || receiverId.isBlank() || callerId == receiverId ||
            offer.isBlank() || offer.length > MAX_SDP_LENGTH
        ) {
            return CallResult.Error("This call cannot be started.")
        }
        return runCatching {
            val call = calls().document()
            call.set(
                mapOf(
                    CALLER_ID to callerId,
                    RECEIVER_ID to receiverId,
                    TYPE to type.firestoreValue,
                    STATUS to CallStatus.Ringing.firestoreValue,
                    OFFER to offer,
                    ANSWER to "",
                    CREATED_AT to FieldValue.serverTimestamp(),
                    ACCEPTED_AT to null,
                    ENDED_AT to null
                )
            ).await()
            call.id
        }.fold(
            onSuccess = { CallResult.Created(it) },
            onFailure = { CallResult.Error("We could not start this call.") }
        )
    }

    override fun listenForIncomingCalls(receiverId: String): Flow<List<CallSession>> =
        callbackFlow {
            val registration = calls()
                .whereEqualTo(RECEIVER_ID, receiverId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    trySend(
                        snapshot?.documents.orEmpty()
                            .map { it.toCallSession() }
                            .filter { it.status == CallStatus.Ringing }
                            .sortedByDescending(CallSession::createdAtMillis)
                    )
                }
            awaitClose { registration.remove() }
        }

    override fun listenToCall(callId: String): Flow<CallSession?> = callbackFlow {
        val registration = calls().document(callId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.takeIf { it.exists() }?.toCallSession())
        }
        awaitClose { registration.remove() }
    }

    override suspend fun acceptCallWithAnswer(
        callId: String,
        receiverId: String,
        answer: String
    ): CallResult {
        if (callId.isBlank() || receiverId.isBlank() ||
            answer.isBlank() || answer.length > MAX_SDP_LENGTH
        ) {
            return CallResult.Error("This call cannot be accepted.")
        }
        return updateCall(
            callId,
            mapOf(
                ANSWER to answer,
                STATUS to CallStatus.Accepted.firestoreValue,
                ACCEPTED_AT to FieldValue.serverTimestamp()
            ),
            "This call cannot be accepted."
        )
    }

    override suspend fun rejectCall(callId: String, receiverId: String): CallResult {
        if (callId.isBlank() || receiverId.isBlank()) return CallResult.Error("Invalid call.")
        return updateTerminalStatus(callId, CallStatus.Rejected)
    }

    override suspend fun endCall(callId: String, currentUserId: String): CallResult {
        if (callId.isBlank() || currentUserId.isBlank()) return CallResult.Error("Invalid call.")
        return updateTerminalStatus(callId, CallStatus.Ended)
    }

    override suspend fun addCallerIceCandidate(
        callId: String,
        callerId: String,
        candidate: CallIceCandidate
    ): CallResult = addIceCandidate(callId, callerId, candidate, CALLER_CANDIDATES)

    override suspend fun addReceiverIceCandidate(
        callId: String,
        receiverId: String,
        candidate: CallIceCandidate
    ): CallResult = addIceCandidate(callId, receiverId, candidate, RECEIVER_CANDIDATES)

    override fun listenCallerIceCandidates(callId: String): Flow<List<CallIceCandidate>> =
        listenIceCandidates(callId, CALLER_CANDIDATES)

    override fun listenReceiverIceCandidates(callId: String): Flow<List<CallIceCandidate>> =
        listenIceCandidates(callId, RECEIVER_CANDIDATES)

    private suspend fun addIceCandidate(
        callId: String,
        ownerId: String,
        candidate: CallIceCandidate,
        collection: String
    ): CallResult {
        if (callId.isBlank() || ownerId.isBlank() || candidate.senderId != ownerId ||
            candidate.candidate.isBlank()
        ) {
            return CallResult.Error("Invalid network candidate.")
        }
        return runCatching {
            calls().document(callId).collection(collection).add(
                mapOf(
                    SENDER_ID to ownerId,
                    SDP_MID to candidate.sdpMid,
                    SDP_MLINE_INDEX to candidate.sdpMLineIndex,
                    CANDIDATE to candidate.candidate,
                    CREATED_AT to FieldValue.serverTimestamp()
                )
            ).await()
        }.fold(
            onSuccess = { CallResult.Success },
            onFailure = { CallResult.Error("Network negotiation could not continue.") }
        )
    }

    private fun listenIceCandidates(
        callId: String,
        collection: String
    ): Flow<List<CallIceCandidate>> = callbackFlow {
        val registration = calls().document(callId).collection(collection)
            .orderBy(CREATED_AT, Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents.orEmpty().map { it.toIceCandidate() })
            }
        awaitClose { registration.remove() }
    }

    private suspend fun updateTerminalStatus(
        callId: String,
        status: CallStatus
    ): CallResult = updateCall(
        callId,
        mapOf(
            STATUS to status.firestoreValue,
            ENDED_AT to FieldValue.serverTimestamp()
        ),
        "Call status could not be updated."
    )

    private suspend fun updateCall(
        callId: String,
        updates: Map<String, Any>,
        errorMessage: String
    ): CallResult = runCatching {
        calls().document(callId).update(updates).await()
    }.fold(
        onSuccess = { CallResult.Success },
        onFailure = { CallResult.Error(errorMessage) }
    )

    private fun calls() = firestore.collection(CALLS)

    private fun DocumentSnapshot.toCallSession() = CallSession(
        callId = id,
        callerId = getString(CALLER_ID).orEmpty(),
        receiverId = getString(RECEIVER_ID).orEmpty(),
        type = CallType.fromFirestore(getString(TYPE)),
        status = CallStatus.fromFirestore(getString(STATUS)),
        offer = getString(OFFER).orEmpty(),
        answer = getString(ANSWER).orEmpty(),
        createdAtMillis = getTimestamp(CREATED_AT)?.toDate()?.time ?: 0L,
        acceptedAtMillis = getTimestamp(ACCEPTED_AT)?.toDate()?.time ?: 0L,
        endedAtMillis = getTimestamp(ENDED_AT)?.toDate()?.time ?: 0L
    )

    private fun DocumentSnapshot.toIceCandidate() = CallIceCandidate(
        id = id,
        senderId = getString(SENDER_ID).orEmpty(),
        sdpMid = getString(SDP_MID),
        sdpMLineIndex = (getLong(SDP_MLINE_INDEX) ?: 0L).toInt(),
        candidate = getString(CANDIDATE).orEmpty(),
        createdAtMillis = getTimestamp(CREATED_AT)?.toDate()?.time ?: 0L
    )

    private companion object {
        const val MAX_SDP_LENGTH = 65_536
        const val CALLS = "calls"
        const val CALLER_CANDIDATES = "callerCandidates"
        const val RECEIVER_CANDIDATES = "receiverCandidates"
        const val CALLER_ID = "callerId"
        const val RECEIVER_ID = "receiverId"
        const val TYPE = "type"
        const val STATUS = "status"
        const val OFFER = "offer"
        const val ANSWER = "answer"
        const val CREATED_AT = "createdAt"
        const val ACCEPTED_AT = "acceptedAt"
        const val ENDED_AT = "endedAt"
        const val SENDER_ID = "senderId"
        const val SDP_MID = "sdpMid"
        const val SDP_MLINE_INDEX = "sdpMLineIndex"
        const val CANDIDATE = "candidate"
    }
}
