# Step 13: Firebase Register Flow

এই ধাপে Auth flow-তে Login এবং Register আলাদা করা হয়েছে।

## আগে কী ছিল

আগে `AuthRepository`-তে একটাই function ছিল:

```kotlin
submitAuth(...)
```

সমস্যা:

- Login আর Register একই operation না।
- Firebase-এ login-এর জন্য `signInWithEmailAndPassword`
- Register-এর জন্য `createUserWithEmailAndPassword`
- তাই এক function রাখা clean না।

## এখন কী হয়েছে

`AuthRepository` এখন:

```kotlin
interface AuthRepository {
    suspend fun signIn(
        email: String,
        password: String
    ): AuthResult

    suspend fun register(
        email: String,
        password: String
    ): AuthResult
}
```

## `FirebaseAuthRepository`

Login:

```kotlin
firebaseAuth.signInWithEmailAndPassword(
    email.trim(),
    password
).await()
```

Register:

```kotlin
firebaseAuth.createUserWithEmailAndPassword(
    email.trim(),
    password
).await()
```

দুইটার common validation একই helper function-এ রাখা হয়েছে।

## `AuthViewModel`

ViewModel এখন `authMode` দেখে operation decide করে:

```kotlin
val result = when (state.authMode) {
    AuthMode.Login -> authRepository.signIn(...)
    AuthMode.Register -> authRepository.register(...)
}
```

কেন ভালো:

- UI button same থাকতে পারে
- mode অনুযায়ী business operation বদলায়
- Firebase logic screen-এর ভিতরে যায় না

## `AuthScreen`

Text update হয়েছে:

```text
Use email to continue with ContactMe.
```

Field label:

```text
Email
```

কারণ current Firebase implementation email/password auth। Phone/OTP later phase।

## কীভাবে verify করবে

Firebase Console-এ:

1. Authentication > Sign-in method
2. Email/Password enable করো

App-এ:

1. Register mode select করো।
2. নতুন email/password দাও।
3. Register চাপো।
4. Success হলে Profile Setup screen-এ যাবে।
5. App reinstall/clear করে Login mode-এ একই email/password দিয়ে login test করো।

## Common error

- Email badly formatted হলে Firebase error আসবে।
- Password ৬ character-এর কম হলে local validation error দেখাবে।
- Email already used হলে Firebase error দেখাবে।

## এই ধাপে কী শেখা হলো

- Login আর Register আলাদা repository function হওয়া উচিত।
- ViewModel mode অনুযায়ী operation select করে।
- Firebase call coroutine-friendly করতে `await()` ব্যবহার করা যায়।
- UI শুধু state/event handle করে, Firebase logic জানে না।

## পরের step

Next:

- session restore
- logout
- auth state অনুযায়ী Splash থেকে Home/Auth decision
