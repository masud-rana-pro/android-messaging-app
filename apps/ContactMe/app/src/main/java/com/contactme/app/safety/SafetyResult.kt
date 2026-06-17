package com.contactme.app.safety

sealed interface SafetyResult {
    data object Success : SafetyResult
    data class Error(val message: String) : SafetyResult
}
