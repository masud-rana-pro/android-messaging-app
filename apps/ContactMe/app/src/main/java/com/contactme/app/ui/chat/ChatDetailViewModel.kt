package com.contactme.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactme.app.auth.AuthRepository
import com.contactme.app.conversation.ConversationRepository
import com.contactme.app.message.MessageRepository
import com.contactme.app.message.MessageResult
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
    private val messageRepository: MessageRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ChatDetailUiState(currentUserId = authRepository.currentUserId().orEmpty())
    )
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

    private var activeConversationId: String? = null
    private var messagesJob: Job? = null

    fun openConversation(conversationId: String?) {
        if (conversationId == null || activeConversationId == conversationId) return

        activeConversationId = conversationId
        markConversationRead(conversationId)
        messagesJob?.cancel()
        _uiState.update {
            it.copy(
                messages = emptyList(),
                isLoadingMessages = true,
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
    }

    fun onMessageTextChanged(value: String) {
        _uiState.update {
            it.copy(
                messageText = value.take(MAX_MESSAGE_LENGTH),
                errorMessage = null
            )
        }
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
}
