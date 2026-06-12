package com.contactme.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = ContactMePurple,
    onPrimary = ContactMeBackground,
    primaryContainer = ContactMePurpleLight,
    onPrimaryContainer = ContactMePurpleDark,
    background = ContactMeBackground,
    onBackground = ContactMeText,
    surface = ContactMeBackground,
    onSurface = ContactMeText
)

@Composable
fun ContactMeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
