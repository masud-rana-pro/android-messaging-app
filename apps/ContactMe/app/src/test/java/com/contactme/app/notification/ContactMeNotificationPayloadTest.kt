package com.contactme.app.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import com.contactme.app.conversation.ConversationType

class ContactMeNotificationPayloadTest {
    @Test
    fun `message payload requires a conversation id`() {
        val payload = ContactMeNotificationPayload.fromData(
            data = mapOf("type" to "message", "title" to "Masud")
        )

        assertNull(payload)
    }

    @Test
    fun `message payload uses navigation and preview data`() {
        val payload = ContactMeNotificationPayload.fromData(
            data = mapOf(
                "type" to "message",
                "conversationId" to "conversation-1",
                "conversationType" to "group",
                "title" to "Masud",
                "body" to "Hello",
                "photoUrl" to "https://example.com/photo.jpg"
            )
        )

        assertNotNull(payload)
        assertEquals("conversation-1", payload?.conversationId)
        assertEquals(ConversationType.Group, payload?.conversationType)
        assertEquals("Masud", payload?.title)
        assertEquals("Hello", payload?.body)
        assertEquals("conversation_conversation-1", payload?.groupKey)
        assertTrue((payload?.notificationId ?: -1) >= 0)
    }

    @Test
    fun `system payload can render without a conversation`() {
        val payload = ContactMeNotificationPayload.fromData(
            data = mapOf("type" to "system"),
            fallbackTitle = "Account update",
            fallbackBody = "Your settings changed",
            messageId = "system-1"
        )

        assertNotNull(payload)
        assertEquals(ContactMeNotificationChannels.SYSTEM_CHANNEL_ID, payload?.channelId)
        assertEquals("Account update", payload?.title)
        assertEquals("contactme_system", payload?.groupKey)
    }
}
