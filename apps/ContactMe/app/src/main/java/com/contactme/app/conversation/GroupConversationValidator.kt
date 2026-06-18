package com.contactme.app.conversation

internal object GroupConversationValidator {
    fun error(currentUserId: String, title: String, memberUserIds: List<String>): String? {
        if (currentUserId.isBlank()) return "Session expired. Please log in again."
        if (title.trim().length !in 1..100) return "Enter a group name up to 100 characters."

        val otherMembers = memberUserIds
            .map(String::trim)
            .filter(String::isNotBlank)
            .filter { it != currentUserId }
            .distinct()
        if (otherMembers.size < 2) return "Select at least two contacts."
        if (otherMembers.size > 255) return "A group can have up to 256 members."
        return null
    }
}
