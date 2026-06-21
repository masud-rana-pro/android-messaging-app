package com.contactme.app.message

enum class MessageType(val firestoreValue: String) {
    Text("text"),
    Image("image"),
    Document("document"),
    Call("call");

    companion object {
        fun fromFirestore(value: String?): MessageType {
            return entries.firstOrNull { type -> type.firestoreValue == value } ?: Text
        }
    }
}
