# Step 26: Unread And Read Foundation

এই ধাপে ContactMe app-এ unread/read foundation যোগ করা হয়েছে।

## কেন দরকার

Messaging app-এ chat list দেখে user বুঝতে চায় কোন conversation-এ নতুন message আছে।

আগে:

```text
সব conversation একই রকম দেখাত
```

এখন:

```text
unread হলে title/time highlight হয়
green dot দেখা যায়
```

## Firestore field

Conversation document-এ নতুন field:

```text
readAtByUser
```

Example:

```text
readAtByUser: {
  "uidA": timestamp,
  "uidB": timestamp
}
```

মানে প্রত্যেক user-এর last read time আলাদা করে রাখা হয়।

## Unread calculate

Conversation preview তৈরি করার সময়:

```text
lastMessageSenderId != currentUserId
updatedAt > readAtByUser.currentUserId
```

এই দুইটা true হলে unread।

কেন নিজের message unread না:

- তুমি নিজে message পাঠালে সেটা unread দেখানো উচিত না

## Chat open হলে read marker

`ChatDetailViewModel.openConversation()` এখন:

```kotlin
markConversationRead(conversationId)
```

Messages listener-এ নতুন message আসলেও:

```kotlin
if (messages.isNotEmpty()) {
    markConversationRead(conversationId)
}
```

মানে chat screen খোলা থাকলে নতুন message read হিসেবে mark হয়।

## Repository method

```kotlin
suspend fun markConversationRead(
    conversationId: String,
    userId: String
)
```

Firebase implementation:

```kotlin
update("readAtByUser.$userId", FieldValue.serverTimestamp())
```

## UI indicator

Unread হলে:

- title bold
- preview semi-bold
- timestamp green + bold
- ছোট green dot

কেন:

- WhatsApp-like unread visual hierarchy
- unread numeric count এখনো নেই, তাই dot first foundation

## কীভাবে verify করবে

Build:

```powershell
cd apps\ContactMe
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug
```

Manual:

1. User A এবং User B তৈরি করো।
2. User A থেকে User B-কে message পাঠাও।
3. User B login করলে Home Chats list-এ conversation unread style দেখা উচিত।
4. User B conversation open করলে read marker update হবে।
5. Back করলে unread dot চলে যাওয়া উচিত।

## এখনো কী বাকি

- unread numeric count
- message-level read receipts
- double tick/sent status
- read marker rules tests

## পরের step

```text
Message delivery/read status UI
or
WhatsApp-like visual polish pass
```
