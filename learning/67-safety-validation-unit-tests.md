# ধাপ ৬৭: Safety validation unit test

## এই test কেন দরকার

Block ও report করার আগে কয়েকটি business rule সবসময় মানতে হবে: user id খালি হতে পারবে না এবং কেউ নিজেকে block বা report করতে পারবে না। এগুলো শুধু Firebase-এর ভেতরে পরীক্ষা করলে test ধীর ও network-dependent হয়। তাই pure Kotlin validator বানিয়ে local unit test করা হয়েছে।

## `SafetyInputValidator` কী করে

প্রতিটি function invalid input পেলে user-safe error message দেয়, আর input valid হলে `null` দেয়। Repository প্রথমেই এই validator চালায়। Error থাকলে Firestore call না করেই `SafetyResult.Error` ফেরত দেয়।

এই design-এর সুবিধা:

- validation এক জায়গায় থাকে;
- Firebase ছাড়া কয়েক মিলিসেকেন্ডে test চলে;
- repository method ছোট ও পড়তে সহজ হয়;
- ভবিষ্যতে rule বদলালে test সঙ্গে সঙ্গে regression ধরবে।

## Test structure

`src/test`-এর test JVM-এ চলে, emulator লাগে না। ছয়টি test যাচাই করে:

1. blank current user দিয়ে block বাতিল হয়;
2. নিজেকে block করা যায় না;
3. অন্য user-কে block করা valid;
4. blank peer দিয়ে unblock বাতিল হয়;
5. নিজেকে report করা যায় না;
6. অন্য user-কে report করা valid।

## `assertEquals` ও `assertNull`

`assertEquals` expected error message-এর সঙ্গে actual result মেলায়। `assertNull` প্রমাণ করে valid input-এ কোনো validation error নেই। কোনো assertion না মিললে Gradle test task fail করে এবং commit করার আগেই সমস্যা ধরা পড়ে।

## যাচাই command

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

`testDebugUnitTest` local unit tests চালায় এবং `assembleDebug` নিশ্চিত করে production app code-ও compile হচ্ছে।
