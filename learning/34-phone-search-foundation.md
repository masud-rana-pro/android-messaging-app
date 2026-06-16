# Step 34: Phone Search Foundation

এই step-এ আমরা ContactMe app-এ username search-এর সাথে phone number search যোগ করেছি। যেহেতু appটা WhatsApp-এর মতো phone-primary identity follow করবে, তাই phone search খুব important।

## 1. আগে কী ছিল?

আগে discovery শুধু username দিয়ে search করত।

Example:

```text
masud
```

কিন্তু WhatsApp-like app-এ user সাধারণত phone number দিয়ে মানুষ খুঁজে। তাই এখন search input username অথবা phone number দুইটাই নিতে পারবে।

## 2. `UserProfile` update

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/profile/UserProfile.kt`

আগে:

```kotlin
data class UserProfile(
    val userId: String,
    val displayName: String,
    val username: String
)
```

এখন:

```kotlin
data class UserProfile(
    val userId: String,
    val displayName: String,
    val username: String,
    val phoneNumber: String = ""
)
```

`phoneNumber` default empty রাখা হয়েছে যাতে পুরনো code ভাঙে না।

## 3. Search function generic করা

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/profile/ProfileRepository.kt`

আগে parameter ছিল:

```kotlin
usernameQuery: String
```

এখন:

```kotlin
query: String
```

কারণ query username-ও হতে পারে, phone number-ও হতে পারে।

## 4. Profile save করার সময় phone/email রাখা

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/profile/FirebaseProfileRepository.kt`

Profile save করার সময় Firebase Auth current user থেকে নেওয়া হচ্ছে:

```kotlin
val currentUser = firebaseAuth.currentUser
```

তারপর profile data-তে:

```kotlin
"phoneNumber" to currentUser?.phoneNumber.orEmpty(),
"email" to currentUser?.email.orEmpty(),
```

এতে phone login করলে phone number save হবে। Email login করলে email save হবে।

## 5. Phone normalization

Search-এর আগে query phone number কিনা check করা হচ্ছে।

Supported Bangladesh format:

```text
01575634380
8801575634380
+8801575634380
1575634380
```

সবগুলো normalize হয়ে:

```text
+8801575634380
```

মানে Firebase Auth যেভাবে phone number রাখে, search-ও same E.164 format use করবে।

## 6. Phone search কীভাবে কাজ করে?

যদি query valid Bangladesh phone number হয়:

```kotlin
whereEqualTo("phoneNumber", normalizedPhoneQuery)
```

এটা exact match search। Phone number unique identity হিসেবে behave করবে।

## 7. Username search এখনও আছে

যদি query phone number না হয়, তাহলে আগের মতো username prefix search:

```kotlin
orderBy("username")
startAt(normalizedQuery)
endAt("$normalizedQuery\uf8ff")
```

তাই username search ভাঙেনি।

## 8. Discovery input update

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/discovery/ContactDiscoveryViewModel.kt`

আগে `+` character allow ছিল না। Phone number search করতে E.164 input `+880...` support দরকার।

এখন allowed:

```kotlin
character.isLetterOrDigit() || character == '_' || character == '.' || character == '+'
```

## 9. Home UI update

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/screens/HomeScreen.kt`

Placeholder:

```text
Search username or phone
```

Search result-এ phone number থাকলে দেখায়:

```kotlin
if (profile.phoneNumber.isNotBlank()) {
    Text(text = profile.phoneNumber)
}
```

## 10. Firestore rules update

File:

`firebase/firestore.rules`

Profile create/update এখন `phoneNumber` এবং `email` string field expect করে:

```text
request.resource.data.phoneNumber is string
request.resource.data.email is string
```

কারণ app এখন profile document-এ এই fields save করছে।

## 11. Existing user issue

যেসব user আগে profile save করেছে, তাদের document-এ phoneNumber/email field নাও থাকতে পারে।

Fix:

1. user profile edit/save করবে
2. নতুন fields merge হবে
3. তারপর phone search-এ পাওয়া যাবে

Future-এ migration/backfill করা যেতে পারে।

## 12. কীভাবে verify করবে?

Build:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug
```

Manual test:

1. Phone auth দিয়ে login করো।
2. Profile save/update করো।
3. Firebase Console -> Firestore -> `users/{uid}` check করো।
4. `phoneNumber` আছে কি না দেখো।
5. অন্য account দিয়ে phone number search করো।
6. Username search এখনও কাজ করে কি না দেখো।

Rules deploy:

```bash
scripts/firebase_deploy.sh
```

## 13. Main learning

WhatsApp-like app-এ identity শুধু username না। Phone number হলো primary discovery identity।

এই step-এ আমরা শিখলাম:

- Auth provider থেকে phone/email profile-এ save করা
- same search box দিয়ে username/phone দুইটা support করা
- phone input normalize করা
- Firestore exact match query করা
- UI-তে result information enhance করা
