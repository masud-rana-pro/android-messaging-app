package com.contactme.app.ui.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactme.app.auth.AuthRepository
import com.contactme.app.conversation.ConversationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ConversationListViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val conversationRepository: ConversationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ConversationListUiState())
    val uiState: StateFlow<ConversationListUiState> = _uiState.asStateFlow()

    init {
        observeConversations()
    }

    private fun observeConversations() {
        val currentUserId = authRepository.currentUserId()

        if (currentUserId == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    message = "Session expired. Please log in again."
                )
            }
            return
        }

        viewModelScope.launch {
            conversationRepository.observeConversationPreviews(currentUserId).collect { conversations ->
                _uiState.update {
                    it.copy(
                        conversations = conversations,
                        isLoading = false,
                        message = if (conversations.isEmpty()) {
                            "Search people to start a conversation."
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }
}
