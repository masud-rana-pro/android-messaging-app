# Step 45 - Real New Chat Entry

এই ধাপে আমরা Chats screen-এর green floating action button বা FAB-কে real কাজ দিয়েছি।

আগে FAB শুধু visual ছিল। Tap করলে meaningful কিছু করত না। এখন এটা new chat শুরু করার entry point।

## New chat বলতে কী বোঝাচ্ছি?

এখনো dedicated full contact picker screen বানানো হয়নি। কিন্তু app-এ already real user search আছে:

```text
username search
phone number search
```

তাই এই ধাপে FAB tap করলে user সরাসরি search field-এ চলে যাবে। এরপর সে real user search করে chat open করতে পারবে।

## কী behavior যোগ হয়েছে?

FAB tap করলে:

1. Chats tab active থাকে।
2. screen top/search area-তে scroll করে।
3. search input focus হয়।
4. keyboard open হয়।

এতে user বুঝতে পারে: “নতুন chat শুরু করতে search করো।”

## State কীভাবে কাজ করে?

`HomeScreen`-এ একটা counter রাখা হয়েছে:

```kotlin
var newChatRequestCount by remember { mutableStateOf(0) }
```

FAB tap করলে:

```kotlin
newChatRequestCount += 1
```

Counter use করার কারণ হলো একই button বারবার tap করলেও `LaunchedEffect` trigger করা যায়।

## Scroll-to-search কীভাবে হলো?

`ChatsContent`-এ scroll state রাখা হয়েছে:

```kotlin
val scrollState = rememberScrollState()
```

তারপর request এলে:

```kotlin
scrollState.animateScrollTo(0)
```

মানে screen উপরে search area-তে ফিরে যায়।

## Search field focus কীভাবে হলো?

`FocusRequester` use করা হয়েছে:

```kotlin
val searchFocusRequester = remember { FocusRequester() }
```

Text field-এ attach:

```kotlin
Modifier.focusRequester(searchFocusRequester)
```

তারপর FAB request এলে:

```kotlin
searchFocusRequester.requestFocus()
```

## Keyboard কীভাবে open হয়?

Compose keyboard controller:

```kotlin
val keyboardController = LocalSoftwareKeyboardController.current
```

তারপর:

```kotlin
keyboardController?.show()
```

## Fake data যোগ করা হয়েছে?

না।

এই step-এ কোনো fake user, fake chat, fake message যোগ করা হয়নি। FAB শুধু real search flow-কে দ্রুত access করার shortcut।

## কীভাবে verify করবে?

1. App চালাও।
2. Chats screen-এ যাও।
3. নিচে scroll করো।
4. green `+` FAB tap করো।
5. screen search field-এর দিকে যাবে।
6. keyboard open হবে।
7. username বা phone লিখে real user search করো।
8. result tap করলে real direct chat open হবে।

## এই ধাপ থেকে শেখার বিষয়

- Placeholder UI রাখা উচিত না; button থাকলে action থাকা দরকার।
- New chat screen না বানিয়েও existing search flow দিয়ে useful entry করা যায়।
- `FocusRequester` input focus control করতে use হয়।
- `LaunchedEffect` event-like state change handle করতে কাজে লাগে।

## পরের কাজ

এখন app-এর main visible chat flow ভালো হচ্ছে। পরের বড় feature হতে পারে:

- media messaging foundation
- push notification foundation
- অথবা dedicated contacts picker screen
