package com.contactme.app.message

enum class MessageStatus(val firestoreValue: String) {
    Sent("sent");

    companion object {
        fun fromFirestore(value: String?): MessageStatus {
            return entries.firstOrNull { status -> status.firestoreValue == value } ?: Sent
        }
    }
}
