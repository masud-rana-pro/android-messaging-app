# Step 42 - Real Saved Contacts List

এই ধাপে তোমার কথামতো নতুন contacts feature থেকে fake implementation সরিয়ে real Firebase-based implementation করা হয়েছে।

## কেন `FakeContactRepository` সরানো হলো?

আগের ধাপে contact feature শুরু করার সময় `FakeContactRepository` রাখা হয়েছিল। সাধারণত development বা preview/testing-এর জন্য fake repository রাখা হয়। কিন্তু তুমি যেহেতু বলেছো app-টা real implementation হিসেবেই এগোবে, তাই নতুন contacts feature-এ fake class রাখা হয়নি।

এই ধাপে delete করা হয়েছে:

```text
apps/ContactMe/app/src/main/java/com/contactme/app/contact/FakeContactRepository.kt
```

## এবার contacts কীভাবে real হলো?

`ContactRepository` interface-এ নতুন method যোগ হয়েছে:

```kotlin
fun observeContacts(ownerUserId: String): Flow<List<UserProfile>>
```

এর কাজ:

- current user-এর saved contacts observe করা
- Firestore update হলে UI auto update করা
- contact list real Firebase data থেকে আনা

## Firestore path

Saved contacts পড়া হচ্ছে এই path থেকে:

```text
contacts/{uid}/items/{contactUid}
```

Example:

```text
contacts/userA/items/userB
```

এর মানে User A তার saved contacts-এ User B-কে রেখেছে।

## FirebaseContactRepository কীভাবে observe করে?

Repository `addSnapshotListener` ব্যবহার করে:

```kotlin
firestore.collection("contacts")
    .document(ownerUserId)
    .collection("items")
    .addSnapshotListener { snapshot, error -> ... }
```

এটা realtime listener। Firestore data change হলে app আবার নতুন contacts list পায়।

## শুধু contact document পড়লেই কেন যথেষ্ট না?

Contact document-এ cached display data থাকে। কিন্তু user profile পরে change হতে পারে।

তাই আমরা contact id নিয়ে current `users/{contactUid}` document পড়ি:

```text
contacts/userA/items/userB
        |
        -> users/userB
```

এতে latest display name, username, phone, photo privacy পাওয়া যায়।

## Profile photo privacy কীভাবে রাখা হলো?

Contact list-এ photo দেখানোর আগেও privacy check করা হয়েছে:

```text
Everyone -> photo visible
Contacts -> viewer owner-এর contact list-এ থাকলে visible
Nobody -> hidden
```

যদি photo hidden হয়, repository empty string দেয়। UI তখন initials দেখায়।

## ContactListViewModel কেন দরকার?

UI সরাসরি repository call করে না। ViewModel app state ধরে:

```kotlin
data class ContactListUiState(
    val contacts: List<UserProfile> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null
)
```

এই state UI-তে দেখানো হয়।

## UI-তে কী change হয়েছে?

Chats screen-এ এখন real section এসেছে:

```text
Saved contacts
```

এখানে Firestore থেকে পাওয়া real contacts দেখাবে।

আগের dummy chat preview list সরানো হয়েছে। যেমন:

```text
Ayesha Rahman
Team ContactMe
Design Notes
```

এগুলো এখন আর থাকবে না, কারণ এগুলো real data না।

## Scroll fix

Chats content-এ এখন vertical scroll দেওয়া হয়েছে:

```kotlin
Modifier.verticalScroll(rememberScrollState())
```

তাই search results, saved contacts, conversations একসাথে বেশি হলে screen scroll করা যাবে।

## কীভাবে verify করবে?

1. App build করো।
2. Firebase rules deploy করা আছে কিনা নিশ্চিত করো।
3. User A দিয়ে User B search করো।
4. User B-এর chat open করো।
5. Firebase Console-এ দেখো:

```text
contacts/{userA}/items/{userB}
```

6. App-এ Chats screen-এ ফিরে আসো।
7. `Saved contacts` section-এ User B দেখা উচিত।
8. User B-তে tap করলে real direct conversation open হওয়া উচিত।

## এই ধাপ থেকে শেখার বিষয়

- Fake implementation দ্রুত development-এ help করে, কিন্তু production behavior verify করতে real repository দরকার।
- Realtime contacts list-এর জন্য `addSnapshotListener` ভালো fit।
- UI state আলাদা ViewModel-এ রাখলে screen clean থাকে।
- Dummy UI data remove করলে app-এর current বাস্তব অবস্থা পরিষ্কার বোঝা যায়।
