package com.contactme.app.ui.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactme.app.auth.AuthRepository
import com.contactme.app.profile.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ContactDiscoveryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContactDiscoveryUiState())
    val uiState: StateFlow<ContactDiscoveryUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChanged(value: String) {
        val normalizedQuery = value
            .lowercase()
            .filter { character ->
                character.isLetterOrDigit() || character == '_' || character == '.'
            }
            .take(MAX_QUERY_LENGTH)

        _uiState.update {
            it.copy(
                query = normalizedQuery,
                message = null
            )
        }

        searchJob?.cancel()

        if (normalizedQuery.length < MIN_QUERY_LENGTH) {
            _uiState.update {
                it.copy(
                    results = emptyList(),
                    isSearching = false,
                    message = null
                )
            }
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            searchProfiles(normalizedQuery)
        }
    }

    private suspend fun searchProfiles(query: String) {
        val currentUserId = authRepository.currentUserId()

        if (currentUserId == null) {
            _uiState.update {
                it.copy(
                    isSearching = false,
                    message = "Session expired. Please log in again."
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isSearching = true,
                message = null
            )
        }

        val results = profileRepository.searchProfiles(
            usernameQuery = query,
            currentUserId = currentUserId
        )

        _uiState.update {
            it.copy(
                results = results,
                isSearching = false,
                message = if (results.isEmpty()) {
                    "No users found."
                } else {
                    null
                }
            )
        }
    }

    private companion object {
        const val MIN_QUERY_LENGTH = 3
        const val MAX_QUERY_LENGTH = 24
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
