# Security Rules Plan

- Every write validates `request.auth.uid`.
- Only participants can read/write conversation messages.
- `senderId` must equal the authenticated user.
- Chat media must be limited to conversation participants.
- Blocked users cannot message or call each other.
- Reports are stored for moderation.
- Do not hardcode FCM server keys or Zego secrets in the client.
- Do not claim E2EE until it is properly implemented and tested.
