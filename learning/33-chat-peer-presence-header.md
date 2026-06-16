# Step 33: Chat Peer Presence Header

এই step-এ আমরা previous presence write foundation ব্যবহার করে chat header-এ অন্য user-এর online/last seen দেখানোর foundation করেছি।

## 1. আগের step আর এই step-এর পার্থক্য

Step 32:

- current user online/offline লিখত
- `presence/{uid}` update করত

Step 33:

- chat-এর other participant কে তা বের করে
- তার `presence/{uid}` observe করে
- UI header update করে

মানে Step 32 ছিল write foundation, Step 33 হলো read/display foundation।

## 2. `PresenceStatus`

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/presence/PresenceStatus.kt`

```kotlin
data class PresenceStatus(
    val isOnline: Boolean = false,
    val lastSeenAtMillis: Long = 0L
)
```

এটা UI-friendly model।

`isOnline` true হলে user online।

`lastSeenAtMillis` দিয়ে last seen time format করা যায়।

## 3. Repository interface update

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/presence/PresenceRepository.kt`

নতুন function:

```kotlin
fun observeConversationPeerPresence(
    conversationId: String,
    currentUserId: String
): Flow<PresenceStatus>
```

এটা conversation id এবং current user id নেয়। তারপর অন্য participant-এর presence observe করে।

## 4. অন্য participant বের করা

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/presence/FirebasePresenceRepository.kt`

প্রথমে conversation document observe করা হয়:

```kotlin
firestore.collection("conversations")
    .document(conversationId)
    .addSnapshotListener { snapshot, error -> ... }
```

তারপর `participantIds` থেকে current user বাদ দিয়ে peer user বের করা হয়:

```kotlin
firstOrNull { userId -> userId != currentUserId }
```

Direct chat-এ participant দুইজন, তাই current user বাদ দিলে অন্য user থাকে।

## 5. Peer presence observe

Peer id পাওয়ার পর Realtime Database path observe:

```text
presence/{peerUid}
```

Listener থেকে data আসে:

```kotlin
PresenceStatus(
    isOnline = snapshot.child("isOnline").getValue(Boolean::class.java) ?: false,
    lastSeenAtMillis = snapshot.child("lastSeenAt").getValue(Long::class.java) ?: 0L
)
```

এই model ViewModel-এ যায়।

## 6. Listener cleanup কেন দরকার?

Conversation বদলালে পুরনো peer presence listener remove করতে হয়।

না করলে:

- old chat-এর listener চলতে থাকবে
- memory leak হতে পারে
- wrong user status UI-তে আসতে পারে

তাই `clearPresenceListener()` আছে।

## 7. `ChatDetailUiState` update

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/chat/ChatDetailUiState.kt`

নতুন field:

```kotlin
val peerPresence: PresenceStatus = PresenceStatus()
```

এটা UI-তে header subtitle বানানোর জন্য লাগে।

## 8. `ChatDetailViewModel` presence observe করছে

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/chat/ChatDetailViewModel.kt`

Conversation open হলে:

```kotlin
presenceRepository.observeConversationPeerPresence(
    conversationId = conversationId,
    currentUserId = currentUserId
).collect { peerPresence ->
    _uiState.update {
        it.copy(peerPresence = peerPresence)
    }
}
```

এতে peer online/offline change হলেই state update হয়।

## 9. Chat header priority

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/screens/ChatDetailScreen.kt`

Subtitle priority:

```text
demo chat -> online
typing -> typing...
peer online -> online
peer offline with lastSeen -> last seen h:mm AM/PM
fallback -> last seen recently
```

Typing online-এর চেয়ে বেশি priority পায়, কারণ কেউ typing করলে সেটা সবচেয়ে useful signal।

## 10. Last seen format

```kotlin
private fun Long.formatPresenceTime(): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(this))
}
```

এখন শুধু time দেখাচ্ছে। Future-এ today/yesterday/date logic add করা যাবে।

## 11. কীভাবে verify করবে?

Build:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug
```

Manual test:

1. দুইটা account দিয়ে same direct chat open করো।
2. Account B foreground রাখো।
3. Account A chat header-এ `online` দেখো।
4. Account B background করো।
5. Account A header-এ `last seen ...` আসে কি না দেখো।
6. Account B typing করলে Account A-তে `typing...` দেখো।

## 12. Main learning

একটা chat header subtitle শুধু static text না। এটা multiple realtime state combine করে:

- typing
- online
- last seen
- fallback

এই step-এ আমরা শিখলাম কীভাবে Firestore conversation data এবং Realtime Database presence data একসাথে ব্যবহার করে UI বানানো যায়।
