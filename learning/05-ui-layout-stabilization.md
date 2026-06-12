# Step 5: UI Layout Stabilization

এই ধাপে UI layout ছোট screen-এ usable রাখার জন্য কিছু important improvement করা হয়েছে।

## Roadmap অনুযায়ী কেন এই step

আমরা এখন `v0.1 UI Demo` phase-এ আছি। এই phase-এর কাজ শুধু screen বানানো না, screen গুলো basic ভাবে ব্যবহারযোগ্য করা।

তাই Firebase/Auth real implementation শুরু করার আগে layout stable করা দরকার।

এই step-এ focus:

- ছোট screen-এ form cut না হওয়া
- Auth/Profile screen scrollable করা
- spacing এক জায়গা থেকে control করা
- bottom navigation label overflow কমানো

## কী কী file change হয়েছে

```text
navigation/HomeTab.kt
ui/theme/Spacing.kt
ui/screens/AuthScreen.kt
ui/screens/ProfileSetupScreen.kt
ui/screens/HomeScreen.kt
```

## `Spacing.kt`

Path:

```text
apps/ContactMe/app/src/main/java/com/contactme/app/ui/theme/Spacing.kt
```

Code:

```kotlin
object ContactMeSpacing {
    val screenHorizontal = 24.dp
    val screenVertical = 28.dp
    val contentGap = 20.dp
    val fieldGap = 12.dp
    val sectionGap = 28.dp
}
```

এটা app-এর common spacing values রাখে।

কেন দরকার:

- একই padding বারবার hardcode করতে হয় না।
- Design consistent থাকে।
- পরে spacing কমাতে/বাড়াতে হলে এক file change করলেই হয়।

Example:

```kotlin
.padding(
    horizontal = ContactMeSpacing.screenHorizontal,
    vertical = ContactMeSpacing.screenVertical
)
```

মানে screen-এর left/right padding ২৪dp, top/bottom padding ২৮dp।

## Auth screen scrollable করা

Path:

```text
ui/screens/AuthScreen.kt
```

Added imports:

```kotlin
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
```

Main change:

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .imePadding()
        .navigationBarsPadding()
        .padding(
            horizontal = ContactMeSpacing.screenHorizontal,
            vertical = ContactMeSpacing.screenVertical
        ),
    verticalArrangement = Arrangement.Center
)
```

লাইন ধরে ব্যাখ্যা:

- `fillMaxSize()`  
  screen-এর full available area নেয়।

- `verticalScroll(rememberScrollState())`  
  content screen-এর height-এর চেয়ে বড় হলে vertical scroll করা যাবে।

- `imePadding()`  
  keyboard open হলে keyboard-এর জায়গা হিসাব করে bottom padding দেয়।

- `navigationBarsPadding()`  
  Android navigation bar-এর পেছনে button আটকে যাওয়া কমায়।

- `rememberScrollState()`  
  scroll position state মনে রাখে।

- `padding(...)`  
  content edge-এর সাথে লেগে থাকে না।

কেন দরকার:

ছোট phone বা keyboard open হলে form field/button cut হতে পারে। Scroll না থাকলে user নিচের button দেখতে পাবে না। Auth form-এ scroll রাখা safe pattern।

Important: content যদি screen-এর চেয়ে ছোট হয়, তাহলে scroll naturally feel হবে না। Scroll visible হবে যখন content viewport-এর চেয়ে বড় হয়, যেমন ছোট emulator, landscape mode, অথবা keyboard open অবস্থায়।

## Keyboard resize support

Path:

```text
apps/ContactMe/app/src/main/AndroidManifest.xml
```

Activity-তে যোগ করা হয়েছে:

```xml
android:windowSoftInputMode="adjustResize"
```

কেন দরকার:

- Keyboard open হলে Android যেন app window resize করে।
- Resize হলে `verticalScroll` + `imePadding` form content accessible রাখতে পারে।
- এটা না থাকলে keyboard field/button ঢেকে দিতে পারে।

## Hardcoded spacing কমানো

আগে অনেক জায়গায় ছিল:

```kotlin
Spacer(modifier = Modifier.height(20.dp))
```

এখন করা হয়েছে:

```kotlin
Spacer(modifier = Modifier.height(ContactMeSpacing.contentGap))
```

কেন ভালো:

- magic number কমে।
- spacing meaning clear হয়।
- design system তৈরি হতে শুরু করে।

## Profile setup screen scrollable করা

Path:

```text
ui/screens/ProfileSetupScreen.kt
```

Main change:

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .imePadding()
        .navigationBarsPadding()
        .padding(
            horizontal = ContactMeSpacing.screenHorizontal,
            vertical = ContactMeSpacing.screenVertical
        ),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
)
```

কেন দরকার:

Profile setup screen-এ photo placeholder, title, subtitle, display name, username, button আছে। ছোট screen-এ এগুলো vertical space বেশি নিতে পারে। Scrollable করলে content accessible থাকে।

## Bottom navigation label compact করা

Path:

```text
navigation/HomeTab.kt
```

আগে:

```kotlin
enum class HomeTab(val label: String)
```

এখন:

```kotlin
enum class HomeTab(val label: String, val shortLabel: String)
```

Example:

```kotlin
Communities("Communities", "Groups")
```

কেন:

`Communities` শব্দটা bottom navigation-এর জন্য বড়। ছোট phone-এ text overlap/cut হতে পারে। তাই full label data হিসেবে রাখা হয়েছে, কিন্তু bottom nav-এ compact `Groups` দেখানো হচ্ছে।

HomeScreen change:

```kotlin
icon = { Text(text = tab.shortLabel.first().toString()) },
label = { Text(text = tab.shortLabel) }
```

এখন bottom tab label বেশি compact।

## Home content padding consistent করা

Path:

```text
ui/screens/HomeScreen.kt
```

আগে:

```kotlin
.padding(20.dp)
```

এখন:

```kotlin
.padding(
    horizontal = ContactMeSpacing.screenHorizontal,
    vertical = ContactMeSpacing.contentGap
)
```

কেন:

- Home screen আর Auth/Profile screen একই spacing language follow করে।
- UI বেশি consistent লাগে।

## কীভাবে verify করবে

### Build verify

```powershell
cd apps\ContactMe
.\gradlew.bat assembleDebug
```

Expected:

```text
BUILD SUCCESSFUL
```

এই step-এ build successful হয়েছে।

### App run করে verify

Android Studio থেকে app run করো।

Check করো:

- Splash screen ঠিকমতো আসে।
- Auth screen-এ field/button edge-এর সাথে লেগে নেই।
- Auth screen ছোট emulator-এ scroll করা যায়।
- Keyboard open করলে নিচের button keyboard-এর পেছনে হারিয়ে যায় না।
- Login/Register button চাপলে title/action text বদলায়।
- Main auth button চাপলে Profile setup screen আসে।
- Profile screen ছোট emulator-এ scroll করা যায়।
- Continue চাপলে Home screen আসে।
- Bottom navigation-এ `Groups` label দেখা যায়, `Communities` overflow করে না।

## এই ধাপে কী শেখা হলো

- Form screen scrollable করা mobile UI-এর জন্য important।
- Common spacing object UI consistency বাড়ায়।
- Hardcoded `dp` value কমালে design maintain করা সহজ হয়।
- Long bottom navigation label ছোট screen-এ problem করতে পারে।
- UI polish আর layout stabilization আলাদা জিনিস। এখন আমরা stabilization করেছি, final polish পরে হবে।

## পরের step

Roadmap অনুযায়ী `v0.1 UI Demo` আরও complete করতে Auth/Profile/Home UI polish করা যায়:

- better button hierarchy
- simple top branding
- placeholder icons
- reusable form components

তারপর `v0.2 Auth Build`-এ Firebase Auth শুরু করা যাবে।
