# Step 35: Profile Photo Foundation

এই step-এ আমরা ContactMe app-এ profile photo upload foundation করেছি। এখন user profile setup/edit screen থেকে image pick করতে পারবে, preview দেখতে পারবে, এবং save করলে Firebase Storage-এ upload হবে।

## 1. Profile photo কেন আলাদা feature?

Profile data যেমন display name, username, phone number Firestore-এ থাকে। কিন্তু image file Firestore-এ রাখা উচিত না।

কারণ:

- Firestore document text/metadata-এর জন্য
- image file বড় binary data
- Firebase Storage binary file রাখার জন্য তৈরি

তাই architecture:

```text
Firebase Storage -> actual image file
Firestore users/{uid}.photoUrl -> image download URL
```

## 2. Dependency যোগ করা

File:

`apps/ContactMe/app/build.gradle.kts`

নতুন dependencies:

```kotlin
implementation("io.coil-kt:coil-compose:2.7.0")
implementation("com.google.firebase:firebase-storage")
```

`firebase-storage` image upload করতে লাগে।

`coil-compose` image preview/display করতে লাগে।

## 3. FirebaseStorage provider

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/di/FirebaseModule.kt`

```kotlin
fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
```

এতে repository Hilt থেকে Storage dependency পাবে।

## 4. `photoUrl` field

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/profile/UserProfile.kt`

নতুন field:

```kotlin
val photoUrl: String = ""
```

Default empty রাখা হয়েছে যাতে পুরনো profile document ভাঙে না।

## 5. Profile save এখন `photoUrl` নেয়

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/profile/ProfileRepository.kt`

```kotlin
suspend fun saveProfile(
    userId: String,
    displayName: String,
    username: String,
    photoUrl: String
): ProfileResult
```

কারণ profile save করার সময় Firestore document-এ photoUrl রাখতে হবে।

## 6. Profile photo repository

Files:

```text
profile/ProfilePhotoRepository.kt
profile/FirebaseProfilePhotoRepository.kt
profile/FakeProfilePhotoRepository.kt
profile/ProfilePhotoResult.kt
```

Photo upload আলাদা repository-তে রাখা হয়েছে, কারণ এটা profile metadata save নয়; এটা file upload।

## 7. Upload path

Firebase Storage path:

```text
profile_photos/{uid}/profile.jpg
```

প্রতিটা user-এর একটাই current profile photo থাকবে। নতুন photo দিলে same path overwrite হবে।

## 8. Upload flow

`FirebaseProfilePhotoRepository`:

```kotlin
photoReference.putFile(photoUri).await()
photoReference.downloadUrl.await().toString()
```

Flow:

1. picked image URI নেয়
2. Storage-এ upload করে
3. download URL নেয়
4. ViewModel-কে URL দেয়
5. ViewModel profile save করার সময় URL পাঠায়

## 9. Profile UI picker

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/screens/ProfileSetupScreen.kt`

Android Photo Picker launcher:

```kotlin
rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia(),
    onResult = onPhotoSelected
)
```

Circle avatar click করলে picker open হয়।

## 10. Preview image

Selected photo থাকলে:

```kotlin
AsyncImage(
    model = photoModel,
    contentScale = ContentScale.Crop
)
```

`photoModel` হলো:

```kotlin
uiState.selectedPhotoUri.ifBlank { uiState.photoUrl }
```

মানে:

- নতুন selected photo থাকলে সেটা দেখাবে
- না থাকলে saved photoUrl দেখাবে
- দুটোই না থাকলে `Photo` placeholder দেখাবে

## 11. ViewModel save flow

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/profile/ProfileSetupViewModel.kt`

Save flow:

1. validate display name/username
2. selected photo থাকলে upload
3. upload success হলে photoUrl পাওয়া যায়
4. profile save হয় photoUrl সহ

যদি upload fail করে, profile save বন্ধ হয় এবং error দেখায়।

## 12. Storage rules

File:

`firebase/storage.rules`

Rule:

```text
profile_photos/{userId}/profile.jpg
```

Only owner can write:

```text
request.auth.uid == userId
```

Only image under 5MB:

```text
request.resource.size < 5 * 1024 * 1024
request.resource.contentType.matches('image/.*')
```

Signed-in users can read profile photos।

## 13. Deploy script update

File:

`scripts/firebase_deploy.sh`

এখন Storage rules-ও deploy করে:

```bash
firebase deploy --only firestore:rules,firestore:indexes,database,storage
```

## 14. কীভাবে verify করবে?

Build:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug
```

Manual test:

1. Profile setup/edit screen open করো।
2. circle photo area tap করো।
3. image select করো।
4. preview দেখা যায় কি না দেখো।
5. Save চাপো।
6. Firebase Storage-এ `profile_photos/{uid}/profile.jpg` আছে কি না দেখো।
7. Firestore `users/{uid}.photoUrl` fill হয়েছে কি না দেখো।

Rules deploy:

```bash
scripts/firebase_deploy.sh
```

## 15. Main learning

Profile photo feature-এ দুই ধরনের data থাকে:

- file data: Firebase Storage
- metadata/url: Firestore

এই separation professional app architecture-এর জন্য দরকার।
