# Step 38: Last Seen Privacy Enforcement

এই step-এ আমরা saved privacy settings-এর প্রথম real enforcement করেছি।

আগের step-এ user Settings থেকে Last seen visibility save করতে পারত। কিন্তু app behavior-এ সেটা apply হচ্ছিল না। এখন peer যদি Last seen `Nobody` করে, তাহলে chat header exact last seen time দেখাবে না।

## 1. কোন privacy setting enforce হলো?

Field:

```text
users/{uid}.lastSeenVisibility
```

Value:

```text
everyone
contacts
nobody
```

এই step-এ `nobody` enforce করা হয়েছে।

`contacts` আপাতত visible হিসেবে treat করা হয়েছে, কারণ contacts model/native contact sync এখনও implement হয়নি।

## 2. `PresenceStatus` update

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/presence/PresenceStatus.kt`

নতুন field:

```kotlin
val canShowLastSeen: Boolean = true
```

এর মানে UI exact last seen time দেখাবে কি না।

## 3. Peer privacy read করা

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/presence/FirebasePresenceRepository.kt`

Peer user id পাওয়ার পর repository Firestore থেকে peer profile পড়ে:

```kotlin
users/{peerUid}.lastSeenVisibility
```

তারপর:

```kotlin
PrivacyVisibility.fromFirestore(visibility) != PrivacyVisibility.Nobody
```

যদি `Nobody` হয়, তাহলে `canShowLastSeen = false`।

## 4. UI enforcement

File:

`apps/ContactMe/app/src/main/java/com/contactme/app/ui/screens/ChatDetailScreen.kt`

আগে:

```kotlin
peerPresence.lastSeenAtMillis > 0L -> "last seen ..."
```

এখন:

```kotlin
peerPresence.lastSeenAtMillis > 0L && peerPresence.canShowLastSeen
```

মানে exact time দেখানোর আগে permission check হচ্ছে।

## 5. Hidden হলে কী দেখাবে?

যদি last seen hidden হয়:

```text
last seen recently
```

এটা exact time leak করে না।

## 6. Typing/online priority বদলায়নি

Header priority এখনও:

1. demo chat
2. typing
3. online
4. visible last seen
5. fallback

Typing এবং online behavior এই step-এ বদলানো হয়নি।

## 7. কেন শুধু `Nobody` enforce?

`Contacts` enforce করতে হলে app-কে জানতে হবে:

- কে আমার contact
- native contact sync আছে কি না
- server-side contacts collection কীভাবে maintained হবে

এইগুলো এখনো implement হয়নি। তাই safe MVP enforcement:

```text
Nobody = hide exact last seen
Everyone/Contacts = visible for now
```

## 8. কীভাবে verify করবে?

Build:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.11'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat assembleDebug
```

Manual test:

1. User B Settings খুলে Last seen `Nobody` করো।
2. User B app background করো।
3. User A chat খুলে User B-এর header দেখো।
4. exact time না দেখিয়ে `last seen recently` দেখার কথা।
5. User B Last seen `Everyone` করলে exact time দেখা যেতে পারে।

## 9. Main learning

Privacy feature শুধু setting save করা না। Real privacy হলো UI/data behavior-এ সেই setting apply করা।

এই step-এ আমরা শিখলাম:

- peer profile privacy পড়া
- presence status-এর সাথে privacy permission combine করা
- UI-তে exact sensitive time conditionally দেখানো
