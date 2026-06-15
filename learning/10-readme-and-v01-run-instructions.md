# Step 10: README and v0.1 Run Instructions

এই ধাপে project README files update করা হয়েছে, যাতে app কী অবস্থায় আছে এবং কীভাবে build/run করতে হবে সেটা পরিষ্কার থাকে।

## কেন README update দরকার

Project শুধু code দিয়ে বোঝা যায় না। README হলো project-এর front door।

README থেকে জানা উচিত:

- project কী
- current phase কী
- কোথা থেকে Android app open করতে হবে
- কীভাবে build করতে হবে
- current limitations কী
- next phase কী

## কোন files update হয়েছে

```text
README.md
apps/ContactMe/README.md
CHANGELOG.md
```

## Root `README.md`

Root README পুরো repository explain করে।

এখানে যোগ করা হয়েছে:

- ContactMe কী
- current phase: `v0.1 UI Demo`
- demo flow
- Android app location
- build command
- tech stack
- API levels
- important docs links
- current limitations

কেন root README দরকার:

Root project-এ শুধু Android app নেই। এখানে future backend, firebase, docs, design, scripts, tests সব থাকবে। তাই root README পুরো project context দেয়।

## `apps/ContactMe/README.md`

এই README শুধু Android app-এর জন্য।

এখানে আছে:

- Android Studio-তে কোন folder open করতে হবে
- terminal থেকে কীভাবে build করতে হবে
- app source structure
- implemented placeholder flow
- next phase

কেন আলাদা Android README দরকার:

যে developer শুধু Android app নিয়ে কাজ করবে, তাকে root docs ঘাঁটতে হবে না। সে সরাসরি Android app folder-এর README দেখে কাজ শুরু করতে পারবে।

## `CHANGELOG.md`

Changelog-এ current unreleased work summarize করা হয়েছে।

এখানে আছে:

- কী added হয়েছে
- কী changed হয়েছে
- known limitations

কেন দরকার:

Release history maintain করতে changelog helpful। v0.1 release বানালে এখান থেকে release notes তৈরি করা সহজ হবে।

## Build verify

README-তে দেওয়া command:

```powershell
cd apps\ContactMe
.\gradlew.bat assembleDebug
```

Expected:

```text
BUILD SUCCESSFUL
```

## এই ধাপে কী শেখা হলো

- README project-এর entry document।
- Root README আর app-specific README আলাদা purpose serve করে।
- Changelog future release tracking সহজ করে।
- Current limitations লিখে রাখা honest এবং professional।

## পরের step

এখন v0.1 UI Demo documentation almost ready।

Next best implementation phase:

```text
v0.2 Auth Build foundation
```

তার আগে Git clean করে commit/push করা উচিত।
