package com.contactme.app.presence

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class FirebasePresenceRepository @Inject constructor(
    private val database: FirebaseDatabase
) : PresenceRepository {
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
        const val PRESENCE_PATH = "presence"
    }
}
