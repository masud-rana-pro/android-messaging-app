package com.contactme.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = ContactMeGreen,
    onPrimary = ContactMeLightSurface,
    primaryContainer = ContactMeGreenLight,
    onPrimaryContainer = ContactMeGreenDark,
    secondaryContainer = ContactMeGreenLight,
    onSecondaryContainer = ContactMeGreenDark,
    background = ContactMeLightBackground,
    onBackground = ContactMeLightText,
    surface = ContactMeLightSurface,
    onSurface = ContactMeLightText,
    surfaceVariant = ContactMeGreenLight
)

@Composable
fun ContactMeTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = ContactMeGreen,
    onPrimary = ContactMeDarkBackground,
    primaryContainer = ContactMeGreenDark,
    onPrimaryContainer = ContactMeDarkText,
    secondaryContainer = ContactMeDarkSurfaceVariant,
    onSecondaryContainer = ContactMeDarkText,
    background = ContactMeDarkBackground,
    onBackground = ContactMeDarkText,
    surface = ContactMeDarkSurface,
    onSurface = ContactMeDarkText,
    surfaceVariant = ContactMeDarkSurfaceVariant
)
