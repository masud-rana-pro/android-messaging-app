package com.contactme.app.profile

data class UserProfile(
    val userId: String,
    val displayName: String,
    val username: String,
    val phoneNumber: String = ""
)
