# Step 36: Profile Photo Display Surfaces

এই step-এ আমরা uploaded profile photo app-এর main people-facing UI-তে দেখানো শুরু করেছি।

আগের step-এ upload ছিল। কিন্তু upload করলেই user experience complete হয় না। App-এর chat list/search result-এ photo দেখা দরকার।

## 1. `ConversationPreview` update

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/conversation/ConversationPreview.kt`

নতুন field:

```kotlin
val photoUrl: String
```

Conversation list-এ অন্য user-এর avatar দেখাতে এই URL দরকার।

## 2. Firebase conversation preview-তে photoUrl পড়া

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/conversation/FirebaseConversationRepository.kt`

Other user document থেকে:

```kotlin
photoUrl = otherUser.getString("photoUrl").orEmpty()
```

এর ফলে conversation preview-এর সাথে display name, last message-এর পাশাপাশি photoUrl-ও আসে।

## 3. Fake repository update

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/conversation/FakeConversationRepository.kt`

Fake preview-তে:

```kotlin
photoUrl = ""
```

এটা দিলে fake data initials fallback দেখাবে।

## 4. Reusable `ContactAvatar`

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/screens/HomeScreen.kt`

নতুন composable:

```kotlin
@Composable
private fun ContactAvatar(
    label: String,
    photoUrl: String,
    size: Int
)
```

এটা দুই জায়গায় ব্যবহার হচ্ছে:

- conversation list
- search result

## 5. Photo থাকলে কী হয়?

```kotlin
if (photoUrl.isNotBlank()) {
    AsyncImage(...)
}
```

Coil `AsyncImage` URL থেকে image load করে।

`ContentScale.Crop` দেওয়া হয়েছে যাতে circle avatar-এর ভিতরে image সুন্দর fit হয়।

## 6. Photo না থাকলে কী হয়?

```kotlin
label.profileInitials()
```

মানে photo না থাকলে আগের initials fallback থাকবে।

Example:

```text
Masud Rana -> MR
ContactMe User -> CU
```

## 7. কেন reusable composable ভালো?

আগে search result আর conversation list আলাদা avatar UI বানাচ্ছিল।

এখন same `ContactAvatar`:

- duplicate code কমায়
- UI consistent রাখে
- future online ring/badge যোগ করা সহজ করে

## 8. কোথায় এখনো বাকি?

এই step-এ সব জায়গায় photo বসানো হয়নি।

এখনো future কাজ:

- chat detail header avatar
- settings profile header
- group member avatar
- call screen avatar

## 9. কীভাবে verify করবে?

Build:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug
```

Manual test:

1. Account A profile photo upload করে save করো।
2. Account B থেকে Account A search করো।
3. Search result-এ photo দেখা যায় কি না দেখো।
4. Account A-এর সাথে chat open করো।
5. Home chat list-এ Account A-এর photo দেখা যায় কি না দেখো।

## 10. Main learning

Upload feature আর display feature আলাদা।

একটা image upload করার পরে app-এর যেসব জায়গায় user identity দেখায়, সেসব জায়গায় photoUrl pass করে image render করতে হয়।

এই step-এ আমরা profile photo data UI surface-এ connect করেছি।
