package com.contactme.app.notification

import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationVisibilityTrackerTest {
    @After
    fun resetTracker() {
        NotificationVisibilityTracker.setAppForeground(false)
        NotificationVisibilityTracker.setActiveConversation(null)
    }

    @Test
    fun `suppresses the currently visible conversation`() {
        NotificationVisibilityTracker.setAppForeground(true)
        NotificationVisibilityTracker.setActiveConversation("conversation-1")

        assertTrue(NotificationVisibilityTracker.shouldSuppress("conversation-1"))
    }

    @Test
    fun `does not suppress while app is backgrounded`() {
        NotificationVisibilityTracker.setAppForeground(false)
        NotificationVisibilityTracker.setActiveConversation("conversation-1")

        assertFalse(NotificationVisibilityTracker.shouldSuppress("conversation-1"))
    }

    @Test
    fun `does not suppress a different conversation`() {
        NotificationVisibilityTracker.setAppForeground(true)
        NotificationVisibilityTracker.setActiveConversation("conversation-1")

        assertFalse(NotificationVisibilityTracker.shouldSuppress("conversation-2"))
    }
}
