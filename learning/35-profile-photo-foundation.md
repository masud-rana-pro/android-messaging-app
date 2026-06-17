# Step 35: Profile Photo Foundation

এই step-এ ContactMe app-এ profile photo upload foundation তৈরি হয়েছিল। শুরুতে plan ছিল Firebase Storage ব্যবহার করা, কিন্তু current বাস্তব implementation এখন Cloudinary ব্যবহার করে।

## 1. Profile photo feature-এর কাজ কী?

User profile setup/edit screen থেকে image pick করতে পারে, preview দেখতে পারে, এবং save করলে সেই image upload হয়। তারপর image URL Firestore user profile document-এ save হয়।

Architecture:

```text
Cloudinary -> actual image file
Firestore users/{uid}.photoUrl -> image secure URL
```

Firestore-এ image file রাখা হয় না, কারণ Firestore metadata/document database। বড় binary file আলাদা media provider-এ রাখা professional design।

## 2. Main dependencies

File:

```text
apps/ContactMe/app/build.gradle.kts
```

বর্তমান দরকারি dependencies:

```kotlin
implementation("io.coil-kt:coil-compose:2.7.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

- `coil-compose`: image preview/display করতে লাগে।
- `okhttp`: Cloudinary API-তে upload request পাঠাতে লাগে।

## 3. `photoUrl` field

File:

```text
apps/ContactMe/app/src/main/java/com/contactme/app/profile/UserProfile.kt
```

Profile model-এ image URL রাখার জন্য field আছে:

```kotlin
val photoUrl: String = ""
```

Default empty রাখা হয়েছে যাতে পুরনো profile document ভেঙে না যায়।

## 4. Profile photo repository

Photo upload আলাদা repository-তে রাখা হয়েছে:

```text
ProfilePhotoRepository
CloudinaryProfilePhotoRepository
ProfilePhotoResult
```

কারণ profile metadata save আর image upload একই কাজ না।

Flow:

```text
ProfileSetupViewModel
-> ProfilePhotoRepository
-> CloudinaryProfilePhotoRepository
-> CloudinaryUploadClient
-> Cloudinary secure URL
-> Firestore users/{uid}.photoUrl
```

## 5. Cloudinary profile upload

`CloudinaryProfilePhotoRepository` selected image URI নেয় এবং Cloudinary-তে upload করে।

Upload success হলে:

```text
secureUrl
```

ফিরে আসে। এই URL profile save-এর সময় Firestore-এ যায়।

## 6. Profile UI picker

Profile screen Android Photo Picker ব্যবহার করে:

```kotlin
rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia(),
    onResult = onPhotoSelected
)
```

Circle avatar tap করলে picker open হয়।

## 7. Preview image

Selected photo থাকলে screen-এ আগে local selected image preview দেখায়। Saved photo থাকলে `photoUrl` থেকে image load হয়।

```text
selectedPhotoUri থাকলে -> selected preview
না থাকলে photoUrl থাকলে -> saved photo
না থাকলে -> placeholder
```

## 8. কীভাবে verify করবে?

Build:

```powershell
cd apps/ContactMe
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug
```

Manual test:

1. Profile setup/edit screen open করো।
2. Circle photo area tap করো।
3. Image select করো।
4. Preview দেখা যায় কি না দেখো।
5. Save চাপো।
6. Cloudinary Media Library-তে image আছে কি না দেখো।
7. Firestore `users/{uid}.photoUrl` secure URL দিয়ে fill হয়েছে কি না দেখো।

## 9. Main learning

Profile photo feature-এ দুই ধরনের data থাকে:

- file data: Cloudinary
- metadata/url: Firestore

এটা clean architecture, কারণ app database আর media file storage আলাদা responsibility পালন করে।
