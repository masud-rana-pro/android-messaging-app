# Step 18: Settings Profile Load And Edit Foundation

এই ধাপে ContactMe app-এর Settings screen আর static placeholder profile দেখায় না। এখন Firestore থেকে saved profile data load করে display name, username এবং initials দেখায়।

## কেন এই step দরকার

আগে Settings screen-এ hardcoded data ছিল:

```text
ContactMe User
@contactme
CM
```

সমস্যা:

- user Profile Setup-এ নাম save করলেও Settings-এ দেখা যেত না
- app-এর real data flow incomplete ছিল
- profile edit করার কোনো entry point ছিল না

এখন flow:

```text
Home
-> Settings
-> current user id নেয়
-> Firestore users/{uid} পড়ে
-> displayName + username দেখায়
```

আর edit foundation:

```text
Settings -> Edit profile -> Profile Setup -> existing data prefill -> Save changes
```

## কোন files change হয়েছে

```text
profile/UserProfile.kt
profile/ProfileRepository.kt
profile/FirebaseProfileRepository.kt
profile/FakeProfileRepository.kt
ui/settings/SettingsUiState.kt
ui/settings/SettingsViewModel.kt
ui/screens/SettingsScreen.kt
ui/profile/ProfileSetupUiState.kt
ui/profile/ProfileSetupViewModel.kt
ui/screens/ProfileSetupScreen.kt
ui/ContactMeApp.kt
docs/17-settings-profile-load.md
```

## `UserProfile.kt`

নতুন model:

```kotlin
data class UserProfile(
    val displayName: String,
    val username: String
)
```

কেন দরকার:

- Firestore document থেকে profile data app-এর ভিতরে structured ভাবে আনতে হবে
- `Map<String, Any>` UI পর্যন্ত পাঠালে code fragile হয়
- model থাকলে ViewModel/UI পরিষ্কার থাকে

## `ProfileRepository.kt`

নতুন function:

```kotlin
suspend fun getProfile(userId: String): UserProfile?
```

কেন:

- Settings screen profile পড়বে
- ProfileSetup screen edit mode-এ old value prefill করবে
- profile read logic repository-এর ভিতরে থাকবে

## `FirebaseProfileRepository.kt`

Profile read:

```kotlin
val document = firestore.collection("users")
    .document(userId)
    .get()
    .await()
```

তারপর:

```kotlin
UserProfile(
    displayName = document.getString("displayName").orEmpty(),
    username = document.getString("username").orEmpty()
)
```

কেন `orEmpty()`:

- Firestore field missing হলে app crash করবে না
- UI fallback value দেখাতে পারবে

## `FakeProfileRepository.kt`

আগে fake repository শুধু completed user id রাখত।

এখন:

```kotlin
private val profiles = mutableMapOf<String, UserProfile>()
```

কেন:

- fake mode/test mode-এও saved profile read করা যাবে
- real repository আর fake repository একই behavior follow করবে

## `SettingsUiState.kt`

State:

```kotlin
data class SettingsUiState(
    val displayName: String = "ContactMe User",
    val username: String = "contactme",
    val isLoadingProfile: Boolean = true,
    val errorMessage: String? = null
)
```

কেন:

- Settings screen loading state দেখাতে পারে
- Firestore fail/missing হলে friendly error দেখাতে পারে
- UI directly repository call করে না

## `SettingsViewModel.kt`

এই ViewModel Settings screen-এর profile load logic handle করে।

Current user id নেয়:

```kotlin
val userId = authRepository.currentUserId()
```

তারপর profile load:

```kotlin
val profile = profileRepository.getProfile(userId)
```

যদি profile পাওয়া যায়:

```kotlin
displayName = profile.displayName.ifBlank { "ContactMe User" }
username = profile.username.ifBlank { "contactme" }
```

কেন `ifBlank`:

- empty string থাকলে UI blank দেখাবে না
- fallback text থাকবে

## `SettingsScreen.kt`

আগে Settings screen নিজে static text দেখাত।

এখন:

```kotlin
val uiState by viewModel.uiState.collectAsState()
```

তারপর:

```kotlin
text = uiState.displayName
text = "@${uiState.username}"
```

Initials:

```kotlin
private fun String.profileInitials(): String
```

এটা display name থেকে first letter নেয়।

Example:

```text
Masud Rana -> MR
Masud -> M
empty -> CM
```

Edit profile button:

```kotlin
OutlinedButton(
    onClick = onEditProfile
) {
    Text(text = "Edit profile")
}
```

কেন:

- Settings থেকে profile edit করার entry point দরকার
- এখন আলাদা ProfileEditScreen বানাইনি, existing ProfileSetup screen reuse করেছি

## `ProfileSetupViewModel.kt`

নতুন behavior:

```kotlin
init {
    loadExistingProfile()
}
```

যদি saved profile থাকে:

```kotlin
displayName = profile.displayName
username = profile.username
isExistingProfile = true
```

কেন:

- Settings থেকে Edit profile করলে আগের data field-এ দেখা যাবে
- user শুধু পরিবর্তন করে save করতে পারবে

## `ProfileSetupScreen.kt`

Title dynamic:

```text
new profile -> Set up your profile
existing profile -> Edit your profile
```

Button dynamic:

```text
new profile -> Save and continue
existing profile -> Save changes
```

কেন:

- একই screen setup আর edit দুই কাজে ব্যবহার হচ্ছে
- user যেন বুঝতে পারে সে নতুন profile setup করছে নাকি edit করছে

## `ContactMeApp.kt`

Settings screen-এ নতুন callback:

```kotlin
onEditProfile = { currentScreen = AppScreen.ProfileSetup }
```

মানে:

```text
Settings -> Edit profile -> ProfileSetup
```

## কীভাবে verify করবে

Build:

```powershell
cd apps\ContactMe
.\gradlew.bat assembleDebug
```

Manual:

1. App open করো।
2. sign in করো।
3. Profile Setup-এ display name + username save করো।
4. Home থেকে Settings open করো।
5. Settings-এ saved display name, username, initials দেখা উচিত।
6. `Edit profile` চাপো।
7. Profile Setup screen open হবে এবং আগের data prefilled থাকবে।
8. data change করে save করো।
9. Settings-এ ফিরে আবার check করলে updated data দেখা উচিত।

## এখনো কী বাকি

- Edit save করার পর Settings screen-এ ফিরিয়ে আনা
- username uniqueness check
- profile photo upload
- Settings screen design polish
- Profile details Firestore realtime listener

## পরের step

আমি recommend করব:

```text
Username uniqueness + Contact discovery foundation
```

কারণ WhatsApp-like app-এ next দরকার user discovery/contact relation।
