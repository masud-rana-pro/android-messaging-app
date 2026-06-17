# Step 48 - Cloudinary Secret Cleanup

এই ধাপে আমরা Cloudinary secret issue fix করেছি।

## সমস্যা কী ছিল?

তোমার তৈরি করা local onboarding script-এ Cloudinary এর sensitive value ছিল:

```text
api_key
api_secret
```

বিশেষ করে `api_secret` খুব sensitive। এটা যদি GitHub-এ চলে যায় বা app-এর ভিতরে থাকে, তাহলে অন্য কেউ তোমার Cloudinary account ব্যবহার করে upload/API কাজ করতে পারে।

## আমি কী করেছি?

1. `scripts/cloudinary_onboarding.gradle.kts` delete করেছি।
2. `.gitignore`-এ Cloudinary local secret files ignore করেছি:

```text
cloudinary.properties
cloudinary.local.properties
scripts/cloudinary_onboarding.gradle.kts
```

3. Cloudinary dependency version ঠিক করেছি:

```kotlin
implementation("com.cloudinary:cloudinary-android-core:3.1.2")
```

আগের `3.1.0` dependency build করতে সমস্যা করছিল।

4. Android build verify করেছি।

## এখন তোমার কী করতে হবে?

Cloudinary dashboard-এ গিয়ে exposed secret rotate/regenerate করো।

কেন?

কারণ secret একবার কোথাও দেখা গেলে ধরে নিতে হয় সেটা compromised হতে পারে।

## Android app-এ কী রাখা যাবে?

Android app-এ রাখা যাবে:

```text
cloud_name
unsigned upload_preset
```

Android app-এ রাখা যাবে না:

```text
api_secret
```

## Upload preset কীভাবে করবে?

Cloudinary Dashboard:

1. Settings
2. Upload
3. Upload presets
4. Add upload preset
5. Signing mode: Unsigned
6. Folder: `contactme/chats`
7. Allowed formats: `jpg,jpeg,png,webp,pdf`
8. Max file size: 5MB or 10MB
9. Save

তারপর আমাকে শুধু দেবে:

```text
cloud_name = ...
upload_preset = ...
```

## শেখার বিষয়

- Client app-এ কখনো API secret রাখা যাবে না।
- Git ignore শুধু future protection দেয়; exposed secret dashboard থেকে rotate করতে হয়।
- Unsigned preset MVP-এর জন্য workable, কিন্তু preset-এ strict limits দিতে হবে।
