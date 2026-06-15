# ContactMe Android App

This folder contains the main Android application for ContactMe.

## Current Phase

```text
v0.1 UI Demo
```

The current app is a Jetpack Compose UI prototype with dummy data and placeholder screens.

## Open In Android Studio

Open this folder:

```text
D:\my-projects\github-projects\android-projects\ContactMe\apps\ContactMe
```

## Build From Terminal

```powershell
.\gradlew.bat assembleDebug
```

Expected:

```text
BUILD SUCCESSFUL
```

## Implemented Placeholder Flow

```text
Splash
-> Auth
-> Profile Setup
-> Home
-> Chat Detail
-> Profile & Settings
```

## Main Source Structure

```text
app/src/main/java/com/contactme/app/
  MainActivity.kt
  navigation/
  ui/
    ContactMeApp.kt
    screens/
    theme/
```

## Next Phase

```text
v0.2 Auth Build
```

Planned work:

- Hilt setup
- ViewModel structure
- Firebase Auth setup
- Login/register implementation
- Session restore
- Profile persistence
