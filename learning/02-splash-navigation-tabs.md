# Step 2: Splash, Simple Navigation, Home Tabs

এই ধাপে ContactMe app-এ প্রথম screen flow তৈরি করা হয়েছে। এই document-এ code ধরে ধরে ব্যাখ্যা করা হলো, যেন তুমি শুধু copy না করে বুঝে শিখতে পারো।

## এই step-এর goal

আমাদের app এখন launch হলে সরাসরি chat list দেখানোর বদলে এই flow follow করবে:

```text
App Launch -> Splash Screen -> Home Screen -> Bottom Tabs
```

Roadmap অনুযায়ী `v0.1 UI Demo`-তে app-এর basic screen map দরকার। তাই এই step-এ real Firebase/Auth/Chat না এনে আগে navigation shell বানানো হয়েছে।

## Main file

এই step-এর সব main code আছে:

```text
apps/ContactMe/app/src/main/java/com/contactme/app/MainActivity.kt
```

## Package line

```kotlin
package com.contactme.app
```

এটা বলে এই Kotlin file কোন package-এর অংশ। Android app-এর namespace/package structure organize করার জন্য এটা দরকার।

কেন দরকার:

- একই project-এ অনেক class থাকলে package দিয়ে organize করা যায়।
- Android Manifest থেকে `MainActivity` খুঁজে পেতে package structure গুরুত্বপূর্ণ।
- পরে feature অনুযায়ী package আলাদা করা যাবে, যেমন `ui.auth`, `ui.chat`, `data`, `domain`।

## `MainActivity`

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ContactMeTheme {
                ContactMeApp()
            }
        }
    }
}
```

এটাই Android app-এর entry point। User app icon চাপলে Android system প্রথমে এই Activity চালায়।

লাইন ধরে ব্যাখ্যা:

- `class MainActivity : ComponentActivity()`  
  `MainActivity` Android-এর একটি screen/activity। Compose app-এর জন্য `ComponentActivity` ব্যবহার করা হয়।

- `onCreate(...)`  
  Activity প্রথম তৈরি হলে এই function run হয়।

- `super.onCreate(savedInstanceState)`  
  Android system-এর default setup আগে complete করতে হয়।

- `setContent { ... }`  
  এখানে Compose UI বসানো হয়। XML layout ব্যবহার না করে Kotlin code দিয়েই UI বানানো হচ্ছে।

- `ContactMeTheme { ... }`  
  পুরো app-এ ContactMe-এর color/theme apply করে।

- `ContactMeApp()`  
  এটা আমাদের root composable। এখান থেকেই app-এর screen flow শুরু।

কেন দরকার:

- Android app চালানোর জন্য Activity দরকার।
- Compose UI দেখানোর জন্য `setContent` দরকার।
- Theme wrapper না দিলে app-এর color/design inconsistent হয়ে যাবে।

## `AppScreen` enum

```kotlin
private enum class AppScreen {
    Splash,
    Home
}
```

এটা app-এর বড় screen state define করে। এখন app দুইটা top-level screen জানে:

- `Splash`
- `Home`

কেন enum ব্যবহার করা হলো:

- String লিখলে typo হতে পারে, যেমন `"splash"` বনাম `"Splash"`।
- Enum compile-time safe।
- পরে নতুন screen add করা সহজ:

```kotlin
private enum class AppScreen {
    Splash,
    Auth,
    ProfileSetup,
    Home
}
```

কেন `private`:

- এই enum শুধু `MainActivity.kt` file-এর ভিতর দরকার।
- বাইরে expose করার দরকার নেই, তাই private রাখা clean।

## `HomeTab` enum

```kotlin
private enum class HomeTab(val label: String) {
    Chats("Chats"),
    Status("Status"),
    Calls("Calls"),
    Communities("Communities"),
    Channels("Channels")
}
```

এটা Home screen-এর bottom navigation tab list।

প্রতিটি tab-এর একটা `label` আছে, যেটা UI-তে দেখানো হয়।

কেন দরকার:

- Roadmap-এর required screens অনুযায়ী Home screen-এ main modules দেখাতে হবে।
- একই জায়গায় সব tab define থাকলে maintain করা সহজ।
- `HomeTab.entries` ব্যবহার করে loop চালিয়ে সব tab UI বানানো যায়।

পরে কীভাবে grow করবে:

- `Chats` tab-এ real conversation list আসবে।
- `Status` tab-এ story/status feature আসবে।
- `Calls` tab-এ call history আসবে।
- `Communities` tab-এ community/group collection আসবে।
- `Channels` tab-এ broadcast channel list আসবে।

## Root composable: `ContactMeApp`

```kotlin
@Composable
fun ContactMeApp() {
    var currentScreen by remember { mutableStateOf(AppScreen.Splash) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (currentScreen) {
            AppScreen.Splash -> SplashScreen(
                onSplashFinished = { currentScreen = AppScreen.Home }
            )

            AppScreen.Home -> HomeScreen()
        }
    }
}
```

এটা app-এর root UI controller।

লাইন ধরে ব্যাখ্যা:

- `@Composable`  
  এই function UI draw করতে পারে। Compose UI function-কে composable বলা হয়।

- `var currentScreen by remember { mutableStateOf(AppScreen.Splash) }`  
  এখানে আমরা current screen state রাখছি। শুরুতে value `Splash`।

- `remember`  
  Compose recompose হলেও value মনে রাখে।

- `mutableStateOf(...)`  
  State change হলে Compose automatic UI redraw করে।

- `Surface(...)`  
  Material Design-এর base container। background color দেয়।

- `Modifier.fillMaxSize()`  
  পুরো screen occupy করে।

- `MaterialTheme.colorScheme.background`  
  theme থেকে background color নেয়।

- `when (currentScreen)`  
  current state অনুযায়ী কোন screen দেখাবে সেটা decide করে।

- `onSplashFinished = { currentScreen = AppScreen.Home }`  
  Splash শেষ হলে state change করে Home দেখায়।

কেন দরকার:

- এখন আমরা full Navigation library ব্যবহার করছি না, কারণ app এখনো ছোট।
- শেখার জন্য simple state navigation clear।
- পরে `Navigation Compose` add করলে এই logic route-based navigation-এ convert হবে।

## Splash screen

```kotlin
@Composable
private fun SplashScreen(onSplashFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(900)
        onSplashFinished()
    }
    ...
}
```

এই screen app launch হলে প্রথমে দেখা যায়।

গুরুত্বপূর্ণ অংশ:

- `onSplashFinished: () -> Unit`  
  এটা callback function। Splash নিজে জানে না পরে কোথায় যাবে। Parent composable তাকে বলে দেয়।

- `LaunchedEffect(Unit)`  
  Compose lifecycle-এর ভিতরে side effect চালায়। Splash delay একবারই run করার জন্য এটা ব্যবহার করা হয়েছে।

- `delay(900)`  
  ৯০০ milliseconds wait করে।

- `onSplashFinished()`  
  delay শেষ হলে parent-কে signal দেয়।

কেন callback ব্যবহার:

- `SplashScreen` reusable থাকে।
- Splash screen navigation decision নিজে নেয় না।
- Parent `ContactMeApp` screen state control করে।

## Splash UI layout

```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
    contentAlignment = Alignment.Center
)
```

`Box` হলো simple layout container। এখানে content center-এ রাখা হয়েছে।

কেন দরকার:

- Splash screen সাধারণত center-aligned হয়।
- `fillMaxSize` না দিলে Box পুরো screen নেবে না।
- `padding(24.dp)` দিলে edge থেকে breathing space থাকে।

```kotlin
Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(14.dp)
)
```

`Column` child elements vertical ভাবে সাজায়।

এখানে আছে:

- circular logo placeholder
- `ContactMe` title
- tagline

## Logo placeholder

```kotlin
Box(
    modifier = Modifier
        .size(76.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primary),
    contentAlignment = Alignment.Center
)
```

এটা circular logo-like UI।

লাইন ধরে:

- `.size(76.dp)`  
  Box-এর width/height ৭৬dp।

- `.clip(CircleShape)`  
  Box-কে circle shape করে।

- `.background(MaterialTheme.colorScheme.primary)`  
  ContactMe purple background দেয়।

- `contentAlignment = Alignment.Center`  
  ভিতরের `CM` text center করে।

কেন এখন placeholder:

- Final logo এখনো design হয়নি।
- UI flow verify করার জন্য temporary brand mark যথেষ্ট।
- পরে `design/logo` থেকে real logo resource বসানো যাবে।

## Home screen

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen() {
    var selectedTab by remember { mutableStateOf(HomeTab.Chats) }
    ...
}
```

Home screen হলো main app shell।

গুরুত্বপূর্ণ অংশ:

- `@OptIn(ExperimentalMaterial3Api::class)`  
  `TopAppBar` Material 3 API experimental হতে পারে, তাই explicit opt-in করা হয়েছে।

- `selectedTab`  
  কোন tab selected আছে সেটা track করে।

- শুরুতে `HomeTab.Chats` selected।

## Scaffold

```kotlin
Scaffold(
    topBar = { ... },
    bottomBar = { ... }
) { innerPadding ->
    ...
}
```

`Scaffold` Material app-এর common layout structure দেয়।

এতে থাকে:

- top app bar
- bottom navigation
- content area
- floating action button
- drawer/snackbar support

এখন আমরা ব্যবহার করেছি:

- `topBar`
- `bottomBar`
- body content

কেন দরকার:

- Manual layout করলে top/bottom bar overlap হতে পারে।
- `Scaffold` automatically body content-এর padding calculate করে।
- বড় app shell বানাতে এটা standard Compose pattern।

## Top app bar

```kotlin
TopAppBar(
    title = {
        Text(
            text = "ContactMe",
            fontWeight = FontWeight.Bold
        )
    },
    colors = TopAppBarDefaults.topAppBarColors(
        titleContentColor = MaterialTheme.colorScheme.primary
    )
)
```

এটা Home screen-এর top title bar।

কেন দরকার:

- App identity দেখায়।
- পরে এখানে search, camera, menu icon add করা যাবে।
- WhatsApp-like apps-এ top bar খুব গুরুত্বপূর্ণ navigation area।

## Bottom navigation

```kotlin
NavigationBar {
    HomeTab.entries.forEach { tab ->
        NavigationBarItem(
            selected = selectedTab == tab,
            onClick = { selectedTab = tab },
            icon = { Text(text = tab.label.first().toString()) },
            label = { Text(text = tab.label) }
        )
    }
}
```

এটা bottom tabs তৈরি করে।

লাইন ধরে:

- `HomeTab.entries.forEach { tab -> ... }`  
  enum-এর সব tab loop করে।

- `selected = selectedTab == tab`  
  current tab selected কিনা check করে।

- `onClick = { selectedTab = tab }`  
  User tab চাপলে selected tab change হয়।

- `icon = { Text(text = tab.label.first().toString()) }`  
  এখন temporary icon হিসেবে label-এর প্রথম অক্ষর দেখানো হয়েছে।

- `label = { Text(text = tab.label) }`  
  tab name দেখায়।

কেন temporary text icon:

- এখনো icon library add করা হয়নি।
- পরের UI polish step-এ Material/Lucide style icon add করা যাবে।
- আগে behavior verify করা বেশি জরুরি।

## Body content switching

```kotlin
when (selectedTab) {
    HomeTab.Chats -> ChatsTab()
    HomeTab.Status -> PlaceholderTab(...)
    HomeTab.Calls -> PlaceholderTab(...)
    HomeTab.Communities -> PlaceholderTab(...)
    HomeTab.Channels -> PlaceholderTab(...)
}
```

User যে tab select করে, সেই tab-এর body content দেখানো হয়।

কেন দরকার:

- Same Home screen-এর ভিতরে multiple module preview দেখানো যায়।
- পরে প্রতিটা tab আলাদা composable/file-এ move করা যাবে।

## `ChatsTab`

```kotlin
@Composable
private fun ChatsTab() {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        ...
        ChatPreviewList()
    }
}
```

এটা Chats tab-এর content।

এখন এতে আছে:

- `Chats` title
- subtitle
- dummy chat list

কেন দরকার:

- Roadmap অনুযায়ী messenger core first priority।
- তাই default selected tab `Chats` রাখা হয়েছে।
- পরের দিকে এখানেই real Firestore conversation list আসবে।

## `ChatPreviewList`

```kotlin
private fun ChatPreviewList() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ChatPreviewItem(...)
        ChatPreviewItem(...)
        ChatPreviewItem(...)
    }
}
```

এটা dummy chat row list।

কেন dummy data:

- Backend/Firebase ছাড়া UI verify করা যায়।
- Design spacing, typography, row layout আগে ঠিক করা যায়।
- পরে data model থেকে list render করা সহজ হবে।

## `ChatPreviewItem`

```kotlin
private fun ChatPreviewItem(
    name: String,
    message: String,
    time: String
)
```

একটা chat row দেখায়।

Parameters:

- `name`: contact/group name
- `message`: last message preview
- `time`: last message time

Row structure:

```text
Avatar placeholder | Name + message | Time
```

কেন আলাদা composable:

- একই UI বারবার reuse করা যায়।
- List বড় হলে code clean থাকে।
- পরে avatar, unread count, mute/pin status add করা সহজ।

## `PlaceholderTab`

```kotlin
private fun PlaceholderTab(
    title: String,
    subtitle: String
)
```

Status, Calls, Communities, Channels-এর temporary screen।

কেন দরকার:

- User tab click করলে blank screen না আসে।
- Roadmap modules visually present থাকে।
- পরে একেকটা placeholder real screen দিয়ে replace করা যাবে।

## Preview

```kotlin
@Preview(showBackground = true)
@Composable
fun ContactMeAppPreview() {
    ContactMeTheme {
        HomeScreen()
    }
}
```

Android Studio preview panel-এ UI দেখার জন্য।

কেন `HomeScreen()` preview করা হয়েছে:

- `ContactMeApp()` preview করলে splash delay/state থাকতে পারে।
- Home UI directly preview করা সহজ।

## কীভাবে verify করবে

### Android Studio দিয়ে

1. Android Studio open করো।
2. এই project open করো:

```text
D:\my-projects\github-projects\android-projects\ContactMe\apps\ContactMe
```

3. Gradle sync complete হতে দাও।
4. App run করো।

Expected output:

- App চালু হলে প্রথমে `ContactMe` splash screen দেখা যাবে।
- অল্প সময় পর Home screen আসবে।
- Top bar-এ `ContactMe` দেখা যাবে।
- Bottom navigation-এ `Chats`, `Status`, `Calls`, `Communities`, `Channels` দেখা যাবে।
- Tab click করলে content change হবে।

### Command line দিয়ে

```powershell
cd apps\ContactMe
.\gradlew.bat assembleDebug
```

Expected output:

```text
BUILD SUCCESSFUL
```

এই step-এ build verify করা হয়েছে এবং result successful ছিল।

APK output:

```text
apps\ContactMe\app\build\outputs\apk\debug\app-debug.apk
```

## এই ধাপে কী শেখা হলো

- `Activity` Android app-এর entry point।
- `setContent` Compose UI attach করে।
- `@Composable` UI function define করে।
- `remember` UI state ধরে রাখে।
- `mutableStateOf` state update হলে UI redraw করে।
- `LaunchedEffect` lifecycle-aware side effect চালায়।
- `Scaffold` app shell বানায়।
- `TopAppBar` top title/action area দেয়।
- `NavigationBar` bottom tab navigation বানায়।
- `enum class` screen/tab state clean রাখে।
- Callback দিয়ে child composable parent-কে event জানাতে পারে।

## পরের step

পরের step হবে Auth placeholder flow:

- Login screen
- Register screen
- Profile setup placeholder
- Home screen-এর আগে Auth screen দেখানোর state

## মনে রাখার নিয়ম

প্রতিটা implementation step-এ আমরা এই pattern follow করব:

1. আগে Git status clean কিনা check করব।
2. তারপর ছোট scope-এর code change করব।
3. Build/run verify করব।
4. `/learning` folder-এ বাংলা শেখার note update করব।
5. শেষে Git add/commit/push instruction দেব।
