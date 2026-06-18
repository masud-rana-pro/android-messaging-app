# ধাপ ৭৪: Group creation screen

## User flow

Chats screen-এর **New group** action dedicated screen খোলে। User group name লেখেন, saved contact থেকে অন্তত দুইজন নির্বাচন করেন এবং **Create group** চাপেন। ViewModel real repository call করে Firestore document তৈরি করে; সফল হলে live conversation list-এ ফিরে আসে।

## UI state

`GroupCreationUiState` title, contacts, selected IDs, loading, creating এবং error এক জায়গায় রাখে। `Set<String>` ব্যবহারে একই contact দুইবার select হয় না এবং checkbox lookup দ্রুত হয়।

## ViewModel দায়িত্ব

- ContactRepository থেকে saved contacts observe করা
- ১০০ character-এর মধ্যে title রাখা
- contact selection toggle করা
- creator ID ও selected member IDs repository-তে পাঠানো
- repository validation error screen-এ দেখানো

Composable Firestore জানে না; এটি শুধু state render ও event forward করে।

## Navigation

নতুন `CreateGroup` app screen যোগ হয়েছে। সফল creation-এর পর Home-এ ফিরলে existing Firestore listener group-টি Recent chats-এ দেখায়। `ChatTarget` এখন conversation type রাখে, যা পরের ধাপে direct-only behavior থেকে group behavior আলাদা করতে ব্যবহৃত হবে।

## যাচাই

1. অন্তত দুইটি saved contact রাখুন।
2. Chats → New group খুলুন।
3. নাম ও দুই contact নির্বাচন করুন।
4. Create group চাপুন।
5. Home list এবং Firestore conversation document যাচাই করুন।
