# Step 31: Typing Indicator Foundation

এই step-এ আমরা chat-এর জন্য typing indicator foundation করেছি। WhatsApp-এর মতো কেউ message লিখলে অন্য user chat header-এ `typing...` দেখতে পাবে।

## 1. Typing indicator কেন দরকার?

Chat app-এ typing indicator ছোট feature মনে হলেও user experience-এর জন্য important।

এটা user-কে বোঝায়:

- অন্য user active আছে
- conversation live feel করছে
- reply আসতে পারে

Chat MVP finish করার জন্য typing indicator natural next step।

## 2. Firestore data structure

Typing data রাখা হচ্ছে conversation-এর subcollection-এ:

```text
conversations/{conversationId}/typing/{uid}
  userId
  isTyping
  updatedAt
```

কেন conversation-এর ভিতরে?

- typing state conversation-specific
- এক user এক chat-এ typing করলেও অন্য chat-এ typing নাও করতে পারে
- participant security rules apply করা সহজ

## 3. `TypingRepository` interface

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/typing/TypingRepository.kt`

```kotlin
interface TypingRepository {
    fun observeOtherTyping(
        conversationId: String,
        currentUserId: String
    ): Flow<Boolean>

    suspend fun setTyping(
        conversationId: String,
        userId: String,
        isTyping: Boolean
    )
}
```

এখানে দুইটা কাজ আছে:

1. অন্য user typing করছে কি না observe করা
2. নিজের typing state update করা

UI বা ViewModel Firestore সরাসরি জানে না। তারা repository interface ব্যবহার করে।

## 4. `FirebaseTypingRepository`

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/typing/FirebaseTypingRepository.kt`

এটা real Firestore implementation।

### Observe typing

```kotlin
firestore.collection("conversations")
    .document(conversationId)
    .collection("typing")
    .addSnapshotListener { snapshot, error -> ... }
```

এই listener typing subcollection observe করে। কোনো participant typing update করলে listener নতুন snapshot পায়।

### অন্য user filter করা

```kotlin
userId != currentUserId && isTyping && isFresh
```

এর মানে:

- current user-এর নিজের typing ignore হবে
- `isTyping` true হতে হবে
- typing data stale না হতে হবে

## 5. Stale typing কী?

Typing state যদি কোনো কারণে false না হয়, তাহলে header সারাজীবন `typing...` দেখাতে পারে।

তাই repository 15 seconds freshness check করে:

```kotlin
now - updatedAtMillis <= 15_000L
```

এটা perfect cleanup না, কিন্তু MVP foundation হিসেবে ভালো।

Future-এ debounce + timeout cleanup আরও ভালো করা হবে।

## 6. `setTyping()`

```kotlin
.document(userId)
.set(
    mapOf(
        "userId" to userId,
        "isTyping" to isTyping,
        "updatedAt" to FieldValue.serverTimestamp()
    )
)
```

Document id userId রাখা হয়েছে। এতে একই user-এর typing state বারবার same document update হয়।

কেন new document create করা হয়নি?

কারণ typing event history দরকার নেই। শুধু latest state দরকার।

## 7. Fake repository কেন?

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/typing/FakeTypingRepository.kt`

Fake repository test/demo/future preview-এর জন্য। Real Firebase ছাড়া ViewModel test করতে চাইলে fake implementation ব্যবহার করা যায়।

## 8. Hilt module

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/di/TypingModule.kt`

```kotlin
@Binds
@Singleton
abstract fun bindTypingRepository(
    firebaseTypingRepository: FirebaseTypingRepository
): TypingRepository
```

এর মানে ViewModel যখন `TypingRepository` চাইবে, Hilt তাকে `FirebaseTypingRepository` দেবে।

## 9. `ChatDetailUiState` update

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/chat/ChatDetailUiState.kt`

নতুন field:

```kotlin
val isOtherUserTyping: Boolean = false
```

UI এই boolean দেখে header text বদলায়।

## 10. `ChatDetailViewModel` কী করছে?

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/chat/ChatDetailViewModel.kt`

### Conversation open হলে observe শুরু

```kotlin
typingRepository.observeOtherTyping(
    conversationId = conversationId,
    currentUserId = currentUserId
).collect { isOtherUserTyping ->
    _uiState.update {
        it.copy(isOtherUserTyping = isOtherUserTyping)
    }
}
```

এতে অন্য user typing করলে UI state update হয়।

### Text change হলে typing update

```kotlin
updateTypingState(isTyping = nextMessageText.isNotBlank())
```

User কিছু লিখলে `true`, text empty হলে `false`।

### Message send success হলে typing false

Message পাঠানো হয়ে গেলে আর typing দেখানোর দরকার নেই:

```kotlin
updateTypingState(isTyping = false)
```

## 11. বারবার Firestore write কমানো

ViewModel-এ `lastTypingValue` রাখা হয়েছে।

যদি আগেও typing true ছিল, আবার true set করার দরকার নেই।

এতে প্রতি character লিখলে Firestore write না হয়ে শুধু false -> true বা true -> false change হলে write হবে।

## 12. UI header update

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/screens/ChatDetailScreen.kt`

Header subtitle:

```kotlin
if (conversationId == null) {
    "online"
} else if (uiState.isOtherUserTyping) {
    "typing..."
} else {
    "last seen recently"
}
```

Demo chat হলে `online`, real conversation হলে typing state অনুযায়ী text।

## 13. Firestore rules update

File:

`firebase/firestore.rules`

নতুন rule:

```text
match /typing/{userId}
```

Rules:

- participant হলে read করতে পারবে
- user শুধু নিজের typing document create/update করতে পারবে
- `isTyping` boolean হতে হবে

এতে অন্য user আপনার নামে typing state লিখতে পারবে না।

## 14. কীভাবে verify করবে?

Build:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug
```

Manual test:

1. দুইটা account দিয়ে একই conversation open করো।
2. Account A থেকে message box-এ type করো।
3. Account B-এর header-এ `typing...` আসে কি না দেখো।
4. Account A text clear করলে typing বন্ধ হয় কি না দেখো।
5. Account A message send করলে typing বন্ধ হয় কি না দেখো।
6. Firestore console-এ typing document check করো।

Rules deploy:

```bash
firebase deploy --only firestore:rules,firestore:indexes
```

## 15. Main learning

Typing indicator হলো realtime presence-এর ছোট version।

এখানে আমরা শিখলাম:

- conversation-specific realtime state কোথায় রাখা যায়
- নিজের typing আর অন্য user-এর typing কীভাবে আলাদা করা যায়
- Firestore listener দিয়ে UI state update করা যায়
- security rules দিয়ে user নিজের document ছাড়া অন্যের document লিখতে না পারে

এই step Chat MVP-কে আরও live messenger feel-এর দিকে নিয়ে গেল।
