package com.contactme.app.ui.chat

import android.util.Log
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.contactme.app.auth.AuthRepository
import com.contactme.app.conversation.ConversationRepository
import com.contactme.app.conversation.ConversationType
import com.contactme.app.call.CallType
import com.contactme.app.message.MessageRepository
import com.contactme.app.message.MessageResult
import com.contactme.app.message.ChatMessage
import com.contactme.app.message.MessageReply
import com.contactme.app.message.MessageType
import com.contactme.app.media.ImageMessageQueue
import com.contactme.app.media.ImageQueueResult
import com.contactme.app.media.QueuedImageMessage
import com.contactme.app.media.DocumentMessageQueue
import com.contactme.app.media.DocumentQueueResult
import com.contactme.app.media.QueuedDocumentMessage
import com.contactme.app.media.VoiceMessageQueue
import com.contactme.app.media.VoiceQueueResult
import com.contactme.app.media.QueuedVoiceMessage
import com.contactme.app.media.VoiceRecorder
import com.contactme.app.media.VoicePlayer
import com.contactme.app.presence.PresenceRepository
import com.contactme.app.safety.ReportReason
import com.contactme.app.safety.SafetyRepository
import com.contactme.app.safety.SafetyResult
import com.contactme.app.typing.TypingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository,
    private val typingRepository: TypingRepository,
    private val presenceRepository: PresenceRepository,
    private val safetyRepository: SafetyRepository,
    private val imageMessageQueue: ImageMessageQueue,
    private val documentMessageQueue: DocumentMessageQueue,
    private val voiceMessageQueue: VoiceMessageQueue,
    private val voiceRecorder: VoiceRecorder,
    private val voicePlayer: VoicePlayer
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ChatDetailUiState(currentUserId = authRepository.currentUserId().orEmpty())
    )
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

    private var activeConversationId: String? = null
    private var activeConversationType = ConversationType.Direct
    private var messagesJob: Job? = null
    private var typingJob: Job? = null
    private var presenceJob: Job? = null
    private var readReceiptJob: Job? = null
    private var recordingJob: Job? = null
    private var lastTypingValue = false

    fun openConversation(
        conversationId: String?,
        conversationType: ConversationType = ConversationType.Direct
    ) {
        if (
            conversationId == null ||
            (activeConversationId == conversationId && activeConversationType == conversationType)
        ) return

        activeConversationId?.takeIf { activeConversationType == ConversationType.Direct }
            ?.let { previousConversationId ->
            updateTypingState(
                conversationId = previousConversationId,
                isTyping = false
            )
        }

        activeConversationId = conversationId
        activeConversationType = conversationType
        lastTypingValue = false
        markConversationRead(conversationId)
        messagesJob?.cancel()
        typingJob?.cancel()
        presenceJob?.cancel()
        readReceiptJob?.cancel()

        val currentUserId = authRepository.currentUserId()
        val peerUserId = if (conversationType == ConversationType.Direct && currentUserId != null) {
            conversationId.split("__").firstOrNull { it != currentUserId }
        } else null

        _uiState.update {
            it.copy(
                peerUserId = peerUserId,
                messages = emptyList(),
                messageText = "",
                replyingTo = null,
                editingMessageId = null,
                isLoadingMessages = true,
                messageLoadError = null,
                isOtherUserTyping = false,
                peerPresence = com.contactme.app.presence.PresenceStatus(),
                readReceiptState = com.contactme.app.conversation.ReadReceiptState(),
                isSafetyActionInProgress = false,
                isChatBlocked = false,
                canUnblockChat = false,
                statusMessage = null,
                errorMessage = null
            )
        }
        if (conversationType == ConversationType.Direct) {
            loadSafetyState(conversationId)
        }
        observeConversationMessages(conversationId)

        if (currentUserId != null && conversationType == ConversationType.Direct) {
            typingJob = viewModelScope.launch {
                typingRepository.observeOtherTyping(
                    conversationId = conversationId,
                    currentUserId = currentUserId
                ).collect { isOtherUserTyping ->
                    _uiState.update {
                        it.copy(isOtherUserTyping = isOtherUserTyping)
                    }
                }
            }

            presenceJob = viewModelScope.launch {
                presenceRepository.observeConversationPeerPresence(
                    conversationId = conversationId,
                    currentUserId = currentUserId
                ).collect { peerPresence ->
                    _uiState.update {
                        it.copy(peerPresence = peerPresence)
                    }
                }
            }

            readReceiptJob = viewModelScope.launch {
                conversationRepository.observeReadReceiptState(
                    conversationId = conversationId,
                    currentUserId = currentUserId
                ).collect { readReceiptState ->
                    _uiState.update {
                        it.copy(readReceiptState = readReceiptState)
                    }
                }
            }
        }
    }

    private fun observeConversationMessages(conversationId: String) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            messageRepository.observeMessages(conversationId)
                .catch {
                    _uiState.update {
                        it.copy(
                            isLoadingMessages = false,
                            messageLoadError = "Messages could not be synced. Check your connection."
                        )
                    }
                }
                .collect { messages ->
                    _uiState.update {
                        it.copy(
                            messages = messages,
                            isLoadingMessages = false,
                            messageLoadError = null,
                            errorMessage = null
                        )
                    }
                    if (messages.isNotEmpty()) {
                        markConversationRead(conversationId)
                    }
                }
        }
    }

    fun retryLoadingMessages() {
        val conversationId = activeConversationId ?: return
        _uiState.update { it.copy(isLoadingMessages = true, messageLoadError = null) }
        observeConversationMessages(conversationId)
    }

    fun onMessageTextChanged(value: String) {
        val nextMessageText = value.take(MAX_MESSAGE_LENGTH)
        _uiState.update {
            it.copy(
                messageText = nextMessageText,
                statusMessage = null,
                errorMessage = null
            )
        }
        updateTypingState(isTyping = nextMessageText.isNotBlank())
    }

    fun onEmojiSelected(emoji: String) {
        _uiState.update {
            it.copy(
                messageText = it.messageText + emoji,
                errorMessage = null
            )
        }
        updateTypingState(isTyping = true)
    }

    fun setCallError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    fun startRecording(cacheDir: java.io.File) {
        val outputFile = java.io.File(cacheDir, "voice_rec_${System.currentTimeMillis()}.m4a")
        runCatching {
            voiceRecorder.start(outputFile)
            _uiState.update { it.copy(isRecording = true, recordingDurationMillis = 0L) }
            recordingJob?.cancel()
            recordingJob = viewModelScope.launch {
                val start = System.currentTimeMillis()
                while (true) {
                    kotlinx.coroutines.delay(100)
                    _uiState.update { it.copy(recordingDurationMillis = System.currentTimeMillis() - start) }
                }
            }
        }.onFailure {
            _uiState.update { it.copy(errorMessage = "Could not start recording.") }
        }
    }

    fun stopRecording() {
        Log.d(TAG, "stopRecording called")
        recordingJob?.cancel()
        val file = voiceRecorder.stop()
        _uiState.update { it.copy(isRecording = false) }
        if (file != null && file.exists()) {
            Log.d(TAG, "Voice recording stopped. File: ${file.absolutePath}, size: ${file.length()}")
            sendVoiceMessage(Uri.fromFile(file), _uiState.value.recordingDurationMillis)
        } else {
            Log.e(TAG, "Voice recording stopped but file is null or does not exist")
        }
    }

    fun cancelRecording() {
        Log.d(TAG, "cancelRecording called")
        recordingJob?.cancel()
        voiceRecorder.cancel()
        _uiState.update { it.copy(isRecording = false, recordingDurationMillis = 0L) }
    }

    private fun sendVoiceMessage(uri: Uri, duration: Long) {
        val conversationId = activeConversationId ?: return
        val senderId = authRepository.currentUserId() ?: return
        
        Log.d(TAG, "sendVoiceMessage internal. URI: $uri")
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, errorMessage = null) }
            when (val result = voiceMessageQueue.enqueue(conversationId, senderId, uri, duration)) {
                is VoiceQueueResult.Queued -> {
                    Log.d(TAG, "Voice message enqueued: ${result.message.workId}")
                    observeQueuedVoice(result.message)
                }
                is VoiceQueueResult.Error -> {
                    Log.e(TAG, "Voice message enqueue failed: ${result.message}")
                    _uiState.update { it.copy(errorMessage = result.message) }
                }
            }
            _uiState.update { it.copy(isSending = false) }
        }
    }

    private suspend fun observeQueuedVoice(message: QueuedVoiceMessage) {
        Log.d(TAG, "Observing queued voice: ${message.workId}")
        voiceMessageQueue.observe(message.workId)
            .filterNotNull()
            .first { workInfo ->
                Log.d(TAG, "Voice message workInfo state: ${workInfo.state}")
                if (workInfo.state.isFinished) {
                    if (workInfo.state != WorkInfo.State.SUCCEEDED) {
                        val error = workInfo.outputData.getString(VoiceMessageQueue.ERROR_KEY)
                            ?: "We could not send this voice message. Please try again."
                        Log.e(TAG, "Voice message worker failed: $error")
                        _uiState.update { it.copy(errorMessage = error) }
                    } else {
                        Log.d(TAG, "Voice message worker succeeded")
                    }
                    true
                } else false
            }
    }

    fun onCameraPhotoCaptured(uri: Uri) {
        Log.d(TAG, "onCameraPhotoCaptured: $uri")
        sendImageMessages(listOf(uri))
    }

    fun sendMessage() {
        val conversationId = activeConversationId
        val state = _uiState.value

        if (conversationId == null) {
            _uiState.update { it.copy(errorMessage = "Open a real conversation first.") }
            return
        }

        if (state.isSending || state.isChatBlocked) return

        val senderId = authRepository.currentUserId()

        if (senderId == null) {
            _uiState.update { it.copy(errorMessage = "Session expired. Please log in again.") }
            return
        }

        _uiState.update {
            it.copy(
                isSending = true,
                pendingImageUri = "",
                failedImageUri = "",
                errorMessage = null
            )
        }
        viewModelScope.launch {
            val result = state.editingMessageId?.let { messageId ->
                messageRepository.editMessage(
                    conversationId = conversationId,
                    messageId = messageId,
                    currentUserId = senderId,
                    text = state.messageText
                )
            } ?: messageRepository.sendMessage(
                    conversationId = conversationId,
                    senderId = senderId,
                    text = state.messageText,
                    replyTo = state.replyingTo
                )
            when (result) {
                MessageResult.Success -> {
                    updateTypingState(isTyping = false)
                    _uiState.update {
                        it.copy(
                            messageText = "",
                            replyingTo = null,
                            editingMessageId = null,
                            isSending = false,
                            pendingImageUri = "",
                            failedImageUri = "",
                            errorMessage = null
                        )
                    }
                }

                is MessageResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun startReply(message: ChatMessage) {
        if (_uiState.value.isSending || message.isDeleted) return
        _uiState.update {
            it.copy(
                messageText = "",
                editingMessageId = null,
                replyingTo = MessageReply(
                    messageId = message.id,
                    senderName = message.senderDisplayName.ifBlank {
                        if (message.senderId == it.currentUserId) "You" else "ContactMe user"
                    },
                    preview = when (message.type) {
                        MessageType.Text -> message.text.take(120)
                        MessageType.Image -> "Photo"
                        MessageType.Document -> message.fileName.ifBlank { "Document" }
                        MessageType.Call -> if (message.text.startsWith("Video call", ignoreCase = true)) {
                            "Video call"
                        } else {
                            "Voice call"
                        }
                        MessageType.Voice -> "Voice message"
                    },
                    type = message.type
                ),
                errorMessage = null
            )
        }
        updateTypingState(isTyping = false)
    }

    fun cancelReply() {
        _uiState.update { it.copy(replyingTo = null) }
    }

    fun startEdit(message: ChatMessage) {
        if (_uiState.value.isSending ||
            message.senderId != _uiState.value.currentUserId ||
            message.type != MessageType.Text || message.isDeleted
        ) return
        _uiState.update {
            it.copy(
                messageText = message.text,
                replyingTo = null,
                editingMessageId = message.id,
                errorMessage = null
            )
        }
        updateTypingState(isTyping = false)
    }

    fun cancelEdit() {
        _uiState.update { it.copy(messageText = "", editingMessageId = null, errorMessage = null) }
        updateTypingState(isTyping = false)
    }

    fun deleteMessage(message: ChatMessage) {
        val conversationId = activeConversationId ?: return
        val currentUserId = authRepository.currentUserId() ?: return
        if (message.senderId != currentUserId || message.isDeleted || _uiState.value.isSending) return

        _uiState.update { it.copy(isSending = true, errorMessage = null) }
        viewModelScope.launch {
            when (messageRepository.deleteMessage(conversationId, message.id, currentUserId)) {
                MessageResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            replyingTo = it.replyingTo?.takeUnless { reply -> reply.messageId == message.id },
                            editingMessageId = it.editingMessageId?.takeUnless { id -> id == message.id },
                            messageText = if (it.editingMessageId == message.id) "" else it.messageText,
                            statusMessage = "Message deleted."
                        )
                    }
                }
                is MessageResult.Error -> {
                    _uiState.update {
                        it.copy(isSending = false, errorMessage = "We could not delete this message.")
                    }
                }
            }
        }
    }

    fun sendImageMessages(imageUris: List<Uri>) {
        val conversationId = activeConversationId
        Log.d(TAG, "sendImageMessages called. conversationId: $conversationId, uris: ${imageUris.size}")
        if (conversationId == null) {
            _uiState.update { it.copy(errorMessage = "Select a chat first.") }
            return
        }

        val senderId = authRepository.currentUserId()
        if (senderId == null) {
            Log.e(TAG, "senderId is null")
            _uiState.update { it.copy(errorMessage = "Session expired. Please log in again.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, errorMessage = null) }
            imageUris.forEach { uri ->
                Log.d(TAG, "Enqueuing image: $uri")
                when (val result = imageMessageQueue.enqueue(conversationId, senderId, uri)) {
                    is ImageQueueResult.Queued -> observeQueuedImage(result.message)
                    is ImageQueueResult.Error -> {
                        Log.e(TAG, "Image enqueue failed: ${result.message}")
                        _uiState.update { it.copy(errorMessage = result.message) }
                    }
                }
            }
            _uiState.update { it.copy(isSending = false) }
        }
    }

    private suspend fun observeQueuedImage(message: QueuedImageMessage) {
        Log.d(TAG, "Observing queued image: ${message.workId}")
        imageMessageQueue.observe(message.workId)
            .filterNotNull()
            .first { workInfo ->
                Log.d(TAG, "Image message workInfo state: ${workInfo.state}")
                if (workInfo.state.isFinished) {
                    if (workInfo.state != WorkInfo.State.SUCCEEDED) {
                        val error = workInfo.outputData.getString(ImageMessageQueue.ERROR_KEY)
                            ?: "We could not send this photo. Please try again."
                        Log.e(TAG, "Image message worker failed: $error")
                        _uiState.update {
                            it.copy(
                                errorMessage = error
                            )
                        }
                    } else {
                        Log.d(TAG, "Image message worker succeeded")
                    }
                    true
                } else false
            }
    }

    fun sendDocumentMessages(documentUris: List<Uri>) {
        val conversationId = activeConversationId ?: return
        val senderId = authRepository.currentUserId() ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, errorMessage = null) }
            documentUris.forEach { uri ->
                when (val result = documentMessageQueue.enqueue(conversationId, senderId, uri)) {
                    is DocumentQueueResult.Queued -> observeQueuedDocument(result.message)
                    is DocumentQueueResult.Error -> _uiState.update { it.copy(errorMessage = result.message) }
                }
            }
            _uiState.update { it.copy(isSending = false) }
        }
    }

    private suspend fun observeQueuedDocument(message: QueuedDocumentMessage) {
        documentMessageQueue.observe(message.workId).filterNotNull().first { workInfo ->
            if (workInfo.state.isFinished) {
                if (workInfo.state != WorkInfo.State.SUCCEEDED) {
                    _uiState.update {
                        it.copy(
                            errorMessage = workInfo.outputData.getString(DocumentMessageQueue.ERROR_KEY)
                                ?: "We could not send this document. Please try again."
                        )
                    }
                }
                true
            } else false
        }
    }

    fun retryFailedImageMessage() {
        val failedImageUri = _uiState.value.failedImageUri
        if (failedImageUri.isBlank()) return
        sendImageMessages(listOf(Uri.parse(failedImageUri)))
    }

    fun retryFailedDocumentMessage() {
        val state = _uiState.value
        if (state.failedDocumentUri.isNotBlank()) {
            sendDocumentMessages(listOf(Uri.parse(state.failedDocumentUri)))
        }
    }

    fun blockCurrentChat() {
        val conversationId = activeConversationId
        val userId = authRepository.currentUserId()

        if (conversationId == null || userId == null) {
            _uiState.update { it.copy(errorMessage = "Open a real conversation first.") }
            return
        }

        if (_uiState.value.isSafetyActionInProgress) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSafetyActionInProgress = true,
                    statusMessage = null,
                    errorMessage = null
                )
            }

            when (
                val result = safetyRepository.blockConversationPeer(
                    currentUserId = userId,
                    conversationId = conversationId
                )
            ) {
                SafetyResult.Success -> {
                    updateTypingState(isTyping = false)
                    _uiState.update {
                        it.copy(
                            messageText = "",
                            isSafetyActionInProgress = false,
                            isChatBlocked = true,
                            canUnblockChat = true,
                            statusMessage = "User blocked.",
                            errorMessage = null
                        )
                    }
                }

                is SafetyResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSafetyActionInProgress = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun unblockCurrentChat() {
        val conversationId = activeConversationId
        val userId = authRepository.currentUserId()

        if (conversationId == null || userId == null) {
            _uiState.update { it.copy(errorMessage = "Open a real conversation first.") }
            return
        }

        if (_uiState.value.isSafetyActionInProgress) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSafetyActionInProgress = true,
                    statusMessage = null,
                    errorMessage = null
                )
            }

            when (
                val result = safetyRepository.unblockConversationPeer(
                    currentUserId = userId,
                    conversationId = conversationId
                )
            ) {
                SafetyResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSafetyActionInProgress = false,
                            isChatBlocked = false,
                            canUnblockChat = false,
                            statusMessage = "User unblocked.",
                            errorMessage = null
                        )
                    }
                }

                is SafetyResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSafetyActionInProgress = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun reportCurrentChat(reason: ReportReason) {
        val conversationId = activeConversationId
        val userId = authRepository.currentUserId()

        if (conversationId == null || userId == null) {
            _uiState.update { it.copy(errorMessage = "Open a real conversation first.") }
            return
        }

        if (_uiState.value.isSafetyActionInProgress) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSafetyActionInProgress = true,
                    statusMessage = null,
                    errorMessage = null
                )
            }

            when (
                val result = safetyRepository.reportConversationPeer(
                    reporterUserId = userId,
                    conversationId = conversationId,
                    reason = reason
                )
            ) {
                SafetyResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSafetyActionInProgress = false,
                            statusMessage = "Report sent.",
                            errorMessage = null
                        )
                    }
                }

                is SafetyResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSafetyActionInProgress = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun startGroupCall(callType: CallType, roomUrl: String, onStarted: () -> Unit) {
        val conversationId = activeConversationId
        val senderId = authRepository.currentUserId()
        if (activeConversationType != ConversationType.Group || conversationId == null || senderId == null) {
            _uiState.update { it.copy(errorMessage = "Open a group chat first.") }
            return
        }
        if (_uiState.value.isSending) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, errorMessage = null, statusMessage = null) }
            val label = if (callType == CallType.Video) "Group video call" else "Group audio call"
            when (messageRepository.sendGroupCallInvitation(conversationId, senderId, "$label · Join now", roomUrl)) {
                MessageResult.Success -> {
                    _uiState.update { it.copy(isSending = false, statusMessage = "$label started.") }
                    onStarted()
                }
                is MessageResult.Error -> {
                    _uiState.update { it.copy(isSending = false, errorMessage = "Group call could not be started.") }
                }
            }
        }
    }

    fun toggleVoiceMessagePlayback(message: ChatMessage) {
        if (message.mediaUrl.isBlank()) return
        
        if (_uiState.value.voiceMessagePlayingId == message.id) {
            voicePlayer.stop()
            _uiState.update { it.copy(voiceMessagePlayingId = null) }
        } else {
            voicePlayer.play(message.id, message.mediaUrl) {
                _uiState.update { it.copy(voiceMessagePlayingId = null) }
            }
            _uiState.update { it.copy(voiceMessagePlayingId = message.id) }
        }
    }

    override fun onCleared() {
        voicePlayer.stop()
        activeConversationId?.takeIf { activeConversationType == ConversationType.Direct }
            ?.let { conversationId ->
            updateTypingState(
                conversationId = conversationId,
                isTyping = false
            )
        }
        super.onCleared()
    }

    private companion object {
        const val TAG = "ChatDetailViewModel"
        const val MAX_MESSAGE_LENGTH = 4000
    }

    private fun markConversationRead(conversationId: String) {
        val userId = authRepository.currentUserId() ?: return

        viewModelScope.launch {
            conversationRepository.markConversationRead(
                conversationId = conversationId,
                userId = userId
            )
        }
    }

    private fun loadSafetyState(conversationId: String) {
        val userId = authRepository.currentUserId() ?: return

        viewModelScope.launch {
            val isBlocked = safetyRepository.hasBlockInConversation(
                currentUserId = userId,
                conversationId = conversationId
            )
            val canUnblock = safetyRepository.hasCurrentUserBlockedConversationPeer(
                currentUserId = userId,
                conversationId = conversationId
            )

            _uiState.update {
                it.copy(
                    isChatBlocked = isBlocked,
                    canUnblockChat = canUnblock,
                    statusMessage = if (isBlocked) {
                        "Chat unavailable."
                    } else {
                        it.statusMessage
                    }
                )
            }
        }
    }

    private fun updateTypingState(
        conversationId: String? = activeConversationId,
        isTyping: Boolean
    ) {
        val targetConversationId = conversationId ?: return
        val userId = authRepository.currentUserId() ?: return
        if (activeConversationType != ConversationType.Direct) return

        if (conversationId == activeConversationId && lastTypingValue == isTyping) return
        if (conversationId == activeConversationId) {
            lastTypingValue = isTyping
        }

        viewModelScope.launch {
            typingRepository.setTyping(
                conversationId = targetConversationId,
                userId = userId,
                isTyping = isTyping
            )
        }
    }
}
