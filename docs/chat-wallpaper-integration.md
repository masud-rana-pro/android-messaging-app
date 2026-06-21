# Chat Wallpaper Integration

This document describes the integration of the custom doodle wallpaper into the `ContactMe` chat experience.

## Overview
The chat screen now features a custom doodle background that supports both light and dark themes. This replaces the previous solid-color placeholder and provides a more polished, branded experience.

## Assets
-   **Light Mode:** `app/src/main/res/drawable-nodpi/chat_bg_light.png`
-   **Dark Mode:** `app/src/main/res/drawable-nodpi/chat_bg_dark.png`

The assets are placed in `drawable-nodpi` to avoid unnecessary scaling by the Android system, preserving the fine details of the doodle pattern.

## Implementation Details
The `ChatWallpaper` composable in `ChatDetailScreen.kt` handles the display and theme switching:

```kotlin
@Composable
private fun ChatWallpaper() {
    val isDark = isSystemInDarkTheme()
    val wallpaperRes = if (isDark) R.drawable.chat_bg_dark else R.drawable.chat_bg_light
    Image(
        painter = painterResource(id = wallpaperRes),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        alpha = if (isDark) 0.35f else 0.45f,
        colorFilter = if (isDark) ColorFilter.tint(Color.Black, BlendMode.Darken) else null
    )
}
```

### Visual Polish
-   **Alpha Blending:** The wallpaper uses a subtle alpha (0.35 - 0.45) to ensure it doesn't distract from the message bubbles.
-   **Dark Mode Optimization:** In dark mode, a `ColorFilter.tint(Color.Black, BlendMode.Darken)` is applied to further dim the background, ensuring readability and comfort in low-light environments.
-   **Content Scale:** `ContentScale.Crop` is used to ensure the wallpaper fills the entire background without distortion, regardless of screen aspect ratio.

## Manual Test Flow
1.  Open any chat.
2.  Verify the doodle wallpaper is visible behind the messages.
3.  Switch the device to **Dark Theme**.
4.  Verify the wallpaper dims significantly and the background turns dark while maintaining the doodle pattern visibility.
5.  Ensure all message text remains easily readable in both modes.
