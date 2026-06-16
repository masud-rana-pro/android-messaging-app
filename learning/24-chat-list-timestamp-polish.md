# Step 24: Chat List Timestamp Polish

এই ধাপে Home Chats tab-এর real conversation rows আরও WhatsApp-like করা হয়েছে।

## কী হয়েছে

- conversation row-তে timestamp দেখানো হয়েছে
- today হলে time দেখায়
- yesterday হলে `Yesterday`
- older হলে date
- title এবং last message এক লাইনে থাকে
- long text হলে ellipsis হয়

## কেন দরকার

Chat list-এ user দ্রুত বুঝতে চায়:

```text
কার সাথে chat
শেষ message কী
কখন হয়েছিল
```

আগে last message ছিল, কিন্তু time দেখা যেত না।

এখন row structure:

```text
Avatar | Name + last message | time
```

## Timestamp formatter

```kotlin
private fun Long.formatConversationTime(): String
```

Logic:

```text
0 বা invalid time -> blank
আজ -> h:mm AM/PM
গতকাল -> Yesterday
তার আগের -> M/d/yy
```

কেন:

- WhatsApp-style chat list এ recent chat time দেখায়
- older chat date দেখায়
- invalid/pending server timestamp হলে blank রাখা safer

## Same day check

```kotlin
private fun Calendar.isSameDay(other: Calendar): Boolean
```

কেন:

- শুধু date compare করা দরকার
- timestamp-এর hour/minute matter করে না

## Yesterday check

```kotlin
private fun Calendar.isYesterday(today: Calendar): Boolean
```

কেন:

- yesterday label user-friendly
- raw date এর চেয়ে recent context better

## Text overflow

```kotlin
maxLines = 1
overflow = TextOverflow.Ellipsis
```

কেন:

- long display name বা message পুরো row height বাড়িয়ে দেবে না
- chat list clean/dense থাকবে
- WhatsApp-like compact list feel থাকবে

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
2. message send করো।
3. Home-এ ফিরে আসো।
4. conversation row-তে last message + time দেখা উচিত।
5. long message পাঠিয়ে দেখো preview এক লাইনে ellipsis হয় কিনা।

## এখনো কী বাকি

- unread count
- message delivery/read status
- pinned chats
- archive/mute
- visual dark/light full polish

## পরের step

```text
Unread count foundation
or
Firestore security rules documentation/update
```
