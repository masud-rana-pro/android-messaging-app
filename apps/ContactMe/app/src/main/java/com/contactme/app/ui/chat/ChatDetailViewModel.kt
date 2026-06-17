package com.contactme.app.ui.chat

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactme.app.auth.AuthRepository
import com.contactme.app.conversation.ConversationRepository
import com.contactme.app.message.MessageRepository
import com.contactme.app.message.MessageResult
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
import kotlinx.coroutines.launch

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val conversationRepository: ConversationRepository,
    private val messageRepository: MessageRepository,
    private val typingRepository: TypingRepository,
    private val presenceRepository: PresenceRepository,
    private val safetyRepository: SafetyRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ChatDetailUiState(currentUserId = authRepository.currentUserId().orEmpty())
    )
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

    private var activeConversationId: String? = null
    private var messagesJob: Job? = null
    private var typingJob: Job? = null
    private var presenceJob: Job? = null
    private var readReceiptJob: Job? = null
    private var lastTypingValue = false

    fun openConversation(conversationId: String?) {
        if (conversationId == null || activeConversationId == conversationId) return

        activeConversationId?.let { previousConversationId ->
            updateTypingState(
                conversationId = previousConversationId,
                isTyping = false
            )
        }

        activeConversationId = conversationId
        lastTypingValue = false
        markConversationRead(conversationId)
        messagesJob?.cancel()
        typingJob?.cancel()
        presenceJob?.cancel()
        readReceiptJob?.cancel()
        _uiState.update {
            it.copy(
                messages = emptyList(),
                isLoadingMessages = true,
                isOtherUserTyping = false,
                peerPresence = com.contactme.app.presence.PresenceStatus(),
                readReceiptState = com.contactme.app.conversation.ReadReceiptState(),
                isSafetyActionInProgress = false,
                isChatBlocked = false,
                statusMessage = null,
                errorMessage = null
            )
        }
        messagesJob = viewModelScope.launch {
            messageRepository.observeMessages(conversationId).collect { messages ->
                _uiState.update {
                    it.copy(
                        messages = messages,
                        isLoadingMessages = false,
                        errorMessage = null
                    )
                }
                if (messages.isNotEmpty()) {
                    markConversationRead(conversationId)
                }
            }
        }

        val currentUserId = authRepository.currentUserId()
        if (currentUserId != null) {
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

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSending = true,
                    pendingImageUri = "",
                    failedImageUri = "",
                    errorMessage = null
                )
            }

            when (
                val result = messageRepository.sendMessage(
                    conversationId = conversationId,
                    senderId = senderId,
                    text = state.messageText
                )
            ) {
                MessageResult.Success -> {
                    updateTypingState(isTyping = false)
                    _uiState.update {
                        it.copy(
                            messageText = "",
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

    fun sendImageMessage(imageUri: Uri) {
        val conversationId = activeConversationId

        if (conversationId == null) {
            _uiState.update { it.copy(errorMessage = "Select a chat first.") }
            return
        }

        if (_uiState.value.isSending || _uiState.value.isChatBlocked) return

        val senderId = authRepository.currentUserId()

        if (senderId == null) {
            _uiState.update { it.copy(errorMessage = "Session expired. Please log in again.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSending = true,
                    pendingImageUri = imageUri.toString(),
                    failedImageUri = "",
                    errorMessage = null
                )
            }

            when (
                val result = messageRepository.sendImageMessage(
                    conversationId = conversationId,
                    senderId = senderId,
                    imageUri = imageUri
                )
            ) {
                MessageResult.Success -> {
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

                is MessageResult.Error -> {
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

    fun retryFailedImageMessage() {
        val failedImageUri = _uiState.value.failedImageUri

        if (failedImageUri.isBlank()) return

        sendImageMessage(Uri.parse(failedImageUri))
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

    fun reportCurrentChat() {
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
                    reason = ReportReason.Other
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
        activeConversationId?.let { conversationId ->
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

    private fun updateTypingState(
        conversationId: String? = activeConversationId,
        isTyping: Boolean
    ) {
        val targetConversationId = conversationId ?: return
        val userId = authRepository.currentUserId() ?: return

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
