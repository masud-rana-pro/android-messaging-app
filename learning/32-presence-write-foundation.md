# Step 32: Presence Write Foundation

এই step-এ আমরা ContactMe app-এ presence write foundation করেছি। এখন signed-in user app open করলে Firebase Realtime Database-এ online হবে, app background/stop হলে offline হবে।

## 1. Presence কী?

Presence মানে user এখন online নাকি offline, আর last seen কখন।

WhatsApp-এর মতো app-এ presence দিয়ে দেখানো যায়:

- online
- last seen
- active recently
- future privacy rules

এই step-এ আমরা শুধু নিজের presence write করছি। অন্য user-এর presence observe করা পরের step।

## 2. কেন Firestore না, Realtime Database?

Presence fast এবং temporary realtime data।

Realtime Database-এর বড় সুবিধা:

```kotlin
onDisconnect()
```

App হঠাৎ internet হারালে বা close হলে Firebase নিজে offline state লিখতে পারে।

Firestore-এ `onDisconnect()` নেই। তাই presence-এর জন্য Realtime Database বেশি appropriate।

## 3. Dependency যোগ করা

File:

`apps/ContactMe/app/build.gradle.kts`

নতুন dependency:

```kotlin
implementation("com.google.firebase:firebase-database")
```

এটা Firebase Realtime Database SDK যোগ করে।

## 4. Firebase provider

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/di/FirebaseModule.kt`

নতুন provider:

```kotlin
fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()
```

Repository নিজে `FirebaseDatabase.getInstance()` call না করে Hilt থেকে পাবে। এতে dependency injection clean থাকে।

## 5. `PresenceRepository`

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/presence/PresenceRepository.kt`

```kotlin
interface PresenceRepository {
    suspend fun markOnline(userId: String)
    suspend fun markOffline(userId: String)
}
```

এখানে interface রাখা হয়েছে যেন UI/ViewModel Firebase-specific code না জানে।

## 6. Realtime Database path

Data save হবে:

```text
presence/{uid}
  isOnline
  lastSeenAt
```

Example:

```json
{
  "isOnline": true,
  "lastSeenAt": 1780000000000
}
```

`lastSeenAt` server timestamp, তাই client phone-এর ভুল time-এর উপর নির্ভর করতে হয় না।

## 7. `markOnline()`

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/presence/FirebasePresenceRepository.kt`

Online state:

```kotlin
val onlineState = mapOf(
    "isOnline" to true,
    "lastSeenAt" to ServerValue.TIMESTAMP
)
```

Offline fallback:

```kotlin
presenceReference.onDisconnect().setValue(offlineState)
```

তারপর:

```kotlin
presenceReference.setValue(onlineState)
```

এর মানে:

1. আগে disconnect handler register
2. তারপর online state write

এটা ভালো order, কারণ app online write করার পর হঠাৎ disconnect হলেও offline fallback ready থাকে।

## 8. `markOffline()`

App background/stop হলে:

```kotlin
setValue(offlineState)
```

এতে `isOnline = false` এবং `lastSeenAt` update হয়।

## 9. `PresenceViewModel`

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/presence/PresenceViewModel.kt`

ViewModel current Firebase user id নেয়:

```kotlin
val userId = authRepository.currentUserId() ?: return
```

User login না থাকলে no-op। Login থাকলে repository দিয়ে online/offline write করে।

## 10. `ContactMeApp` lifecycle observer

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/ContactMeApp.kt`

Lifecycle observe করা হয়েছে:

```kotlin
Lifecycle.Event.ON_START -> presenceViewModel.markOnline()
Lifecycle.Event.ON_STOP -> presenceViewModel.markOffline()
```

App foreground এলে online, background এলে offline।

## 11. `LaunchedEffect(currentScreen)` কেন?

```kotlin
LaunchedEffect(currentScreen) {
    presenceViewModel.markOnline()
}
```

App শুরুতে Splash/Auth অবস্থায় user নাও থাকতে পারে। Login/session restore হওয়ার পর screen বদলায়। তখন আবার `markOnline()` call করলে signed-in user-এর presence set হয়।

## 12. Realtime Database rules

File:

`firebase/database.rules.json`

Rules:

```json
"presence": {
  "$uid": {
    ".read": "auth != null",
    ".write": "auth != null && auth.uid == $uid"
  }
}
```

এর মানে:

- signed-in user presence পড়তে পারে
- user শুধু নিজের `presence/{uid}` লিখতে পারে
- অন্য user আপনার online/offline status বদলাতে পারে না

## 13. কীভাবে verify করবে?

Firebase console:

1. Realtime Database enable করো।
2. Rules deploy করো।
3. App run করে login করো।
4. Firebase Console -> Realtime Database -> `presence/{uid}` দেখো।
5. App foreground হলে `isOnline: true`।
6. App background/close করলে `isOnline: false`।

Rules deploy:

```bash
scripts/firebase_deploy.sh
```

এই step-এ root `firebase.json` যোগ করা হয়েছে, যাতে Firebase CLI rules file-গুলোর path জানে।

Deploy script এখন চালায়:

```bash
firebase deploy --only firestore:rules,firestore:indexes,database
```

Build:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug
```

## 14. Important note

এই step-এ chat header এখনো অন্য user-এর online/last seen দেখাচ্ছে না। এটা deliberate।

কারণ implementation ছোট এবং safe রাখতে আমরা আগে write foundation করলাম। পরের step-এ:

- other user presence observe
- chat header text update
- last seen formatting

করব।

## 15. Main learning

Presence feature-এ lifecycle খুব important।

User online/offline শুধু button click না, app foreground/background state-এর সাথে connected।

এই step-এ আমরা শিখলাম:

- Realtime Database dependency যোগ করা
- Hilt provider বানানো
- Repository pattern follow করা
- `onDisconnect()` দিয়ে offline fallback set করা
- Compose root app থেকে lifecycle observe করা
- Realtime Database rules দিয়ে own-user write protect করা
