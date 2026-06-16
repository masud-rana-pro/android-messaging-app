# Step 27: Message Status Foundation

এই step-এ আমরা message-এর জন্য basic status system শুরু করেছি। এখন শুধু `Sent` status আছে। WhatsApp-এর মতো delivered/read receipt পরে করা হবে, কারণ সেগুলোর জন্য real recipient-side data দরকার।

## 1. `MessageStatus` enum কেন বানানো হলো?

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/message/MessageStatus.kt`

Code idea:

```kotlin
enum class MessageStatus(val firestoreValue: String) {
    Sent("sent");
}
```

এখানে `MessageStatus` app-এর internal type। UI বা repository যেন raw string `"sent"` নিয়ে কাজ না করে, এজন্য enum ব্যবহার করা হয়েছে।

কেন দরকার:

- typo কমে যায়।
- পরে `Delivered`, `Read`, `Failed` যোগ করা সহজ হয়।
- Firestore-এর value আর Kotlin code-এর status mapping এক জায়গায় থাকে।

## 2. `fromFirestore()` কী করছে?

```kotlin
fun fromFirestore(value: String?): MessageStatus {
    return entries.firstOrNull { status -> status.firestoreValue == value } ?: Sent
}
```

Firestore থেকে string আসে, যেমন `"sent"`। App-এর ভিতরে আমরা সেটা `MessageStatus.Sent` বানিয়ে ব্যবহার করি।

শেষে `?: Sent` দেওয়া হয়েছে কারণ পুরনো messages-এ `status` field নাও থাকতে পারে। তাহলে app crash না করে default `Sent` ধরে নেবে।

## 3. `ChatMessage` model-এ status যোগ করা

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/message/ChatMessage.kt`

```kotlin
val status: MessageStatus = MessageStatus.Sent
```

এর মানে প্রতিটা message এখন নিজের status বহন করতে পারে। Default `Sent` রাখা হয়েছে যাতে পুরনো demo/fake/old message code সহজে ভাঙে না।

## 4. Firebase-এ status save করা

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/message/FirebaseMessageRepository.kt`

নতুন message পাঠানোর সময়:

```kotlin
"status" to MessageStatus.Sent.firestoreValue
```

এতে Firestore message document-এ `status: "sent"` save হবে।

এখনকার flow:

1. User message লিখে Send চাপবে।
2. Repository text trim করবে।
3. Message document তৈরি হবে।
4. Document-এর ভিতরে senderId, text, type, status, createdAt থাকবে।
5. Conversation document-এর last message info update হবে।

## 5. Firebase থেকে status পড়ে আনা

একই repository-তে observe করার সময়:

```kotlin
status = MessageStatus.fromFirestore(document.getString("status"))
```

Firestore string value app model-এ enum হয়ে ঢুকছে। UI সরাসরি Firestore string জানে না, এটা ভালো separation।

## 6. Fake repository update কেন?

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/message/FakeMessageRepository.kt`

Fake repository preview/test/demo flow-এর জন্য। Real Firebase ছাড়া app-এর কিছু অংশ চালাতে হলে fake data লাগে। তাই fake message-এও:

```kotlin
status = MessageStatus.Sent
```

দেওয়া হয়েছে।

## 7. UI-তে check mark কোথায় দেখানো হচ্ছে?

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/screens/ChatDetailScreen.kt`

আগে bubble-এর নিচে শুধু time দেখাত। এখন `MessageMetaRow()` time এবং status marker দেখায়।

```kotlin
MessageMetaRow(
    sentAtMillis = message.sentAtMillis,
    status = message.status,
    isMine = isMine
)
```

`isMine` true হলে check mark দেখাবে। অন্যের message-এ status দেখানো হবে না, কারণ অন্যের পাঠানো message আপনার app থেকে sent/read status দেখানোর বিষয় না।

## 8. কেন এখন শুধু `✓`?

বর্তমানে app জানে message Firestore-এ save হয়েছে। তাই আমরা বলতে পারি message sent হয়েছে।

কিন্তু delivered/read দেখাতে হলে দরকার:

- recipient device message পেয়েছে কি না,
- recipient chat খুলেছে কি না,
- কোন message পর্যন্ত read হয়েছে।

এই data এখনো model-এ নেই। তাই UI-তে fake double tick বা blue tick দেখানো ঠিক হবে না।

## 9. কীভাবে verify করবে?

1. App run করো।
2. Chat খুলে message পাঠাও।
3. নিজের পাঠানো bubble-এর time-এর পাশে ছোট `✓` দেখো।
4. Firestore console-এ message document খুলে `status: "sent"` আছে কি না দেখো।
5. Build verify:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug
```

এই step-এর output হলো: app এখন message status রাখার জন্য প্রস্তুত।
