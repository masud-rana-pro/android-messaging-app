package com.contactme.app.call

data class CallSession(
    val callId: String,
    val callerId: String,
    val receiverId: String,
    val type: CallType,
    val status: CallStatus,
    val offer: String = "",
    val answer: String = "",
    val createdAtMillis: Long = 0L,
    val acceptedAtMillis: Long = 0L,
    val endedAtMillis: Long = 0L
)
