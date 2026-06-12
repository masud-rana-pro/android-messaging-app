# Notification Flow

1. User sends a message or call request.
2. Firestore/Realtime Database state changes.
3. Cloud Functions validates the event.
4. FCM sends notification to receiver devices.
5. Android opens the correct screen through deep link navigation.
