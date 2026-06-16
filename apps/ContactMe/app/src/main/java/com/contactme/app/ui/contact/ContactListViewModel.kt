package com.contactme.app.ui.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactme.app.auth.AuthRepository
import com.contactme.app.contact.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val contactRepository: ContactRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContactListUiState())
    val uiState: StateFlow<ContactListUiState> = _uiState.asStateFlow()

    init {
        observeContacts()
    }

    private fun observeContacts() {
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
            contactRepository.observeContacts(currentUserId).collect { contacts ->
                _uiState.update {
                    it.copy(
                        contacts = contacts,
                        isLoading = false,
                        message = null
                    )
                }
            }
        }
    }
}
