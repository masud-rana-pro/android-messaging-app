package com.contactme.app.call

enum class CallType(val firestoreValue: String) {
    Audio("audio"),
    Video("video");

    companion object {
        fun fromFirestore(value: String?): CallType =
            entries.firstOrNull { it.firestoreValue == value } ?: Audio
    }
}
