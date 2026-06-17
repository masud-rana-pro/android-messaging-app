# Step 50 - Settings Profile Photo Preview

এই ধাপে আমরা Settings screen-এ uploaded profile photo দেখানোর bug fix করেছি।

## সমস্যা কী ছিল?

Profile photo upload হচ্ছিল, Cloudinary URL Firestore-এ save হচ্ছিল। কিন্তু Settings screen-এ avatar preview দেখা যাচ্ছিল না।

কারণ:

```text
SettingsViewModel profile load করছিল
কিন্তু SettingsUiState-এ photoUrl রাখছিল না
SettingsScreen avatar শুধু initials দেখাচ্ছিল
```

তাই upload ঠিক হলেও Settings UI image দেখানোর data পাচ্ছিল না।

## কী change করা হলো?

### 1. SettingsUiState-এ photoUrl যোগ

```kotlin
val photoUrl: String = ""
```

এখন Settings screen profile image URL state থেকে পাবে।

### 2. SettingsViewModel profile.photoUrl map করে

```kotlin
photoUrl = profile.photoUrl
```

Firestore থেকে profile load হলে `photoUrl` UI state-এ যাবে।

### 3. SettingsScreen avatar image দেখায়

যদি `photoUrl` থাকে:

```kotlin
AsyncImage(
    model = uiState.photoUrl,
    contentDescription = "Profile photo",
    contentScale = ContentScale.Crop
)
```

আর যদি `photoUrl` empty হয়, আগের মতো initials দেখাবে।

## কেন fallback initials রাখা হলো?

সব user profile photo upload করবে না। তাই photo না থাকলে avatar blank না রেখে initials দেখানো ভালো UX।

Flow:

```text
photoUrl আছে -> uploaded profile photo
photoUrl নাই -> display name initials
```

## কীভাবে verify করবে?

1. Profile edit/setup screen থেকে image upload করো।
2. Save successful হলে Settings screen-এ যাও।
3. Profile header-এর গোল avatar-এ image দেখা উচিত।
4. যদি সাথে সাথে না দেখায়, Settings থেকে back গিয়ে আবার Settings open করো।
5. Firestore `users/{uid}.photoUrl` field আছে কিনা check করো।

## শেখার বিষয়

Backend বা upload success হলেই UI automatically update হয় না। UI যে screen-এ image দেখাবে, সেই screen-এর state model-এ image URL থাকতে হবে এবং composable-এ image renderer থাকতে হবে।
