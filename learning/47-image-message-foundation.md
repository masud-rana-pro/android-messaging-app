# Step 47 - Image Message Foundation

এই ধাপে আমরা ContactMe app-এ real image message foundation যোগ করেছি।

আগে app শুধু text message পাঠাতে পারত। এখন user chat screen থেকে image pick করে পাঠাতে পারবে। Image file যাবে Firebase Storage-এ, আর message metadata যাবে Firestore-এ।

## Fake কিছু রাখা হয়েছে?

না।

এই step-এ পুরনো `FakeMessageRepository` delete করা হয়েছে।

```text
apps/ContactMe/app/src/main/java/com/contactme/app/message/FakeMessageRepository.kt
```

Message feature এখন Firebase repository দিয়েই চলবে।

## MessageType কেন যোগ করা হলো?

আগে message ধরে নেওয়া হচ্ছিল শুধু text। কিন্তু এখন message দুই রকম:

```text
text
image
```

তাই enum যোগ করা হয়েছে:

```kotlin
enum class MessageType(val firestoreValue: String) {
    Text("text"),
    Image("image")
}
```

এতে UI বুঝতে পারে কোন message text আর কোন message image।

## ChatMessage কীভাবে বদলেছে?

আগে:

```kotlin
val text: String
```

এখন যোগ হয়েছে:

```kotlin
val type: MessageType
val mediaUrl: String
```

Image message হলে:

```text
type = image
mediaUrl = Firebase Storage download URL
text = empty
```

## Image send flow

Flow:

```text
User image picks
    |
Firebase Storage upload
    |
Download URL নেওয়া
    |
Firestore message create
    |
Conversation lastMessageText = Photo
```

## Storage path

Image রাখা হচ্ছে:

```text
chat_media/{conversationId}/{messageId}/image.jpg
```

এখানে `messageId` আগে generate করা হয়, তারপর ওই id দিয়ে Storage path তৈরি হয়।

## FirebaseMessageRepository change

নতুন method:

```kotlin
suspend fun sendImageMessage(
    conversationId: String,
    senderId: String,
    imageUri: Uri
): MessageResult
```

এটা:

1. message document id বানায়
2. Storage reference বানায়
3. `putFile(imageUri)` দিয়ে upload করে
4. `downloadUrl` নেয়
5. Firestore-এ image message save করে

## Chat UI change

Chat input bar-এ `+` action যোগ হয়েছে। এটা Android Photo Picker open করে।

Picker result এলে:

```kotlin
viewModel.sendImageMessage(uri)
```

## Image display

Image message দেখানোর জন্য `AsyncImage` use করা হয়েছে:

```kotlin
AsyncImage(
    model = message.mediaUrl,
    contentDescription = "Photo message"
)
```

## Firestore rules

Message rules এখন দুই type support করে:

- text message
- image message

Image message-এর জন্য `mediaUrl` non-empty হতে হবে।

## Storage rules

MVP হিসেবে signed-in user image upload করতে পারবে:

```text
chat_media/{conversationId}/{messageId}/{fileName}
```

Limit:

```text
10 MB
image/*
```

পরে এই rules আরও strict করা উচিত, যাতে শুধু conversation participant upload/read করতে পারে।

## কীভাবে verify করবে?

1. Firebase rules deploy করো।
2. App চালাও।
3. real chat open করো।
4. input bar-এর `+` tap করো।
5. image select করো।
6. image chat bubble-এ দেখা উচিত।
7. Firebase Storage-এ `chat_media` path তৈরি হয়েছে কিনা দেখো।
8. Firestore message document-এ `type = image` এবং `mediaUrl` আছে কিনা দেখো।

## শেখার বিষয়

- Media message দুই জায়গায় data রাখে: file Storage-এ, metadata Firestore-এ।
- Firestore document id আগে বানালে Storage path stable হয়।
- UI শুধু `type` দেখে ঠিক renderer choose করে।
- Production-ready media feature করতে progress, retry, compression, stricter security rules দরকার।
