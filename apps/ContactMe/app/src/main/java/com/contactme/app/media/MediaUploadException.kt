package com.contactme.app.media

class MediaUploadException(
    val userMessage: String
) : Exception(userMessage)
