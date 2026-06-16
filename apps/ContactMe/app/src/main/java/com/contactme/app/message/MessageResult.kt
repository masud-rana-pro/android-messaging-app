package com.contactme.app.message

sealed interface MessageResult {
    data object Success : MessageResult
    data class Error(val message: String) : MessageResult
}
