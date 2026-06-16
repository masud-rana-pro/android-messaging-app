# Step 41 - Contact Relationship Privacy Foundation

এই ধাপে আমরা ContactMe app-এ প্রথম contact relationship model যোগ করেছি। এর আগে privacy settings-এ `Contacts` option ছিল, কিন্তু app জানত না কে কার contact। তাই `Contacts` আর `Everyone` একইভাবে behave করছিল। এখন থেকে `Contacts` আলাদা meaning পাবে।

## মূল ধারণা

WhatsApp-এর মতো privacy ভাবলে, কোনো user যখন বলে:

```text
Profile photo: Contacts
Last seen: Contacts
```

এর মানে হলো: “আমার contact list-এ যারা আছে, শুধু তারা দেখতে পারবে।”

তাই User B-এর data User A দেখতে পারবে কি না, সেটা check করার জন্য app এই document খুঁজবে:

```text
contacts/B/items/A
```

যদি document থাকে, তাহলে User A হলো User B-এর saved contact। না থাকলে User A contact না।

## Firestore structure

নতুন collection path:

```text
contacts/{ownerUid}/items/{contactUid}
```

Example:

```text
contacts/userA/items/userB
```

এর মানে User A তার contact list-এ User B-কে save করেছে।

Document fields:

```text
userId
displayName
username
phoneNumber
photoUrl
updatedAt
```

এখানে `ownerUid` path থেকে বোঝা যায় contact list কার। আর `contactUid` document id থেকে বোঝা যায় কাকে save করা হয়েছে।

## ContactRepository কেন বানানো হলো?

App বড় হলে contacts feature শুধু এক জায়গায় থাকবে না। পরে লাগবে:

- contact save
- contact list
- contact delete
- contact search
- privacy check
- native phonebook sync

তাই শুরুতেই repository interface বানানো হয়েছে:

```kotlin
interface ContactRepository {
    suspend fun saveContact(
        ownerUserId: String,
        contact: UserProfile
    ): ContactResult
}
```

এতে UI/ViewModel সরাসরি Firestore জানে না। ViewModel শুধু বলে: “এই user-কে contact হিসেবে save করো।”

## FirebaseContactRepository কী করে?

`FirebaseContactRepository` Firestore-এ contact document save করে:

```kotlin
firestore.collection("contacts")
    .document(ownerUserId)
    .collection("items")
    .document(contact.userId)
    .set(contactData, SetOptions.merge())
```

এখানে `SetOptions.merge()` use করা হয়েছে যাতে future-এ নতুন field যোগ করলে পুরনো data পুরো replace না হয়।

## কখন contact save হয়?

এই ধাপে simple foundation হিসেবে discovery থেকে direct chat open করলে peer user current user's contacts list-এ save হয়।

Flow:

```text
Search user
    |
Open direct chat
    |
Conversation create/reuse
    |
Peer saved as contact
```

Code জায়গা:

```text
apps/ContactMe/app/src/main/java/com/contactme/app/ui/conversation/ConversationViewModel.kt
```

এই জায়গায় conversation open success হলে:

```kotlin
contactRepository.saveContact(
    ownerUserId = currentUserId,
    contact = otherUser
)
```

## Privacy check কীভাবে বদলেছে?

আগে `Contacts` setting visible হিসেবেই ধরা হচ্ছিল।

এখন:

```text
Everyone -> visible
Contacts -> contact document থাকলে visible
Nobody -> hidden
```

Profile photo-এর ক্ষেত্রে:

```kotlin
PrivacyVisibility.Contacts -> {
    if (isContact(ownerUserId = id, viewerUserId = viewerUserId)) {
        getString("photoUrl").orEmpty()
    } else {
        ""
    }
}
```

যদি visible না হয়, empty string return হয়। UI তখন initials fallback দেখায়।

Last seen-এর ক্ষেত্রেও same rule:

```kotlin
PrivacyVisibility.Contacts -> isContact(
    ownerUserId = peerUserId,
    viewerUserId = currentUserId
)
```

## Firestore rules কেন update করা হলো?

Client যদি `contacts/B/items/A` read করতে না পারে, তাহলে User A বুঝতেই পারবে না সে User B-এর contact কিনা। তাই rule দেওয়া হয়েছে:

```text
owner নিজে read করতে পারবে
অথবা contactUserId যদি current auth user হয়, সে ওই item read করতে পারবে
```

এর ফলে User A শুধু নিজের item check করতে পারবে:

```text
contacts/B/items/A
```

কিন্তু User B-এর পুরো contact list দেখতে পারবে না।

## Important limitation

এই ধাপে full contacts screen বানানো হয়নি। তাই contact save হচ্ছে chat open করার সময়। পরে dedicated Contacts screen করলে user নিজে contact manage করতে পারবে।

আরেকটা বিষয়: User A যদি User B-কে save করে, তাহলে তৈরি হবে:

```text
contacts/A/items/B
```

কিন্তু User B-এর `Contacts` privacy User A-কে allow করতে হলে দরকার:

```text
contacts/B/items/A
```

মানে owner যার data protect করছে, তার list-এ viewer থাকতে হবে।

## কীভাবে verify করবে?

1. App build করো।
2. Firestore rules deploy করো।
3. User A দিয়ে User B search করে chat open করো।
4. Firebase Console-এ দেখো `contacts/A/items/B` তৈরি হয়েছে কি না।
5. User B যদি User A-কেও contact হিসেবে save করে, তাহলে `contacts/B/items/A` তৈরি হবে।
6. User B profile photo বা last seen `Contacts` করলে User A দেখতে পারবে শুধু step 5 complete হলে।
7. User B `Nobody` করলে User A দেখতে পারবে না।

## এই ধাপ থেকে শেখার বিষয়

- Privacy settings-এর জন্য relationship data দরকার।
- `Contacts` privacy owner-side contact list দিয়ে check করা উচিত।
- Repository pattern app-কে clean রাখে।
- Firestore rules এমনভাবে লিখতে হয় যাতে privacy check করা যায়, কিন্তু full contact list leak না হয়।

## পরের কাজ

পরের logical step হলো dedicated contacts screen বা saved contacts list বানানো, যাতে user শুধু chat open করে নয়, app-এর ভিতর থেকেই contacts manage করতে পারে।
