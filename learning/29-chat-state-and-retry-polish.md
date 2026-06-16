# Step 29: Chat State and Retry Polish

এই step-এ আমরা Chat Detail screen-কে একটু বেশি real app-এর মতো করেছি। আগে message list খালি থাকলে সবসময় শুধু `No messages yet.` দেখাত। কিন্তু real chat app-এ খালি list দুই কারণে হতে পারে:

1. messages এখনো load হচ্ছে
2. conversation সত্যিই empty

এই দুইটা state আলাদা না করলে user বুঝতে পারে না app কাজ করছে নাকি message নেই।

## 1. `ChatDetailUiState`-এ `isLoadingMessages`

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/chat/ChatDetailUiState.kt`

নতুন field:

```kotlin
val isLoadingMessages: Boolean = false
```

এটার কাজ হলো UI-কে জানানো:

- true হলে message snapshot এখনো আসেনি
- false হলে first snapshot এসে গেছে

আগে UI শুধু `messages.isEmpty()` দেখে সিদ্ধান্ত নিত। কিন্তু empty list loading-এর সময়ও হতে পারে, real empty conversation-এও হতে পারে। তাই আলাদা loading flag দরকার।

## 2. Conversation open হলে loading শুরু

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/chat/ChatDetailViewModel.kt`

Conversation open হওয়ার সময়:

```kotlin
_uiState.update {
    it.copy(
        messages = emptyList(),
        isLoadingMessages = true,
        errorMessage = null
    )
}
```

কেন `messages = emptyList()`?

কারণ user যদি এক chat থেকে আরেক chat-এ যায়, পুরনো chat-এর messages নতুন chat-এ কিছুক্ষণ দেখানো উচিত না।

কেন `isLoadingMessages = true`?

কারণ এখন app Firestore listener থেকে নতুন conversation-এর first snapshot অপেক্ষা করছে।

কেন `errorMessage = null`?

কারণ নতুন conversation open করলে আগের send error আর দেখানো ঠিক না।

## 3. First snapshot এলে loading বন্ধ

Firestore থেকে messages collect করার সময়:

```kotlin
_uiState.update {
    it.copy(
        messages = messages,
        isLoadingMessages = false,
        errorMessage = null
    )
}
```

এখানে messages empty হলেও loading false হয়। কারণ empty snapshot মানে Firestore বলেছে: conversation আছে, কিন্তু message নেই।

এই জায়গায় loading আর empty আলাদা হয়ে যায়।

## 4. Chat screen-এ loading state

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/screens/ChatDetailScreen.kt`

Message list render করার সময়:

```kotlin
if (uiState.isLoadingMessages) {
    ChatListStateMessage(
        title = "Loading messages",
        subtitle = "Syncing this conversation."
    )
}
```

এতে user বুঝবে app message sync করছে।

## 5. Empty state

যখন loading false এবং messages empty:

```kotlin
ChatListStateMessage(
    title = "No messages yet",
    subtitle = "Send the first message to start the conversation."
)
```

এটা clear product message। এটা error না, শুধু conversation empty।

## 6. `ChatListStateMessage` composable

এই নতুন composable loading/empty state দেখানোর reusable UI।

```kotlin
@Composable
private fun ChatListStateMessage(
    title: String,
    subtitle: String
)
```

এর ভিতরে:

- centered layout
- title
- subtitle
- loading title হলে `CircularProgressIndicator`

এটা আলাদা function করার কারণ:

- main `ChatDetailContent` clean থাকে
- loading/empty UI একই style follow করে
- future-এ icon/animation যোগ করা সহজ হবে

## 7. Send error box

আগে error শুধু red text হিসেবে দেখাত। এখন error container আছে:

```kotlin
SendErrorMessage(
    message = message,
    canRetry = uiState.messageText.isNotBlank() && conversationId != null,
    onRetry = onSendMessage
)
```

কেন ভালো?

- error আলাদা box হিসেবে চোখে পড়ে
- message text থাকলে Retry button দেখা যায়
- user আবার send চাপতে পারে

## 8. Retry কীভাবে কাজ করে?

এই step-এ আলাদা retry API বানানো হয়নি। কারণ failed send হলে `messageText` clear করা হয় না।

Flow:

1. User message লিখে Send চাপবে
2. Firebase send fail করলে error দেখাবে
3. `messageText` আগের মতো থাকবে
4. Retry চাপলে একই `sendMessage()` আবার চলবে

এটা simple কিন্তু practical MVP retry foundation।

## 9. `Sent` marker কেন check mark থেকে text হলো?

আগে status marker check mark ছিল। কিন্তু source/output-এ encoding issue দেখা গিয়েছিল:

```text
âœ“
```

তাই আপাতত ASCII-safe:

```kotlin
MessageStatus.Sent -> "Sent"
```

ব্যবহার করা হয়েছে।

ভবিষ্যতে icon system বা vector icon দিয়ে proper sent/delivered/read marker করা ভালো হবে।

## 10. কীভাবে verify করবে?

Build:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug
```

Manual test:

1. App open করো।
2. কোনো real conversation open করো।
3. শুরুতে loading message দেখতে পাও কি না দেখো।
4. message না থাকলে `No messages yet` দেখো।
5. message send করো।
6. network/Firebase issue হলে error box এবং Retry button দেখো।
7. successful sent message-এর পাশে time-এর সাথে `Sent` দেখো।

## 11. এই step-এর main learning

Realtime app-এ empty data মানেই empty product state না। কখনো সেটা loading, কখনো error, কখনো সত্যিকারের empty।

তাই ভালো UI state design করতে হলে:

- loading state
- empty state
- error state
- success data state

আলাদা করে রাখতে হয়।

এই step Chat MVP finish করার প্রথম polish step।
