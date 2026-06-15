package com.contactme.app.navigation

enum class AuthMode(
    val title: String,
    val actionLabel: String
) {
    Phone(
        title = "Continue with phone",
        actionLabel = "Send code"
    ),
    EmailLogin(
        title = "Log in with email",
        actionLabel = "Log in"
    ),
    EmailRegister(
        title = "Create email account",
        actionLabel = "Register"
    )
}
