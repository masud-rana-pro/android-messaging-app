package com.contactme.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.contactme.app.conversation.ReadReceiptState
import com.contactme.app.message.ChatMessage
import com.contactme.app.message.MessageStatus
import com.contactme.app.presence.PresenceStatus
import com.contactme.app.ui.chat.ChatDetailUiState
import com.contactme.app.ui.chat.ChatDetailViewModel
import com.contactme.app.ui.theme.ContactMeSpacing
import com.contactme.app.ui.theme.ContactMeTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatName: String,
    conversationId: String? = null,
    onBack: () -> Unit,
    viewModel: ChatDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(conversationId) {
        viewModel.openConversation(conversationId)
    }

    ChatDetailContent(
        chatName = chatName,
        conversationId = conversationId,
        uiState = uiState,
        onBack = onBack,
        onMessageTextChanged = viewModel::onMessageTextChanged,
        onSendMessage = viewModel::sendMessage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatDetailContent(
    chatName: String,
    conversationId: String?,
    uiState: ChatDetailUiState,
    onBack: () -> Unit,
    onMessageTextChanged: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    val messages = if (conversationId == null) {
        demoMessages(currentUserId = uiState.currentUserId)
    } else {
        uiState.messages
    }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = chatName, fontWeight = FontWeight.Bold)
                        Text(
                            text = chatSubtitle(
                                conversationId = conversationId,
                                isOtherUserTyping = uiState.isOtherUserTyping,
                                peerPresence = uiState.peerPresence
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
                        )
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(text = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(
                    horizontal = ContactMeSpacing.screenHorizontal,
                    vertical = ContactMeSpacing.contentGap
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (uiState.isLoadingMessages) {
                    item {
                        ChatListStateMessage(
                            title = "Loading messages",
                            subtitle = "Syncing this conversation."
                        )
                    }
                } else if (messages.isEmpty()) {
                    item {
                        ChatListStateMessage(
                            title = "No messages yet",
                            subtitle = "Send the first message to start the conversation."
                        )
                    }
                }

                items(
                    items = messages,
                    key = { message -> message.id }
                ) { message ->
                    MessageBubble(
                        message = message,
                        isMine = message.senderId == uiState.currentUserId,
                        readReceiptState = uiState.readReceiptState
                    )
                }
            }
            Column(
                modifier = Modifier.padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.errorMessage?.let { message ->
                    SendErrorMessage(
                        message = message,
                        canRetry = uiState.messageText.isNotBlank() && conversationId != null,
                        onRetry = onSendMessage
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = uiState.messageText,
                        onValueChange = onMessageTextChanged,
                        enabled = conversationId != null && !uiState.isSending,
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = if (conversationId == null) {
                                    "Open a contact to message"
                                } else {
                                    "Message"
                                }
                            )
                        }
                    )
                    Button(
                        enabled = conversationId != null && !uiState.isSending,
                        onClick = onSendMessage,
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(text = "Send")
                    }
                }
            }
        }
    }
}

private fun chatSubtitle(
    conversationId: String?,
    isOtherUserTyping: Boolean,
    peerPresence: PresenceStatus
): String {
    return when {
        conversationId == null -> "online"
        isOtherUserTyping -> "typing..."
        peerPresence.isOnline -> "online"
        peerPresence.lastSeenAtMillis > 0L && peerPresence.canShowLastSeen -> {
            "last seen ${peerPresence.lastSeenAtMillis.formatPresenceTime()}"
        }
        else -> "last seen recently"
    }
}

private fun Long.formatPresenceTime(): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(this))
}

@Composable
private fun ChatListStateMessage(
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 36.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (title == "Loading messages") {
                CircularProgressIndicator()
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
            )
        }
    }
}

@Composable
private fun SendErrorMessage(
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
        if (canRetry) {
            TextButton(onClick = onRetry) {
                Text(text = "Retry")
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isMine: Boolean,
    readReceiptState: ReadReceiptState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .background(
                    color = if (isMine) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            MessageMetaRow(
                sentAtMillis = message.sentAtMillis,
                status = message.status,
                isMine = isMine,
                isSeen = isMine && message.isSeenByPeer(readReceiptState)
            )
        }
    }
}

@Composable
private fun MessageMetaRow(
    sentAtMillis: Long,
    status: MessageStatus,
    isMine: Boolean,
    isSeen: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = sentAtMillis.formatChatTime(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.58f)
        )
        if (isMine) {
            Text(
                text = status.toDisplayMark(isSeen = isSeen),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun MessageStatus.toDisplayMark(isSeen: Boolean): String {
    if (isSeen) return "Seen"

    return when (this) {
        MessageStatus.Sent -> "Sent"
    }
}

private fun ChatMessage.isSeenByPeer(readReceiptState: ReadReceiptState): Boolean {
    return readReceiptState.canShowPeerReadReceipt &&
        sentAtMillis > 0L &&
        readReceiptState.peerReadAtMillis >= sentAtMillis
}

private fun Long.formatChatTime(): String {
    if (this <= 0L) return "Now"

    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(this))
}

private fun demoMessages(currentUserId: String): List<ChatMessage> {
    return listOf(
        ChatMessage(
            id = "demo-1",
            senderId = "other",
            text = "Hey, is the ContactMe UI demo ready?",
            sentAtMillis = 0L
        ),
        ChatMessage(
            id = "demo-2",
            senderId = currentUserId,
            text = "The first screen map is almost done.",
            sentAtMillis = 0L
        ),
        ChatMessage(
            id = "demo-3",
            senderId = currentUserId,
            text = "Next we can connect real auth.",
            sentAtMillis = 0L
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun ChatDetailScreenPreview() {
    ContactMeTheme {
        ChatDetailScreen(
            chatName = "Ayesha Rahman",
            conversationId = null,
            onBack = {}
        )
    }
}
