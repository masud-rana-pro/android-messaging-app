package com.contactme.app.profile

sealed interface ProfileResult {
    data object Success : ProfileResult
    data class Error(val message: String) : ProfileResult
}
