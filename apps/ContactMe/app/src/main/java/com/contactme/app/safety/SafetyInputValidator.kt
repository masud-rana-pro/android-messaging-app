package com.contactme.app.safety

internal object SafetyInputValidator {
    fun blockError(currentUserId: String, blockedUserId: String): String? {
        if (currentUserId.isBlank() || blockedUserId.isBlank()) {
            return "We could not update this setting. Please try again."
        }

        return if (currentUserId == blockedUserId) "You cannot block yourself." else null
    }

    fun unblockError(currentUserId: String, blockedUserId: String): String? {
        return if (currentUserId.isBlank() || blockedUserId.isBlank()) {
            "We could not update this setting. Please try again."
        } else {
            null
        }
    }

    fun reportError(reporterUserId: String, reportedUserId: String): String? {
        if (reporterUserId.isBlank() || reportedUserId.isBlank()) {
            return "We could not send this report. Please try again."
        }

        return if (reporterUserId == reportedUserId) "You cannot report yourself." else null
    }
}
