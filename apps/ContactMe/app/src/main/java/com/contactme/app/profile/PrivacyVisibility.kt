package com.contactme.app.profile

enum class PrivacyVisibility(val firestoreValue: String) {
    Everyone("everyone"),
    Contacts("contacts"),
    Nobody("nobody");

    fun next(): PrivacyVisibility {
        return when (this) {
            Everyone -> Contacts
            Contacts -> Nobody
            Nobody -> Everyone
        }
    }

    companion object {
        fun fromFirestore(value: String?): PrivacyVisibility {
            return entries.firstOrNull { visibility -> visibility.firestoreValue == value } ?: Everyone
        }
    }
}
