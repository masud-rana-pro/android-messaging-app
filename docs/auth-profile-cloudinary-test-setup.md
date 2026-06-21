# Auth, Profile, and Cloudinary Test Setup

This guide explains how to set up and verify the authentication, profile creation, and media upload flows for development and testing.

## 1. Authentication Setup

### Recommended Path: Email/Password
For emulator testing, **Email/Password** is the most reliable method as it avoids the complexities of app verification and SMS quotas.

-   **Verification:** After login/register, the app checks if the user's profile is complete. If not, it redirects to the Profile Setup screen.

### Optional Path: Phone OTP (Emulator)
To test Phone OTP on emulators, you must configure test numbers in the Firebase Console:

1.  Go to **Firebase Console > Authentication > Sign-in method**.
2.  Enable **Phone**.
3.  Add **Phone numbers for testing (optional)**:
    -   `+880 1600 000000` / OTP: `123456`
    -   `+880 1700 000000` / OTP: `123456`
4.  Ensure your **SHA-1** and **SHA-256** fingerprints are added in **Project Settings**.

## 2. Profile Creation

-   **Required Fields:** `displayName` and `username`.
-   **Username Normalization:** The app automatically converts usernames to lowercase and removes invalid characters.
-   **Uniqueness:** A Firestore transaction ensures that usernames are unique across the platform.

## 3. Cloudinary Media Setup

The app uses **Cloudinary** for image and document storage with **Unsigned Upload Presets**.

### Manual Configuration
Ensure your Cloudinary account (`dew95musb`) has the following:
-   **Unsigned Upload Preset:** `contactme_unsigned`
-   **Security:** Never include your Cloudinary API Secret in the Android app or GitHub repository.

## 4. Two-Emulator Test Scenario

Follow these steps to verify the full flow:

### User A
1.  Open Device 1.
2.  Navigate to **Email** tab.
3.  Register: `usera@test.com` / `123456`.
4.  Set Profile: Name `User A`, Username `usera`, upload a photo.
5.  Reach Home screen.

### User B
1.  Open Device 2.
2.  Navigate to **Email** tab.
3.  Register: `userb@test.com` / `123456`.
4.  Set Profile: Name `User B`, Username `userb`, upload a photo.
5.  Reach Home screen.

### Verification
-   **Search:** User A searches for `userb` and starts a chat.
-   **Messaging:** Send a text message and verify it arrives.
-   **Calling:** Tap the Call icon to start an audio call.
