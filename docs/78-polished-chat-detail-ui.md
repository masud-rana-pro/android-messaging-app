# Step 78: Polished Chat Detail UI

## Goal

Improve chat readability and composer ergonomics without exposing non-functional call controls.

## Implementation

- Replaced text glyph controls with Material back, more, attachment, and send icons.
- Added Today, Yesterday, and formatted date separators.
- Added a multiline message field capped at four visible lines.
- Moved attachment into the composer and kept send as a stable circular action.
- Send is enabled only when valid text is present.
- Preserved image retry, blocked-chat, and sending states.

## Verification

Run `./gradlew testDebugUnitTest assembleDebug`, then verify multi-day messages, multiline input, attachment selection, and disabled states in light/dark themes.

## Commit

`feat(chat): polish chat detail interface`
