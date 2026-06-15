# Step 17: Profile Persistence With Firestore

এই ধাপে ContactMe app-এ Profile Setup screen আর placeholder নেই। এখন user profile Firebase Firestore-এ save হয়।

## কেন এই step দরকার

আগের flow:

```text
Auth success -> Profile Setup -> Continue -> Home
```

সমস্যা:

- display name save হতো না
- username save হতো না
- app reopen করলে profile complete কিনা জানা যেত না
- signed-in user সবসময় Profile Setup-এ যেত

এই step-এর পর flow:

```text
Auth success
-> Profile Setup
-> Save display name + username
-> Firestore users/{uid}
-> Home
```

App launch flow:

```text
Splash
-> signed out হলে Auth
-> signed in + profile incomplete হলে Profile Setup
-> signed in + profile complete হলে Home
```

## কোন files change হয়েছে

```text
app/build.gradle.kts
di/FirebaseModule.kt
di/ProfileModule.kt
auth/AuthRepository.kt
auth/FirebaseAuthRepository.kt
auth/FakeAuthRepository.kt
profile/ProfileRepository.kt
profile/FirebaseProfileRepository.kt
profile/FakeProfileRepository.kt
profile/ProfileResult.kt
ui/profile/ProfileSetupUiState.kt
ui/profile/ProfileSetupViewModel.kt
ui/screens/ProfileSetupScreen.kt
ui/session/SessionViewModel.kt
ui/ContactMeApp.kt
docs/16-profile-persistence.md
```

## Firestore dependency

`app/build.gradle.kts`-এ যোগ হয়েছে:

```kotlin
implementation("com.google.firebase:firebase-firestore")
```

কেন:

- Firebase Auth শুধু login/session handle করে
- user profile save করার জন্য database দরকার
- এই step-এ Firestore ব্যবহার করা হয়েছে

## `FirebaseModule.kt`

আগে শুধু `FirebaseAuth` provide করা হতো। এখন:

```kotlin
fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
```

কেন:

- Hilt dependency injection দিয়ে repository-তে Firestore দিতে হবে
- repository নিজে `FirebaseFirestore.getInstance()` call করলে testability কমে যায়

## AuthRepository-তে `currentUserId`

নতুন function:

```kotlin
fun currentUserId(): String?
```

কেন:

- profile document save হবে current Firebase user-এর uid দিয়ে
- Firestore path হবে `users/{uid}`
- তাই auth layer থেকে current user id দরকার

## Firestore document structure

Collection:

```text
users
```

Document id:

```text
Firebase Auth uid
```

Example:

```text
users/abc123
```

Fields:

```text
displayName
username
profileComplete
createdAt
updatedAt
```

কেন document id হিসেবে uid:

- same user-এর profile খুঁজতে query লাগে না
- direct path জানা থাকে
- security rules লেখা সহজ হয়

## `ProfileRepository.kt`

Interface:

```kotlin
interface ProfileRepository {
    suspend fun isProfileComplete(userId: String): Boolean

    suspend fun saveProfile(
        userId: String,
        displayName: String,
        username: String
    ): ProfileResult
}
```

কেন আলাদা repository:

- Auth আর Profile আলাদা responsibility
- Auth login করে, Profile user data save করে
- পরে profile photo, about, status add করা সহজ হবে

## `FirebaseProfileRepository.kt`

Profile complete check:

```kotlin
firestore.collection("users")
    .document(userId)
    .get()
    .await()
    .getBoolean("profileComplete") == true
```

মানে:

- Firestore থেকে current user-এর document পড়া হয়
- `profileComplete` true হলে user profile setup শেষ করেছে

Profile save:

```kotlin
firestore.collection("users")
    .document(userId)
    .set(profileData, SetOptions.merge())
    .await()
```

`profileData`-তে আছে:

```kotlin
"displayName" to displayName.trim()
"username" to username.trim().lowercase()
"profileComplete" to true
"updatedAt" to FieldValue.serverTimestamp()
"createdAt" to FieldValue.serverTimestamp()
```

Latest code-এ `createdAt` শুধু document আগে না থাকলে যোগ হয়। আর `SetOptions.merge()` ব্যবহার করা হয়েছে, যাতে future profile field যেমন photoUrl/about/privacy settings accidentally মুছে না যায়।

`serverTimestamp()` কেন:

- device time ভুল হতে পারে
- server time reliable
- পরে sorting/audit কাজে লাগবে

## `ProfileResult.kt`

```kotlin
sealed interface ProfileResult {
    data object Success : ProfileResult
    data class Error(val message: String) : ProfileResult
}
```

কেন:

- save success নাকি fail সেটা type-safe ভাবে UI-তে পাঠানো যায়
- raw Firebase exception সরাসরি UI-তে না দেখিয়ে friendly message দেখানো যায়

## `ProfileSetupViewModel.kt`

এই ViewModel Profile Setup screen-এর business logic handle করে।

State:

```kotlin
data class ProfileSetupUiState(
    val displayName: String = "",
    val username: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

Display name update:

```kotlin
displayName = value.take(40)
```

কেন:

- খুব বড় নাম UI ভাঙতে পারে
- basic length limit রাখা হয়েছে

Username update:

```kotlin
value
    .lowercase()
    .filter { it.isLetterOrDigit() || it == '_' || it == '.' }
    .take(24)
```

কেন:

- username lowercase রাখা হয়েছে
- space/special symbol বাদ দেওয়া হয়েছে
- future user search/link-এর জন্য clean username দরকার

Validation:

```text
session না থাকলে error
display name 2 character-এর কম হলে error
username 3 character-এর কম হলে error
```

Save:

```kotlin
profileRepository.saveProfile(
    userId = userId,
    displayName = state.displayName,
    username = state.username
)
```

Success হলে:

```kotlin
onProfileReady()
```

মানে Home screen-এ যাবে।

## `ProfileSetupScreen.kt`

আগে local `remember` state ছিল:

```kotlin
var displayName by remember { mutableStateOf("") }
var username by remember { mutableStateOf("") }
```

এখন ViewModel state:

```kotlin
val uiState by viewModel.uiState.collectAsState()
```

কেন:

- screen rotate/recompose হলেও state handling ভালো হয়
- save/loading/error logic UI থেকে আলাদা থাকে
- testing সহজ হয়

Button:

```text
Save and continue
```

Loading হলে:

```text
Saving...
```

## `SessionViewModel.kt`

আগে signed-in user সবসময় ProfileSetup-এ যেত।

এখন:

```kotlin
val startScreen = when {
    userId == null -> AppScreen.Auth
    profileRepository.isProfileComplete(userId) -> AppScreen.Home
    else -> AppScreen.ProfileSetup
}
```

ব্যাখ্যা:

- user signed out হলে Auth
- user signed in এবং profile complete হলে Home
- user signed in কিন্তু profile incomplete হলে Profile Setup

## Firebase Console কাজ

এই step test করতে Firestore enabled থাকা লাগবে।

Firebase Console:

```text
Firestore Database -> Create database
```

Development-এর জন্য প্রথমে test mode ব্যবহার করা যায়। Production-এর আগে security rules লাগবে।

Rule idea:

```text
users/{userId}
allow read, write: if request.auth != null && request.auth.uid == userId;
```

মানে:

- user শুধু নিজের profile read/write করতে পারবে
- অন্য user-এর data edit করতে পারবে না

## কীভাবে verify করবে

Build:

```powershell
cd apps\ContactMe
.\gradlew.bat assembleDebug
```

Manual:

1. App open করো।
2. Phone/email দিয়ে sign in করো।
3. Profile Setup screen-এ display name দাও।
4. username দাও।
5. `Save and continue` চাপো।
6. Home screen আসা উচিত।
7. Firebase Console -> Firestore Database -> `users` collection check করো।
8. current user uid-এর document থাকা উচিত।
9. app close করে reopen করো।
10. profileComplete true থাকলে Splash-এর পর সরাসরি Home আসা উচিত।

## এখনো কী বাকি

- username uniqueness check
- profile photo upload
- saved profile Settings screen-এ দেখানো
- Firestore security rules final করা
- profile edit screen

## পরের step

Profile polish বা Contacts foundation:

```text
Option 1: Profile edit + Settings data load
Option 2: Contacts/user discovery foundation
```
