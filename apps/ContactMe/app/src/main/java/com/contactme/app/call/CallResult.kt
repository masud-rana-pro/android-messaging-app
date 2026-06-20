package com.contactme.app.call

sealed interface CallResult {
    data class Created(val callId: String) : CallResult
    data object Success : CallResult
    data class Error(val message: String) : CallResult
}
