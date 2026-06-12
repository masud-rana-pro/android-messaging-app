package com.contactme.app.navigation

enum class AuthMode(val title: String, val actionLabel: String) {
    Login(
        title = "Welcome back",
        actionLabel = "Log in"
    ),
    Register(
        title = "Create account",
        actionLabel = "Register"
    )
}
