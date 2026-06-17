# Step 52 - Image Message Retry

এই ধাপে আমরা image message upload fail করলে retry করার foundation করেছি।

## সমস্যা কী ছিল?

আগে image upload fail করলে app error দেখাত, কিন্তু retry করলে শুধু text message retry করার logic ছিল। Photo আবার পাঠাতে হলে user-কে আবার picker খুলে image select করতে হতো।

এটা ভালো UX না।

## কী change করা হলো?

### 1. `failedImageUri` state যোগ

File:

```text
apps/ContactMe/app/src/main/java/com/contactme/app/ui/chat/ChatDetailUiState.kt
```

নতুন field:

```kotlin
val failedImageUri: String = ""
```

এখানে last failed image-এর URI রাখা হয়।

## 2. Image send fail হলে URI store করা

`sendImageMessage(imageUri)` upload fail করলে:

```kotlin
failedImageUri = imageUri.toString()
```

মানে app মনে রাখে কোন image upload fail করেছিল।

## 3. Retry method যোগ

File:

```text
apps/ContactMe/app/src/main/java/com/contactme/app/ui/chat/ChatDetailViewModel.kt
```

নতুন method:

```kotlin
fun retryFailedImageMessage() {
    val failedImageUri = _uiState.value.failedImageUri
    if (failedImageUri.isBlank()) return
    sendImageMessage(Uri.parse(failedImageUri))
}
```

এটা saved URI থেকে আবার same image upload চেষ্টা করে।

## 4. Retry button এখন smart

Chat screen-এ error হলে retry action দেখে:

```text
failedImageUri আছে -> image retry
messageText আছে -> text retry
```

তাই photo fail হলে retry চাপলেই same photo আবার upload হবে।

## 5. Placeholder text update

Input bar এখন state অনুযায়ী text দেখায়:

```text
Sending...
Retry photo or choose another
Message
Select a chat
```

এতে user বুঝতে পারে app এখন কী state-এ আছে।

## কীভাবে verify করবে?

1. Real chat open করো।
2. Emulator/device internet বন্ধ করো।
3. Image select করে send করো।
4. Error আসবে।
5. Internet চালু করো।
6. Retry চাপো।
7. Same image upload হয়ে chat bubble-এ দেখা উচিত।

## শেখার বিষয়

Retry feature করতে হলে শুধু error message যথেষ্ট না। যে operation fail করেছে, সেটার minimum input state ধরে রাখতে হয়।

এই ক্ষেত্রে fail operation-এর input হলো:

```text
imageUri
```

তাই আমরা `failedImageUri` state রাখলাম।
