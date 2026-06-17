# Step 53 - Image Upload Preview

এই ধাপে image message পাঠানোর সময় selected photo-এর local preview দেখানোর ব্যবস্থা করেছি।

## কেন দরকার?

আগে user photo select করলে app upload শুরু করত, কিন্তু upload চলার সময় user বুঝতে পারত না কোন photo যাচ্ছে। Fail হলে retry ছিল, কিন্তু failed photo কোনটা ছিল সেটাও visually clear ছিল না।

এখন:

```text
upload চললে -> selected photo preview + Sending photo
upload fail করলে -> same photo preview + Photo not sent
```

## Fake message বানানো হয়েছে?

না।

আমরা Firestore-এ কোনো fake/pending message create করিনি। শুধু local UI state দিয়ে selected image preview দেখিয়েছি।

## 1. `pendingImageUri` state

File:

```text
apps/ContactMe/app/src/main/java/com/contactme/app/ui/chat/ChatDetailUiState.kt
```

নতুন field:

```kotlin
val pendingImageUri: String = ""
```

এটা upload চলার সময় selected image URI ধরে রাখে।

## 2. Upload start হলে pending URI set

`sendImageMessage(imageUri)` শুরু হলে:

```kotlin
pendingImageUri = imageUri.toString()
failedImageUri = ""
```

মানে app জানে এখন কোন photo upload হচ্ছে।

## 3. Success হলে preview clear

Upload success হলে:

```kotlin
pendingImageUri = ""
failedImageUri = ""
```

কারণ তখন real image message Firestore থেকে chat bubble হিসেবে চলে আসবে।

## 4. Fail হলে failed preview

Upload fail হলে:

```kotlin
pendingImageUri = ""
failedImageUri = imageUri.toString()
```

এতে user একই failed photo preview দেখতে পায় এবং retry করতে পারে।

## 5. UI preview component

File:

```text
apps/ContactMe/app/src/main/java/com/contactme/app/ui/screens/ChatDetailScreen.kt
```

নতুন composable:

```kotlin
PendingImagePreview(...)
```

এটা `AsyncImage` দিয়ে local URI দেখায়।

## Preview কোন URI দেখাবে?

```kotlin
val imagePreviewUri = uiState.pendingImageUri.ifBlank { uiState.failedImageUri }
```

মানে:

- upload চললে pending image দেখাবে
- upload fail করলে failed image দেখাবে
- কিছু না থাকলে preview দেখাবে না

## কীভাবে verify করবে?

1. Real chat open করো।
2. Image select করো।
3. Upload চলার সময় preview দেখা উচিত।
4. Internet off করে image পাঠালে error হবে।
5. Failed photo preview থাকবে।
6. Internet on করে Retry চাপলে same image upload হবে।
7. Success হলে preview clear হয়ে real chat bubble দেখা উচিত।

## শেখার বিষয়

Professional messaging app-এ media upload-এর সময় user feedback দরকার। কিন্তু pending UI দেখানোর জন্য database-এ fake message বানানো জরুরি না। প্রথম MVP-তে local state দিয়েই clean feedback দেওয়া যায়।
