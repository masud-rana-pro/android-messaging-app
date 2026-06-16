# Step 22: Real Conversation List

এই ধাপে Home Chats tab আর শুধু dummy chat list না। এখন Firestore থেকে current user-এর real conversations load করে list দেখায়।

## কেন এই step দরকার

আগে:

```text
Search user -> Chat Detail -> message send
Back Home -> dummy chats
```

সমস্যা:

- real conversation list দেখা যেত না
- last message preview ছিল না
- same conversation আবার খুলতে search করতে হতো

এখন:

```text
Home Chats
-> observe conversations
-> show real conversation rows
-> tap row
-> open Chat Detail with conversationId
```

## কোন files change হয়েছে

```text
conversation/ConversationPreview.kt
conversation/ConversationRepository.kt
conversation/FirebaseConversationRepository.kt
conversation/FakeConversationRepository.kt
ui/conversation/ConversationListUiState.kt
ui/conversation/ConversationListViewModel.kt
ui/screens/HomeScreen.kt
ui/ContactMeApp.kt
docs/22-real-conversation-list.md
```

## `ConversationPreview.kt`

```kotlin
data class ConversationPreview(
    val conversationId: String,
    val otherUserId: String,
    val title: String,
    val subtitle: String,
    val updatedAtMillis: Long
)
```

কেন:

- Chats tab row render করার জন্য আলাদা lightweight model দরকার
- full message list load না করে conversation summary দেখানো যায়

## Repository method

```kotlin
fun observeConversationPreviews(currentUserId: String): Flow<List<ConversationPreview>>
```

কেন `Flow`:

- নতুন message পাঠালে conversation `updatedAt`/`lastMessageText` বদলায়
- listener UI-কে updated list দেয়

## Firestore query

```kotlin
firestore.collection("conversations")
    .whereArrayContains("participantIds", currentUserId)
```

এর মানে:

- যে conversation document-এ current user participant, শুধু সেগুলো load হবে

কেন `orderBy` ব্যবহার করা হয়নি:

- `arrayContains + orderBy` দিলে Firestore composite index চাইতে পারে
- এই foundation step-এ index complexity কম রাখতে app-side sort করা হয়েছে

Sort:

```kotlin
previews.sortedByDescending { it.updatedAtMillis }
```

## Other user name

Conversation document-এ participant ids থাকে। Current user বাদ দিলে other user id পাওয়া যায়:

```kotlin
participantIds.first { userId -> userId != currentUserId }
```

তারপর:

```kotlin
users/{otherUserId}
```

থেকে displayName/username পড়ে row title বানানো হয়।

## `ConversationListViewModel`

Current user id নেয়:

```kotlin
authRepository.currentUserId()
```

তারপর repository observe করে:

```kotlin
conversationRepository.observeConversationPreviews(currentUserId)
```

State:

```kotlin
ConversationListUiState(
    conversations = conversations,
    isLoading = false
)
```

## `HomeScreen.kt`

Chats tab এখন দুই জিনিস দেখায়:

- Find people search
- Real conversation list

Real row tap:

```kotlin
onConversationSelected(conversation.conversationId, conversation.title)
```

যদি real conversation না থাকে, তখন demo rows fallback হিসেবে থাকে।

## `ContactMeApp.kt`

নতুন callback:

```kotlin
onConversationSelected = { conversationId, chatName ->
    selectedConversationId = conversationId
    selectedChatName = chatName
    currentScreen = AppScreen.ChatDetail
}
```

এতে Home row tap করলে Chat Detail real conversation id পায়।

## কীভাবে verify করবে

Build:

```powershell
cd apps\ContactMe
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug
```

Manual:

1. User A login করো।
2. User B search করে chat open করো।
3. message send করো।
4. Back/Home-এ যাও।
5. Chats tab-এ User B-এর real conversation row দেখা উচিত।
6. last message preview দেখা উচিত।
7. row tap করলে একই conversation খুলবে।

## এখনো কী বাকি

- unread count
- timestamp formatting
- group conversations
- conversation row loading optimization
- WhatsApp-like visual polish

## পরের step

```text
Timestamp + auto scroll + WhatsApp-like chat detail polish
```
