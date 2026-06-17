# Step 45 - Real New Chat Entry

## Goal

Turn the Chats floating action button from a visual placeholder into a useful real new-chat entry point.

## What Changed

- The Chats FAB now triggers a new-chat search action.
- Tapping the FAB keeps the user on the Chats tab.
- The Chats content scrolls back to the top.
- The search field receives focus.
- The keyboard opens so the user can immediately search by username or phone.
- No fake chat/contact data was added.

## Verification Checklist

1. Build the app with `./gradlew.bat assembleDebug`.
2. Open the Chats screen.
3. Scroll down if there are saved contacts or conversations.
4. Tap the green FAB.
5. Confirm the screen returns to the search area.
6. Confirm the search input is focused and keyboard opens.
7. Search a real user and open a real conversation.

## Next Step

Continue with media messaging foundation or notification foundation.
