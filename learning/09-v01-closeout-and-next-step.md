# Step 9: v0.1 UI Demo Closeout

এই ধাপে কোনো নতুন Android screen code যোগ করা হয়নি। বরং v0.1 UI Demo কোথায় দাঁড়িয়ে আছে, কীভাবে verify করতে হবে, এবং পরের phase কী হবে সেটা documentation-এ পরিষ্কার করা হয়েছে।

## কেন এই step দরকার

কোনো phase শেষ করার আগে শুধু code লিখলেই হয় না। আমাদের জানতে হবে:

- কী কী screen তৈরি হয়েছে
- কী কী এখনো placeholder
- কীভাবে build verify করব
- কীভাবে manually app flow check করব
- next phase শুরু করার আগে কী gap আছে

এই কারণেই docs update করা হয়েছে।

## কোন docs update হয়েছে

```text
docs/02-feature-specification.md
docs/08-release-checklist.md
docs/10-v01-ui-demo-verification.md
```

## `docs/02-feature-specification.md`

এখানে v0.1 UI Demo scope যোগ করা হয়েছে।

এখন document-এ আছে:

- implemented placeholder screens
- current demo flow
- not implemented yet list

কেন দরকার:

যাতে আমরা ভুল করে v0.1 placeholder-কে real feature ভাবি না।

Example:

```text
Auth screen আছে, কিন্তু Firebase Auth এখনো নেই।
Chat detail screen আছে, কিন্তু real message send এখনো নেই।
```

## `docs/08-release-checklist.md`

এখানে v0.1 checklist যোগ করা হয়েছে।

Done:

- Android scaffold
- theme
- splash
- auth placeholder
- profile setup placeholder
- home tabs
- chat list
- chat detail
- settings
- debug build

Pending manual:

- emulator/device run check
- screenshots
- README run instructions

## `docs/10-v01-ui-demo-verification.md`

এটা নতুন verification document।

এখানে আছে:

- build command
- expected build output
- manual app flow
- screen-by-screen check
- known limitations

## এখন আমরা কোথায়

আমরা `v0.1 UI Demo` phase-এর শেষ দিকে।

Code-wise:

```text
v0.1 mostly complete
```

Documentation-wise:

```text
verification/checklist added
```

Manual work বাকি:

- Android Studio/emulator-এ run করে flow check
- screenshots নেওয়া
- README update

## পরের recommended কাজ

Next best step:

```text
README update with v0.1 run instructions
```

তারপর:

```text
v0.2 Auth Build শুরু
```

v0.2 শুরু করার আগে ideally:

- Hilt dependency
- ViewModel structure
- Firebase project decision
- `google-services.json` secret/config policy

## শেখা বিষয়

- phase closeout documentation important
- checklist ছাড়া project progress vague থাকে
- placeholder আর real feature আলাদা করে document করা দরকার
- verification document থাকলে future bug/debug সহজ হয়
