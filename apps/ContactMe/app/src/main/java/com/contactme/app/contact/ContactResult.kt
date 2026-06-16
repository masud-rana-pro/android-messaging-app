package com.contactme.app.contact

sealed interface ContactResult {
    data object Success : ContactResult
    data object Error : ContactResult
}
