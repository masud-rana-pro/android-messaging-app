package com.contactme.app.conversation

enum class ConversationType(val firestoreValue: String) {
    Direct("direct"),
    Group("group");

    companion object {
        fun fromFirestore(value: String?): ConversationType {
            return entries.firstOrNull { it.firestoreValue == value } ?: Direct
        }
    }
}
