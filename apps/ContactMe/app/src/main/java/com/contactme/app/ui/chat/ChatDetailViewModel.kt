package com.contactme.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactme.app.auth.AuthRepository
import com.contactme.app.conversation.ConversationRepository
import com.contactme.app.message.MessageRepository
import com.contactme.app.message.MessageResult
import com.contactme.app.presence.PresenceRepository
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
    private val presenceRepository: PresenceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ChatDetailUiState(currentUserId = authRepository.currentUserId().orEmpty())
    )
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

    private var activeConversationId: String? = null
    private var messagesJob: Job? = null
    private var typingJob: Job? = null
    private var presenceJob: Job? = null
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
        _uiState.update {
            it.copy(
                messages = emptyList(),
                isLoadingMessages = true,
                isOtherUserTyping = false,
                peerPresence = com.contactme.app.presence.PresenceStatus(),
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
        }
    }

    fun onMessageTextChanged(value: String) {
        val nextMessageText = value.take(MAX_MESSAGE_LENGTH)
        _uiState.update {
            it.copy(
                messageText = nextMessageText,
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

        if (state.isSending) return

        val senderId = authRepository.currentUserId()

        if (senderId == null) {
            _uiState.update { it.copy(errorMessage = "Session expired. Please log in again.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSending = true,
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
