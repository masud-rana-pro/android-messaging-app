# Firebase Phone Auth Flow

ContactMe now has the first Firebase Phone Auth implementation path.

## Current Flow

```text
Auth screen phone mode
-> enter mobile number
-> Send OTP
-> Firebase sends verification code
-> OTP input appears
-> Verify OTP
-> Firebase signs in
-> Profile Setup
```

## Required Firebase Console Setup

Before testing real phone OTP:

1. Go to Firebase Console.
2. Open Authentication.
3. Open Sign-in method.
4. Enable Phone provider.
5. Add test phone numbers for development.

## Recommended Development Testing

Use Firebase test phone numbers first. This avoids SMS quota, regional SMS delivery issues, and repeated real OTP attempts.

Example:

```text
Phone: +8801XXXXXXXXX
Code: 123456
```

Use your own test number/code pair from Firebase Console.

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
