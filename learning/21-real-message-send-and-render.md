# Step 21: Real Message Send And Render

এই ধাপে ContactMe app-এ Chat Detail screen থেকে real text message send করা এবং Firestore realtime listener দিয়ে messages render করা শুরু হয়েছে।

## কেন এই step দরকার

আগে:

```text
Chat Detail -> dummy bubbles
Input -> placeholder/read only
```

এখন:

```text
Real conversation open
-> message type
-> Send
-> Firestore messages subcollection
-> realtime listener
-> Chat Detail render
```

## কোন files add/change হয়েছে

```text
message/ChatMessage.kt
message/MessageResult.kt
message/MessageRepository.kt
message/FirebaseMessageRepository.kt
message/FakeMessageRepository.kt
di/MessageModule.kt
ui/chat/ChatDetailUiState.kt
ui/chat/ChatDetailViewModel.kt
ui/screens/ChatDetailScreen.kt
docs/21-real-message-send.md
```

## Firestore structure

Conversation document:

```text
conversations/{conversationId}
```

Messages subcollection:

```text
conversations/{conversationId}/messages/{messageId}
```

Message fields:

```text
senderId
text
type = text
createdAt
```

Conversation cache update:

```text
lastMessageText
lastMessageSenderId
updatedAt
```

কেন conversation document-এ last message cache রাখা হয়:

- পরে chat list Firestore থেকে load করলে last message preview দেখাতে হবে
- প্রতিবার messages subcollection query না করে conversation list দ্রুত দেখানো যাবে

## `ChatMessage.kt`

```kotlin
data class ChatMessage(
    val id: String,
    val senderId: String,
    val text: String,
    val sentAtMillis: Long
)
```

কেন:

- UI bubble render করতে senderId লাগে
- text দেখাতে text লাগে
- later timestamp দেখাতে sentAtMillis লাগবে

## `MessageRepository.kt`

```kotlin
interface MessageRepository {
    fun observeMessages(conversationId: String): Flow<List<ChatMessage>>

    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        text: String
    ): MessageResult
}
```

কেন `Flow`:

- Firestore realtime listener data change হলেই নতুন list দেয়
- Compose state update হয়ে UI refresh হয়

## `FirebaseMessageRepository.kt`

Realtime listener:

```kotlin
firestore.collection("conversations")
    .document(conversationId)
    .collection("messages")
    .orderBy("createdAt", Query.Direction.ASCENDING)
    .addSnapshotListener { snapshot, error -> ... }
```

কেন order by `createdAt`:

- old message আগে, new message পরে
- chat bubble natural order-এ দেখাবে

Send message:

```kotlin
batch.set(messageDocument, messageData)
batch.update(conversationDocument, lastMessageData)
```

কেন batch:

- message document create এবং conversation last message update একসাথে করা ভালো
- এক operation fail হলে data inconsistent হওয়ার chance কমে

## `ChatDetailViewModel.kt`

State:

```kotlin
data class ChatDetailUiState(
    val currentUserId: String = "",
    val messageText: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isSending: Boolean = false,
    val errorMessage: String? = null
)
```

Conversation open:

```kotlin
fun openConversation(conversationId: String?)
```

এটা listener start করে:

```kotlin
messageRepository.observeMessages(conversationId).collect { messages -> ... }
```

Send:

```kotlin
messageRepository.sendMessage(
    conversationId = conversationId,
    senderId = senderId,
    text = state.messageText
)
```

Success হলে input clear হয়:

```kotlin
messageText = ""
```

## `ChatDetailScreen.kt`

আগে input read-only ছিল। এখন:

```kotlin
OutlinedTextField(
    value = uiState.messageText,
    onValueChange = onMessageTextChanged
)
Button(onClick = onSendMessage) {
    Text("Send")
}
```

Real conversation না হলে:

```text
Open a contact to message
```

মানে dummy chat row খুললে message পাঠানো যাবে না। Search result থেকে real conversation খুললে message পাঠানো যাবে।

Bubble side:

```kotlin
isMine = message.senderId == uiState.currentUserId
```

নিজের message ডান পাশে, অন্যের message বাম পাশে।

## কীভাবে verify করবে

Build:

```powershell
cd apps\ContactMe
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug
```

Manual:

1. দুই user profile তৈরি করো।
2. User A login করো।
3. Chats tab থেকে User B username search করো।
4. result tap করে real conversation open করো।
5. message type করো।
6. Send চাপো।
7. bubble screen-এ দেখা উচিত।
8. Firebase Console-এ check করো:

```text
conversations/{conversationId}/messages/{messageId}
```

## এখনো কী বাকি

- message timestamp UI
- auto scroll to latest message
- conversation list Firestore থেকে load
- send failure retry
- delivery/read receipt
- media message
- WhatsApp-like light/dark UI polish

## পরের step

```text
Home Chats tab -> load real conversations
show lastMessageText + updatedAt
```
