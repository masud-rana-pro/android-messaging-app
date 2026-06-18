package com.contactme.app.conversation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GroupConversationValidatorTest {
    @Test
    fun `requires a group name`() {
        assertEquals(
            "Enter a group name up to 100 characters.",
            GroupConversationValidator.error("owner", " ", listOf("user-1", "user-2"))
        )
    }

    @Test
    fun `requires two contacts besides the owner`() {
        assertEquals(
            "Select at least two contacts.",
            GroupConversationValidator.error("owner", "Team", listOf("owner", "user-1"))
        )
    }

    @Test
    fun `accepts a valid unique member list`() {
        assertNull(
            GroupConversationValidator.error(
                "owner",
                "Team",
                listOf("user-1", "user-2", "user-2")
            )
        )
    }
}
