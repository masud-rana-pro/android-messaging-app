package com.contactme.app.ui.screens

import android.util.Log
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.contactme.app.R
import com.contactme.app.conversation.ConversationType
import com.contactme.app.conversation.ReadReceiptState
import com.contactme.app.message.ChatMessage
import com.contactme.app.message.MessageReply
import com.contactme.app.message.MessageStatus
import com.contactme.app.message.MessageType
import androidx.core.content.FileProvider
import com.contactme.app.BuildConfig
import java.io.File
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

import com.contactme.app.notification.NotificationVisibilityTracker
import com.contactme.app.presence.PresenceStatus
import com.contactme.app.safety.ReportReason
import com.contactme.app.ui.chat.ChatDetailUiState
import com.contactme.app.ui.chat.ChatDetailViewModel
import com.contactme.app.ui.theme.ContactMeGreen
import com.contactme.app.ui.theme.ContactMeSpacing
import com.contactme.app.ui.theme.ContactMeTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatName: String,
    conversationId: String? = null,
    chatPhotoUrl: String = "",
    conversationType: ConversationType = ConversationType.Direct,
    onBack: () -> Unit,
    onVoiceCallClick: (String) -> Unit,
    onVideoCallClick: (String) -> Unit,
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
        onVoiceCallClick = { onVoiceCallClick(uiState.peerUserId ?: "") },
        onVideoCallClick = { onVideoCallClick(uiState.peerUserId ?: "") },
        onMessageTextChanged = viewModel::onMessageTextChanged,
        onSendMessage = viewModel::sendMessage,
        onRetryImageMessage = viewModel::retryFailedImageMessage,
        onRetryDocumentMessage = viewModel::retryFailedDocumentMessage,
        onRetryLoadingMessages = viewModel::retryLoadingMessages,
        onReportChat = viewModel::reportCurrentChat,
        onBlockChat = viewModel::blockCurrentChat,
        onUnblockChat = viewModel::unblockCurrentChat,
        onImageSelected = viewModel::sendImageMessages,
        onDocumentSelected = viewModel::sendDocumentMessages,
        onReplyMessage = viewModel::startReply,
        onCancelReply = viewModel::cancelReply,
        onEditMessage = viewModel::startEdit,
        onCancelEdit = viewModel::cancelEdit,
        onDeleteMessage = viewModel::deleteMessage,
        onEmojiSelected = viewModel::onEmojiSelected,
        onCameraPhotoCaptured = viewModel::onCameraPhotoCaptured,
        onStartRecording = { viewModel.startRecording(it) },
        onStopRecording = viewModel::stopRecording,
        onCancelRecording = viewModel::cancelRecording,
        onVoiceMessageToggle = viewModel::toggleVoiceMessagePlayback
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
    onVoiceCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onMessageTextChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onRetryImageMessage: () -> Unit,
    onRetryDocumentMessage: () -> Unit,
    onRetryLoadingMessages: () -> Unit,
    onReportChat: (ReportReason) -> Unit,
    onBlockChat: () -> Unit,
    onUnblockChat: () -> Unit,
    onImageSelected: (List<Uri>) -> Unit,
    onDocumentSelected: (List<Uri>) -> Unit,
    onReplyMessage: (ChatMessage) -> Unit,
    onCancelReply: () -> Unit,
    onEditMessage: (ChatMessage) -> Unit,
    onCancelEdit: () -> Unit,
    onDeleteMessage: (ChatMessage) -> Unit,
    onEmojiSelected: (String) -> Unit,
    onCameraPhotoCaptured: (Uri) -> Unit,
    onStartRecording: (File) -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    onVoiceMessageToggle: (ChatMessage) -> Unit
) {
    val messages = uiState.messages
    val listState = rememberLazyListState()
    var selectedMessageForActions by remember { mutableStateOf<ChatMessage?>(null) }
    var isEmojiPanelVisible by remember { mutableStateOf(false) }
    var isStartingCall by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.messages) {
        // Reset starting call state once we receive updates (indicating we are active in chat)
        isStartingCall = false
    }

    val context = LocalContext.current
    val cacheDir = context.cacheDir
    
    var cameraPhotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            Log.d("ChatDetailScreen", "Camera TakePicture result: $success")
            if (success) {
                cameraPhotoUri?.let { uri ->
                    Log.d("ChatDetailScreen", "Captured URI: $uri")
                    onCameraPhotoCaptured(uri)
                } ?: Log.e("ChatDetailScreen", "Camera success but cameraPhotoUri is null!")
            } else {
                Log.e("ChatDetailScreen", "Camera TakePicture failed")
            }
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
            val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
            Log.d("ChatDetailScreen", "Permissions result: audio=$audioGranted, camera=$cameraGranted")
            
            if (audioGranted && !isStartingCall) {
                // If this was a voice call attempt
                if (cameraGranted) {
                   // Possible video call or just both granted
                }
            }
        }
    )

    fun startCamera() {
        Log.d("ChatDetailScreen", "startCamera called")
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val file = File(cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
            cameraPhotoUri = uri
            Log.d("ChatDetailScreen", "Launching camera with URI: $uri")
            cameraLauncher.launch(uri)
        } else {
            Log.d("ChatDetailScreen", "Requesting camera permission")
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        }
    }

    fun startVoiceRecording() {
        Log.d("ChatDetailScreen", "startVoiceRecording called")
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            onStartRecording(cacheDir)
        } else {
            Log.d("ChatDetailScreen", "Requesting audio permission")
            permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
        }
    }

    fun startVoiceCall() {
        if (isStartingCall) return
        Log.d("ChatDetailScreen", "startVoiceCall called for peer: ${uiState.peerUserId}")
        val hasAudio = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        
        if (hasAudio) {
            if (uiState.peerUserId != null) {
                isStartingCall = true
                onVoiceCallClick()
            }
        } else {
            Log.d("ChatDetailScreen", "Requesting audio permission for call")
            permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
        }
    }

    fun startVideoCall() {
        if (isStartingCall) return
        Log.d("ChatDetailScreen", "startVideoCall called for peer: ${uiState.peerUserId}")
        val hasAudio = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        val hasCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        
        if (hasAudio && hasCamera) {
            if (uiState.peerUserId != null) {
                isStartingCall = true
                onVideoCallClick()
            }
        } else {
            Log.d("ChatDetailScreen", "Requesting permissions for video call")
            permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA))
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> if (uris.isNotEmpty()) onImageSelected(uris) }
    )
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris -> if (uris.isNotEmpty()) onDocumentSelected(uris) }
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
                            conversationType = conversationType,
                            isOtherUserTyping = uiState.isOtherUserTyping,
                            peerPresence = uiState.peerPresence
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (conversationId != null && conversationType == ConversationType.Direct) {
                        IconButton(onClick = { startVideoCall() }) {
                            Icon(imageVector = Icons.Outlined.Videocam, contentDescription = "Video Call")
                        }
                        IconButton(onClick = { startVoiceCall() }) {
                            Icon(imageVector = Icons.Outlined.Call, contentDescription = "Voice Call")
                        }
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
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            ChatWallpaper()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    if (uiState.isLoadingMessages) {
                        item { LoadingMessagesState() }
                    } else if (uiState.messageLoadError != null) {
                        item {
                            ChatListStateMessage(
                                title = "Messages unavailable",
                                subtitle = uiState.messageLoadError,
                                actionLabel = "Retry",
                                onAction = onRetryLoadingMessages
                            )
                        }
                    } else if (messages.isEmpty()) {
                        item {
                            ChatListStateMessage(
                                title = if (conversationId == null) "No chat selected" else "No messages yet",
                                subtitle = if (conversationId == null) "Choose a contact to start." else "Send the first message to start the conversation."
                            )
                        }
                    }

                    itemsIndexed(
                        items = messages,
                        key = { _, message -> message.id }
                    ) { index, message ->
                        val previousMessage = messages.getOrNull(index - 1)
                        if (previousMessage == null || !message.sentAtMillis.isSameChatDay(previousMessage.sentAtMillis)) {
                            ChatDateSeparator(sentAtMillis = message.sentAtMillis)
                        }
                        MessageBubble(
                            message = message,
                            isMine = message.senderId == uiState.currentUserId,
                            showSenderName = conversationType == ConversationType.Group,
                            onLongPress = { if (!uiState.isSending) selectedMessageForActions = message },
                            readReceiptState = uiState.readReceiptState,
                            isPlaying = uiState.voiceMessagePlayingId == message.id,
                            onVoiceToggle = { onVoiceMessageToggle(message) }
                        )
                    }
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    if (uiState.isRecording) {
                        RecordingBar(
                            durationMillis = uiState.recordingDurationMillis,
                            onStop = onStopRecording,
                            onCancel = onCancelRecording
                        )
                    } else {
                        uiState.editingMessageId?.let { EditComposerPreview(onCancel = onCancelEdit) }
                        uiState.replyingTo?.let { reply -> ReplyComposerPreview(reply = reply, onCancel = onCancelReply) }
                        
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
                        
                        uiState.statusMessage?.let { ChatStatusMessage(message = it) }
                        uiState.errorMessage?.let { msg ->
                            SendErrorMessage(
                                message = msg,
                                canRetry = conversationId != null,
                                onRetry = {
                                    if (uiState.failedDocumentUri.isNotBlank()) onRetryDocumentMessage()
                                    else if (uiState.failedImageUri.isNotBlank()) onRetryImageMessage()
                                    else onSendMessage()
                                }
                            )
                        }

                        MessageInputBar(
                            text = uiState.messageText,
                            enabled = conversationId != null && !uiState.isSending && !uiState.isChatBlocked,
                            isSending = uiState.isSending,
                            attachmentsEnabled = uiState.editingMessageId == null,
                            hasFailedImage = uiState.failedImageUri.isNotBlank(),
                            isChatBlocked = uiState.isChatBlocked,
                            onMessageTextChanged = onMessageTextChanged,
                            onSendMessage = onSendMessage,
                            onImageClick = {
                                imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            onDocumentClick = {
                                documentPickerLauncher.launch(arrayOf("*/*"))
                            },
                            onEmojiClick = { isEmojiPanelVisible = !isEmojiPanelVisible },
                            onCameraClick = { startCamera() },
                            onMicClick = { startVoiceRecording() }
                        )

                        if (isEmojiPanelVisible) {
                            EmojiPicker(onEmojiSelected = { 
                                onEmojiSelected(it)
                                isEmojiPanelVisible = false
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatWallpaper() {
    val isDark = isSystemInDarkTheme()
    val wallpaperRes = if (isDark) R.drawable.chat_bg_dark else R.drawable.chat_bg_light
    Image(
        painter = painterResource(id = wallpaperRes),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        alpha = if (isDark) 0.35f else 0.45f,
        colorFilter = if (isDark) ColorFilter.tint(Color.Black, BlendMode.Darken) else null
    )
}

@Composable
private fun ChatHeaderTitle(chatName: String, chatPhotoUrl: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier.size(38.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (chatPhotoUrl.isNotBlank()) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = chatPhotoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = chatName.take(1).uppercase(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Column {
            Text(text = chatName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        }
    }
}

private fun chatSubtitle(conversationType: ConversationType, isOtherUserTyping: Boolean, peerPresence: PresenceStatus): String {
    if (isOtherUserTyping) return "typing..."
    if (peerPresence.isOnline) return "online"
    if (conversationType == ConversationType.Group) return "Group"
    if (peerPresence.canShowLastSeen && peerPresence.lastSeenAtMillis > 0L) {
        return "last seen ${peerPresence.lastSeenAtMillis.formatPresenceTime()}"
    }
    return "last seen recently"
}

private fun Long.formatPresenceTime(): String {
    val now = Calendar.getInstance()
    val time = Calendar.getInstance().apply { timeInMillis = this@formatPresenceTime }
    val isToday = now.get(Calendar.YEAR) == time.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == time.get(Calendar.DAY_OF_YEAR)
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val isYesterday = yesterday.get(Calendar.YEAR) == time.get(Calendar.YEAR) && yesterday.get(Calendar.DAY_OF_YEAR) == time.get(Calendar.DAY_OF_YEAR)
    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(this@formatPresenceTime))
    return when {
        isToday -> "today at $timeStr"
        isYesterday -> "yesterday at $timeStr"
        else -> SimpleDateFormat("d MMM 'at' h:mm a", Locale.getDefault()).format(Date(this@formatPresenceTime))
    }
}

@Composable
private fun MessageBubble(message: ChatMessage, isMine: Boolean, showSenderName: Boolean, onLongPress: () -> Unit, readReceiptState: ReadReceiptState, isPlaying: Boolean = false, onVoiceToggle: () -> Unit = {}) {
    val bubbleColor = if (isMine) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.98f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
    val shape = RoundedCornerShape(
        topStart = if (isMine) 16.dp else 2.dp,
        topEnd = if (isMine) 2.dp else 16.dp,
        bottomStart = 16.dp,
        bottomEnd = 16.dp
    )

    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .pointerInput(message.id) { detectTapGestures(onLongPress = { onLongPress() }) }
                .background(bubbleColor, shape)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            if (message.isDeleted) {
                Text(text = "This message was deleted", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                message.replyTo?.let { MessageReplyPreview(it) }
                if (showSenderName && !isMine) {
                    Text(text = message.senderDisplayName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                when (message.type) {
                    MessageType.Text -> Text(text = message.text, style = MaterialTheme.typography.bodyLarge)
                    MessageType.Image -> ImageMessageContent(message)
                    MessageType.Document -> DocumentMessageContent(message)
                    MessageType.Call -> CallLogContent(message, isMine)
                    MessageType.Voice -> VoiceMessageContent(message, isMine, isPlaying, onVoiceToggle)
                }
            }
            Row(modifier = Modifier.align(Alignment.End), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = message.sentAtMillis.formatChatTime(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                if (isMine) {
                    val isSeen = message.isSeenByPeer(readReceiptState)
                    val tickIcon = if (message.status == MessageStatus.Sent && !isSeen) Icons.Filled.Done else Icons.Filled.DoneAll
                    val tickColor = if (isSeen) Color(0xFF34B7F1) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    Icon(imageVector = tickIcon, contentDescription = null, modifier = Modifier.size(14.dp), tint = tickColor)
                }
            }
        }
    }
}

@Composable
private fun MessageInputBar(
    text: String,
    enabled: Boolean,
    isSending: Boolean,
    attachmentsEnabled: Boolean,
    hasFailedImage: Boolean,
    isChatBlocked: Boolean,
    onMessageTextChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onImageClick: () -> Unit,
    onDocumentClick: () -> Unit,
    onEmojiClick: () -> Unit,
    onCameraClick: () -> Unit,
    onMicClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                IconButton(onClick = onEmojiClick) { Icon(Icons.Outlined.EmojiEmotions, contentDescription = null) }
                OutlinedTextField(
                    value = text, onValueChange = onMessageTextChanged, modifier = Modifier.weight(1f), enabled = enabled,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, disabledBorderColor = Color.Transparent),
                    placeholder = { Text(if (isChatBlocked) "Chat unavailable" else "Message") }
                )
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Outlined.AttachFile, contentDescription = null)
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(text = { Text("Gallery") }, leadingIcon = { Icon(Icons.Outlined.Image, null) }, onClick = { menuExpanded = false; onImageClick() })
                        DropdownMenuItem(text = { Text("Document") }, leadingIcon = { Icon(Icons.Outlined.Description, null) }, onClick = { menuExpanded = false; onDocumentClick() })
                    }
                }
                if (text.isBlank()) {
                    IconButton(onClick = onCameraClick) { Icon(Icons.Outlined.PhotoCamera, null) }
                }
            }
        }
        FloatingActionButton(
            onClick = { if (text.isNotBlank()) onSendMessage() else onMicClick() },
            containerColor = ContactMeGreen, contentColor = Color.White, shape = CircleShape, modifier = Modifier.size(48.dp),
            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
        ) {
            if (isSending) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            else Icon(if (text.isNotBlank()) Icons.AutoMirrored.Outlined.Send else Icons.Outlined.Mic, null)
        }
    }
}

@Composable
private fun CallLogContent(message: ChatMessage, isMine: Boolean) {
    Surface(color = Color.Black.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.Call, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Column {
                Text("Voice call", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(message.text.ifBlank { "No answer" }, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun EmojiPicker(onEmojiSelected: (String) -> Unit) {
    val emojis = listOf("😀", "😁", "😂", "😊", "😍", "😎", "😢", "😡", "👍", "👎", "❤️", "🤲", "✅", "❌", "🎉", "📷", "🎤", "📄")
    Surface(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Emojis", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(emojis.size) { index ->
                    Text(
                        text = emojis[index],
                        modifier = Modifier.clickable { onEmojiSelected(emojis[index]) }.padding(8.dp),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordingBar(durationMillis: Long, onStop: () -> Unit, onCancel: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Outlined.Mic, contentDescription = null, tint = Color.Red)
            Text(
                text = String.format(Locale.getDefault(), "%02d:%02d", (durationMillis / 1000) / 60, (durationMillis / 1000) % 60),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onCancel) { Text("Cancel", color = MaterialTheme.colorScheme.error) }
            IconButton(onClick = onStop) {
                Surface(shape = CircleShape, color = ContactMeGreen) {
                    Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

@Composable
private fun VoiceMessageContent(message: ChatMessage, isMine: Boolean, isPlaying: Boolean, onToggle: () -> Unit) {
    Surface(
        color = Color.Black.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Outlined.PauseCircle else Icons.Outlined.PlayCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Voice message", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                val durationStr = if (message.durationMillis > 0) {
                    val seconds = (message.durationMillis / 1000) % 60
                    val minutes = (message.durationMillis / 1000) / 60
                    String.format(Locale.getDefault(), "%02d:%02d • ", minutes, seconds)
                } else ""
                Text("$durationStr${message.fileSizeBytes.formatFileSize()}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun LoadingMessagesState() {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ChatDateSeparator(sentAtMillis: Long) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Surface(color = Color.Black.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp)) {
            Text(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), text = sentAtMillis.formatChatDate().uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ChatActionMenu(uiState: ChatDetailUiState, onReportChat: (ReportReason) -> Unit, onBlockChat: () -> Unit, onUnblockChat: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) { Icon(Icons.Outlined.MoreVert, null) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Report") }, onClick = { expanded = false; /* TODO */ })
            DropdownMenuItem(text = { Text(if (uiState.canUnblockChat) "Unblock" else "Block") }, onClick = { expanded = false; if (uiState.canUnblockChat) onUnblockChat() else onBlockChat() })
        }
    }
}

@Composable private fun ImageMessageContent(message: ChatMessage) {
    AsyncImage(model = message.mediaUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
}

@Composable private fun DocumentMessageContent(message: ChatMessage) {
    val context = LocalContext.current
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(alpha = 0.05f)).clickable { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(message.mediaUrl))) } }.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Outlined.Description, null)
        Column {
            Text(message.fileName, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(message.fileSizeBytes.formatFileSize(), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable private fun PendingImagePreview(imageUri: String, isUploading: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
        Text(if (isUploading) "Uploading image..." else "Upload failed", modifier = Modifier.weight(1f))
        if (isUploading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
    }
}

@Composable private fun PendingDocumentPreview(fileName: String, isUploading: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Outlined.Description, null)
        Text(fileName, modifier = Modifier.weight(1f))
        if (isUploading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
    }
}

@Composable private fun ChatStatusMessage(message: String) {
    Text(message, modifier = Modifier.fillMaxWidth().padding(8.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
}

@Composable private fun SendErrorMessage(message: String, canRetry: Boolean, onRetry: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(message, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.error)
        if (canRetry) TextButton(onClick = onRetry) { Text("Retry") }
    }
}

@Composable private fun MessageActionsDialog(canDelete: Boolean, canEdit: Boolean, onDismiss: () -> Unit, onReply: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Actions") }, text = { Column {
        TextButton(onClick = onReply) { Text("Reply") }
        if (canEdit) TextButton(onClick = onEdit) { Text("Edit") }
        if (canDelete) TextButton(onClick = onDelete) { Text("Delete", color = Color.Red) }
    } }, confirmButton = {}, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable private fun EditComposerPreview(onCancel: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("Editing message...", modifier = Modifier.weight(1f))
        IconButton(onClick = onCancel) { Icon(Icons.Outlined.Close, null) }
    }
}

@Composable private fun ReplyComposerPreview(reply: MessageReply, onCancel: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Replying to ${reply.senderName}", fontWeight = FontWeight.Bold)
            Text(reply.preview, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onCancel) { Icon(Icons.Outlined.Close, null) }
    }
}

@Composable private fun MessageReplyPreview(reply: MessageReply) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp).background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(4.dp)).padding(4.dp)) {
        Text(reply.senderName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
        Text(reply.preview, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable private fun ChatListStateMessage(title: String, subtitle: String, actionLabel: String? = null, onAction: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontWeight = FontWeight.Bold)
        Text(subtitle)
        actionLabel?.let { TextButton(onClick = onAction) { Text(it) } }
    }
}

private fun ChatMessage.isSeenByPeer(readReceiptState: ReadReceiptState): Boolean = readReceiptState.canShowPeerReadReceipt && sentAtMillis > 0L && readReceiptState.peerReadAtMillis >= sentAtMillis
private fun Long.formatChatTime(): String = if (this <= 0L) "Now" else SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(this))
private fun Long.formatFileSize(): String = when { this <= 0L -> "0 B"; this < 1024 * 1024 -> "${this / 1024} KB"; else -> String.format(Locale.getDefault(), "%.1f MB", this / (1024.0 * 1024.0)) }
private fun Long.isSameChatDay(other: Long): Boolean { val c1 = Calendar.getInstance().apply { timeInMillis = this@isSameChatDay }; val c2 = Calendar.getInstance().apply { timeInMillis = other }; return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR) }
private fun Long.formatChatDate(): String { val now = Calendar.getInstance(); val date = Calendar.getInstance().apply { timeInMillis = this@formatChatDate }; if (now.get(Calendar.YEAR) == date.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)) return "Today"; val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }; if (yesterday.get(Calendar.YEAR) == date.get(Calendar.YEAR) && yesterday.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)) return "Yesterday"; return SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(this)) }
private fun String.profileInitials(): String = trim().split(" ").filter { it.isNotBlank() }.take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")

@Composable private fun HeaderBackButton(onClick: () -> Unit) { IconButton(onClick = onClick) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") } }

@Preview(showBackground = true) @Composable private fun ChatDetailScreenPreview() { ContactMeTheme { ChatDetailScreen(chatName = "ContactMe User", conversationId = null, onBack = {}, onVoiceCallClick = {}, onVideoCallClick = {}) } }
