# Step 59 - Roadmap State Sync

## Goal

Sync roadmap and planning docs with the implemented state after media and notification foundation work.

## What Changed

- Updated feature specification so completed foundations are no longer listed as not started.
- Updated notification flow with current client-side FCM/token/rendering state.
- Updated release checklist for profile photo, image messages, and notification foundation.
- Updated current-state roadmap:
  - typing/presence foundation done
  - FCM token sync done
  - notification channels done
  - runtime permission done
  - foreground notification renderer done
  - remaining notification work is Cloud Functions fanout and deep links

## Verification

1. Confirm docs no longer say profile photo, image messages, typing/presence, and FCM token sync are entirely not started.
2. Run `git diff --stat`.
3. Confirm only docs/learning files changed.
