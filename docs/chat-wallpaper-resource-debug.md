# Chat Wallpaper Resource Debugging

This document provides guidance on resolving issues where the chat wallpaper does not appear correctly or updates inconsistently across devices/emulators.

## Root Causes of Hidden Wallpaper
1.  **Opaque Overlays:** A parent `Surface` or `Box` with a solid background color (e.g., `MaterialTheme.colorScheme.background`) might be drawn on top of the wallpaper layer.
2.  **Conflicting Resources:** If a `chat_bg.xml` exists in `res/drawable`, it may take precedence over `chat_bg_light.png` or `chat_bg_dark.png` if not referenced explicitly.
3.  **Asset Format:** Ensure the assets are valid PNG files and placed in `drawable-nodpi` to prevent system scaling artifacts.

## Emulator Update Inconsistency
Android Studio sometimes fails to push updated resources (drawables, assets) to one of multiple running emulators during a incremental "Run".

### Resolution Steps
1.  **Uninstall & Reinstall:** Manually uninstall the app from the failing emulator and run again. This forces a full APK push including all new resources.
2.  **Clean Build:** Run `./gradlew clean` to ensure all cached resource IDs are regenerated.
3.  **Verify Resource IDs:** Check `R.drawable.chat_bg_light` and `R.drawable.chat_bg_dark` are used in `ChatDetailScreen.kt`.

## Current Asset Configuration
-   **Source:** `assets/chat-wallpapers/`
-   **Target:** `apps/ContactMe/app/src/main/res/drawable-nodpi/`
-   **Names:** `chat_bg_light.png`, `chat_bg_dark.png`

## Retest Flow
1.  Verify `chat_bg_light.png` is visible in Light Mode.
2.  Verify `chat_bg_dark.png` (or dimmed variant) is visible in Dark Mode.
3.  Confirm message bubbles are readable against the doodle pattern.
