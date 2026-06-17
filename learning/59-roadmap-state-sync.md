# Step 59 - Roadmap State Sync

এই ধাপে আমরা roadmap/docs current code state অনুযায়ী sync করেছি।

## কেন দরকার?

Project দ্রুত এগোচ্ছে। যদি docs update না করি, তাহলে roadmap পুরনো কথা বলবে:

```text
Profile photo not started
Media messages not started
Notifications not started
Typing/presence not started
```

কিন্তু codebase-এ এগুলোর foundation ইতিমধ্যে করা হয়েছে। তাই docs ভুল থাকলে পরের step plan ভুল হবে।

## কী update করা হলো?

### 1. Feature specification

`docs/02-feature-specification.md`

এখানে implemented foundation list update করা হয়েছে:

- profile photo
- image messages
- retry/preview/validation/compression
- typing/presence
- FCM token sync
- notification channels
- runtime permission
- foreground notification renderer

### 2. Notification flow

`docs/06-notification-flow.md`

এখানে current আর planned আলাদা করা হয়েছে।

Current:

```text
FCM dependency
device token sync
notification channels
runtime permission
foreground renderer
```

Planned:

```text
Cloud Functions fanout
deep links
call notification actions
```

### 3. Release checklist

`docs/08-release-checklist.md`

যেগুলো already done, সেগুলো checked করা হয়েছে।

### 4. Current state roadmap

`docs/28-current-state-and-next-roadmap.md`

এখন roadmap বলছে notification partial, not started না।

## শেখার বিষয়

Implementation যত গুরুত্বপূর্ণ, roadmap sync-ও তত গুরুত্বপূর্ণ। কারণ বড় project-এ docs হলো navigation map। Map পুরনো থাকলে developer ভুল রাস্তায় যাবে।
