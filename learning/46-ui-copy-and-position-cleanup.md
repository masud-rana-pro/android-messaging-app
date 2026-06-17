# Step 46 - UI Copy and Position Cleanup

এই ধাপে আমরা app-এর visible UI text এবং input design clean করেছি।

তুমি বলেছিলে app-এর কোথাও unnecessary কথা/word রাখা যাবে না। তাই active screen-গুলোতে long explanatory বা demo-feel text কমানো হয়েছে।

## কোন screen-গুলো clean করা হয়েছে?

- Auth screen
- Profile setup/edit screen
- Chats screen
- Chat detail screen
- Settings screen

## Auth screen

আগে text একটু বেশি explanatory ছিল:

```text
Enter your mobile number to receive a verification code.
Email and password are available as a fallback login method.
```

এখন concise:

```text
Enter your phone number.
Use your email and password.
```

Input field rounded করা হয়েছে:

```kotlin
shape = RoundedCornerShape(18.dp)
```

## Profile screen

আগের helper text:

```text
Add the name people will see in ContactMe.
```

এখন:

```text
Your public profile
```

Display name এবং username field rounded করা হয়েছে।

## Chats screen

Header subtitle now:

```text
Search or open a chat
```

Search placeholder:

```text
Username or phone
```

Search field rounded করা হয়েছে:

```kotlin
shape = RoundedCornerShape(22.dp)
```

Saved contacts empty text এখন:

```text
No saved contacts
```

## Future tabs

আগে Status/Calls/Communities/Channels tab-এ লম্বা placeholder ছিল:

```text
... will appear here
```

এখন সব জায়গায় standard concise state:

```text
Coming soon
```

## Settings screen

যেসব setting এখনো implement হয়নি সেগুলো active screen থেকে বাদ দেওয়া হয়েছে:

- Notifications
- Storage
- Blocked users

কারণ এগুলো এখন দেখালে user ভাববে feature ready, কিন্তু বাস্তবে এখনো কাজ করে না।

Profile header rounded surface করা হয়েছে যাতে screen বেশি polished লাগে।

## Chat detail input

Message input rounded করা হয়েছে:

```kotlin
shape = RoundedCornerShape(22.dp)
```

যদি chat selected না থাকে:

```text
Select a chat
```

## কীভাবে verify করবে?

1. App build করো।
2. Auth screen দেখো input rounded কিনা।
3. Profile screen দেখো text concise কিনা।
4. Chats screen search box rounded এবং ঠিক position-এ আছে কিনা।
5. Settings screen-এ শুধু implemented settings আছে কিনা।
6. Chat detail screen input bar clean কিনা।

## শেখার বিষয়

- App screen-এ text ছোট, clear, action-focused হওয়া উচিত।
- Unimplemented feature active settings-এ দেখানো ঠিক না।
- Rounded input/row consistent হলে app বেশি professional লাগে।
- UI cleanup করতে গিয়ে data behavior বদলানো উচিত না।
