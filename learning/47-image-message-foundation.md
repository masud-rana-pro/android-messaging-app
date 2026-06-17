# Step 47 - Image Message Foundation

এই ধাপে ContactMe app-এ real image message foundation যোগ করা হয়েছে।

আগে app শুধু text message পাঠাতে পারত। এখন user chat screen থেকে image pick করে পাঠাতে পারে। Current implementation-এ image file Cloudinary-তে upload হয়, আর message metadata Firestore-এ save হয়।

## Fake কিছু রাখা হয়েছে?

না।

পুরনো fake message repository remove করা হয়েছে, যাতে chat feature real Firestore repository দিয়েই চলে।

## MessageType কেন দরকার?

আগে message মানেই text ধরে নেওয়া হচ্ছিল। এখন message দুই ধরনের:

```text
text
image
```

তাই enum দরকার:

```kotlin
enum class MessageType(val firestoreValue: String) {
    Text("text"),
    Image("image")
}
```

UI এই `type` দেখে বুঝতে পারে text bubble দেখাবে নাকি image bubble দেখাবে।

## ChatMessage কীভাবে বদলেছে?

Image message support করার জন্য model-এ media fields যোগ হয়েছে:

```kotlin
val type: MessageType
val mediaUrl: String
val mediaProvider: String
val mediaPublicId: String
val mimeType: String
```

Image message হলে:

```text
type = image
mediaProvider = cloudinary
mediaUrl = Cloudinary secure URL
text = empty
```

## Image send flow

```text
User image picks
    |
Cloudinary unsigned upload
    |
secure_url, public_id, mime_type পাওয়া
    |
Firestore message create
    |
Conversation lastMessageText = Photo
```

## FirebaseMessageRepository কী করে?

নতুন method:

```kotlin
suspend fun sendImageMessage(
    conversationId: String,
    senderId: String,
    imageUri: Uri
): MessageResult
```

এটা:

1. Cloudinary-তে selected image upload করে।
2. `secureUrl`, `publicId`, `mimeType` নেয়।
3. Firestore message document তৈরি করে।
4. Conversation preview update করে।

## Firestore image message shape

```text
conversations/{conversationId}/messages/{messageId}
  senderId
  type: "image"
  text: ""
  mediaProvider: "cloudinary"
  mediaUrl: secure_url
  mediaPublicId: public_id
  mimeType: image/jpeg
  status: "sent"
  createdAt
```

## Chat UI change

Chat input bar-এর `+` action Android Photo Picker open করে।

Picker result এলে:

```kotlin
viewModel.sendImageMessage(uri)
```

## Image display

Image message দেখানোর জন্য `AsyncImage` use করা হয়:

```kotlin
AsyncImage(
    model = message.mediaUrl,
    contentDescription = "Photo message"
)
```

মানে UI Cloudinary URL থেকে image load করে।

## Firestore rules

Firestore rules image message-এর জন্য minimum metadata validate করে:

- `type = image`
- `mediaUrl` empty না
- `senderId` current user

Actual image file validation Cloudinary upload preset restriction দিয়ে control করতে হবে।

## কীভাবে verify করবে?

1. Cloudinary dashboard-এ `contactme_unsigned` preset enabled আছে কি না দেখো।
2. App চালাও।
3. Real chat open করো।
4. Input bar-এর `+` tap করো।
5. Image select করো।
6. Image chat bubble-এ দেখা উচিত।
7. Cloudinary Media Library-তে image আছে কি না দেখো।
8. Firestore message document-এ `type`, `mediaProvider`, `mediaUrl`, `mediaPublicId`, `mimeType` আছে কি না দেখো।

## শেখার বিষয়

- Media message দুই জায়গায় data রাখে: file Cloudinary-তে, metadata Firestore-এ।
- Firestore বড় binary file রাখার জায়গা না।
- UI শুধু message `type` দেখে renderer choose করে।
- Production-ready media feature করতে progress, retry, compression, signed upload দরকার।
