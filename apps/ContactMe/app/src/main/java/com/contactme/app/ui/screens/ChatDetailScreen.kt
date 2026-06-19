package com.contactme.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.contactme.app.conversation.ReadReceiptState
import com.contactme.app.conversation.ConversationType
import com.contactme.app.message.ChatMessage
import com.contactme.app.message.MessageStatus
import com.contactme.app.message.MessageType
import com.contactme.app.message.MessageReply
import com.contactme.app.notification.NotificationVisibilityTracker
import com.contactme.app.presence.PresenceStatus
import com.contactme.app.safety.ReportReason
import com.contactme.app.ui.chat.ChatDetailUiState
import com.contactme.app.ui.chat.ChatDetailViewModel
import com.contactme.app.ui.theme.ContactMeSpacing
import com.contactme.app.ui.theme.ContactMeTheme
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatName: String,
    conversationId: String? = null,
    chatPhotoUrl: String = "",
    conversationType: ConversationType = ConversationType.Direct,
    onBack: () -> Unit,
    viewModel: ChatDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(conversationId, conversationType) {
        viewModel.openConversation(conversationId, conversationType)
    }

    DisposableEffect(conversationId) {
        NotificationVisibilityTracker.setActiveConversation(conversationId)
        onDispose {
            NotificationVisibilityTracker.clearActiveConversation(conversationId)
        }
    }

    ChatDetailContent(
        chatName = chatName,
        chatPhotoUrl = chatPhotoUrl,
        conversationId = conversationId,
        conversationType = conversationType,
        uiState = uiState,
        onBack = onBack,
        onMessageTextChanged = viewModel::onMessageTextChanged,
        onSendMessage = viewModel::sendMessage,
        onRetryImageMessage = viewModel::retryFailedImageMessage,
        onRetryDocumentMessage = viewModel::retryFailedDocumentMessage,
        onReportChat = viewModel::reportCurrentChat,
        onBlockChat = viewModel::blockCurrentChat,
        onUnblockChat = viewModel::unblockCurrentChat,
        onImageSelected = viewModel::sendImageMessage,
        onDocumentSelected = { uri -> viewModel.sendDocumentMessage(uri) },
        onReplyMessage = viewModel::startReply,
        onCancelReply = viewModel::cancelReply,
        onEditMessage = viewModel::startEdit,
        onCancelEdit = viewModel::cancelEdit,
        onDeleteMessage = viewModel::deleteMessage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatDetailContent(
    chatName: String,
    chatPhotoUrl: String,
    conversationId: String?,
    conversationType: ConversationType,
    uiState: ChatDetailUiState,
    onBack: () -> Unit,
    onMessageTextChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onRetryImageMessage: () -> Unit,
    onRetryDocumentMessage: () -> Unit,
    onReportChat: (ReportReason) -> Unit,
    onBlockChat: () -> Unit,
    onUnblockChat: () -> Unit,
    onImageSelected: (Uri) -> Unit,
    onDocumentSelected: (Uri) -> Unit,
    onReplyMessage: (ChatMessage) -> Unit,
    onCancelReply: () -> Unit,
    onEditMessage: (ChatMessage) -> Unit,
    onCancelEdit: () -> Unit,
    onDeleteMessage: (ChatMessage) -> Unit
) {
    val messages = uiState.messages
    val listState = rememberLazyListState()
    var selectedMessageForActions by remember { mutableStateOf<ChatMessage?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let(onImageSelected) }
    )
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> uri?.let(onDocumentSelected) }
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    selectedMessageForActions?.let { selectedMessage ->
        MessageActionsDialog(
            canDelete = selectedMessage.senderId == uiState.currentUserId,
            canEdit = selectedMessage.senderId == uiState.currentUserId && selectedMessage.type == MessageType.Text,
            onDismiss = { selectedMessageForActions = null },
            onReply = {
                selectedMessageForActions = null
                onReplyMessage(selectedMessage)
            },
            onEdit = {
                selectedMessageForActions = null
                onEditMessage(selectedMessage)
            },
            onDelete = {
                selectedMessageForActions = null
                onDeleteMessage(selectedMessage)
            }
        )
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
                            conversationType = conversationType,
                            isOtherUserTyping = uiState.isOtherUserTyping,
                            peerPresence = uiState.peerPresence
                        )
                    )
                },
                navigationIcon = {
                    HeaderBackButton(onClick = onBack)
                },
                actions = {
                    if (conversationId != null && conversationType == ConversationType.Direct) {
                        ChatActionMenu(
                            uiState = uiState,
                            onReportChat = onReportChat,
                            onBlockChat = onBlockChat,
                            onUnblockChat = onUnblockChat
                        )
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

                itemsIndexed(
                    items = messages,
                    key = { _, message -> message.id }
                ) { index, message ->
                    val previousMessage = messages.getOrNull(index - 1)
                    if (previousMessage == null || !message.sentAtMillis.isSameChatDay(
                            previousMessage.sentAtMillis
                        )
                    ) {
                        ChatDateSeparator(sentAtMillis = message.sentAtMillis)
                    }
                    MessageBubble(
                        message = message,
                        isMine = message.senderId == uiState.currentUserId,
                        showSenderName = conversationType == ConversationType.Group,
                        onLongPress = { selectedMessageForActions = message },
                        readReceiptState = uiState.readReceiptState
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.editingMessageId?.let {
                    EditComposerPreview(onCancel = onCancelEdit)
                }
                uiState.replyingTo?.let { reply ->
                    ReplyComposerPreview(reply = reply, onCancel = onCancelReply)
                }
                val imagePreviewUri = uiState.pendingImageUri.ifBlank { uiState.failedImageUri }
                if (imagePreviewUri.isNotBlank()) {
                    PendingImagePreview(
                        imageUri = imagePreviewUri,
                        isUploading = uiState.pendingImageUri.isNotBlank() && uiState.isSending
                    )
                }
                if (uiState.pendingDocumentName.isNotBlank() || uiState.failedDocumentName.isNotBlank()) {
                    PendingDocumentPreview(
                        fileName = uiState.pendingDocumentName.ifBlank { uiState.failedDocumentName },
                        isUploading = uiState.pendingDocumentName.isNotBlank() && uiState.isSending
                    )
                }
                uiState.statusMessage?.let { message ->
                    ChatStatusMessage(message = message)
                }
                uiState.errorMessage?.let { message ->
                    val hasFailedImage = uiState.failedImageUri.isNotBlank()
                    val hasFailedDocument = uiState.failedDocumentUri.isNotBlank()
                    SendErrorMessage(
                        message = message,
                        canRetry = conversationId != null && (
                            uiState.messageText.isNotBlank() || hasFailedImage || hasFailedDocument
                            ),
                        onRetry = {
                            if (hasFailedDocument) {
                                onRetryDocumentMessage()
                            } else if (hasFailedImage) {
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
                    },
                    onDocumentClick = {
                        documentPickerLauncher.launch(DOCUMENT_MIME_TYPES)
                    }
                )
            }
        }
    }
}

@Composable
private fun ChatActionMenu(
    uiState: ChatDetailUiState,
    onReportChat: (ReportReason) -> Unit,
    onBlockChat: () -> Unit,
    onUnblockChat: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isReportDialogVisible by remember { mutableStateOf(false) }
    var isBlockDialogVisible by remember { mutableStateOf(false) }

    Box {
        IconButton(
            enabled = !uiState.isSafetyActionInProgress,
            onClick = { isExpanded = true }
        ) {
            Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = "Chat options")
        }
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(text = "Report") },
                enabled = !uiState.isSafetyActionInProgress,
                onClick = {
                    isExpanded = false
                    isReportDialogVisible = true
                }
            )
            DropdownMenuItem(
                text = {
                    Text(text = if (uiState.canUnblockChat) "Unblock" else "Block")
                },
                enabled = !uiState.isSafetyActionInProgress &&
                    (!uiState.isChatBlocked || uiState.canUnblockChat),
                onClick = {
                    isExpanded = false
                    if (uiState.canUnblockChat) {
                        onUnblockChat()
                    } else {
                        isBlockDialogVisible = true
                    }
                }
            )
        }
    }

    if (isReportDialogVisible) {
        ReportReasonDialog(
            onDismiss = { isReportDialogVisible = false },
            onReasonSelected = { reason ->
                isReportDialogVisible = false
                onReportChat(reason)
            }
        )
    }

    if (isBlockDialogVisible) {
        AlertDialog(
            onDismissRequest = { isBlockDialogVisible = false },
            title = { Text(text = "Block user?") },
            text = { Text(text = "They will no longer be able to message you.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        isBlockDialogVisible = false
                        onBlockChat()
                    }
                ) {
                    Text(text = "Block")
                }
            },
            dismissButton = {
                TextButton(onClick = { isBlockDialogVisible = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

@Composable
private fun ReportReasonDialog(
    onDismiss: () -> Unit,
    onReasonSelected: (ReportReason) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Report user") },
        text = {
            Column {
                ReportReason.entries.forEach { reason ->
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onReasonSelected(reason) }
                    ) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = reason.displayLabel()
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}

private fun ReportReason.displayLabel(): String {
    return when (this) {
        ReportReason.Spam -> "Spam"
        ReportReason.Harassment -> "Harassment"
        ReportReason.Scam -> "Scam or fraud"
        ReportReason.Other -> "Other"
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
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Back"
        )
    }
}

private fun chatSubtitle(
    conversationId: String?,
    conversationType: ConversationType,
    isOtherUserTyping: Boolean,
    peerPresence: PresenceStatus
): String {
    return when {
        conversationId == null -> "online"
        conversationType == ConversationType.Group -> "Group"
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
private fun MessageActionsDialog(
    canDelete: Boolean,
    canEdit: Boolean,
    onDismiss: () -> Unit,
    onReply: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Message actions") },
        text = {
            Column {
                TextButton(modifier = Modifier.fillMaxWidth(), onClick = onReply) {
                    Text(modifier = Modifier.fillMaxWidth(), text = "Reply")
                }
                if (canEdit) {
                    TextButton(modifier = Modifier.fillMaxWidth(), onClick = onEdit) {
                        Text(modifier = Modifier.fillMaxWidth(), text = "Edit")
                    }
                }
                if (canDelete) {
                    TextButton(modifier = Modifier.fillMaxWidth(), onClick = onDelete) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Delete",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = "Cancel") }
        }
    )
}

@Composable
private fun EditComposerPreview(onCancel: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(
            MaterialTheme.colorScheme.surfaceVariant,
            RoundedCornerShape(12.dp)
        ).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = "Editing message",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onCancel) {
            Icon(imageVector = Icons.Outlined.Close, contentDescription = "Cancel edit")
        }
    }
}

@Composable
private fun ReplyComposerPreview(
    reply: MessageReply,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Replying to ${reply.senderName}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = reply.preview,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onCancel) {
            Icon(imageVector = Icons.Outlined.Close, contentDescription = "Cancel reply")
        }
    }
}

@Composable
private fun MessageReplyPreview(reply: MessageReply) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.68f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Text(
            text = reply.senderName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = reply.preview,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isMine: Boolean,
    showSenderName: Boolean,
    onLongPress: () -> Unit,
    readReceiptState: ReadReceiptState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .pointerInput(message.id, message.isDeleted) {
                    if (!message.isDeleted) {
                        detectTapGestures(onLongPress = { onLongPress() })
                    }
                }
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
            if (message.isDeleted) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "This message was deleted",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                message.replyTo?.let { reply -> MessageReplyPreview(reply = reply) }
            if (showSenderName && !isMine) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = message.senderDisplayName.ifBlank { "Group member" },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
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
            if (message.type == MessageType.Document) {
                DocumentMessageContent(message = message)
            }
            }
            MessageMetaRow(
                sentAtMillis = message.sentAtMillis,
                isEdited = !message.isDeleted && message.editedAtMillis > 0L,
                status = message.status,
                isMine = isMine,
                isSeen = isMine && message.isSeenByPeer(readReceiptState)
            )
        }
    }
}

@Composable
private fun ChatDateSeparator(sentAtMillis: Long) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                text = sentAtMillis.formatChatDate(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun DocumentMessageContent(message: ChatMessage) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
            .clickable(enabled = message.mediaUrl.isNotBlank()) {
                runCatching {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(message.mediaUrl)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                    )
                }
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(imageVector = Icons.Outlined.Description, contentDescription = null)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = message.fileName.ifBlank { "Document" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
            Text(
                text = message.fileSizeBytes.formatFileSize(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
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
private fun PendingDocumentPreview(fileName: String, isUploading: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isUploading) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(imageVector = Icons.Outlined.Description, contentDescription = null)
            Text(
                modifier = Modifier.weight(1f),
                text = fileName,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (isUploading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        }
    }
}

@Composable
private fun MessageMetaRow(
    sentAtMillis: Long,
    isEdited: Boolean,
    status: MessageStatus,
    isMine: Boolean,
    isSeen: Boolean
) {
    Row(
        modifier = Modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isEdited) {
            Text(
                text = "edited",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.58f)
            )
        }
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
    onImageClick: () -> Unit,
    onDocumentClick: () -> Unit
) {
    var attachmentMenuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = text,
            onValueChange = onMessageTextChanged,
            enabled = enabled,
            minLines = 1,
            maxLines = 4,
            shape = RoundedCornerShape(24.dp),
            trailingIcon = {
                Box {
                    IconButton(
                        enabled = enabled && !isSending,
                        onClick = { attachmentMenuExpanded = true }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AttachFile,
                            contentDescription = "Attach"
                        )
                    }
                    DropdownMenu(
                        expanded = attachmentMenuExpanded,
                        onDismissRequest = { attachmentMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = "Photo") },
                            leadingIcon = { Icon(Icons.Outlined.Image, contentDescription = null) },
                            onClick = {
                                attachmentMenuExpanded = false
                                onImageClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = "Document") },
                            leadingIcon = { Icon(Icons.Outlined.Description, contentDescription = null) },
                            onClick = {
                                attachmentMenuExpanded = false
                                onDocumentClick()
                            }
                        )
                    }
                }
            },
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
        FilledIconButton(
            enabled = enabled && !isSending && text.isNotBlank(),
            onClick = onSendMessage
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    contentDescription = "Send"
                )
            }
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

private fun Long.formatFileSize(): String {
    return when {
        this <= 0L -> "Document"
        this < 1024L * 1024L -> "${(this / 1024L).coerceAtLeast(1)} KB"
        else -> String.format(Locale.getDefault(), "%.1f MB", this / (1024.0 * 1024.0))
    }
}

private fun Long.isSameChatDay(otherMillis: Long): Boolean {
    if (this <= 0L || otherMillis <= 0L) return false
    val first = Calendar.getInstance().apply { timeInMillis = this@isSameChatDay }
    val second = Calendar.getInstance().apply { timeInMillis = otherMillis }
    return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
        first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
}

private fun Long.formatChatDate(): String {
    if (this <= 0L) return "Today"
    val messageDate = Calendar.getInstance().apply { timeInMillis = this@formatChatDate }
    val today = Calendar.getInstance()
    if (messageDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
        messageDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    ) return "Today"

    val yesterday = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
    if (messageDate.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
        messageDate.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)
    ) return "Yesterday"

    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(this))
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

private val DOCUMENT_MIME_TYPES = arrayOf(
    "application/pdf",
    "text/plain",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
)

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
