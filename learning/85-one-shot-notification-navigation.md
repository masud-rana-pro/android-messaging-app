# ধাপ ৮৫: Notification navigation একবার consume করা

## সমস্যাটি

`notificationChatTarget` আগে `MainActivity`-তে non-null থাকত। Chat খোলার পরে user Back চাপলে screen Home হতো। `LaunchedEffect(notificationChatTarget, currentScreen)` আবার চলত এবং একই target দিয়ে chat পুনরায় খুলে দিত। ফলে user navigation loop-এ আটকে যেতে পারত। Activity recreate হলেও পুরোনো intent extras থেকে একই target আবার তৈরি হতো।

## One-shot event pattern

এখন `ContactMeApp` target দিয়ে chat খোলার সঙ্গে সঙ্গে `onNotificationChatTargetConsumed()` callback চালায়। `MainActivity` তখন:

- Compose state-এর target null করে;
- activity intent থেকে conversation id, title, photo ও type extras সরিয়ে দেয়।

এতে state এবং source intent—দুই জায়গাতেই event consume হয়।

## Auth restore behavior

Splash বা Auth screen-এ target সঙ্গে সঙ্গে consume হয় না। User session/profile ready হয়ে Home-এ পৌঁছালে target chat খোলে, তারপর consume হয়। অর্থাৎ cold-start notification intent auth flow পার হতে পারে, কিন্তু সফল navigation-এর পরে আর replay হয় না।

## নতুন notification

Activity চলমান থাকলে নতুন notification tap `onNewIntent(...)` দিয়ে নতুন target set করে। আগের target clear করা নতুন intent handling বন্ধ করে না।

## Manual verification

1. App বন্ধ রেখে message notification tap করুন।
2. Authenticated হলে target chat খুলেছে দেখুন।
3. Back চাপলে Home-এ থেকে যাচ্ছে, chat আবার খুলছে না নিশ্চিত করুন।
4. Chat খোলা অবস্থায় অন্য conversation-এর notification tap করে নতুন chat খুলছে দেখুন।
5. Signed-out cold start-এ login/profile flow শেষে target chat খোলে কিনা দেখুন।

## Build note

Low-token নির্দেশনা অনুযায়ী Gradle build/test চালানো হয়নি। Targeted state-flow review এবং `git diff --check` করা হয়েছে।
