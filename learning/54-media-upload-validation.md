# Step 54 - Media Upload Validation

এই ধাপে আমরা media upload-এর আগে validation যোগ করেছি।

## কেন দরকার?

আগে app যেকোনো selected URI Cloudinary upload করার চেষ্টা করত। যদি file unavailable হয়, বড় হয়, বা image না হয়, তাহলে generic error দেখা যেত।

Professional app-এ user-কে clear message দিতে হয়:

```text
Only image files can be uploaded here.
Choose a photo smaller than 10 MB.
```

## 1. `MediaUploadException`

নতুন file:

```text
apps/ContactMe/app/src/main/java/com/contactme/app/media/MediaUploadException.kt
```

Code:

```kotlin
class MediaUploadException(
    val userMessage: String
) : Exception(userMessage)
```

এর কাজ হলো upload layer থেকে safe user-facing message repository/UI পর্যন্ত পাঠানো।

## 2. MIME type validation

Cloudinary upload-এর আগে app MIME type check করে:

```kotlin
if (!mimeType.startsWith("image/")) {
    throw MediaUploadException("Only image files can be uploaded here.")
}
```

মানে এই screen-এ আপাতত শুধু image allowed।

## 3. File size validation

Limit:

```text
10 MB
```

Code:

```kotlin
if (fileSizeBytes > MAX_IMAGE_SIZE_BYTES) {
    throw MediaUploadException("Choose a photo smaller than 10 MB.")
}
```

এতে বড় image upload শুরু হওয়ার আগেই app stop করে।

## 4. Chat image error message

`FirebaseMessageRepository.sendImageMessage(...)` এখন validation error হলে exact message দেখায়।

যেমন:

```text
Choose a photo smaller than 10 MB.
```

অন্য unexpected error হলে fallback:

```text
We could not send this photo. Please try again.
```

## 5. Profile photo error message

Profile photo upload-ও same validation ব্যবহার করে। তাই profile এবং chat image দুটো flow একই upload safety পায়।

## কীভাবে verify করবে?

1. Normal photo upload করো।
2. Normal chat image পাঠাও।
3. 10 MB-এর বেশি image দিলে size error আসা উচিত।
4. Non-image file দিলে image-only error আসা উচিত।
5. Build run করো:

```powershell
cd apps/ContactMe
.\gradlew.bat assembleDebug
```

## শেখার বিষয়

Validation শুধু backend/security না, UX-এরও অংশ। Upload শুরু হওয়ার আগে app যদি clear reason জানায়, user বুঝতে পারে কী ঠিক করতে হবে।
