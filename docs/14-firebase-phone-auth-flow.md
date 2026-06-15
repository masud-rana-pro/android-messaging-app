# Firebase Phone Auth Flow

ContactMe now has the first Firebase Phone Auth implementation path.

## Current Flow

```text
Auth screen phone mode
-> enter mobile number
-> Send code
-> Firebase sends verification code
-> verification code input appears
-> Verify code
-> Firebase signs in
-> Profile Setup
```

## App Copy Standard

Phone auth UI should use product-friendly language instead of Firebase/internal wording.

Current UI copy:

```text
Phone number
01XXXXXXXXX
Send code
Verification code
Verify code
```

Firebase technical errors are also mapped to general user-facing messages. For example, SMS region/quota/provider errors should not expose console setup details inside the app. The app shows a short retry message, while setup instructions stay in project docs.

## Required Firebase Console Setup

Before testing real phone OTP:

1. Go to Firebase Console.
2. Open Authentication.
3. Open Sign-in method.
4. Enable Phone provider.
5. Add test phone numbers for development.

If this is not enabled, Firebase returns an operation-not-allowed error.

## Recommended Development Testing

Use Firebase test phone numbers first. This avoids SMS quota, regional SMS delivery issues, and repeated real OTP attempts.

Example:

```text
Phone: +8801XXXXXXXXX
Code: 123456
```

Use your own test number/code pair from Firebase Console.

## Bangladesh Number Format

The app accepts common Bangladesh number input and normalizes it before sending to Firebase.

Examples:

```text
01575634380      -> +8801575634380
8801575634380   -> +8801575634380
+8801575634380  -> +8801575634380
```

Firebase Phone Auth requires E.164 format:

```text
+8801575634380
```

## SHA Setup

Phone Auth may require app signing fingerprints for reliable verification.

Add these fingerprints to Firebase project settings when needed:

- SHA-1
- SHA-256

## Current Code

```text
auth/PhoneOtpResult.kt
auth/AuthRepository.kt
auth/FirebaseAuthRepository.kt
ui/auth/AuthUiState.kt
ui/auth/AuthViewModel.kt
ui/screens/AuthScreen.kt
```

## Current Limitations

- Resend OTP is not implemented yet.
- OTP timeout UI is not implemented yet.
- Account linking with email is not implemented yet.
- Phone-authenticated profile save is not implemented yet.

## Next Step

Add session restore:

```text
Splash -> if Firebase user exists -> Home/Profile decision
Splash -> if no user -> Auth
```
