package com.contactme.app.call

sealed interface CallTokenResult {
    data class Success(val callToken: CallToken) : CallTokenResult
    data class Error(val message: String) : CallTokenResult
}
