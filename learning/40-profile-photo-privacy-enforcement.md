# Step 40 - Profile Photo Privacy Enforcement

এই ধাপে আমরা `Profile photo` privacy setting-কে আসল app behavior-এ apply করেছি।

আগে user settings screen থেকে `Profile photo` visibility change করা যেত, কিন্তু অন্য user search result বা chat list-এ সেই setting মানা হচ্ছিল না। এখন যদি কোনো user তার profile photo visibility `Nobody` করে, তাহলে অন্য user তার photo দেখতে পাবে না; photo-এর জায়গায় initials/avatar fallback দেখাবে।

## কেন এই কাজ দরকার?

Privacy setting শুধু UI-তে option হিসেবে থাকলেই complete হয় না। setting save হওয়ার পরে app-এর যেসব জায়গায় ওই private data দেখানো হয়, সেসব জায়গায় rule enforce করতে হয়।

এই app-এ profile photo দেখা যায় মূলত:

- user search result-এ
- conversation/chat list-এ
- নিজের profile edit/settings screen-এ

এখানে গুরুত্বপূর্ণ বিষয় হলো নিজের profile screen-এ user নিজের photo দেখতে পারবে। কিন্তু অন্য user যদি search করে বা chat list দেখে, তখন privacy মানতে হবে।

## Data field কোনটা?

Firestore `users/{uid}` document-এ এই field থাকে:

```text
profilePhotoVisibility
```

Possible value:

```text
everyone
contacts
nobody
```

আমাদের Kotlin enum:

```kotlin
enum class PrivacyVisibility(val firestoreValue: String) {
    Everyone("everyone"),
    Contacts("contacts"),
    Nobody("nobody")
}
```

এই enum-এর কাজ হলো Firestore-এর string value-কে type-safe Kotlin value হিসেবে use করা।

## Repository level-এ কেন enforce করলাম?

আমরা চাই না UI-এর প্রতিটা screen আলাদা আলাদা করে privacy logic জানুক।

ভালো flow:

```text
Firestore raw user data
        |
Repository privacy check করে
        |
UI only visible/safe data পায়
```

মানে UI শুধু `photoUrl` পাবে। যদি photo দেখানো allowed হয়, তাহলে real URL পাবে। allowed না হলে empty string পাবে।

## FirebaseProfileRepository change

Search result তৈরি করার সময় আগে সরাসরি photo URL নেওয়া হচ্ছিল:

```kotlin
photoUrl = document.getString("photoUrl").orEmpty()
```

এখন search result-এর জন্য helper use করা হচ্ছে:

```kotlin
photoUrl = document.visibleProfilePhotoUrl()
```

Helper:

```kotlin
private fun DocumentSnapshot.visibleProfilePhotoUrl(): String {
    val visibility = PrivacyVisibility.fromFirestore(getString("profilePhotoVisibility"))
    return if (visibility == PrivacyVisibility.Nobody) {
        ""
    } else {
        getString("photoUrl").orEmpty()
    }
}
```

এর মানে:

- `Nobody` হলে empty string return হবে
- অন্য setting হলে actual `photoUrl` return হবে

## নিজের profile কেন raw রাখলাম?

`getProfile(userId)` method নিজের profile edit/settings screen-এও use হতে পারে।

যদি user নিজের profile photo visibility `Nobody` করে, তবুও সে নিজের photo edit screen-এ দেখতে পারা উচিত। তাই নিজের profile load করার সময় raw `photoUrl` রাখা হয়েছে।

Privacy enforce করা হয়েছে শুধু অন্য user-কে দেখানোর জায়গায়:

- search result
- conversation preview

## FirebaseConversationRepository change

Conversation list-এ peer user-এর photo আগে সরাসরি নেওয়া হচ্ছিল:

```kotlin
photoUrl = otherUser.getString("photoUrl").orEmpty()
```

এখন privacy-aware helper use করা হচ্ছে:

```kotlin
photoUrl = otherUser.visibleProfilePhotoUrl()
```

এতে chat list-এও same rule apply হবে।

## UI কীভাবে fallback দেখায়?

আমাদের existing `ContactAvatar` component already এই logic handle করে:

```text
photoUrl blank না হলে -> image দেখাও
photoUrl blank হলে -> initials দেখাও
```

তাই UI নতুন করে complex করার দরকার হয়নি। Repository empty string দিলে UI নিজে থেকেই initials দেখাবে।

## Contacts setting এখন কীভাবে behave করছে?

`contacts` এখন temporarily visible হিসেবে behave করছে।

কারণ contact relationship model এখনো implement হয়নি। অর্থাৎ app এখনো জানে না:

- কে কার saved contact
- কে mutual contact
- কে শুধু searched user

এই model implement হলে `contacts` setting আলাদা করে enforce করা যাবে।

## কীভাবে verify করবে?

1. দুইটা user/account রাখো: User A এবং User B।
2. User B profile photo upload করো।
3. User B settings থেকে `Profile photo` visibility `Nobody` করো।
4. User A দিয়ে User B-কে username বা phone দিয়ে search করো।
5. User A যেন User B-এর photo না দেখে, initials দেখে।
6. User A chat list-এ গেলে User B-এর photo-এর জায়গায় initials দেখা উচিত।
7. User B visibility আবার `Everyone` করলে photo আবার দেখা উচিত।

## এই ধাপ থেকে শেখার বিষয়

- Privacy setting save করা আর enforce করা দুইটা আলাদা কাজ।
- Sensitive/display data repository layer থেকেই sanitize করা ভালো।
- UI fallback ঠিকভাবে বানানো থাকলে privacy change সহজ হয়।
- Own profile view আর public/peer profile view একইভাবে treat করা উচিত না।

## পরের logical কাজ

এর পরে contact relationship model implement করলে `contacts` privacy সত্যিকারভাবে enforce করা যাবে।
