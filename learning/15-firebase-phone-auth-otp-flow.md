# Step 15: Firebase Phone Auth OTP Flow

এই ধাপে ContactMe app-এ Firebase Phone Auth OTP flow-এর প্রথম implementation যোগ করা হয়েছে।

## কেন এই step দরকার

ContactMe WhatsApp-like app। তাই phone number primary identity হওয়া উচিত।

আগে phone screen ছিল, কিন্তু OTP real ছিল না। এখন:

```text
Phone number input -> Send OTP -> OTP input -> Verify OTP
```

flow তৈরি হয়েছে।

## কোন files change হয়েছে

```text
auth/PhoneOtpResult.kt
auth/AuthRepository.kt
auth/FakeAuthRepository.kt
auth/FirebaseAuthRepository.kt
ui/auth/AuthUiState.kt
ui/auth/AuthViewModel.kt
ui/screens/AuthScreen.kt
docs/14-firebase-phone-auth-flow.md
```

## `PhoneOtpResult`

নতুন sealed interface:

```kotlin
sealed interface PhoneOtpResult {
    data class CodeSent(val verificationId: String) : PhoneOtpResult
    data object Verified : PhoneOtpResult
    data class Error(val message: String) : PhoneOtpResult
}
```

কেন দরকার:

Phone auth-এর result শুধু success/error না।

Possible result:

- OTP code sent হয়েছে
- auto verification completed
- error হয়েছে

তাই আলাদা result type দরকার।

## `AuthRepository`

নতুন functions:

```kotlin
suspend fun requestPhoneOtp(
    phoneNumber: String,
    activity: Activity
): PhoneOtpResult

suspend fun verifyPhoneOtp(
    verificationId: String,
    otpCode: String
): AuthResult
```

কেন `Activity` দরকার:

Firebase Phone Auth `PhoneAuthOptions`-এ Activity চায়, কারণ verification callbacks app lifecycle-এর সাথে যুক্ত।

## `FirebaseAuthRepository`

OTP send:

```kotlin
PhoneAuthProvider.verifyPhoneNumber(options)
```

Important callbacks:

```kotlin
onVerificationCompleted
onVerificationFailed
onCodeSent
```

### `onCodeSent`

Firebase OTP পাঠালে verification id দেয়।

এই id পরে OTP verify করতে লাগে:

```kotlin
PhoneOtpResult.CodeSent(verificationId)
```

### `onVerificationCompleted`

কখনো Firebase auto verify করতে পারে। তখন direct sign-in হয়।

### `onVerificationFailed`

OTP send/verification fail হলে error message UI-তে দেখানো হয়।

## OTP verify

```kotlin
val credential = PhoneAuthProvider.getCredential(
    verificationId,
    otpCode
)

firebaseAuth.signInWithCredential(credential).await()
```

এখানে verification id + OTP code দিয়ে credential তৈরি হয়, তারপর Firebase sign-in হয়।

## `AuthUiState`

নতুন fields:

```kotlin
val otpCode: String = ""
val phoneVerificationId: String? = null
val statusMessage: String? = null
```

Computed property:

```kotlin
val isOtpSent: Boolean = phoneVerificationId != null
```

কেন:

- OTP sent হলে code input দেখাতে হবে
- verification id store করতে হবে
- success/info message দেখাতে হবে

## `AuthViewModel`

Phone mode submit এখন দুই ভাগ:

```text
if OTP not sent -> requestPhoneOtp
if OTP sent -> verifyPhoneOtp
```

এতে same button first time `Send OTP`, next time `Verify OTP` হিসেবে কাজ করে।

## `AuthScreen`

নতুন behavior:

- Phone mode default
- Mobile number input
- OTP sent হলে OTP code input দেখা যায়
- Button text হয় `Verify OTP`
- Status/error message দেখা যায়

Activity পাওয়ার জন্য:

```kotlin
LocalContext.current.findActivity()
```

## Firebase Console setup

Real phone auth test করতে:

1. Firebase Console open করো।
2. Authentication > Sign-in method।
3. Phone provider enable করো।
4. Development-এর জন্য test phone number add করো।

Test phone number ব্যবহার করা ভালো কারণ:

- SMS quota নষ্ট হয় না
- repeated testing সহজ
- regional SMS issue avoid হয়

## কীভাবে verify করবে

Build:

```powershell
cd apps\ContactMe
.\gradlew.bat assembleDebug
```

Expected:

```text
BUILD SUCCESSFUL
```

Manual:

1. Auth screen open করো।
2. Phone mode-এ Firebase test phone number দাও।
3. Send OTP চাপো।
4. OTP input দেখা যাবে।
5. Firebase test code দাও।
6. Verify OTP চাপো।
7. Success হলে Profile Setup screen-এ যাবে।

## এখনো কী বাকি

- Resend OTP
- OTP timeout
- session restore
- phone/email account linking
- user profile save

## পরের step

Session restore:

```text
Splash checks Firebase currentUser
if user exists -> Home/Profile
else -> Auth
```
