package com.contactme.app.presence

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.contactme.app.profile.PrivacyVisibility
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebasePresenceRepository @Inject constructor(
    private val database: FirebaseDatabase,
    private val firestore: FirebaseFirestore
) : PresenceRepository {
    override fun observeConversationPeerPresence(
        conversationId: String,
        currentUserId: String
    ): Flow<PresenceStatus> = callbackFlow {
        var presenceReference: DatabaseReference? = null
        var presenceListener: ValueEventListener? = null
        var canShowLastSeen = true

        fun clearPresenceListener() {
            val reference = presenceReference
            val listener = presenceListener

            if (reference != null && listener != null) {
                reference.removeEventListener(listener)
            }

            presenceReference = null
            presenceListener = null
        }

        val conversationRegistration = firestore.collection(CONVERSATIONS_COLLECTION)
            .document(conversationId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(PresenceStatus())
                    return@addSnapshotListener
                }

                val participantIds = snapshot
                    ?.get("participantIds") as? List<*>
                val peerUserId = participantIds
                    .orEmpty()
                    .filterIsInstance<String>()
                    .firstOrNull { userId -> userId != currentUserId }

                clearPresenceListener()

                if (peerUserId == null) {
                    trySend(PresenceStatus())
                    return@addSnapshotListener
                }

                launch {
                    canShowLastSeen = canShowPeerLastSeen(peerUserId)

                    val nextReference = database.reference
                        .child(PRESENCE_PATH)
                        .child(peerUserId)
                    val nextListener = object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            trySend(
                                PresenceStatus(
                                    isOnline = snapshot.child("isOnline").getValue(Boolean::class.java) ?: false,
                                    lastSeenAtMillis = snapshot.child("lastSeenAt").getValue(Long::class.java) ?: 0L,
                                    canShowLastSeen = canShowLastSeen
                                )
                            )
                        }

                        override fun onCancelled(error: DatabaseError) {
                            trySend(PresenceStatus())
                        }
                    }

                    presenceReference = nextReference
                    presenceListener = nextListener
                    nextReference.addValueEventListener(nextListener)
                }
            }

        awaitClose {
            conversationRegistration.remove()
            clearPresenceListener()
        }
    }

    private suspend fun canShowPeerLastSeen(peerUserId: String): Boolean {
        return runCatching {
            val visibility = firestore.collection(USERS_COLLECTION)
                .document(peerUserId)
                .get()
                .await()
                .getString("lastSeenVisibility")

            PrivacyVisibility.fromFirestore(visibility) != PrivacyVisibility.Nobody
        }.getOrDefault(true)
    }

    override suspend fun markOnline(userId: String) {
        val presenceReference = database.reference
            .child(PRESENCE_PATH)
            .child(userId)

        val offlineState = mapOf(
            "isOnline" to false,
            "lastSeenAt" to ServerValue.TIMESTAMP
        )
        val onlineState = mapOf(
            "isOnline" to true,
            "lastSeenAt" to ServerValue.TIMESTAMP
        )

        runCatching {
            presenceReference.onDisconnect().setValue(offlineState).await()
            presenceReference.setValue(onlineState).await()
        }
    }

    override suspend fun markOffline(userId: String) {
        val offlineState = mapOf(
            "isOnline" to false,
            "lastSeenAt" to ServerValue.TIMESTAMP
        )

        runCatching {
            database.reference
                .child(PRESENCE_PATH)
                .child(userId)
                .setValue(offlineState)
                .await()
        }
    }

    private companion object {
        const val CONVERSATIONS_COLLECTION = "conversations"
        const val USERS_COLLECTION = "users"
        const val PRESENCE_PATH = "presence"
    }
}
