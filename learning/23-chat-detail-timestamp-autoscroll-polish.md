# Step 23: Chat Detail Timestamp, Auto-scroll, And Polish

এই ধাপে Chat Detail screen-এ real messaging UX একটু ভালো করা হয়েছে।

## কী যোগ হয়েছে

- message timestamp
- latest message-এ auto-scroll
- scrollable message list
- rounded send button
- WhatsApp-like light/default chat polish

## কেন দরকার

আগে messages `Column`-এ দেখানো হচ্ছিল।

সমস্যা:

- message বেশি হলে scroll করা যেত না
- নতুন message পাঠালে latest message দেখা নিশ্চিত ছিল না
- timestamp দেখা যেত না

এখন:

```text
LazyColumn -> scrollable messages
LaunchedEffect(messages.size) -> latest message auto-scroll
Bubble -> text + time
```

## `LazyColumn`

```kotlin
val listState = rememberLazyListState()

LazyColumn(
    state = listState
) {
    items(messages) { message ->
        MessageBubble(...)
    }
}
```

কেন:

- chat list long হতে পারে
- LazyColumn শুধু visible item efficiently compose করে
- WhatsApp-like chat screen-এর জন্য scrollable message list দরকার

## Auto-scroll

```kotlin
LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
        listState.animateScrollToItem(messages.lastIndex)
    }
}
```

কেন:

- নতুন message এলে `messages.size` বদলায়
- তখন latest message-এ scroll হয়
- user send করার পর নিজের message দেখতে পায়

## Timestamp

```kotlin
private fun Long.formatChatTime(): String {
    if (this <= 0L) return "Now"
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(this))
}
```

Firestore `createdAt` server timestamp প্রথম snapshot-এ null হতে পারে। তখন `sentAtMillis = 0` হলে UI `Now` দেখায়।

## Bubble layout

আগে bubble শুধু text দেখাত। এখন:

```text
message text
time
```

নিজের message:

```text
right aligned
primaryContainer background
```

অন্যের message:

```text
left aligned
surfaceVariant background
```

## Send button polish

Button shape:

```kotlin
shape = RoundedCornerShape(22.dp)
```

কেন:

- WhatsApp-like rounded action feel
- default light theme-এর সাথে clean থাকে

## Theme reminder

এই UI polish light theme ধরে করা হয়েছে, কারণ ContactMe default light থাকবে।

Design direction:

```text
Default: Light
Available: Light + Dark
Style: WhatsApp-like
Accent: Green
```

## কীভাবে verify করবে

Build:

```powershell
cd apps\ContactMe
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug
```

Manual:

1. real conversation open করো।
2. কয়েকটা message send করো।
3. latest message automatically visible হচ্ছে কিনা দেখো।
4. প্রতিটা bubble-এ time দেখা যাচ্ছে কিনা দেখো।
5. dummy chat row open করলে demo messages এখনো দেখা যায় কিনা দেখো।

## এখনো কী বাকি

- keyboard-aware better scroll
- date separator
- read/delivery ticks
- message status
- dark mode full visual QA
- WhatsApp-like chat background pattern

## পরের step

```text
Firestore security rules documentation/update
or
Chat list timestamp/unread count
```
