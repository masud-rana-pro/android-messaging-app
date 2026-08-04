package com.contactme.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme

private val LightColorScheme = lightColorScheme(
    primary = ContactMeGreen,
    onPrimary = ContactMeLightSurface,
    primaryContainer = ContactMeGreenLight,
    onPrimaryContainer = ContactMeGreenDark,
    secondary = ContactMeCyan,
    onSecondary = ContactMeLightSurface,
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
    darkTheme: Boolean = isSystemInDarkTheme(),
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
    onPrimary = ContactMeDarkText,
    primaryContainer = ContactMeGreenDark,
    onPrimaryContainer = ContactMeDarkText,
    secondary = ContactMeCyan,
    onSecondary = ContactMeDarkText,
    secondaryContainer = ContactMeDarkSurfaceVariant,
    onSecondaryContainer = ContactMeDarkText,
    background = ContactMeDarkBackground,
    onBackground = ContactMeDarkText,
    surface = ContactMeDarkSurface,
    onSurface = ContactMeDarkText,
    surfaceVariant = ContactMeDarkSurfaceVariant
)
