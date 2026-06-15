# Step 16: Session Restore And Logout

এই ধাপে ContactMe app-এ basic session restore আর logout flow যোগ করা হয়েছে।

## কেন এই step দরকার

আগে app খুললেই flow ছিল:

```text
Splash -> Auth
```

মানে user একবার login করলেও app আবার খুললে Auth screen দেখাত। Real messaging app-এ এটা ঠিক না।

Industry app flow হওয়া উচিত:

```text
Splash -> session check -> user signed in হলে app-এর ভিতরে যাবে
Splash -> session check -> user signed out হলে Auth screen দেখাবে
```

## এই step-এ কী হয়েছে

```text
App launch
-> Splash
-> Firebase currentUser check
-> signed in হলে Profile Setup
-> signed out হলে Auth
```

আর Settings screen থেকে:

```text
Log out -> Firebase signOut -> Auth
```

## কোন files change হয়েছে

```text
auth/AuthRepository.kt
auth/FirebaseAuthRepository.kt
auth/FakeAuthRepository.kt
ui/session/SessionViewModel.kt
ui/ContactMeApp.kt
ui/screens/SettingsScreen.kt
docs/15-session-restore-and-logout.md
```

## `AuthRepository.kt`

নতুন contract:

```kotlin
fun hasActiveSession(): Boolean
fun signOut()
```

কেন দরকার:

- app জানতে চায় user already signed in কিনা
- Settings থেকে logout করতে হবে
- Firebase implementation আর fake implementation দুই জায়গায় একই interface follow করবে

Repository pattern-এর লাভ:

- UI সরাসরি `FirebaseAuth` চেনে না
- পরে Firebase বদলালেও UI code কম বদলাবে
- test/fake auth রাখা সহজ হয়

## `FirebaseAuthRepository.kt`

Session check:

```kotlin
override fun hasActiveSession(): Boolean {
    return firebaseAuth.currentUser != null
}
```

ব্যাখ্যা:

- Firebase login successful হলে local auth state ধরে রাখে
- `currentUser != null` মানে user signed in আছে
- app restart হলেও Firebase SDK এই state restore করতে পারে

Logout:

```kotlin
override fun signOut() {
    firebaseAuth.signOut()
}
```

ব্যাখ্যা:

- Firebase-এর current user clear করে
- পরের app launch-এ `currentUser` null হবে
- তাই Splash user-কে Auth screen-এ পাঠাবে

## `FakeAuthRepository.kt`

Fake repository-তে local variable যোগ হয়েছে:

```kotlin
private var signedIn = false
```

কেন:

- fake auth ব্যবহার করলে Firebase থাকে না
- তারপরও session behavior simulate করতে হবে

Successful fake login/register/OTP verification হলে:

```kotlin
signedIn = true
```

Logout হলে:

```kotlin
signedIn = false
```

## `SessionViewModel.kt`

নতুন ViewModel:

```kotlin
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel()
```

কেন আলাদা ViewModel:

- app-level session decision AuthViewModel-এর responsibility না
- AuthViewModel শুধু login/register/OTP handle করে
- SessionViewModel app start আর logout-এর decision handle করে

Start screen decision:

```kotlin
fun startScreenAfterSplash(): AppScreen {
    return if (authRepository.hasActiveSession()) {
        AppScreen.ProfileSetup
    } else {
        AppScreen.Auth
    }
}
```

এখন signed-in user কেন `ProfileSetup`-এ যাচ্ছে:

আমাদের profile save/load feature এখনো implement করা হয়নি।

তাই এখনকার safe flow:

```text
signed in -> ProfileSetup
signed out -> Auth
```

Future flow হবে:

```text
signed in + profile complete -> Home
signed in + profile incomplete -> ProfileSetup
signed out -> Auth
```

Logout:

```kotlin
fun signOut(onSignedOut: () -> Unit) {
    authRepository.signOut()
    onSignedOut()
}
```

`onSignedOut` callback দিয়ে UI screen change করে।

## `ContactMeApp.kt`

আগে Splash শেষ হলে সবসময় Auth-এ যেত:

```kotlin
onSplashFinished = { currentScreen = AppScreen.Auth }
```

এখন:

```kotlin
onSplashFinished = {
    currentScreen = sessionViewModel.startScreenAfterSplash()
}
```

মানে Splash নিজে decision নিচ্ছে না। Parent app shell decision নিচ্ছে।

Settings logout wiring:

```kotlin
onSignOut = {
    sessionViewModel.signOut {
        currentScreen = AppScreen.Auth
    }
}
```

মানে:

1. Settings screen থেকে logout click
2. SessionViewModel repository দিয়ে Firebase signOut করে
3. screen Auth-এ ফিরে যায়

## `SettingsScreen.kt`

নতুন parameter:

```kotlin
onSignOut: () -> Unit
```

নতুন button:

```kotlin
OutlinedButton(
    modifier = Modifier.fillMaxWidth(),
    onClick = onSignOut
) {
    Text(
        text = "Log out",
        color = MaterialTheme.colorScheme.error
    )
}
```

কেন `error` color:

- logout destructive/action-sensitive কাজ
- তাই visually normal settings item থেকে আলাদা হওয়া উচিত

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

Manual check:

1. App open করো।
2. Phone/email দিয়ে sign in করো।
3. Profile Setup screen এলে app close করে আবার open করো।
4. Splash-এর পর Auth না এসে Profile Setup আসা উচিত।
5. Profile Setup থেকে Home-এ যাও।
6. Settings open করো।
7. `Log out` চাপো।
8. Auth screen-এ ফিরে আসা উচিত।
9. App close করে আবার open করলে Splash-এর পর Auth screen আসা উচিত।

## এখনো কী বাকি

- profile data save/load
- profile complete কিনা check করা
- signed-in + profile complete হলে সরাসরি Home-এ যাওয়া
- logout confirmation dialog
- account delete flow

## পরের step

Profile persistence:

```text
Profile Setup form
-> save user profile in Firestore
-> Splash/session checks profile completion
-> complete হলে Home
-> incomplete হলে ProfileSetup
```
