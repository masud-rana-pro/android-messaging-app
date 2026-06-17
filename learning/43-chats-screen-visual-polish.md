# Step 43 - Chats Screen Visual Polish

এই ধাপে আমরা app-এর এমন জায়গায় কাজ করেছি যেটা তুমি app খুললেই দেখতে পারো: `Chats` screen।

আগের অনেক কাজ ছিল ভিতরের foundation:

- Firebase data
- repository
- privacy
- contacts
- unread/read

কিন্তু চোখে progress কম দেখা যাচ্ছিল। তাই এই step থেকে visual progress বাড়ানো শুরু হলো।

## এই step-এর main goal

Chats screen যেন plain demo screen না লাগে। এটা যেন real messenger app-এর মতো মনে হয়।

তবে important rule রাখা হয়েছে:

```text
কোনো fake chat/contact data যোগ করা হয়নি।
```

মানে UI polish হয়েছে, কিন্তু data এখনো real Firebase-backed flow থেকেই আসছে।

## Header polish

আগে subtitle ছিল:

```text
Messenger core UI demo
```

এটা demo-feel দিচ্ছিল। এখন করা হয়েছে:

```text
Real conversations and saved contacts
```

এতে screen-এর বর্তমান বাস্তব কাজটা পরিষ্কার বোঝায়।

## Search field polish

আগে search field সরাসরি screen-এ ছিল। এখন তার চারপাশে rounded search surface দেওয়া হয়েছে।

Code idea:

```kotlin
SearchSurface {
    OutlinedTextField(...)
}
```

`SearchSurface` আলাদা composable করা হয়েছে, যাতে পরে search design change করতে হলে এক জায়গায় করা যায়।

## Contact row polish

আগে contact/search result plain `Row` ছিল। এখন `Surface` দিয়ে rounded row করা হয়েছে:

```kotlin
Surface(
    shape = RoundedCornerShape(18.dp),
    tonalElevation = 1.dp
)
```

এর ভিতরে:

- avatar
- display name
- username
- phone number
- action pill

সব সাজানো হয়েছে।

## Conversation row polish

Conversation row-ও এখন rounded surface:

- avatar বড় করা হয়েছে
- title bold/read state অনুযায়ী weight
- subtitle max one line
- time right side
- unread badge

Unread আগে ছোট dot ছিল। এখন small badge:

```text
1
```

এটা WhatsApp-like unread indicator-এর কাছাকাছি।

## Section header

Repeated text style এক জায়গায় রাখতে `SectionHeader` বানানো হয়েছে:

```kotlin
private fun SectionHeader(title: String)
```

এখন `Saved contacts` এবং `Recent chats` একই visual style follow করে।

## Supporting text

Loading, empty, message text বারবার একই style-এ দরকার হয়। তাই helper:

```kotlin
private fun SupportingText(text: String)
```

এতে repeated color/typography কমে।

## Empty state

Conversation না থাকলে এখন polished empty state দেখাবে:

```text
No chats yet
Search by username or phone number, then open a real chat.
```

এটা fake chat দেখানোর চেয়ে ভালো, কারণ app-এর real state user বুঝতে পারে।

## Floating action button

Chats tab-এ এখন green FAB আছে। এখনো new chat screen আলাদা করা হয়নি, তাই এটা future action placeholder।

কেন এখন দিলাম?

- app visually messaging app-এর মতো লাগে
- পরের step-এ new chat/contact action attach করা সহজ হবে

## কীভাবে verify করবে?

1. App build করো।
2. Emulator/device-এ app চালাও।
3. Chats screen দেখো:
   - header updated কিনা
   - search field rounded area-তে আছে কিনা
   - saved contacts section আছে কিনা
   - recent chats section আছে কিনা
   - no chat থাকলে empty state দেখাচ্ছে কিনা
4. username/phone search করে দেখো result row আগের চেয়ে polished কিনা।
5. saved contact বা conversation tap করলে real chat open হচ্ছে কিনা।

## এই ধাপ থেকে শেখার বিষয়

- UI polish মানেই fake data নয়।
- Real app progress দেখাতে empty state খুব important।
- Reusable small composable code clean রাখে।
- Data layer ঠিক থাকলে UI polish আলাদা করে করা যায়।

## পরের কাজ

পরের logical visible step হলো `ChatDetailScreen` polish করা:

- better header
- message bubble spacing
- input bar
- typing/last seen placement
- sent/read indicator visual
