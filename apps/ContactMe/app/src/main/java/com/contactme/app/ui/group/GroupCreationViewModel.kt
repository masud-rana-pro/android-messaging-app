package com.contactme.app.ui.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactme.app.auth.AuthRepository
import com.contactme.app.contact.ContactRepository
import com.contactme.app.conversation.ConversationRepository
import com.contactme.app.conversation.ConversationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class GroupCreationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val contactRepository: ContactRepository,
    private val conversationRepository: ConversationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupCreationUiState())
    val uiState: StateFlow<GroupCreationUiState> = _uiState.asStateFlow()

    init {
        observeContacts()
    }

    fun onTitleChanged(title: String) {
        if (title.length <= MAX_TITLE_LENGTH) {
            _uiState.update { it.copy(title = title, errorMessage = null) }
        }
    }

    fun toggleContact(userId: String) {
        _uiState.update { state ->
            val selected = state.selectedUserIds.toMutableSet()
            if (!selected.add(userId)) selected.remove(userId)
            state.copy(selectedUserIds = selected, errorMessage = null)
        }
    }

    fun createGroup(onCreated: () -> Unit) {
        val currentUserId = authRepository.currentUserId()
        if (currentUserId == null || _uiState.value.isCreating) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, errorMessage = null) }
            when (
                val result = conversationRepository.createGroupConversation(
                    currentUserId = currentUserId,
                    title = _uiState.value.title,
                    memberUserIds = _uiState.value.selectedUserIds.toList()
                )
            ) {
                is ConversationResult.Success -> {
                    _uiState.update { it.copy(isCreating = false) }
                    onCreated()
                }
                is ConversationResult.Error -> {
                    _uiState.update {
                        it.copy(isCreating = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    private fun observeContacts() {
        val userId = authRepository.currentUserId()
        if (userId == null) {
            _uiState.update {
                it.copy(isLoadingContacts = false, errorMessage = "Session expired. Please log in again.")
            }
            return
        }

        viewModelScope.launch {
            contactRepository.observeContacts(userId).collect { contacts ->
                _uiState.update { it.copy(contacts = contacts, isLoadingContacts = false) }
            }
        }
    }

    private companion object {
        const val MAX_TITLE_LENGTH = 100
    }
}
