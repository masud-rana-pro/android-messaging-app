package com.contactme.app.call

import com.google.firebase.functions.FirebaseFunctions
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class FirebaseCallTokenRepository @Inject constructor(
    private val firebaseFunctions: FirebaseFunctions
) : CallTokenRepository {
    override suspend fun issueToken(callId: String): CallTokenResult {
        if (!CALL_ID_PATTERN.matches(callId)) {
            return CallTokenResult.Error("This call is invalid.")
        }

        return runCatching {
            val response = firebaseFunctions
                .getHttpsCallable(ISSUE_TOKEN_FUNCTION)
                .call(mapOf("callId" to callId))
                .await()
            val data = response.data as? Map<*, *> ?: error("Missing token response")
            val appId = (data["appId"] as? Number)?.toLong() ?: error("Missing app id")
            val token = data["token"] as? String ?: error("Missing token")
            val roomId = data["roomId"] as? String ?: error("Missing room id")
            val expiresAt = (data["expiresAtSeconds"] as? Number)?.toLong()
                ?: error("Missing expiry")
            check(appId > 0L && token.startsWith("04") && roomId.isNotBlank())
            CallToken(
                appId = appId,
                token = token,
                roomId = roomId,
                expiresAtSeconds = expiresAt
            )
        }.fold(
            onSuccess = { CallTokenResult.Success(it) },
            onFailure = { CallTokenResult.Error("Calling is unavailable right now. Please try again.") }
        )
    }

    private companion object {
        const val ISSUE_TOKEN_FUNCTION = "issueZegoCallToken"
        val CALL_ID_PATTERN = Regex("^[A-Za-z0-9_-]{8,128}$")
    }
}
