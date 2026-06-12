# Step 1: Android Project Scaffold

এই ধাপে `apps/ContactMe` folder-এর ভিতরে ContactMe Android app-এর প্রাথমিক কাঠামো তৈরি করা হয়েছে। এই document-এ project structure, Gradle, Manifest, Activity, Theme এবং প্রথম UI code ধরে ধরে ব্যাখ্যা করা হলো।

## এই step-এর goal

এই step-এর উদ্দেশ্য ছিল Android app-এর foundation তৈরি করা।

Foundation বলতে বোঝায়:

- Android Studio দিয়ে open করা যাবে এমন project
- Gradle দিয়ে build করা যাবে এমন setup
- Kotlin + Jetpack Compose support
- App-এর entry point
- Basic ContactMe theme
- প্রথম demo UI

এই foundation ছাড়া Auth, Chat, Firebase, Notification, Calling কিছুই clean ভাবে যোগ করা যাবে না।

## Current Android app location

Android project রাখা হয়েছে:

```text
apps/ContactMe
```

কারণ পুরো repository শুধু Android app না। Root project-এ future backend, Firebase rules, docs, design, tests, scripts সব থাকবে।

Full roadmap-style root:

```text
ContactMe/
  apps/
    ContactMe/
  backend/
  firebase/
  docs/
  design/
  learning/
  scripts/
  tests/
```

এটা monorepo-style structure। বড় project বা full-stack project-এর জন্য এটা ভালো।

## কেন `java` folder আছে, যদিও app Kotlin দিয়ে বানানো

Source file path এখন এমন:

```text
apps/ContactMe/app/src/main/java/com/contactme/app/MainActivity.kt
```

এখানে folder-এর নাম `java`, কিন্তু file হলো Kotlin:

```text
MainActivity.kt
```

Android project-এ historically default source folder:

```text
src/main/java
```

এই folder-এর ভিতরে Java এবং Kotlin দুই ধরনের file রাখা যায়। তাই Kotlin app হলেও `java` folder থাকা normal।

চাইলে পরে এই structure করা যায়:

```text
src/main/kotlin/com/contactme/app
```

কিন্তু এখন `src/main/java` রাখলে কোনো সমস্যা নেই।

## Project-level file: `settings.gradle.kts`

Path:

```text
apps/ContactMe/settings.gradle.kts
```

এই file Gradle-কে বলে:

- project-এর নাম কী
- কোন module build হবে
- dependency repository কোথায় পাওয়া যাবে

Typical code:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ContactMe"
include(":app")
```

লাইন ধরে ব্যাখ্যা:

- `pluginManagement`  
  Android/Kotlin Gradle plugin কোথা থেকে download হবে সেটা বলে।

- `google()`  
  Android Gradle plugin, AndroidX library, Compose library অনেক সময় Google repository থেকে আসে।

- `mavenCentral()`  
  অনেক open-source library Maven Central থেকে আসে।

- `gradlePluginPortal()`  
  Gradle plugin resolve করার জন্য ব্যবহৃত হয়।

- `dependencyResolutionManagement`  
  dependency repository central ভাবে define করে।

- `repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)`  
  project-এর আলাদা module-এ random repository add করা আটকায়। এতে dependency source clean থাকে।

- `rootProject.name = "ContactMe"`  
  Gradle project-এর নাম।

- `include(":app")`  
  Gradle-কে বলে `app` নামে একটি module আছে।

কেন দরকার:

- Android app build করার জন্য Gradle জানতে হবে কোন module build করবে।
- Repository না থাকলে dependency download হবে না।

## Project-level file: `build.gradle.kts`

Path:

```text
apps/ContactMe/build.gradle.kts
```

এই file root Gradle configuration ধরে।

Typical code:

```kotlin
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
```

লাইন ধরে:

- `com.android.application`  
  Android app build করার plugin।

- `org.jetbrains.kotlin.android`  
  Android project-এ Kotlin support দেয়।

- `org.jetbrains.kotlin.plugin.compose`  
  Kotlin compiler-কে Compose code compile করতে সাহায্য করে।

- `apply false`  
  Root project plugin version জানে, কিন্তু এখানেই apply করছে না। Actual apply হবে `app/build.gradle.kts`-এ।

কেন দরকার:

- Plugin version এক জায়গায় থাকলে maintain করা সহজ।
- App module clean থাকে।

## Gradle properties: `gradle.properties`

Path:

```text
apps/ContactMe/gradle.properties
```

Typical content:

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

লাইন ধরে:

- `org.gradle.jvmargs=-Xmx2048m`  
  Gradle build-এর জন্য JVM memory limit দেয়।

- `-Dfile.encoding=UTF-8`  
  file encoding UTF-8 রাখে। বাংলা document/code comment ঠিক রাখার জন্যও useful।

- `android.useAndroidX=true`  
  Modern AndroidX library ব্যবহার করার জন্য।

- `kotlin.code.style=official`  
  Kotlin official formatting style follow করে।

- `android.nonTransitiveRClass=true`  
  Resource access বেশি explicit করে, বড় project-এ build performance/helpful হতে পারে।

## App module file: `app/build.gradle.kts`

Path:

```text
apps/ContactMe/app/build.gradle.kts
```

এটাই actual Android app module-এর build file।

Important sections:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
```

এখানে plugin apply করা হয়েছে। Root file-এ version define ছিল, এখানে actual app module-এ ব্যবহার করা হচ্ছে।

```kotlin
android {
    namespace = "com.contactme.app"
    compileSdk = 35
}
```

- `namespace`  
  Android resource/package namespace। এটা app-এর code identity-এর অংশ।

- `compileSdk`  
  কোন Android SDK version দিয়ে compile হবে।

```kotlin
defaultConfig {
    applicationId = "com.contactme.app"
    minSdk = 24
    targetSdk = 35
    versionCode = 1
    versionName = "0.1.0"
}
```

- `applicationId`  
  Play Store/device-এ app-এর unique id।

- `minSdk`  
  app minimum কোন Android version support করবে।

- `targetSdk`  
  app কোন Android behavior target করে।

- `versionCode`  
  numeric release version। Play Store update বুঝতে ব্যবহার করে।

- `versionName`  
  human-readable version, যেমন `0.1.0`।

```kotlin
buildFeatures {
    compose = true
}
```

এটা না দিলে Jetpack Compose UI compile হবে না।

```kotlin
dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
}
```

Dependency ব্যাখ্যা:

- `compose-bom`  
  Compose libraries-এর compatible versions manage করে।

- `activity-compose`  
  Activity থেকে Compose UI চালাতে লাগে।

- `material3`  
  Material Design 3 components দেয়, যেমন `Text`, `Surface`, `Scaffold`, `NavigationBar`।

- `ui`  
  Compose UI core।

- `ui-tooling-preview`  
  Android Studio preview support।

- `core-ktx`  
  Kotlin-friendly Android utility।

- `lifecycle-runtime-ktx`  
  lifecycle-aware behavior support।

## Android Manifest

Path:

```text
apps/ContactMe/app/src/main/AndroidManifest.xml
```

Manifest Android system-কে app সম্পর্কে basic information দেয়।

Important code:

```xml
<application
    android:allowBackup="true"
    android:icon="@drawable/ic_launcher_foreground"
    android:label="@string/app_name"
    android:roundIcon="@drawable/ic_launcher_foreground"
    android:supportsRtl="true"
    android:theme="@style/Theme.ContactMe">
```

লাইন ধরে:

- `android:allowBackup="true"`  
  Android backup system app data backup করতে পারে।

- `android:icon`  
  Launcher icon।

- `android:label="@string/app_name"`  
  App name string resource থেকে নেয়।

- `android:supportsRtl="true"`  
  Right-to-left language support করে।

- `android:theme="@style/Theme.ContactMe"`  
  App launch theme।

Activity অংশ:

```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:theme="@style/Theme.ContactMe">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

লাইন ধরে:

- `android:name=".MainActivity"`  
  এই Activity app-এর screen।

- `android:exported="true"`  
  Android 12+ এ launcher activity-এর জন্য দরকার।

- `MAIN` action  
  app launch entry point।

- `LAUNCHER` category  
  app icon থেকে launch করা যাবে।

কেন দরকার:

- Manifest ছাড়া Android system জানবে না app কোথা থেকে শুরু হবে।

## String resource

Path:

```text
apps/ContactMe/app/src/main/res/values/strings.xml
```

Content:

```xml
<resources>
    <string name="app_name">ContactMe</string>
</resources>
```

কেন দরকার:

- App name hardcode না করে resource-এ রাখা ভালো।
- পরে localization করতে সহজ হয়।
- Manifest resource থেকে app name নেয়।

## Theme resource

Path:

```text
apps/ContactMe/app/src/main/res/values/themes.xml
```

Content:

```xml
<resources>
    <style name="Theme.ContactMe" parent="android:style/Theme.Material.Light.NoActionBar" />
</resources>
```

এটা Android system-level launch theme।

Compose-এর ভিতরে আরেকটা Kotlin theme আছে:

```text
ui/theme/Theme.kt
```

দুইটা আলাদা:

- XML theme Android system launch/window-level theme।
- Kotlin Compose theme app UI-এর Material theme।

## Color theme file: `Color.kt`

Path:

```text
apps/ContactMe/app/src/main/java/com/contactme/app/ui/theme/Color.kt
```

Code:

```kotlin
val ContactMePurple = Color(0xFFA605E6)
val ContactMePurpleDark = Color(0xFF7200A8)
val ContactMePurpleLight = Color(0xFFF3D9FF)
val ContactMeBackground = Color(0xFFFFFBFE)
val ContactMeText = Color(0xFF211F26)
```

ব্যাখ্যা:

- `ContactMePurple`  
  App-এর primary brand color।

- `ContactMePurpleDark`  
  dark variant, primary container text বা contrast-এর জন্য।

- `ContactMePurpleLight`  
  light container color, avatar placeholder/background-এর জন্য।

- `ContactMeBackground`  
  app screen background।

- `ContactMeText`  
  readable main text color।

কেন আলাদা file:

- Color centralize থাকে।
- একই color বারবার hardcode করতে হয় না।
- Design update হলে এক জায়গায় change করা যায়।

## Compose theme file: `Theme.kt`

Path:

```text
apps/ContactMe/app/src/main/java/com/contactme/app/ui/theme/Theme.kt
```

Important code:

```kotlin
private val LightColorScheme = lightColorScheme(
    primary = ContactMePurple,
    onPrimary = ContactMeBackground,
    primaryContainer = ContactMePurpleLight,
    onPrimaryContainer = ContactMePurpleDark,
    background = ContactMeBackground,
    onBackground = ContactMeText,
    surface = ContactMeBackground,
    onSurface = ContactMeText
)
```

এটা Material 3 color system define করে।

Concept:

- `primary`  
  প্রধান brand color।

- `onPrimary`  
  primary color-এর ওপর যে text/icon থাকবে তার color।

- `primaryContainer`  
  primary-এর soft background।

- `background`  
  screen background।

- `onBackground`  
  background-এর ওপর text color।

- `surface`  
  cards/sheets/app surfaces-এর color।

- `onSurface`  
  surface-এর ওপর text color।

Theme wrapper:

```kotlin
@Composable
fun ContactMeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
```

কেন দরকার:

- পুরো app-এ consistent colors apply করে।
- UI component `MaterialTheme.colorScheme.primary` ব্যবহার করতে পারে।
- পরে typography/shapes যোগ করা সহজ।

## MainActivity and first UI

Step 1-এ `MainActivity.kt`-এ basic UI ছিল। Step 2-এ এটা splash/navigation দিয়ে update হয়েছে, কিন্তু foundation একই:

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

এই code-এর flow:

```text
Android launches MainActivity
-> onCreate runs
-> setContent attaches Compose UI
-> ContactMeTheme applies theme
-> ContactMeApp draws screen
```

## `@Composable` কী

Compose UI function-এর আগে `@Composable` থাকে।

Example:

```kotlin
@Composable
fun ContactMeApp() {
    ...
}
```

মানে:

- এই function UI draw করতে পারে।
- অন্য composable call করতে পারে।
- Compose runtime এই function re-run করে UI update করতে পারে।

## `Modifier` কী

Compose-এ UI component-এর size, padding, background, alignment ইত্যাদি control করতে `Modifier` ব্যবহার হয়।

Example:

```kotlin
Modifier
    .fillMaxSize()
    .padding(20.dp)
```

মানে:

- পুরো available space নাও।
- চারপাশে ২০dp padding দাও।

## First UI কেন dummy ছিল

প্রথম UI-তে dummy chat list ছিল, কারণ:

- Firebase এখনো setup হয়নি।
- আগে UI layout check করা দরকার।
- Chat row design later real data দিয়ে replace করা যাবে।

Dummy UI development-এর common technique। এতে backend ready হওয়ার আগেই screen design এগোয়।

## কীভাবে verify করবে

### Android Studio দিয়ে

1. Android Studio open করো।
2. এই folder open করো:

```text
D:\my-projects\github-projects\android-projects\ContactMe\apps\ContactMe
```

3. Gradle sync complete হতে দাও।
4. Run button চাপো।

Expected:

- App build হবে।
- Emulator/device-এ ContactMe app install হবে।
- এখন Step 2-এর কারণে splash তারপর Home tabs দেখা যাবে।

### Command line দিয়ে

Project root থেকে:

```powershell
cd apps\ContactMe
.\gradlew.bat assembleDebug
```

Expected:

```text
BUILD SUCCESSFUL
```

APK তৈরি হবে:

```text
apps\ContactMe\app\build\outputs\apk\debug\app-debug.apk
```

## Git-এ কী commit করা উচিত না

Android Studio build করার পর কিছু generated file/folder তৈরি হয়:

```text
.gradle/
build/
.idea/
local.properties
.kotlin/
```

এগুলো সাধারণত Git-এ commit করা উচিত না।

কেন:

- এগুলো machine-specific।
- build output auto-generated।
- অন্য developer-এর machine-এ আলাদা হতে পারে।
- repository unnecessary বড় হয়।

Root `.gitignore`-এ এইগুলো ignore করা আছে। পরে আমরা check করব কোনো generated file accidentally tracked আছে কিনা।

## এই ধাপে কী শেখা হলো

- Android app Gradle দিয়ে build হয়।
- `settings.gradle.kts` project/module define করে।
- Root `build.gradle.kts` plugin versions ধরে।
- `app/build.gradle.kts` actual app configuration ধরে।
- `AndroidManifest.xml` Android system-কে app entry point জানায়।
- `MainActivity` app-এর first Activity।
- `setContent` Compose UI attach করে।
- `@Composable` Kotlin function UI draw করে।
- `Color.kt` brand colors centralize করে।
- `Theme.kt` Material theme define করে।
- Kotlin app হলেও `src/main/java` folder থাকা normal।
- Monorepo-style structure future backend/docs/firebase work-এর জন্য useful।

## পরের step

Step 2-এ আমরা Splash screen, simple navigation এবং Home tabs যোগ করেছি।

তার learning note:

```text
learning/02-splash-navigation-tabs.md
```
