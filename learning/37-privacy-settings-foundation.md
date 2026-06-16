# Step 37: Privacy Settings Foundation

এই step-এ আমরা ContactMe app-এ privacy settings foundation করেছি। এখন user Settings screen থেকে last seen, profile photo visibility, এবং read receipts preference save করতে পারবে।

## 1. Privacy settings কেন দরকার?

WhatsApp-like app-এ user control খুব important।

User ঠিক করতে চাইতে পারে:

- কে আমার last seen দেখবে
- কে আমার profile photo দেখবে
- read receipts on/off থাকবে কি না

এই step-এ আমরা preference save করছি। Enforcement later step-এ করব।

## 2. `PrivacyVisibility`

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/profile/PrivacyVisibility.kt`

Values:

```kotlin
Everyone("everyone")
Contacts("contacts")
Nobody("nobody")
```

এখানে enum ব্যবহার করা হয়েছে যাতে raw string ছড়িয়ে না থাকে।

## 3. `next()` function

```kotlin
fun next(): PrivacyVisibility
```

Settings UI-তে tap করলে value cycle করে:

```text
Everyone -> Contacts -> Nobody -> Everyone
```

এটা compact UI foundation। Later আলাদা selection screen করা যাবে।

## 4. `PrivacySettings`

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/profile/PrivacySettings.kt`

```kotlin
data class PrivacySettings(
    val lastSeenVisibility: PrivacyVisibility = PrivacyVisibility.Everyone,
    val profilePhotoVisibility: PrivacyVisibility = PrivacyVisibility.Everyone,
    val readReceiptsEnabled: Boolean = true
)
```

Default values user-friendly রাখা হয়েছে।

## 5. Repository update

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/profile/ProfileRepository.kt`

নতুন functions:

```kotlin
suspend fun getPrivacySettings(userId: String): PrivacySettings
suspend fun savePrivacySettings(userId: String, privacySettings: PrivacySettings): ProfileResult
```

Settings ViewModel সরাসরি Firestore জানে না; repository দিয়ে load/save করে।

## 6. Firebase save

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/profile/FirebaseProfileRepository.kt`

Privacy data save হয়:

```kotlin
"lastSeenVisibility" to privacySettings.lastSeenVisibility.firestoreValue
"profilePhotoVisibility" to privacySettings.profilePhotoVisibility.firestoreValue
"readReceiptsEnabled" to privacySettings.readReceiptsEnabled
```

Firestore path:

```text
users/{uid}
```

## 7. Profile save defaults

Profile save করার সময় privacy defaults রাখা হয়েছে, কিন্তু existing settings থাকলে reset করা হয় না।

কারণ user যদি profile edit করে, তার privacy settings হঠাৎ default হয়ে যাওয়া উচিত না।

## 8. Settings UI state

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/settings/SettingsUiState.kt`

নতুন fields:

```kotlin
val privacySettings: PrivacySettings = PrivacySettings()
val isSavingPrivacy: Boolean = false
```

UI save চলার সময় controls disable করতে পারে।

## 9. Settings ViewModel

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/settings/SettingsViewModel.kt`

নতুন actions:

```kotlin
cycleLastSeenVisibility()
cycleProfilePhotoVisibility()
toggleReadReceipts()
```

প্রতিটা action new settings তৈরি করে repository দিয়ে save করে।

## 10. Settings screen controls

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/screens/SettingsScreen.kt`

UI এখন দেখায়:

- Last seen: Everyone/Contacts/Nobody
- Profile photo: Everyone/Contacts/Nobody
- Read receipts: Switch

## 11. Firestore rules

File:

`firebase/firestore.rules`

Rules privacy fields type validate করে যখন fields থাকে।

এই compatibility দরকার কারণ old profile documents-এ সব field নাও থাকতে পারে।

## 12. এখনো কী enforce হয়নি?

এই step শুধু settings save করে।

এখনো বাকি:

- last seen visibility chat header-এ enforce করা
- profile photo visibility avatar loading-এ enforce করা
- read receipts off হলে read marker update বন্ধ করা
- contacts-only meaning define করা

## 13. কীভাবে verify করবে?

Build:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug
```

Manual test:

1. Settings open করো।
2. Last seen button tap করে value cycle হয় কি না দেখো।
3. Profile photo button tap করে value cycle হয় কি না দেখো।
4. Read receipts switch toggle করো।
5. Firestore `users/{uid}` document check করো।

Deploy:

```bash
scripts/firebase_deploy.sh
```

## 14. Main learning

Privacy feature দুই ধাপে হয়:

1. preference save
2. app behavior-এ preference enforce

এই step হলো foundation। পরের privacy steps-এ এই saved settings বাস্তব behavior control করবে।
