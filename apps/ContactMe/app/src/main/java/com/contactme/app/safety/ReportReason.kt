package com.contactme.app.safety

enum class ReportReason(val firestoreValue: String) {
    Spam("spam"),
    Harassment("harassment"),
    Scam("scam"),
    Other("other");

    companion object {
        fun fromFirestore(value: String?): ReportReason {
            return entries.firstOrNull { reason -> reason.firestoreValue == value } ?: Other
        }
    }
}
