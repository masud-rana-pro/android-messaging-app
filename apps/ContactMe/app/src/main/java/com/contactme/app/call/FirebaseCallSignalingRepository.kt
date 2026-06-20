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
    override fun observeCall(callId: String): Flow<CallSession?> = callbackFlow {
        val registration = calls().document(callId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.takeIf { it.exists() }?.toCallSession())
        }
        awaitClose { registration.remove() }
    }

    override fun observeRemoteIceCandidates(
        callId: String,
        currentUserId: String
    ): Flow<List<CallIceCandidate>> = callbackFlow {
        val callReference = calls().document(callId)
        val call = callReference.get().await()
        val collection = when (currentUserId) {
            call.getString(CALLER_ID) -> RECEIVER_CANDIDATES
            call.getString(RECEIVER_ID) -> CALLER_CANDIDATES
            else -> {
                close(IllegalAccessException("User is not a call participant"))
                return@callbackFlow
            }
        }
        val registration = callReference.collection(collection)
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

    override suspend fun createCall(
        callerId: String,
        receiverId: String,
        type: CallType
    ): CallResult {
        if (callerId.isBlank() || receiverId.isBlank() || callerId == receiverId) {
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
                    OFFER to "",
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

    override suspend fun setOffer(
        callId: String,
        callerId: String,
        offer: String
    ): CallResult = updateField(callId, callerId, OFFER, offer)

    override suspend fun setAnswer(
        callId: String,
        receiverId: String,
        answer: String
    ): CallResult = updateField(callId, receiverId, ANSWER, answer)

    override suspend fun addIceCandidate(
        callId: String,
        currentUserId: String,
        candidate: CallIceCandidate
    ): CallResult {
        if (candidate.candidate.isBlank() || candidate.senderId != currentUserId) {
            return CallResult.Error("Invalid network candidate.")
        }
        return runCatching {
            val call = calls().document(callId).get().await()
            val collection = when (currentUserId) {
                call.getString(CALLER_ID) -> CALLER_CANDIDATES
                call.getString(RECEIVER_ID) -> RECEIVER_CANDIDATES
                else -> error("User is not a call participant")
            }
            call.reference.collection(collection).add(
                mapOf(
                    SENDER_ID to currentUserId,
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

    override suspend fun updateStatus(
        callId: String,
        currentUserId: String,
        status: CallStatus
    ): CallResult {
        if (callId.isBlank() || currentUserId.isBlank()) return CallResult.Error("Invalid call.")
        val updates = mutableMapOf<String, Any?>(STATUS to status.firestoreValue)
        if (status == CallStatus.Accepted) updates[ACCEPTED_AT] = FieldValue.serverTimestamp()
        if (status.isTerminal()) updates[ENDED_AT] = FieldValue.serverTimestamp()
        return runCatching { calls().document(callId).update(updates).await() }.fold(
            onSuccess = { CallResult.Success },
            onFailure = { CallResult.Error("Call status could not be updated.") }
        )
    }

    private suspend fun updateField(
        callId: String,
        currentUserId: String,
        field: String,
        value: String
    ): CallResult {
        if (callId.isBlank() || currentUserId.isBlank() || value.isBlank()) {
            return CallResult.Error("Invalid call signal.")
        }
        return runCatching { calls().document(callId).update(field, value).await() }.fold(
            onSuccess = { CallResult.Success },
            onFailure = { CallResult.Error("Call negotiation could not continue.") }
        )
    }

    private fun calls() = firestore.collection(CALLS)

    private fun DocumentSnapshot.toCallSession() = CallSession(
        id = id,
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

    private fun CallStatus.isTerminal() = this in setOf(
        CallStatus.Rejected,
        CallStatus.Ended,
        CallStatus.Missed,
        CallStatus.Cancelled,
        CallStatus.Timeout,
        CallStatus.Busy
    )

    private companion object {
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
