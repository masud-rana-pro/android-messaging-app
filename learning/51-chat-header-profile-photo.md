# Step 51 - Chat Header Profile Photo

এই ধাপে আমরা chat detail screen-এর header-এ peer/user profile photo দেখানোর ব্যবস্থা করেছি।

## সমস্যা কী ছিল?

Home screen/recent chat list-এ contact photo দেখা যাচ্ছিল, কিন্তু chat open করলে header avatar শুধু initials দেখাচ্ছিল।

কারণ:

```text
HomeScreen -> ChatDetailScreen
```

এই navigation flow-তে শুধু conversation id আর chat name পাঠানো হচ্ছিল। `photoUrl` পাঠানো হচ্ছিল না।

## কী change করা হলো?

### 1. ChatTarget-এ photoUrl যোগ

```kotlin
data class ChatTarget(
    val title: String,
    val conversationId: String?,
    val photoUrl: String = ""
)
```

`ChatTarget` হলো currently selected chat-এর ছোট navigation state। এখন এখানে photo URL-ও থাকবে।

## 2. Recent chat থেকে photoUrl পাঠানো

`ConversationPreview`-এ আগে থেকেই `photoUrl` ছিল:

```kotlin
val photoUrl: String
```

এখন conversation item click করলে তিনটা data যায়:

```text
conversationId
title
photoUrl
```

## 3. Search/contact থেকে chat open করলে photoUrl পাঠানো

`ConversationViewModel.openDirectConversation(...)` এখন callback-এ user photo URL দেয়:

```kotlin
onReady(conversationId, chatName, otherUser.photoUrl)
```

তাই new chat open করলেও header photo হারায় না।

## 4. ChatDetailScreen photoUrl নেয়

```kotlin
fun ChatDetailScreen(
    chatName: String,
    conversationId: String? = null,
    chatPhotoUrl: String = "",
    onBack: () -> Unit
)
```

এখন screen-এর কাছে header image render করার data আছে।

## 5. Header avatar render logic

যদি `chatPhotoUrl` থাকে:

```kotlin
AsyncImage(
    model = chatPhotoUrl,
    contentScale = ContentScale.Crop
)
```

যদি না থাকে:

```text
initials fallback
```

এতে যাদের photo নেই, তাদের avatar blank দেখাবে না।

## কীভাবে verify করবে?

1. অন্য user/profile-এর photo upload করা আছে কিনা নিশ্চিত করো।
2. Home থেকে recent chat open করো।
3. Chat header-এ image দেখা উচিত।
4. Search/contact result থেকে নতুন chat open করো।
5. Header photo একইভাবে দেখা উচিত।

## শেখার বিষয়

Data শুধু repository-তে থাকলেই UI দেখাবে না। যে screen-এ data দেখাতে হবে, navigation/state path দিয়ে সেই data screen পর্যন্ত পৌঁছাতে হবে।
