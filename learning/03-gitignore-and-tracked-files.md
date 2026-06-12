# Step 3: Gitignore and Tracked Files

এই note-এ ContactMe project-এর `.gitignore` policy ব্যাখ্যা করা হলো।

## Goal

আমরা GitHub-এ রাখতে চাই:

- source code
- `docs/`
- `learning/`
- Firebase rules
- scripts
- test plan
- Gradle wrapper files

আমরা GitHub-এ রাখতে চাই না:

- build output
- cache
- local machine settings
- IDE temporary files
- APK/AAB generated file
- secret/signing/config file

## কেন `.gitignore` দরকার

Android Studio/Gradle build করলে অনেক generated file তৈরি হয়। এগুলো source code না।

Example:

```text
build/
.gradle/
.kotlin/
local.properties
.idea/
*.apk
```

এগুলো GitHub-এ রাখলে সমস্যা:

- repository unnecessary বড় হয়
- অন্য machine-এ conflict হতে পারে
- local SDK path leak হতে পারে
- generated files বারবার change দেখায়
- secret/config accidentally commit হতে পারে

## Docs এবং learning কেন ignore করা হয়নি

এই project-এ `docs/` এবং `learning/` important source-of-knowledge।

তাই এগুলো GitHub-এ থাকবে:

```text
docs/
learning/
```

কারণ:

- project roadmap preserve হবে
- তুমি কী শিখেছো তা history-তে থাকবে
- future review সহজ হবে
- portfolio হিসেবে project professional দেখাবে

## Current `.gitignore` policy

### Android / Gradle generated files

```gitignore
.gradle/
.kotlin/
build/
**/build/
```

ব্যাখ্যা:

- `.gradle/` Gradle cache
- `.kotlin/` Kotlin compiler/cache/error generated files
- `build/` build output
- `**/build/` nested module build folders

### Local machine settings

```gitignore
local.properties
**/local.properties
```

`local.properties` file-এ সাধারণত local Android SDK path থাকে। এটা machine-specific, তাই GitHub-এ রাখা উচিত না।

### IDE files

```gitignore
.idea/
**/.idea/
*.iml
```

Android Studio/IntelliJ local settings। Team project-এ সাধারণত এগুলো ignore করা হয়।

### Build artifacts

```gitignore
*.apk
*.ap_
*.aab
*.dex
```

এগুলো generated app output। Release asset হিসেবে GitHub Release-এ upload করা যেতে পারে, কিন্তু source Git commit-এ রাখা উচিত না।

### Logs

```gitignore
*.log
hs_err_pid*
replay_pid*
```

Build/runtime error log generated হয়। এগুলো source code না।

### Secrets

```gitignore
.env
.env.*
*.keystore
*.jks
*.p12
*.pem
google-services.json
GoogleService-Info.plist
```

ব্যাখ্যা:

- `.env` secret config রাখতে পারে
- keystore/signing file app signing-এর জন্য sensitive
- Firebase `google-services.json` environment-specific config

Production project-এ secret/config manage করার জন্য আলাদা secure process দরকার।

## Important: `.gitignore` already tracked file সরায় না

যদি কোনো generated file আগে থেকেই Git tracking-এ ঢুকে যায়, পরে `.gitignore` add করলেও Git সেটা track করতে থাকবে।

তখন এই command লাগে:

```powershell
git rm --cached path\to\file
```

এটা local file delete করে না, শুধু Git tracking থেকে বের করে।

এই project-এ একটি generated file already tracked আছে:

```text
apps/ContactMe/.kotlin/errors/errors-1781235481682.log
```

এটাকে untrack করতে:

```powershell
git rm --cached apps/ContactMe/.kotlin/errors/errors-1781235481682.log
```

তারপর commit করলে future-এ `.gitignore` অনুযায়ী `.kotlin/` ignore থাকবে।

## কীভাবে verify করবে

### Status with ignored files

```powershell
git status --ignored
```

Expected:

- `learning/` এবং `docs/` ignored list-এ থাকবে না।
- `.gradle/`, `build/`, `.idea/`, `local.properties` ignored list-এ থাকবে।

### Tracked generated files check

```powershell
git ls-files | Select-String -Pattern "(^|/)build/|(^|/)\.gradle/|(^|/)\.idea/|local.properties|(^|/)\.kotlin/|\.apk$|\.aab$|errors-.*\.log"
```

Expected:

- কোনো output থাকবে না।

যদি output আসে, সেই generated file Git tracking থেকে remove করতে হবে:

```powershell
git rm --cached <file-path>
```

## এই ধাপে কী শেখা হলো

- `.gitignore` future untracked files ignore করে।
- Already tracked file ignore করতে হলে আগে `git rm --cached` করতে হয়।
- Docs/learning project knowledge, তাই GitHub-এ রাখা উচিত।
- Build/cache/local/secret files GitHub-এ রাখা উচিত না।
