# Step 30: Chat Target Navigation State

এই step-এ আমরা chat navigation state clean করেছি। App-এর behavior খুব বেশি বদলায়নি, কিন্তু future bugs কমানোর জন্য state structure ভালো করা হয়েছে।

## 1. আগের সমস্যা কী ছিল?

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/ContactMeApp.kt`

আগে দুইটা আলাদা state ছিল:

```kotlin
var selectedChatName by remember { mutableStateOf("Ayesha Rahman") }
var selectedConversationId by remember { mutableStateOf<String?>(null) }
```

এই দুইটা একসাথে same chat-এর data হলেও আলাদা state হিসেবে থাকত।

Problem:

- এক জায়গায় name update হলো কিন্তু conversationId update হলো না
- অথবা conversationId update হলো কিন্তু title পুরনো রয়ে গেল
- future deep link/notification যোগ করলে mismatch bug হতে পারে

Example bug:

```text
title = "Ayesha"
conversationId = "conversation-of-Rahim"
```

এমন হলে UI এক user-এর নাম দেখাবে, কিন্তু message অন্য conversation থেকে আসবে। এটা chat app-এর জন্য serious bug।

## 2. নতুন `ChatTarget`

নতুন file:

`apps/ContactMe/app/src/main/java/com/contactme/app/navigation/ChatTarget.kt`

Code:

```kotlin
data class ChatTarget(
    val title: String,
    val conversationId: String?
)
```

এখানে `title` এবং `conversationId` একই object-এর ভিতরে থাকে।

কেন `conversationId` nullable?

- Demo/placeholder chat-এর real Firestore conversation নেই
- Real chat-এর conversationId আছে

তাই:

```kotlin
conversationId = null
```

মানে demo chat।

```kotlin
conversationId = "abc123"
```

মানে real Firestore conversation।

## 3. `ContactMeApp`-এ নতুন state

এখন state:

```kotlin
var selectedChatTarget by remember {
    mutableStateOf(ChatTarget(title = "Ayesha Rahman", conversationId = null))
}
```

এতে selected chat-এর সব navigation data এক জায়গায় থাকে।

## 4. `openChat()` helper কেন?

নতুন helper:

```kotlin
fun openChat(target: ChatTarget) {
    selectedChatTarget = target
    currentScreen = AppScreen.ChatDetail
}
```

এটা করার কারণ:

- chat open করার logic এক জায়গায় থাকে
- demo chat, real conversation, discovered user - সবাই same function ব্যবহার করে
- future-এ analytics/deep link/logging লাগলে এক জায়গায় add করা যাবে

## 5. Demo chat flow

Placeholder chat open করলে:

```kotlin
openChat(
    ChatTarget(
        title = chatName,
        conversationId = null
    )
)
```

এখানে `conversationId = null`, তাই `ChatDetailScreen` demo messages দেখাবে।

## 6. Real conversation flow

Real conversation list থেকে open করলে:

```kotlin
openChat(
    ChatTarget(
        title = chatName,
        conversationId = conversationId
    )
)
```

এখানে real Firestore conversationId pass হয়। তাই `ChatDetailViewModel` real messages observe করে।

## 7. Discovered user flow

User search/discovery থেকে কাউকে select করলে আগে conversation create/open হয়:

```kotlin
conversationViewModel.openDirectConversation(userProfile) { conversationId, chatName ->
    openChat(
        ChatTarget(
            title = chatName,
            conversationId = conversationId
        )
    )
}
```

মানে discovery flow-ও এখন same `ChatTarget` path follow করে।

## 8. Future-এ এটা কীভাবে help করবে?

এই refactor ছোট, কিন্তু future-এর জন্য important।

এটা help করবে:

- notification tap করে exact chat open করতে
- search result থেকে chat open করতে
- contact profile থেকে message button চাপলে chat open করতে
- Navigation Compose route migrate করতে
- deep link payload clean করতে

## 9. কীভাবে verify করবে?

Build:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug
```

Manual test:

1. Home screen থেকে demo chat open করো।
2. Back করে real conversation open করো।
3. User discovery থেকে user select করো।
4. প্রত্যেক ক্ষেত্রে chat title ঠিক আছে কি না দেখো।
5. Real conversation হলে messages real Firestore থেকে আসছে কি না দেখো।

## 10. Main learning

যে data একসাথে বদলায়, সেই data এক object-এ রাখা ভালো।

এই case-এ:

```text
chat title + conversation id = chat navigation target
```

তাই আমরা `ChatTarget` বানিয়েছি।
