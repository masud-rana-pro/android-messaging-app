# Step 8: v0.1 UI Demo Screen Map Completion

এই ধাপে ContactMe app-এর `v0.1 UI Demo` screen map আরও complete করা হয়েছে।

## Roadmap অনুযায়ী কেন এই step

Roadmap-এর `v0.1 UI Demo` target:

```text
All core screens with dummy data and ContactMe theme
```

আগে আমাদের ছিল:

```text
Splash -> Auth -> ProfileSetup -> Home tabs
```

এই step-এ যোগ হয়েছে:

```text
Home Chats -> Chat Detail
Home Settings action -> Profile & Settings
```

এতে real backend ছাড়াই app-এর main user journey বোঝা যায়।

## কী কী file change হয়েছে

```text
navigation/AppScreen.kt
ui/ContactMeApp.kt
ui/screens/HomeScreen.kt
ui/screens/ChatDetailScreen.kt
ui/screens/SettingsScreen.kt
```

## `AppScreen.kt`

আগে:

```kotlin
enum class AppScreen {
    Splash,
    Auth,
    ProfileSetup,
    Home
}
```

এখন:

```kotlin
enum class AppScreen {
    Splash,
    Auth,
    ProfileSetup,
    Home,
    ChatDetail,
    Settings
}
```

কেন:

- Chat list item click করলে আলাদা chat screen দরকার।
- Profile/settings placeholder দরকার।
- App-এর top-level screen flow enum-এ পরিষ্কার থাকে।

## `ContactMeApp.kt`

নতুন state:

```kotlin
var selectedChatName by remember { mutableStateOf("Ayesha Rahman") }
```

কেন দরকার:

- User যে chat row click করে, Chat detail screen-এ সেই নাম দেখাতে হবে।
- এখন real conversation id নেই, তাই demo হিসেবে chat name রাখা হয়েছে।
- পরে এটা `conversationId` হবে।

Home screen callback:

```kotlin
HomeScreen(
    onChatSelected = { chatName ->
        selectedChatName = chatName
        currentScreen = AppScreen.ChatDetail
    },
    onSettingsSelected = {
        currentScreen = AppScreen.Settings
    }
)
```

ব্যাখ্যা:

- `HomeScreen` নিজে navigation state change করে না।
- Home শুধু event পাঠায়।
- Root `ContactMeApp` decide করে কোন screen দেখাবে।

এই pattern ভালো কারণ:

- screen reusable থাকে
- navigation logic এক জায়গায় থাকে
- পরে Navigation Compose-এ migrate করা সহজ

## `HomeScreen.kt`

Function signature update:

```kotlin
fun HomeScreen(
    onChatSelected: (String) -> Unit,
    onSettingsSelected: () -> Unit
)
```

এখানে দুইটা callback:

- `onChatSelected`: chat item click হলে call হয়
- `onSettingsSelected`: Settings button click হলে call হয়

Top app bar action:

```kotlin
actions = {
    TextButton(onClick = onSettingsSelected) {
        Text(text = "Settings")
    }
}
```

কেন:

- এখন app bar থেকে Profile & Settings placeholder খোলা যায়।
- পরে এখানে icon/menu/search আসবে।

Chat item clickable:

```kotlin
modifier = Modifier
    .fillMaxWidth()
    .clickable(onClick = onClick)
```

কেন:

- Chat list row tap করলে detail screen খুলতে হবে।
- `clickable` Compose modifier row-কে tap target বানায়।

## `ChatDetailScreen.kt`

এই নতুন screen dummy chat detail দেখায়।

Function:

```kotlin
fun ChatDetailScreen(
    chatName: String,
    onBack: () -> Unit
)
```

Parameters:

- `chatName`: top bar title
- `onBack`: Home screen-এ ফেরার callback

Top bar:

```kotlin
TopAppBar(
    title = {
        Column {
            Text(text = chatName, fontWeight = FontWeight.Bold)
            Text(text = "online")
        }
    },
    navigationIcon = {
        TextButton(onClick = onBack) {
            Text(text = "Back")
        }
    }
)
```

কেন:

- Chat detail screen-এ contact name দরকার।
- online/last seen area future presence feature-এর placeholder।
- Back action দিয়ে Home screen-এ ফেরা যায়।

Message bubbles:

```kotlin
MessageBubble(
    text = "The first screen map is almost done.",
    isMine = true
)
```

`isMine` দিয়ে বোঝানো হচ্ছে message sender current user কিনা।

Bubble alignment:

```kotlin
horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
```

মানে:

- নিজের message ডান পাশে
- অন্যের message বাম পাশে

Message input placeholder:

```kotlin
OutlinedTextField(
    value = "",
    onValueChange = {},
    readOnly = true,
    placeholder = { Text(text = "Message input placeholder") }
)
```

এখন input real না, শুধু UI map-এর জন্য placeholder।

## `SettingsScreen.kt`

এই screen profile/settings placeholder।

Function:

```kotlin
fun SettingsScreen(onBack: () -> Unit)
```

Content:

- profile avatar placeholder
- display name
- username
- Privacy
- Notifications
- Storage
- Blocked users

কেন দরকার:

Roadmap-এর required screens-এ Profile/Settings আছে:

```text
Profile, privacy, notification settings, storage settings, blocked users
```

এই step-এ real settings logic না দিয়ে screen map তৈরি করা হয়েছে।

## কেন এখন dummy UI

কারণ আমরা এখনো `v0.1 UI Demo` phase-এ।

এই phase-এর কাজ:

- navigation flow
- screen map
- dummy UI
- ContactMe theme

Real Firebase/Auth/Firestore এখনো না।

## কীভাবে verify করবে

### Build

```powershell
cd apps\ContactMe
.\gradlew.bat assembleDebug
```

Expected:

```text
BUILD SUCCESSFUL
```

এই step-এ build successful হয়েছে।

### App run

Expected flow:

```text
Splash
-> Auth
-> Profile setup
-> Home
-> Tap any chat row
-> Chat detail screen
-> Back
-> Home
-> Settings
-> Profile & Settings screen
-> Back
-> Home
```

## এই ধাপে কী শেখা হলো

- Root app state দিয়ে simple navigation করা যায়।
- Callback child screen থেকে parent screen-এ event পাঠায়।
- List row clickable করতে `Modifier.clickable` লাগে।
- Dynamic selected item root state-এ রাখা যায়।
- Dummy screen map real backend-এর আগে user journey validate করতে সাহায্য করে।

## পরের step

এখন v0.1 UI Demo close করার আগে দুইটা ভালো কাজ বাকি:

1. Required screens checklist update করা।
2. UI Demo build/run screenshots বা release checklist prepare করা।

তারপর `v0.2 Auth Build` শুরু করা যাবে:

- Hilt/ViewModel foundation
- Firebase Auth setup
- real login/register
- session restore
