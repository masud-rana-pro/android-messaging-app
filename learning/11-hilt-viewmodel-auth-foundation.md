# Step 11: Hilt, ViewModel, Auth Foundation

এই ধাপে `v0.2 Auth Build` শুরু করার foundation তৈরি করা হয়েছে।

## কেন এই step

আগে Auth screen শুধু local `remember` state ব্যবহার করছিল। সেটা UI demo-এর জন্য ঠিক ছিল, কিন্তু real app-এর জন্য যথেষ্ট না।

Real Auth লাগলে দরকার:

- UI state আলাদা রাখা
- business logic screen থেকে সরানো
- repository layer
- dependency injection
- ViewModel
- later Firebase Auth replace করার clean জায়গা

## কী কী যোগ হয়েছে

```text
ContactMeApplication.kt
auth/AuthRepository.kt
auth/AuthResult.kt
auth/FakeAuthRepository.kt
di/AuthModule.kt
ui/auth/AuthUiState.kt
ui/auth/AuthViewModel.kt
```

Update হয়েছে:

```text
MainActivity.kt
AndroidManifest.xml
AuthScreen.kt
build.gradle.kts
```

## Hilt কী

Hilt হলো dependency injection library।

সহজ ভাষায়:

```text
Class নিজে dependency বানাবে না।
Hilt dependency তৈরি করে class-এ inject করে দেবে।
```

কেন দরকার:

- code loosely coupled থাকে
- fake implementation replace করা সহজ
- Firebase implementation later plug করা যাবে
- ViewModel testable হয়

## `ContactMeApplication.kt`

```kotlin
@HiltAndroidApp
class ContactMeApplication : Application()
```

এটা Hilt setup-এর root।

`@HiltAndroidApp` না দিলে Hilt dependency graph তৈরি হবে না।

Manifest-এ যোগ করা হয়েছে:

```xml
android:name=".ContactMeApplication"
```

এর মাধ্যমে Android app launch হলে custom Application class ব্যবহার করে।

## `MainActivity.kt`

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity()
```

`@AndroidEntryPoint` Hilt-কে বলে এই Activity dependency injection support করবে।

কেন দরকার:

`AuthScreen`-এ `hiltViewModel()` ব্যবহার করতে Activity/host Hilt-aware হতে হয়।

## `AuthRepository`

```kotlin
interface AuthRepository {
    suspend fun submitAuth(
        emailOrPhone: String,
        password: String
    ): AuthResult
}
```

এটা auth data layer-এর contract।

কেন interface:

- UI/ViewModel জানে না auth fake নাকি Firebase
- later `FirebaseAuthRepository` বসানো সহজ
- testing সহজ

## `AuthResult`

```kotlin
sealed interface AuthResult {
    data object Success : AuthResult
    data class Error(val message: String) : AuthResult
}
```

Auth operation success নাকি error সেটা type-safe ভাবে প্রকাশ করে।

কেন sealed interface:

- possible result limited থাকে
- `when` expression exhaustive হতে পারে
- string/null দিয়ে result handle করার চেয়ে safer

## `FakeAuthRepository`

```kotlin
class FakeAuthRepository @Inject constructor() : AuthRepository
```

এটা temporary fake implementation।

Logic:

- email/phone blank হলে error
- password ৬ character-এর কম হলে error
- otherwise success

কেন fake:

- Firebase ছাড়াই ViewModel/UI flow test করা যায়
- loading/error/success state verify করা যায়
- architecture আগে clean হয়

## `AuthModule`

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        fakeAuthRepository: FakeAuthRepository
    ): AuthRepository
}
```

এই module Hilt-কে বলে:

```text
যখন AuthRepository চাইবে, FakeAuthRepository দাও।
```

পরে Firebase এলে শুধু binding বদলানো যাবে।

## `AuthUiState`

```kotlin
data class AuthUiState(
    val authMode: AuthMode = AuthMode.Login,
    val emailOrPhone: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

এই class Auth screen-এর full UI state ধরে।

কেন দরকার:

- screen state এক object-এ থাকে
- loading/error state clean হয়
- Compose screen simple হয়

## `AuthViewModel`

ViewModel UI logic ধরে।

Important state:

```kotlin
private val _uiState = MutableStateFlow(AuthUiState())
val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
```

ব্যাখ্যা:

- `_uiState` private mutable state
- `uiState` public read-only state
- UI observe করবে, কিন্তু direct mutate করতে পারবে না

Input update:

```kotlin
fun onEmailOrPhoneChanged(value: String)
fun onPasswordChanged(value: String)
fun onAuthModeChanged(authMode: AuthMode)
```

Submit:

```kotlin
fun submit(onSuccess: () -> Unit)
```

এখানে:

- loading true হয়
- repository call হয়
- success হলে profile setup screen-এ যায়
- error হলে message দেখায়

## `AuthScreen.kt`

আগে:

```kotlin
var emailOrPhone by remember { mutableStateOf("") }
```

এখন:

```kotlin
val uiState by viewModel.uiState.collectAsState()
```

মানে UI এখন ViewModel state observe করে।

`AuthContent` আলাদা করা হয়েছে:

```kotlin
AuthContent(
    uiState = uiState,
    onEmailOrPhoneChanged = viewModel::onEmailOrPhoneChanged,
    onPasswordChanged = viewModel::onPasswordChanged,
    onAuthModeChanged = viewModel::onAuthModeChanged,
    onSubmit = { viewModel.submit(onSuccess = onAuthSuccess) }
)
```

কেন ভালো:

- `AuthScreen` ViewModel connect করে
- `AuthContent` pure UI
- later preview/test সহজ

## কীভাবে verify করবে

Build:

```powershell
cd apps\ContactMe
.\gradlew.bat assembleDebug
```

Expected:

```text
BUILD SUCCESSFUL
```

Manual app check:

1. Auth screen-এ blank submit করো।
2. Error দেখাবে: `Email or phone is required.`
3. Email/phone লিখে short password দাও।
4. Error দেখাবে: `Password must be at least 6 characters.`
5. Valid input দিলে Profile setup screen-এ যাবে।

## এই ধাপে কী শেখা হলো

- Hilt dependency injection setup করে।
- ViewModel UI logic রাখে।
- Repository data/auth operation abstract করে।
- Fake repository দিয়ে backend ছাড়াই flow test করা যায়।
- StateFlow Compose UI state observe করতে পারে।
- Composable যত pure রাখা যায়, maintainability তত ভালো।

## পরের step

Next step:

```text
Firebase Auth setup plan/config
```

তারপর:

- Firebase dependencies
- FirebaseAuthRepository
- real login/register
- session restore
