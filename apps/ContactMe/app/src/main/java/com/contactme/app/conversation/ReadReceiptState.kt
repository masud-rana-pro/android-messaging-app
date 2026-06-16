package com.contactme.app.conversation

data class ReadReceiptState(
    val peerReadAtMillis: Long = 0L,
    val canShowPeerReadReceipt: Boolean = true
)
