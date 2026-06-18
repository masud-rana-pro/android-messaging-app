package com.contactme.app.safety

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SafetyInputValidatorTest {
    @Test
    fun `block rejects a blank user id`() {
        assertEquals(
            "We could not update this setting. Please try again.",
            SafetyInputValidator.blockError(currentUserId = "", blockedUserId = "user-2")
        )
    }

    @Test
    fun `block rejects the current user`() {
        assertEquals(
            "You cannot block yourself.",
            SafetyInputValidator.blockError(currentUserId = "user-1", blockedUserId = "user-1")
        )
    }

    @Test
    fun `block accepts two different users`() {
        assertNull(
            SafetyInputValidator.blockError(currentUserId = "user-1", blockedUserId = "user-2")
        )
    }

    @Test
    fun `unblock rejects a blank peer id`() {
        assertEquals(
            "We could not update this setting. Please try again.",
            SafetyInputValidator.unblockError(currentUserId = "user-1", blockedUserId = " ")
        )
    }

    @Test
    fun `report rejects the current user`() {
        assertEquals(
            "You cannot report yourself.",
            SafetyInputValidator.reportError(reporterUserId = "user-1", reportedUserId = "user-1")
        )
    }

    @Test
    fun `report accepts two different users`() {
        assertNull(
            SafetyInputValidator.reportError(reporterUserId = "user-1", reportedUserId = "user-2")
        )
    }
}
