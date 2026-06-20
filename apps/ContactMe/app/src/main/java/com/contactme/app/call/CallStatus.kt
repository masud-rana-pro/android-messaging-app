package com.contactme.app.call

enum class CallStatus(val firestoreValue: String) {
    Ringing("ringing"),
    Accepted("accepted"),
    Rejected("rejected"),
    Ended("ended"),
    Missed("missed"),
    Cancelled("cancelled"),
    Timeout("timeout"),
    Busy("busy");

    companion object {
        fun fromFirestore(value: String?): CallStatus =
            entries.firstOrNull { it.firestoreValue == value } ?: Ringing
    }
}
