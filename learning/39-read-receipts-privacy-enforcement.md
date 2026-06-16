# Step 39: Read Receipts Privacy Enforcement

এই step-এ আমরা read receipts privacy setting UI behavior-এ apply করেছি।

আগে নিজের পাঠানো message-এর পাশে শুধু `Sent` দেখাত। এখন peer যদি message read করে এবং তার read receipts enabled থাকে, তাহলে sender `Seen` দেখতে পাবে।

## 1. Read receipts কী?

Read receipt মানে sender জানতে পারে receiver message পড়েছে কি না।

WhatsApp-এর blue tick-এর মতো concept।

আমাদের app-এ এখন text marker:

```text
Sent
Seen
```

## 2. Privacy problem কী?

User যদি Settings থেকে Read receipts off করে, তাহলে অন্য user যেন তার read state দেখতে না পারে।

তাই sender UI-তে `Seen` দেখানোর আগে peer-এর setting check করতে হবে।

## 3. `ReadReceiptState`

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/conversation/ReadReceiptState.kt`

```kotlin
data class ReadReceiptState(
    val peerReadAtMillis: Long = 0L,
    val canShowPeerReadReceipt: Boolean = true
)
```

`peerReadAtMillis` বলে peer কত সময় পর্যন্ত conversation read করেছে।

`canShowPeerReadReceipt` বলে peer তার read receipt দেখাতে allow করেছে কি না।

## 4. Repository function

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/conversation/ConversationRepository.kt`

নতুন function:

```kotlin
fun observeReadReceiptState(
    conversationId: String,
    currentUserId: String
): Flow<ReadReceiptState>
```

এই function current chat-এর peer read status observe করে।

## 5. Firebase implementation

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/conversation/FirebaseConversationRepository.kt`

এটা দুইটা জায়গা থেকে data নেয়:

```text
conversations/{conversationId}.readAtByUser.{peerUid}
users/{peerUid}.readReceiptsEnabled
```

তারপর UI-friendly `ReadReceiptState` emit করে।

## 6. ChatDetailUiState

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/chat/ChatDetailUiState.kt`

নতুন field:

```kotlin
val readReceiptState: ReadReceiptState = ReadReceiptState()
```

UI এই state দিয়ে message meta row বানায়।

## 7. ViewModel observe

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/chat/ChatDetailViewModel.kt`

Conversation open হলে:

```kotlin
conversationRepository.observeReadReceiptState(...)
```

collect করে `uiState.readReceiptState` update করা হয়।

## 8. UI logic

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/screens/ChatDetailScreen.kt`

নিজের message হলে check করে:

```kotlin
readReceiptState.canShowPeerReadReceipt &&
sentAtMillis > 0L &&
readReceiptState.peerReadAtMillis >= sentAtMillis
```

সব true হলে:

```text
Seen
```

না হলে:

```text
Sent
```

## 9. কেন unread clearing বন্ধ করা হয়নি?

বর্তমানে `readAtByUser` একই সাথে দুই কাজ করে:

1. receiver-এর নিজের unread clear করা
2. sender-এর read receipt বোঝা

যদি read receipts off করলে `readAtByUser` লেখা বন্ধ করে দিই, তাহলে receiver-এর নিজের chat list unread থেকে যেতে পারে।

তাই এই step-এ UI-level enforcement করা হয়েছে:

- local unread behavior ভাঙবে না
- sender UI peer setting respect করবে

Later hardening step-এ local unread marker আর public read receipt আলাদা schema করা যেতে পারে।

## 10. কীভাবে verify করবে?

Build:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug
```

Manual test:

1. User B Settings থেকে Read receipts on করো।
2. User A message পাঠাও।
3. User B chat খুলে message read করো।
4. User A-এর screen-এ message status `Seen` হয় কি না দেখো।
5. User B Read receipts off করো।
6. User A আরেকটা message পাঠাও।
7. User B read করলেও User A যেন `Seen` না দেখে, `Sent` দেখে।

## 11. Main learning

Privacy enforcement সবসময় data write বন্ধ করা দিয়ে শুরু করতে হয় না।

কখনো একই data app-এর local behavior-এর জন্য দরকার হয়। তখন UI/display layer peer privacy respect করতে পারে।

এই step-এ আমরা read receipts display privacy enforce করেছি।
