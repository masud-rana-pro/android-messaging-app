package com.contactme.app.call

interface CallTokenRepository {
    suspend fun issueToken(callId: String): CallTokenResult
}
