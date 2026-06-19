package com.contactme.app.call

data class CallToken(
    val appId: Long,
    val token: String,
    val roomId: String,
    val expiresAtSeconds: Long
)
