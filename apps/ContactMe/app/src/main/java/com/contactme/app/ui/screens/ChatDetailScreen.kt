package com.contactme.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.contactme.app.conversation.ReadReceiptState
import com.contactme.app.message.ChatMessage
import com.contactme.app.message.MessageStatus
import com.contactme.app.message.MessageType
import com.contactme.app.presence.PresenceStatus
import com.contactme.app.ui.chat.ChatDetailUiState
import com.contactme.app.ui.chat.ChatDetailViewModel
import com.contactme.app.ui.theme.ContactMeSpacing
import com.contactme.app.ui.theme.ContactMeTheme
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatName: String,
    conversationId: String? = null,
    chatPhotoUrl: String = "",
    onBack: () -> Unit,
    viewModel: ChatDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(conversationId) {
        viewModel.openConversation(conversationId)
    }

    ChatDetailContent(
        chatName = chatName,
        chatPhotoUrl = chatPhotoUrl,
        conversationId = conversationId,
        uiState = uiState,
        onBack = onBack,
        onMessageTextChanged = viewModel::onMessageTextChanged,
        onSendMessage = viewModel::sendMessage,
        onRetryImageMessage = viewModel::retryFailedImageMessage,
        onReportChat = viewModel::reportCurrentChat,
        onBlockChat = viewModel::blockCurrentChat,
        onImageSelected = viewModel::sendImageMessage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatDetailContent(
    chatName: String,
    chatPhotoUrl: String,
    conversationId: String?,
    uiState: ChatDetailUiState,
    onBack: () -> Unit,
    onMessageTextChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onRetryImageMessage: () -> Unit,
    onReportChat: () -> Unit,
    onBlockChat: () -> Unit,
    onImageSelected: (Uri) -> Unit
) {
    val messages = uiState.messages
    val listState = rememberLazyListState()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let(onImageSelected) }
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    ChatHeaderTitle(
                        chatName = chatName,
                        chatPhotoUrl = chatPhotoUrl,
                        subtitle = chatSubtitle(
                            conversationId = conversationId,
                            isOtherUserTyping = uiState.isOtherUserTyping,
                            peerPresence = uiState.peerPresence
                        )
                    )
                },
                navigationIcon = {
                    HeaderBackButton(onClick = onBack)
                },
                actions = {
                    if (conversationId != null) {
                        TextButton(
                            enabled = !uiState.isSafetyActionInProgress,
                            onClick = onReportChat
                        ) {
                            Text(text = "Report")
                        }
                        TextButton(
                            enabled = !uiState.isSafetyActionInProgress && !uiState.isChatBlocked,
                            onClick = onBlockChat
                        ) {
                            Text(text = "Block")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
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
                    vertical = 8.dp
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
                        if (conversationId == null) {
                            ChatListStateMessage(
                                title = "No chat selected",
                                subtitle = "Choose a contact to start."
                            )
                        } else {
                            ChatListStateMessage(
                                title = "No messages yet",
                                subtitle = "Send the first message to start the conversation."
                            )
                        }
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val imagePreviewUri = uiState.pendingImageUri.ifBlank { uiState.failedImageUri }
                if (imagePreviewUri.isNotBlank()) {
                    PendingImagePreview(
                        imageUri = imagePreviewUri,
                        isUploading = uiState.pendingImageUri.isNotBlank() && uiState.isSending
                    )
                }
                uiState.statusMessage?.let { message ->
                    ChatStatusMessage(message = message)
                }
                uiState.errorMessage?.let { message ->
                    val hasFailedImage = uiState.failedImageUri.isNotBlank()
                    SendErrorMessage(
                        message = message,
                        canRetry = conversationId != null && (
                            uiState.messageText.isNotBlank() || hasFailedImage
                            ),
                        onRetry = {
                            if (hasFailedImage) {
                                onRetryImageMessage()
                            } else {
                                onSendMessage()
                            }
                        }
                    )
                }
                MessageInputBar(
                    text = uiState.messageText,
                    enabled = conversationId != null && !uiState.isSending && !uiState.isChatBlocked,
                    isSending = uiState.isSending,
                    hasFailedImage = uiState.failedImageUri.isNotBlank(),
                    isChatBlocked = uiState.isChatBlocked,
                    onMessageTextChanged = onMessageTextChanged,
                    onSendMessage = onSendMessage,
                    onImageClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ChatHeaderTitle(
    chatName: String,
    chatPhotoUrl: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (chatPhotoUrl.isNotBlank()) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = chatPhotoUrl,
                    contentDescription = "$chatName profile photo",
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = chatName.profileInitials(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Column {
            Text(
                text = chatName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
            )
        }
    }
}

@Composable
private fun HeaderBackButton(onClick: () -> Unit) {
    Text(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        text = "<",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 36.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
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
private fun ChatStatusMessage(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
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
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                    },
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isMine) 18.dp else 4.dp,
                        bottomEnd = if (isMine) 4.dp else 18.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 9.dp),
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
        ) {
            if (message.text.isNotBlank()) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            if (message.type == MessageType.Image) {
                ImageMessageContent(message = message)
            }
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
private fun ImageMessageContent(message: ChatMessage) {
    if (message.mediaUrl.isBlank()) {
        Text(
            text = "Photo unavailable",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.64f)
        )
        return
    }

    AsyncImage(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(top = 2.dp)
            .clip(RoundedCornerShape(14.dp)),
        model = message.mediaUrl,
        contentDescription = "Photo message",
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun PendingImagePreview(
    imageUri: String,
    isUploading: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isUploading) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
        } else {
            MaterialTheme.colorScheme.errorContainer
        },
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp)),
                model = imageUri,
                contentDescription = "Selected photo",
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isUploading) "Sending photo" else "Photo not sent",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUploading) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer
                    },
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (isUploading) "Uploading..." else "Retry or choose another photo",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isUploading) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                    } else {
                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.72f)
                    }
                )
            }
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            }
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
        modifier = Modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
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
                color = if (isSeen) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.58f)
                }
            )
        }
    }
}

@Composable
private fun MessageInputBar(
    text: String,
    enabled: Boolean,
    isSending: Boolean,
    hasFailedImage: Boolean,
    isChatBlocked: Boolean,
    onMessageTextChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onImageClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = text,
                onValueChange = onMessageTextChanged,
                enabled = enabled,
                singleLine = true,
                shape = RoundedCornerShape(22.dp),
                placeholder = {
                    Text(
                        text = when {
                            isChatBlocked -> "Chat unavailable"
                            isSending -> "Sending..."
                            hasFailedImage -> "Retry photo or choose another"
                            enabled -> "Message"
                            else -> "Select a chat"
                        }
                    )
                }
            )
            Text(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = enabled && !isSending, onClick = onImageClick)
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                text = "+",
                color = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.42f)
                },
                fontWeight = FontWeight.Bold
            )
            Text(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (enabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.16f)
                        }
                    )
                    .clickable(enabled = enabled && !isSending, onClick = onSendMessage)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                text = if (isSending) "..." else ">",
                color = if (enabled) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.42f)
                },
                fontWeight = FontWeight.Bold
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

private fun String.profileInitials(): String {
    val initials = trim()
        .split(" ")
        .filter(String::isNotBlank)
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")

    return initials.ifBlank { "CM" }
}

@Preview(showBackground = true)
@Composable
private fun ChatDetailScreenPreview() {
    ContactMeTheme {
        ChatDetailScreen(
            chatName = "ContactMe User",
            conversationId = null,
            chatPhotoUrl = "",
            onBack = {}
        )
    }
}
