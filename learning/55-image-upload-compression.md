# Step 55 - Image Upload Compression

এই ধাপে আমরা profile photo এবং chat image upload করার আগে local compression যোগ করেছি।

## কেন compression দরকার?

Mobile app-এ user অনেক বড় image select করতে পারে। বড় image হলে:

- upload slow হয়
- mobile data বেশি লাগে
- Cloudinary quota বেশি খরচ হয়
- chat UX heavy লাগে

তাই upload করার আগে image ছোট করা ভালো।

## কোথায় implement করা হলো?

File:

```text
apps/ContactMe/app/src/main/java/com/contactme/app/media/CloudinaryUploadClient.kt
```

এই client profile photo এবং chat image দুটো flow-তেই use হয়। তাই compression এক জায়গায় রাখলে দুটো feature benefit পায়।

## Flow

```text
selected image URI
-> file bytes read
-> validation
-> bitmap decode
-> max dimension 1600px
-> JPEG quality 82
-> compressed file smaller হলে upload
-> না হলে original upload
```

## Max dimension

```kotlin
const val MAX_UPLOAD_DIMENSION = 1600
```

মানে image-এর width/height এর মধ্যে যেটা বড়, সেটা 1600px-এর বেশি হলে resize হবে।

Example:

```text
4000 x 3000 -> 1600 x 1200
1200 x 900 -> same থাকবে
```

## JPEG quality

```kotlin
const val JPEG_UPLOAD_QUALITY = 82
```

৮২ quality হলো practical balance:

- size কমে
- quality এখনো ভালো থাকে
- profile/chat preview-এর জন্য যথেষ্ট

## কেন সবসময় compressed upload করা হয় না?

কখনো ছোট PNG/JPEG compress করলে file size উল্টো বড় হতে পারে। তাই আমরা check করি:

```kotlin
compressedBytes.size < originalBytes.size
```

শুধু compressed file ছোট হলে compressed upload করি। না হলে original upload করি।

## MIME type কীভাবে handle করা হলো?

যদি compressed JPEG upload করি:

```text
mimeType = image/jpeg
```

যদি original upload করি:

```text
mimeType = original MIME type
```

Firestore message metadata-তে actual uploaded MIME type যায়।

## কীভাবে verify করবে?

1. Profile photo upload করো।
2. Chat image send করো।
3. Image ঠিকভাবে দেখা যায় কিনা দেখো।
4. Firestore message document-এ `mimeType` আছে কিনা দেখো।
5. Build run করো:

```powershell
cd apps/ContactMe
.\gradlew.bat assembleDebug
```

## শেখার বিষয়

Media upload শুধু file পাঠানো না। ভালো app upload-এর আগে file prepare করে:

- validate
- resize
- compress
- তারপর upload

এতে app দ্রুত, কম data-consuming, এবং বেশি production-friendly হয়।
