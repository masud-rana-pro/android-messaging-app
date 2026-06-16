package com.contactme.app.presence

data class PresenceStatus(
    val isOnline: Boolean = false,
    val lastSeenAtMillis: Long = 0L
)
