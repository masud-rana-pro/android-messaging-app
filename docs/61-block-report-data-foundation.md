# Step 61 - Block And Report Data Foundation

## Goal

Add the real data foundation for blocking and reporting users, and make direct chat/message creation respect block relationships.

## What Changed

- Added `SafetyRepository`.
- Added `FirebaseSafetyRepository`.
- Added `SafetyResult`.
- Added `ReportReason`.
- Added Hilt `SafetyModule`.
- Added Firestore rules for:
  - `blocked_users/{uid}/items/{blockedUid}`
  - `reports/{reportId}`
- Direct conversation open now fails when either participant has blocked the other.
- Text/image message send now fails when either participant has blocked the other.

## Firestore Shape

```text
blocked_users/{uid}/items/{blockedUid}
  userId
  createdAt

reports/{reportId}
  reporterUserId
  reportedUserId
  conversationId
  reason
  status: "open"
  createdAt
```

## Verification

1. Run `./gradlew.bat assembleDebug`.
2. Manually create a block document in Firestore for a test pair.
3. Try opening/sending in that direct chat.
4. Confirm the app returns `This chat is not available.`
5. Confirm Firestore rules allow users to manage only their own block list.

## Scope

This step adds repository/data/rules enforcement. The visible chat UI buttons for block/report come next.
