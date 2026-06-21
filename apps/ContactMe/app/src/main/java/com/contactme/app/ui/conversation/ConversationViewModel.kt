package com.contactme.app.ui.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactme.app.auth.AuthRepository
import com.contactme.app.contact.ContactRepository
import com.contactme.app.conversation.ConversationRepository
import com.contactme.app.conversation.ConversationResult
import com.contactme.app.profile.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val contactRepository: ContactRepository,
    private val conversationRepository: ConversationRepository
) : ViewModel() {
    private val _isOpeningChat = MutableStateFlow(false)
    val isOpeningChat: StateFlow<Boolean> = _isOpeningChat.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun openDirectConversation(
        otherUser: UserProfile,
        onReady: (conversationId: String, chatName: String, photoUrl: String) -> Unit
    ) {
        val currentUserId = authRepository.currentUserId()

        if (currentUserId == null) {
            _errorMessage.value = "Session expired. Please log in again."
            return
        }

        if (currentUserId == otherUser.userId) {
            _errorMessage.value = "You cannot chat with yourself."
            return
        }

        viewModelScope.launch {
            _isOpeningChat.value = true
            _errorMessage.value = null

            val result = conversationRepository.getOrCreateDirectConversation(
                currentUserId = currentUserId,
                otherUserId = otherUser.userId
            )

            when (result) {
                is ConversationResult.Success -> {
                    contactRepository.saveContact(
                        ownerUserId = currentUserId,
                        contact = otherUser
                    )
                    _isOpeningChat.value = false
                    onReady(
                        result.conversationId,
                        otherUser.displayName.ifBlank { otherUser.username },
                        otherUser.photoUrl
                    )
                }

                is ConversationResult.Error -> {
                    _isOpeningChat.value = false
                    _errorMessage.value = result.message
                }
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
