# Step 64 - Chat Action Menu

এই ধাপে chat header-এর safety actions menu-তে নেওয়া হয়েছে।

## কেন দরকার?

আগে header-এ সরাসরি দুইটা text button ছিল:

```text
Report
Block
```

এতে chat header crowded লাগতে পারে, বিশেষ করে ছোট screen-এ।

WhatsApp-like app-এ এমন actions সাধারণত overflow menu-তে থাকে।

## কী change করা হলো?

Header-এ এখন শুধু compact trigger:

```text
⋮
```

এই menu খুললে actions দেখা যায়:

```text
Report
Block / Unblock
```

## নতুন composable

```kotlin
ChatActionMenu(...)
```

এটা menu state ধরে:

```kotlin
var isExpanded by remember { mutableStateOf(false) }
```

## Block না Unblock কীভাবে decide হয়?

UI state দেখে:

```text
canUnblockChat = true -> Unblock
canUnblockChat = false -> Block
```

## Safety behavior বদলেছে?

না।

শুধু UI placement বদলেছে। Block/report/unblock logic আগের মতোই ViewModel দিয়ে চলে।

## কীভাবে verify করবে?

1. Chat open করো।
2. Header-এর `⋮` tap করো।
3. `Report` এবং `Block` দেখা উচিত।
4. Block করলে menu-তে `Unblock` দেখা উচিত।

## শেখার বিষয়

Feature কাজ করলেই শেষ না। UI placement-ও গুরুত্বপূর্ণ। Frequently visible জায়গায় বেশি action রাখলে screen cluttered হয়; secondary actions menu-তে রাখা clean design।
