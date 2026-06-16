# Step 20: Direct Conversation Foundation And Design Reference

এই ধাপে দুইটা কাজ হয়েছে:

- search result থেকে real one-to-one conversation create/get foundation
- user দেওয়া WhatsApp-style dark UI reference project memory হিসেবে docs-এ রাখা

## Design direction memory

তুমি যে screenshots দিয়েছো, সেগুলো থেকে design direction:

- dark near-black background
- green action accent
- circular avatar
- rounded search/input
- bottom navigation
- dense chat/call list
- outgoing green bubble
- incoming dark gray bubble
- chat detail-এ dark patterned background feel

এই direction docs-এ রাখা হয়েছে:

```text
docs/20-ui-design-reference.md
```

এটা future UI polish step-গুলোতে follow করা হবে।

## কেন conversation foundation দরকার

আগে:

```text
Search result tap -> placeholder chat opens by display name
```

সমস্যা:

- real conversation id ছিল না
- পরে message save করার path জানা ছিল না
- same দুই user-এর জন্য duplicate chat হতে পারত

এখন:

```text
Search result tap
-> get/create direct conversation
-> Chat Detail opens with conversationId
```

## কোন files add/change হয়েছে

```text
conversation/ConversationRepository.kt
conversation/FirebaseConversationRepository.kt
conversation/FakeConversationRepository.kt
conversation/ConversationResult.kt
di/ConversationModule.kt
ui/conversation/ConversationViewModel.kt
ui/ContactMeApp.kt
ui/screens/HomeScreen.kt
ui/screens/ChatDetailScreen.kt
docs/19-direct-conversation-foundation.md
docs/20-ui-design-reference.md
```

## Conversation id কীভাবে তৈরি হয়

Direct conversation id deterministic:

```kotlin
val participantIds = listOf(currentUserId, otherUserId).sorted()
val conversationId = participantIds.joinToString(separator = "__")
```

কেন sorted:

```text
User A + User B -> A__B
User B + User A -> A__B
```

মানে same দুই user যেদিক থেকেই chat খুলুক, same conversation document ব্যবহার হবে।

## Firestore structure

```text
conversations/{conversationId}
```

Fields:

```text
type = direct
participantIds = [uid1, uid2]
participantKey = uid1__uid2
createdAt = server timestamp
updatedAt = server timestamp
```

## `ConversationRepository.kt`

```kotlin
interface ConversationRepository {
    suspend fun getOrCreateDirectConversation(
        currentUserId: String,
        otherUserId: String
    ): ConversationResult
}
```

কেন repository:

- UI Firestore চেনে না
- conversation creation logic central থাকে
- পরে group conversation add করা সহজ

## `FirebaseConversationRepository.kt`

Core logic:

```kotlin
firestore.runTransaction { transaction ->
    val snapshot = transaction.get(conversationDocument)

    if (!snapshot.exists()) {
        transaction.set(conversationDocument, conversationData)
    } else {
        transaction.update(conversationDocument, "updatedAt", serverTimestamp)
    }
}
```

কেন transaction:

- দুই user একই সময়ে chat open করলে duplicate create হবে না
- read + write একসাথে safe থাকে

## `ConversationResult.kt`

```kotlin
sealed interface ConversationResult {
    data class Success(val conversationId: String) : ConversationResult
    data class Error(val message: String) : ConversationResult
}
```

কেন:

- success হলে conversation id লাগে
- fail হলে friendly error message রাখা যায়

## `ConversationViewModel.kt`

Search result tap করলে app এই ViewModel দিয়ে conversation open করে:

```kotlin
conversationRepository.getOrCreateDirectConversation(
    currentUserId = currentUserId,
    otherUserId = otherUser.userId
)
```

Success হলে callback:

```kotlin
onReady(conversationId, chatName)
```

## `HomeScreen.kt`

আগে search result tap করত:

```text
onChatSelected(profile.displayName)
```

এখন:

```kotlin
onDiscoveredUserSelected(profile)
```

মানে HomeScreen শুধু selected user পাঠায়। Conversation create করার responsibility app-level ViewModel নেয়।

## `ContactMeApp.kt`

নতুন state:

```kotlin
var selectedConversationId by remember { mutableStateOf<String?>(null) }
```

Dummy chat:

```kotlin
selectedConversationId = null
```

Real discovered user:

```kotlin
selectedConversationId = conversationId
selectedChatName = chatName
currentScreen = AppScreen.ChatDetail
```

## `ChatDetailScreen.kt`

এখন optional `conversationId` নেয়:

```kotlin
fun ChatDetailScreen(
    chatName: String,
    conversationId: String? = null,
    onBack: () -> Unit
)
```

এখনো message real না, কিন্তু screen-এর কাছে conversation id আছে। পরের step-এ এই id দিয়ে messages subcollection read/write হবে।

## কীভাবে verify করবে

Build:

```powershell
cd apps\ContactMe
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug
```

Manual:

1. দুইটা user account/profile তৈরি করো।
2. User A দিয়ে login করো।
3. Chats tab-এ User B-এর username search করো।
4. result tap করো।
5. Chat Detail খুলবে।
6. Firebase Console-এ `conversations` collection check করো।
7. `uid1__uid2` format-এর document থাকা উচিত।
8. আবার same user tap করলে same document update হবে, নতুন duplicate document হবে না।

## এখনো কী বাকি

- real message send
- messages listener
- conversation list Firestore থেকে load
- unread count
- last message preview
- UI dark WhatsApp-style polish

## পরের step

```text
Chat Detail input -> send message
Firestore messages subcollection -> render real messages
```

## Theme direction correction

এই design direction update করা হলো:

```text
Default theme: Light
Available themes: Light + Dark
Design style: WhatsApp-like
Accent: Green
```

মানে app by default light mode-এ চলবে। Dark mode থাকবে, কিন্তু default হবে না।

`ContactMeTheme` এখন এভাবে কাজ করে:

```kotlin
fun ContactMeTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
)
```

`darkTheme = false` default, তাই app light theme নেয়।

Light theme:

```text
white/light surface
green primary accent
dark readable text
rounded WhatsApp-like components
```

Dark theme:

```text
near-black background
dark chat surface
green primary accent
light readable text
WhatsApp-like chat/call feel
```

Future UI polish করার সময় মনে রাখতে হবে:

- শুধু dark UI বানানো যাবে না
- light এবং dark দুই mode compatible রাখতে হবে
- default screenshot/test light mode ধরে verify করতে হবে
- WhatsApp-style familiarity থাকবে, কিন্তু direct clone করা যাবে না
