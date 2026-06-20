package com.contactme.app.ui.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.contactme.app.auth.AuthRepository
import com.contactme.app.conversation.ConversationRepository
import com.contactme.app.conversation.ConversationType
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
    private val documentMessageQueue: DocumentMessageQueue
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

    fun sendImageMessage(imageUri: Uri) {
        val conversationId = activeConversationId

        if (conversationId == null) {
            _uiState.update { it.copy(errorMessage = "Select a chat first.") }
            return
        }

        if (_uiState.value.isSending || _uiState.value.isChatBlocked ||
            _uiState.value.editingMessageId != null
        ) return

        val senderId = authRepository.currentUserId()

        if (senderId == null) {
            _uiState.update { it.copy(errorMessage = "Session expired. Please log in again.") }
            return
        }

        _uiState.update {
            it.copy(
                isSending = true,
                pendingImageUri = imageUri.toString(),
                failedImageUri = "",
                pendingDocumentName = "",
                failedDocumentUri = "",
                failedDocumentName = "",
                failedDocumentMimeType = "",
                errorMessage = null
            )
        }
        viewModelScope.launch {
            when (
                val result = imageMessageQueue.enqueue(
                    conversationId = conversationId,
                    senderId = senderId,
                    imageUri = imageUri
                )
            ) {
                is ImageQueueResult.Queued -> {
                    _uiState.update {
                        it.copy(
                            pendingImageUri = result.message.localUri,
                            failedImageUri = "",
                            errorMessage = null
                        )
                    }
                    observeQueuedImage(result.message)
                }

                is ImageQueueResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            pendingImageUri = "",
                            failedImageUri = imageUri.toString(),
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private suspend fun observeQueuedImage(message: QueuedImageMessage) {
        imageMessageQueue.observe(message.workId)
            .filterNotNull()
            .first { workInfo ->
                when {
                    workInfo.state == WorkInfo.State.SUCCEEDED -> {
                        updateTypingState(isTyping = false)
                        _uiState.update {
                            it.copy(
                                isSending = false,
                                pendingImageUri = "",
                                failedImageUri = "",
                                errorMessage = null
                            )
                        }
                    }

                    workInfo.state.isFinished -> {
                        _uiState.update {
                            it.copy(
                                isSending = false,
                                pendingImageUri = "",
                                failedImageUri = message.localUri,
                                errorMessage = workInfo.outputData.getString(ImageMessageQueue.ERROR_KEY)
                                    ?: "We could not send this photo. Please try again."
                            )
                        }
                    }
                }
                workInfo.state.isFinished
            }
    }

    fun retryFailedImageMessage() {
        val failedImageUri = _uiState.value.failedImageUri

        if (failedImageUri.isBlank()) return

        sendImageMessage(Uri.parse(failedImageUri))
    }

    fun sendDocumentMessage(
        documentUri: Uri,
        preferredFileName: String? = null,
        preferredMimeType: String? = null
    ) {
        val conversationId = activeConversationId ?: return
        val senderId = authRepository.currentUserId() ?: return
        if (_uiState.value.isSending || _uiState.value.isChatBlocked ||
            _uiState.value.editingMessageId != null
        ) return

        _uiState.update {
            it.copy(
                isSending = true,
                pendingDocumentName = "Preparing document",
                pendingImageUri = "",
                failedImageUri = "",
                failedDocumentUri = "",
                failedDocumentName = "",
                errorMessage = null
            )
        }
        viewModelScope.launch {
            when (
                val result = documentMessageQueue.enqueue(
                    conversationId,
                    senderId,
                    documentUri,
                    preferredFileName,
                    preferredMimeType
                )
            ) {
                is DocumentQueueResult.Queued -> {
                    _uiState.update { it.copy(pendingDocumentName = result.message.fileName) }
                    observeQueuedDocument(result.message)
                }
                is DocumentQueueResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            pendingDocumentName = "",
                            failedDocumentUri = documentUri.toString(),
                            failedDocumentName = "Document",
                            failedDocumentMimeType = preferredMimeType.orEmpty(),
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private suspend fun observeQueuedDocument(message: QueuedDocumentMessage) {
        documentMessageQueue.observe(message.workId).filterNotNull().first { workInfo ->
            when {
                workInfo.state == WorkInfo.State.SUCCEEDED -> {
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            pendingDocumentName = "",
                            failedDocumentUri = "",
                            failedDocumentName = "",
                            failedDocumentMimeType = "",
                            errorMessage = null
                        )
                    }
                }
                workInfo.state.isFinished -> {
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            pendingDocumentName = "",
                            failedDocumentUri = message.localUri,
                            failedDocumentName = message.fileName,
                            failedDocumentMimeType = message.mimeType,
                            errorMessage = workInfo.outputData.getString(DocumentMessageQueue.ERROR_KEY)
                                ?: "We could not send this document. Please try again."
                        )
                    }
                }
            }
            workInfo.state.isFinished
        }
    }

    fun retryFailedDocumentMessage() {
        val state = _uiState.value
        if (state.failedDocumentUri.isNotBlank()) {
            sendDocumentMessage(
                Uri.parse(state.failedDocumentUri),
                state.failedDocumentName.takeIf(String::isNotBlank),
                state.failedDocumentMimeType.takeIf(String::isNotBlank)
            )
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

    override fun onCleared() {
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
