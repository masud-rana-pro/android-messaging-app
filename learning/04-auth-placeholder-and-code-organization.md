# Step 4: Auth Placeholder Flow and Code Organization

এই ধাপে ContactMe app-এর code structure পরিষ্কার করা হয়েছে এবং roadmap অনুযায়ী Auth placeholder flow যোগ করা হয়েছে।

## Roadmap অনুযায়ী কেন এই step

Roadmap-এর build order:

```text
UI Prototype -> Auth + Profile -> Contacts -> 1-to-1 Chat
```

তাই Home tabs বানানোর পর next logical কাজ হলো Auth/Profile placeholder flow:

```text
Splash -> Auth -> Profile Setup -> Home
```

এখনো real Firebase Auth যোগ করা হয়নি। এটা intentional। আগে UI flow তৈরি হচ্ছে, পরে Firebase connect করা হবে।

## এই step-এ কী করা হয়েছে

- `MainActivity.kt` ছোট করা হয়েছে।
- App route enum আলাদা `navigation` package-এ নেওয়া হয়েছে।
- Home tab enum আলাদা file-এ নেওয়া হয়েছে।
- Auth mode enum আলাদা file-এ নেওয়া হয়েছে।
- `ContactMeApp.kt` root app controller হিসেবে তৈরি করা হয়েছে।
- Screen composable গুলো `ui/screens` package-এ নেওয়া হয়েছে।
- Login/Register placeholder screen যোগ হয়েছে।
- Profile setup placeholder screen যোগ হয়েছে।
- Build verify করা হয়েছে।

## নতুন package structure

```text
com/contactme/app/
  MainActivity.kt
  navigation/
    AppScreen.kt
    AuthMode.kt
    HomeTab.kt
  ui/
    ContactMeApp.kt
    screens/
      SplashScreen.kt
      AuthScreen.kt
      ProfileSetupScreen.kt
      HomeScreen.kt
    theme/
      Color.kt
      Theme.kt
```

## Enum এক file-এ রাখা ভালো, নাকি আলাদা package-এ?

ছোট demo app হলে enum `MainActivity.kt` file-এ রাখলেও চলে।

কিন্তু ContactMe roadmap অনুযায়ী app বড় হবে:

- Auth
- Profile
- Chats
- Chat details
- Status
- Calls
- Groups
- Channels
- Settings
- Privacy
- Admin/support

তাই route/tab/auth mode enum আলাদা package-এ রাখা ভালো।

আমরা করেছি:

```text
navigation/AppScreen.kt
navigation/AuthMode.kt
navigation/HomeTab.kt
```

কেন এটা ভালো:

- `MainActivity` clean থাকে।
- navigation-related সব definition এক জায়গায় থাকে।
- নতুন screen add করা সহজ।
- typo কমে।
- পরে `Navigation Compose` যোগ করলে এখানকার route model কাজে লাগবে।

## `MainActivity.kt`

Path:

```text
apps/ContactMe/app/src/main/java/com/contactme/app/MainActivity.kt
```

Code:

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

এখন `MainActivity` শুধু Android entry point।

এখানে business logic বা screen UI রাখা হয়নি।

কেন:

- Activity ছোট থাকলে maintain করা সহজ।
- UI logic `ContactMeApp` এবং screen files-এ থাকে।
- Android lifecycle আর app UI responsibility আলাদা থাকে।

Flow:

```text
Android launches MainActivity
-> setContent runs
-> ContactMeTheme applies app theme
-> ContactMeApp controls screen flow
```

## `AppScreen.kt`

Path:

```text
navigation/AppScreen.kt
```

Code:

```kotlin
enum class AppScreen {
    Splash,
    Auth,
    ProfileSetup,
    Home
}
```

এটা top-level app screen state।

মানে app একসময় এই চারটার যেকোনো একটা screen দেখাবে:

- `Splash`
- `Auth`
- `ProfileSetup`
- `Home`

কেন দরকার:

- screen flow explicit হয়।
- string route ব্যবহার করলে typo হতে পারে, enum-এ সেটা কমে।
- future screen add করা সহজ।

পরে এটা হতে পারে:

```kotlin
enum class AppScreen {
    Splash,
    Auth,
    ProfileSetup,
    Home,
    Chat,
    Settings
}
```

## `AuthMode.kt`

Path:

```text
navigation/AuthMode.kt
```

Code:

```kotlin
enum class AuthMode(val title: String, val actionLabel: String) {
    Login(
        title = "Welcome back",
        actionLabel = "Log in"
    ),
    Register(
        title = "Create account",
        actionLabel = "Register"
    )
}
```

এটা Auth screen কোন mode-এ আছে সেটা বলে:

- Login mode
- Register mode

প্রতিটা enum value-এর সাথে UI text রাখা হয়েছে:

- `title`
- `actionLabel`

কেন দরকার:

- একই screen দিয়ে Login/Register দুইটা state দেখানো যায়।
- duplicate UI file বানাতে হয়নি।
- button/title mode অনুযায়ী change হয়।

## `HomeTab.kt`

Path:

```text
navigation/HomeTab.kt
```

Code:

```kotlin
enum class HomeTab(val label: String) {
    Chats("Chats"),
    Status("Status"),
    Calls("Calls"),
    Communities("Communities"),
    Channels("Channels")
}
```

এটা Home screen-এর bottom tab list।

কেন আলাদা করা হয়েছে:

- Home tabs roadmap module।
- HomeScreen file বড় হলেও tab definition আলাদা থাকে।
- পরে icon/route যোগ করা সহজ:

```kotlin
enum class HomeTab(val label: String, val route: String)
```

## `ContactMeApp.kt`

Path:

```text
ui/ContactMeApp.kt
```

এই file এখন app-এর root UI controller।

Important code:

```kotlin
var currentScreen by remember { mutableStateOf(AppScreen.Splash) }
```

এখানে current screen state রাখা হচ্ছে।

ব্যাখ্যা:

- `remember` recomposition হলেও value ধরে রাখে।
- `mutableStateOf` value change হলে Compose UI redraw করে।
- শুরুতে screen `Splash`।

Screen switching:

```kotlin
when (currentScreen) {
    AppScreen.Splash -> SplashScreen(
        onSplashFinished = { currentScreen = AppScreen.Auth }
    )

    AppScreen.Auth -> AuthScreen(
        onAuthSuccess = { currentScreen = AppScreen.ProfileSetup }
    )

    AppScreen.ProfileSetup -> ProfileSetupScreen(
        onProfileReady = { currentScreen = AppScreen.Home }
    )

    AppScreen.Home -> HomeScreen()
}
```

Flow:

```text
Splash finishes -> Auth
Auth button click -> ProfileSetup
Profile continue click -> Home
```

কেন callback ব্যবহার করা হয়েছে:

- Child screen নিজে direct parent state change করে না।
- Parent বলে দেয় event হলে কী হবে।
- Screen reusable থাকে।

Example:

```kotlin
AuthScreen(onAuthSuccess = { currentScreen = AppScreen.ProfileSetup })
```

মানে Auth screen login/register button press হলে parent-কে জানাবে, parent screen change করবে।

## `SplashScreen.kt`

Path:

```text
ui/screens/SplashScreen.kt
```

এই file আগের splash UI আলাদা করেছে।

কেন আলাদা file:

- Splash UI আলাদা responsibility।
- `ContactMeApp.kt` ছোট থাকে।
- পরে real session check যোগ করা সহজ।

Future improvement:

```text
Splash -> check session
if logged in -> Home
else -> Auth
```

এখন placeholder হিসেবে সবসময় Auth-এ যায়।

## `AuthScreen.kt`

Path:

```text
ui/screens/AuthScreen.kt
```

এই screen Login/Register placeholder।

State:

```kotlin
var authMode by remember { mutableStateOf(AuthMode.Login) }
var emailOrPhone by remember { mutableStateOf("") }
var password by remember { mutableStateOf("") }
```

ব্যাখ্যা:

- `authMode` বলে Login না Register UI দেখাবে।
- `emailOrPhone` input field-এর value রাখে।
- `password` password field-এর value রাখে।

Text field:

```kotlin
OutlinedTextField(
    value = emailOrPhone,
    onValueChange = { emailOrPhone = it },
    label = { Text(text = "Email or phone") }
)
```

কী কাজ করে:

- User input দিলে `onValueChange` call হয়।
- state update হয়।
- Compose UI updated value দেখায়।

Password field:

```kotlin
visualTransformation = PasswordVisualTransformation()
```

এটা password text hide করে।

Button:

```kotlin
Button(
    modifier = Modifier.fillMaxWidth(),
    onClick = onAuthSuccess
) {
    Text(text = authMode.actionLabel)
}
```

এখন button click করলে fake success ধরে Profile setup screen-এ যায়।

কেন এখন fake:

- Firebase Auth এখনো roadmap-এর next implementation phase।
- UI flow আগে verify করা হচ্ছে।
- পরে এই button real validation/auth call করবে।

Login/Register toggle:

```kotlin
OutlinedButton(onClick = { authMode = AuthMode.Login })
OutlinedButton(onClick = { authMode = AuthMode.Register })
```

এগুলো screen mode বদলায়।

## `ProfileSetupScreen.kt`

Path:

```text
ui/screens/ProfileSetupScreen.kt
```

এই screen profile setup placeholder।

State:

```kotlin
var displayName by remember { mutableStateOf("") }
var username by remember { mutableStateOf("") }
```

কেন দরকার:

- Roadmap-এ Auth + Profile phase আছে।
- User profile ছাড়া chat app meaningful না।
- Later Firebase user document-এ এই data save হবে।

Photo placeholder:

```kotlin
Box(
    modifier = Modifier
        .size(88.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer)
)
```

এখন real image picker নেই। শুধু placeholder আছে।

Future:

- image picker
- Firebase Storage upload
- profile photo URL save

Continue button:

```kotlin
Button(onClick = onProfileReady)
```

এখন Home screen-এ যায়।

## `HomeScreen.kt`

Path:

```text
ui/screens/HomeScreen.kt
```

আগের Home screen code আলাদা file-এ নেওয়া হয়েছে।

কেন:

- Home screen বড় হবে।
- Chats/Status/Calls আলাদা screen/component হবে।
- `MainActivity.kt` বা `ContactMeApp.kt` overloaded হবে না।

## কেন এখনো Navigation Compose ব্যবহার করা হয়নি

এই step-এ simple enum state navigation ব্যবহার করা হয়েছে।

কারণ:

- UI prototype phase।
- screen কম।
- শেখার জন্য state-based flow clear।
- dependency না বাড়িয়ে foundation বোঝা সহজ।

পরে যখন real chat detail, settings, profile, media preview আসবে, তখন `androidx.navigation:navigation-compose` যোগ করা ভালো হবে।

## কীভাবে verify করবে

### Android Studio দিয়ে

1. Project open করো:

```text
D:\my-projects\github-projects\android-projects\ContactMe\apps\ContactMe
```

2. App run করো।

Expected flow:

```text
Splash screen
-> Auth screen
-> Login/Register toggle click করলে title/button text বদলাবে
-> main button click করলে Profile setup screen
-> Continue click করলে Home screen
-> Home tabs click করলে content বদলাবে
```

### Command line দিয়ে

```powershell
cd apps\ContactMe
.\gradlew.bat assembleDebug
```

Expected:

```text
BUILD SUCCESSFUL
```

এই step-এ build command successful হয়েছে।

## এই ধাপে কী শেখা হলো

- বড় app-এ route/tab enum আলাদা package-এ রাখা ভালো।
- `MainActivity` যত ছোট রাখা যায়, তত ভালো।
- Root app state `ContactMeApp`-এ রাখা clean pattern।
- Screen UI আলাদা file/package-এ রাখলে maintainability বাড়ে।
- Callback child screen থেকে parent-কে event জানায়।
- Placeholder flow real backend ছাড়াই user journey verify করতে সাহায্য করে।

## পরের step

Roadmap অনুযায়ী next UI prototype কাজ:

- Auth/Profile UI আরও polish করা
- Auth screen আলাদা Login/Register composable করলে code আরও clean হবে
- অথবা Home tab UI polish করা

Firebase Auth connect করার আগে UI prototype flow stable রাখা ভালো।
