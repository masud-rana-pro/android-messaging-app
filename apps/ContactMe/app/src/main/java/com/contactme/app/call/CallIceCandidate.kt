package com.contactme.app.call

data class CallIceCandidate(
    val id: String = "",
    val senderId: String,
    val sdpMid: String?,
    val sdpMLineIndex: Int,
    val candidate: String,
    val createdAtMillis: Long = 0L
)
