# Step 19: Username Uniqueness And Discovery Foundation

এই ধাপে ContactMe app-এ দুইটা গুরুত্বপূর্ণ foundation যোগ হয়েছে:

- username unique রাখা
- username দিয়ে app user খোঁজার প্রথম discovery UI

## কেন এই step দরকার

Messaging app-এ মানুষকে খুঁজতে identity দরকার। Phone number primary identity হলেও app-এর ভিতরে username দিয়ে search করা user-friendly।

আগে সমস্যা ছিল:

- দুই user একই username নিতে পারত
- Chats tab-এ real user search ছিল না
- Profile save শুধু `users/{uid}` document লিখত

এখন:

```text
Profile save -> username reserve -> duplicate হলে error
Chats tab -> username search -> matching users দেখায়
```

## কোন files change হয়েছে

```text
profile/UserProfile.kt
profile/ProfileRepository.kt
profile/FirebaseProfileRepository.kt
profile/FakeProfileRepository.kt
ui/discovery/ContactDiscoveryUiState.kt
ui/discovery/ContactDiscoveryViewModel.kt
ui/screens/HomeScreen.kt
docs/18-username-discovery.md
```

## `UserProfile.kt`

আগে profile model-এ শুধু display name আর username ছিল। এখন:

```kotlin
data class UserProfile(
    val userId: String,
    val displayName: String,
    val username: String
)
```

`userId` কেন দরকার:

- search result থেকে current user বাদ দিতে হবে
- পরে chat/conversation create করতে selected user id লাগবে
- display name unique না, কিন্তু uid unique

## Firestore structure

Profile:

```text
users/{uid}
```

Username reservation:

```text
usernames/{username}
```

Example:

```text
users/abc123
usernames/masud
```

`usernames/masud` document:

```text
userId: abc123
displayName: Masud Rana
updatedAt: server timestamp
```

কেন আলাদা `usernames` collection:

- username duplicate কিনা direct document path দিয়ে check করা যায়
- query দিয়ে খোঁজার দরকার নেই
- transaction দিয়ে reserve করা যায়

## Username uniqueness transaction

`FirebaseProfileRepository.saveProfile()` এখন Firestore transaction ব্যবহার করে।

Logic:

```text
1. users/{uid} read
2. usernames/{newUsername} read
3. username অন্য user-এর হলে error
4. old username বদলালে old usernames/{oldUsername} delete
5. users/{uid} update
6. usernames/{newUsername} update
```

কেন transaction:

- একই সময়ে দুই user একই username save করতে চাইতে পারে
- transaction না হলে race condition হতে পারে
- transaction read/write atomic ভাবে করে

Duplicate হলে message:

```text
This username is already taken.
```

## `ProfileRepository.searchProfiles`

নতুন function:

```kotlin
suspend fun searchProfiles(
    usernameQuery: String,
    currentUserId: String
): List<UserProfile>
```

কেন:

- Chats tab থেকে username search করতে হবে
- current user নিজের profile result-এ দেখানো ঠিক না

## Firestore username search

Code idea:

```kotlin
firestore.collection("users")
    .orderBy("username")
    .startAt(query)
    .endAt("$query\uf8ff")
    .limit(10)
```

এর মানে:

- username অনুযায়ী sort
- query দিয়ে শুরু হওয়া username খোঁজা
- সর্বোচ্চ 10 result

Example:

```text
query: mas
matches: masud, masud_rana, mashrafe
```

## `ContactDiscoveryUiState`

```kotlin
data class ContactDiscoveryUiState(
    val query: String = "",
    val results: List<UserProfile> = emptyList(),
    val isSearching: Boolean = false,
    val message: String? = null
)
```

কেন:

- search input value রাখতে হবে
- result list রাখতে হবে
- loading text দেখাতে হবে
- no result/session error message দেখাতে হবে

## `ContactDiscoveryViewModel`

Query normalize:

```kotlin
value
    .lowercase()
    .filter { it.isLetterOrDigit() || it == '_' || it == '.' }
    .take(24)
```

কেন:

- username rules profile setup-এর সাথে match করে
- invalid character search query-তে ঢুকবে না

Debounce:

```kotlin
delay(300L)
```

কেন:

- user টাইপ করার প্রতিটা character-এ Firestore hit করা ভালো না
- 300ms wait করলে unnecessary query কমে

Minimum query length:

```text
3 character
```

কেন:

- 1/2 character search করলে অনেক result আসতে পারে
- Firestore read cost বাড়ে
- UX noisy হয়

## `HomeScreen.kt`

Chats tab-এ এখন search box আছে:

```text
Find people
Search username
```

Result row:

```text
Initials | Display name | @username | Open
```

এখন `Open` চাপলে existing placeholder chat detail screen খুলে। Real conversation create এখনো করা হয়নি।

## কীভাবে verify করবে

Build:

```powershell
cd apps\ContactMe
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug
```

Manual:

1. User A দিয়ে sign in করো।
2. username `masud` save করো।
3. User B দিয়ে sign in করার চেষ্টা করো।
4. একই username `masud` save করলে error আসা উচিত: `This username is already taken.`
5. অন্য username save করো।
6. Chats tab-এ search box-এ username-এর প্রথম 3 অক্ষর দাও।
7. matching user list দেখা উচিত।
8. নিজের user result-এ দেখা উচিত না।

## Firebase rules note

এই step-এর জন্য Firestore rules-এ `users` এবং `usernames` collection বিবেচনা করতে হবে।

Development-friendly idea:

```text
users/{userId}: user নিজের profile read/write করতে পারবে
usernames/{username}: signed-in user username reserve করতে পারবে
```

Production rules আরও strict করতে হবে, যাতে কেউ অন্য user-এর username reservation overwrite করতে না পারে।

## এখনো কী বাকি

- real conversation create
- contacts collection
- search result থেকে chat thread open
- username availability live indicator
- stricter Firestore security rules

## পরের step

```text
Search result -> create/get 1-to-1 conversation -> Chat detail real conversation id
```
