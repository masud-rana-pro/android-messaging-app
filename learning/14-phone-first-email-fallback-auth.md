# Step 14: Phone-first Auth With Email Fallback

এই ধাপে Auth UI এবং state model WhatsApp-like direction অনুযায়ী update করা হয়েছে।

## সিদ্ধান্ত

ContactMe auth strategy:

```text
Default: Phone number + OTP
Fallback: Email + password
```

## কেন phone default

WhatsApp-like app-এ phone number identity খুব important।

কারণ:

- user contact list থেকে মানুষ খুঁজতে পারে
- real-world contact identity match করে
- message/call discovery সহজ হয়
- app-এর social graph phone number দিয়ে শুরু করা যায়

## কেন email/password রাখা হয়েছে

Email/password বাদ দেওয়া হয়নি।

কারণ:

- development/testing সহজ
- fallback login method থাকে
- future web/admin/multi-device support-এ useful
- user recovery/support flow-এ helpful

## Important concept

Phone number দিয়ে মানুষ খুঁজবে, কিন্তু call সরাসরি mobile network দিয়ে হবে না।

Flow:

```text
phoneNumber -> user discovery
Firebase uid -> account identity
ZegoCloud/session id -> in-app audio/video call
```

## Code changes

Updated:

```text
navigation/AuthMode.kt
ui/auth/AuthUiState.kt
ui/auth/AuthViewModel.kt
ui/screens/AuthScreen.kt
```

Added docs:

```text
docs/13-auth-identity-strategy.md
```

Updated docs:

```text
docs/04-database-schema.md
docs/11-v02-auth-build-plan.md
```

## `AuthMode`

আগে ছিল:

```kotlin
Login
Register
```

এখন:

```kotlin
Phone
EmailLogin
EmailRegister
```

কেন:

- Phone default mode দরকার
- Email login/register fallback দরকার
- UI mode আর business operation পরিষ্কার হয়

## `AuthUiState`

এখন state:

```kotlin
data class AuthUiState(
    val authMode: AuthMode = AuthMode.Phone,
    val phoneNumber: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

Important:

- default `AuthMode.Phone`
- phone এবং email আলাদা field
- আগে `emailOrPhone` ছিল, সেটা অস্পষ্ট ছিল

## `AuthViewModel`

নতুন function:

```kotlin
fun onPhoneNumberChanged(value: String)
fun onEmailChanged(value: String)
```

Submit logic:

```kotlin
when (state.authMode) {
    AuthMode.Phone -> requestPhoneOtp(state.phoneNumber)
    AuthMode.EmailLogin -> authRepository.signIn(...)
    AuthMode.EmailRegister -> authRepository.register(...)
}
```

Phone OTP এখন placeholder:

```text
Phone OTP verification will be connected in the next auth step.
```

কারণ real Firebase Phone Auth implement করতে OTP callback, Activity context, SHA setup, এবং verification state দরকার।

## `AuthScreen`

Default screen এখন mobile number input দেখায়।

Email fallback button চাপলে email/password fields দেখা যায়।

Email mode-এ আবার:

- email login
- email register toggle

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

1. App open করো।
2. Auth screen-এ mobile number field দেখাবে।
3. `Phone` selected থাকবে।
4. `Email` চাপলে email/password fields দেখাবে।
5. Email login/register toggle কাজ করবে।
6. Phone mode-এ valid number দিলে OTP placeholder message দেখাবে।
7. Email mode-এ valid Firebase email/password দিয়ে login/register test করা যাবে।

## পরের step

Firebase Phone Auth OTP flow:

- OTP send state
- OTP verification screen/input
- Firebase phone credential sign-in
- session restore
