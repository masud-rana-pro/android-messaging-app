# Step 49 - Cloudinary Media Provider

এই ধাপে আমরা ContactMe app-এর real media upload Firebase Storage থেকে Cloudinary-তে নিয়ে এসেছি।

## কেন এই change দরকার হলো?

Firebase Storage তোমার current Firebase Spark/free setup-এ enable করা যাচ্ছে না, কারণ Firebase এখন নতুন project-এর Storage ব্যবহারের জন্য billing চাইতে পারে। কিন্তু app-এ profile photo আর chat image message দরকার।

তাই আমরা এই practical architecture নিলাম:

```text
Firebase Auth -> login/session
Firestore -> user, conversation, message metadata
Cloudinary -> actual image file
```

মানে image file Cloudinary-তে থাকবে, আর image-এর link/metadata Firestore-এ থাকবে।

## তুমি যে config দিলে

```text
cloud_name = dew95musb
upload_preset = contactme_unsigned
```

এই দুইটা Android app-এ রাখা যায়, কারণ এগুলো secret না। কিন্তু Cloudinary `api_secret` app বা GitHub-এ রাখা যাবে না।

## কোন code যোগ করা হলো?

### 1. `CloudinaryUploadClient`

এই class Cloudinary upload endpoint-এ file পাঠায়।

```kotlin
class CloudinaryUploadClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
)
```

এখানে:

- `Context` দিয়ে selected image URI থেকে file bytes পড়া হয়।
- `OkHttpClient` দিয়ে Cloudinary API-তে HTTP request পাঠানো হয়।
- `upload_preset` হিসেবে `contactme_unsigned` পাঠানো হয়।

Upload URL:

```text
https://api.cloudinary.com/v1_1/dew95musb/auto/upload
```

`auto/upload` ব্যবহার করলে Cloudinary file type নিজে detect করতে পারে।

## 2. `CloudinaryUpload`

Cloudinary upload successful হলে response থেকে আমরা এই data রাখি:

```kotlin
data class CloudinaryUpload(
    val secureUrl: String,
    val publicId: String,
    val mimeType: String
)
```

এর কাজ:

- `secureUrl`: image দেখানোর জন্য HTTPS link
- `publicId`: Cloudinary asset identity
- `mimeType`: file type, যেমন `image/jpeg`

## 3. Profile photo upload

আগে profile photo Firebase Storage repository দিয়ে upload করার plan ছিল। এখন real implementation হলো:

```text
CloudinaryProfilePhotoRepository
```

Flow:

```text
User photo select করে
-> Cloudinary upload হয়
-> secure URL পাওয়া যায়
-> Firestore users/{uid}.photoUrl update হয়
```

তাই profile image দেখাতে app শুধু `photoUrl` load করে।

## 4. Chat image message

Image message পাঠানোর সময় flow এখন:

```text
User chat থেকে image pick করে
-> CloudinaryUploadClient upload করে
-> secureUrl/publicId/mimeType পাওয়া যায়
-> Firestore message document create হয়
```

Firestore message shape:

```text
conversations/{conversationId}/messages/{messageId}
  senderId
  type: "image"
  text: ""
  mediaProvider: "cloudinary"
  mediaUrl: secure_url
  mediaPublicId: public_id
  mimeType: image/jpeg
  status: "sent"
  createdAt
```

## কেন Firestore-এ file না রেখে শুধু link রাখা হয়?

Firestore database, file storage না। Firestore-এ বড় file/image রাখা expensive এবং ভুল design।

সঠিক design:

```text
Cloudinary -> actual image binary
Firestore -> image URL and message metadata
```

## Firebase Storage dependency কেন remove করা হলো?

এখন app Firebase Storage use করছে না। তাই dependency রাখলে confusion হয় এবং future developer ভাবতে পারে Storage active আছে।

Remove করা হয়েছে:

```kotlin
implementation("com.google.firebase:firebase-storage")
```

Add করা হয়েছে:

```kotlin
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

OkHttp দিয়ে direct Cloudinary HTTP upload করা হচ্ছে।

## কীভাবে verify করবে?

1. App build হবে:

```powershell
cd apps/ContactMe
.\gradlew.bat assembleDebug
```

2. Cloudinary dashboard-এ গিয়ে check করবে:

```text
Settings / Upload presets
contactme_unsigned enabled
Unsigned mode enabled
```

3. App-এ profile photo upload করবে।

Expected:

```text
Cloudinary Media Library -> নতুন image
Firestore users/{uid}.photoUrl -> secure URL
```

4. Chat screen থেকে image send করবে।

Expected:

```text
Chat bubble image দেখাবে
Cloudinary Media Library -> নতুন image
Firestore message -> mediaProvider/mediaUrl/mediaPublicId/mimeType থাকবে
```

## গুরুত্বপূর্ণ security কথা

Unsigned preset MVP/demo/personal beta-র জন্য acceptable, কিন্তু production hardening-এর আগে signed upload করা ভালো।

Production-ready flow হবে:

```text
Android app
-> Cloud Functions থেকে signed upload signature নেয়
-> Cloudinary signed upload করে
```

এতে upload control বেশি secure হয়।

## এই step-এর commit message pattern

```text
feat(media): use cloudinary uploads
```
