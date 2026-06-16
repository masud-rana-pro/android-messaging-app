package com.contactme.app.ui.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactme.app.auth.AuthRepository
import com.contactme.app.conversation.ConversationRepository
import com.contactme.app.conversation.ConversationResult
import com.contactme.app.profile.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val conversationRepository: ConversationRepository
) : ViewModel() {
    fun openDirectConversation(
        otherUser: UserProfile,
        onReady: (conversationId: String, chatName: String) -> Unit
    ) {
        val currentUserId = authRepository.currentUserId() ?: return

        viewModelScope.launch {
            when (
                val result = conversationRepository.getOrCreateDirectConversation(
                    currentUserId = currentUserId,
                    otherUserId = otherUser.userId
                )
            ) {
                is ConversationResult.Success -> {
                    onReady(
                        result.conversationId,
                        otherUser.displayName.ifBlank { otherUser.username }
                    )
                }

                is ConversationResult.Error -> Unit
            }
        }
    }
}
