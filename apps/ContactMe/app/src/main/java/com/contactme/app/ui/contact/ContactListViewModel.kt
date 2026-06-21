package com.contactme.app.ui.contact

import android.content.Context
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contactme.app.auth.AuthRepository
import com.contactme.app.auth.PhoneNumberFormatter
import com.contactme.app.contact.ContactRepository
import com.contactme.app.profile.ProfileRepository
import com.contactme.app.profile.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class ContactListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val contactRepository: ContactRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContactListUiState())
    val uiState: StateFlow<ContactListUiState> = _uiState.asStateFlow()

    private val _isMatchingContacts = MutableStateFlow(false)
    val isMatchingContacts: StateFlow<Boolean> = _isMatchingContacts.asStateFlow()

    private val _matchedContactMeUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val matchedContactMeUsers: StateFlow<List<UserProfile>> = _matchedContactMeUsers.asStateFlow()

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

    fun findFromPhoneContacts() {
        val currentUserId = authRepository.currentUserId() ?: return

        viewModelScope.launch {
            _isMatchingContacts.value = true
            _uiState.update { it.copy(message = "Matching your contacts...") }

            val localContacts = readLocalContacts()
            val matchedUsers = mutableListOf<UserProfile>()

            localContacts.chunked(10).forEach { batch ->
                batch.forEach { contact ->
                    contact.phoneNumber?.let { phone ->
                        val users = profileRepository.searchProfiles(phone, currentUserId)
                        matchedUsers.addAll(users)
                    }
                    contact.email?.let { email ->
                        val users = profileRepository.searchProfiles(email, currentUserId)
                        matchedUsers.addAll(users)
                    }
                }
            }

            _matchedContactMeUsers.value = matchedUsers.distinctBy { it.userId }
            _isMatchingContacts.value = false
            _uiState.update {
                it.copy(
                    message = if (matchedUsers.isEmpty()) "No ContactMe users found in your phone contacts." else null
                )
            }
        }
    }

    private suspend fun readLocalContacts(): List<LocalContact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<LocalContact>()
        val contentResolver = context.contentResolver

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            null
        )

        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val number = it.getString(numberIndex)
                val normalizedNumber = PhoneNumberFormatter.normalizeBangladeshNumber(number)
                if (normalizedNumber != null) {
                    contacts.add(LocalContact(name, normalizedNumber, null))
                }
            }
        }

        // Also emails
        val emailCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Email.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Email.ADDRESS
            ),
            null,
            null,
            null
        )

        emailCursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME)
            val addressIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val address = it.getString(addressIndex)
                contacts.add(LocalContact(name, null, address))
            }
        }

        contacts
    }

    private data class LocalContact(
        val name: String,
        val phoneNumber: String?,
        val email: String?
    )
}
