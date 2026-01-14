# TesterNest Android Native SDK

Production-ready Android Native SDK (Kotlin) for Testernest/MyAppCrew mobile tracking. This mirrors the Flutter SDK behavior and endpoints.

## Requirements
- minSdk 21
- INTERNET is a normal permission; no runtime prompt is shown to users

## Native Android (Kotlin) Quickstart
Get a tester connected in under 5 minutes.

1) Add Maven Central and the dependency.
Add the dependency in your module-level (app) build.gradle(.kts) dependencies block.

```kotlin
repositories {
    google()
    mavenCentral()
}
```

Where to add repositories (recommended)

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

```groovy
// settings.gradle
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

If your project uses older Gradle setup, add mavenCentral() alongside google() in the project-level build.gradle repositories block.

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.testernest:testernest-android:0.1.1")
}
```

```groovy
// app/build.gradle
dependencies {
    implementation "com.testernest:testernest-android:0.1.1"
}
```

Replace 0.1.1 with the latest version from Maven Central.

2) Initialize in your `Application`:

```kotlin
import android.app.Application
import com.testernest.android.Testernest

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Testernest.init(
            context = this,
            baseUrl = "https://myappcrew-tw.pages.dev",
            publicKey = "YOUR_PUBLIC_KEY",
            enableLogs = false
        )
    }
}
```

Register this in AndroidManifest.xml: `android:name=".App"`

```xml
<application
    android:name=".App"
    ... >
    ...
</application>
```

3) Auto-attach the connect prompt in your Activity on resume:

```kotlin
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onResume() {
        super.onResume()
        Testernest.attachAutoConnectPrompt(this)
    }
}
```

No additional Proguard/R8 rules required (default).

What the tester sees:
- On first open, a 6-digit connect prompt appears.
- After connecting once, the prompt never shows again.

### Verify (logcat)

Expected: BOOTSTRAP 200 -> BATCH 200 -> (after entering code) CLAIM 200

Sanity check:
- First open: BOOTSTRAP 200 then BATCH 200
- After entering 6-digit code: CLAIM 200
- If you never see CLAIM: you are not connected yet (or already connected and prompt won't show)

Windows CMD (findstr):

```bat
adb logcat -v time | findstr /i "Testernest: BOOTSTRAP Testernest: BATCH Testernest: CLAIM"
```

macOS/Linux (grep -E):

```bash
adb logcat -v time | grep -E "Testernest: (BOOTSTRAP|BATCH|CLAIM)"
```

## React Native (Android) Quickstart
Android-only for now. iOS support is planned later.

1) Install the package.

```bash
npm i @testernest/react-native
```

```bash
yarn add @testernest/react-native
```

2) Ensure Android repositories include google() + mavenCentral().

If the npm package isn't published yet, use the local path install below.
If the npm package name differs or you're developing locally, install from a local path.

```bash
yarn add file:../react-native-testernest
```

```bash
npm i ../react-native-testernest
```

3) Initialize and render the connect prompt.

```tsx
import { init, TesternestConnectPrompt } from '@testernest/react-native';

init({
  publicKey: 'YOUR_PUBLIC_KEY',
  baseUrl: 'https://myappcrew-tw.pages.dev',
  enableLogs: false,
});

<TesternestConnectPrompt
  publicKey="YOUR_PUBLIC_KEY"
  baseUrl="https://myappcrew-tw.pages.dev"
/>
```

Render `<TesternestConnectPrompt />` once at the root of the app (above navigation) so it can show on first launch.

## Troubleshooting

| Issue | Likely cause | Fix |
| --- | --- | --- |
| Prompt not showing | Already connected or prompt dismissed | Clear app data (`adb shell pm clear <package>`) and relaunch. |
| Invalid code | Code expired or wrong app/publicKey | Generate a fresh 6-digit code for the same app/publicKey in the dashboard. |
| 401 invalid token | Token expired/invalid | SDK auto-retries bootstrap; if it persists, re-init and verify publicKey/baseUrl. |

## Behavior notes
- Session id is generated per app launch.
- Events are queued in memory and flushed automatically.
- Flush triggers: queue size >= 10, every 12 seconds, app background, manual flush.
- Batch size max: 50 events.
- Retry strategy: auth recovery once on 401/403, then retry up to 2 times with 500ms/1000ms delays.
- Connection supports only 6-digit numeric connect codes (including `connectFromText`).
- Auto connect prompt shows once on first open when not connected.

## Manual test checklist
- Init with valid public key and base URL; verify `app_open` is sent.
- Log 10 events and confirm automatic flush.
- Background and foreground the app; verify `app_background` and `app_foreground` events.
- Trigger `connectTester` with a 6-digit code; confirm new tester id is stored.
- Call `flushNow()` and confirm queued events are sent.
- Call `getDebugSnapshot()` and verify baseUrl/publicKey presence and queue length.

## Maintainers: Publishing
- Add signing + Central Portal credentials in `%USERPROFILE%\.gradle\gradle.properties`.
- Run `.\gradlew clean publishAggregationToCentralPortal`.
- In Central Portal, publish the uploaded deployment.

## Changelog

### 0.1.1
- Docs: clarified Android + React Native quickstarts, added troubleshooting table, and iOS status note.
- Hygiene: ensure release metadata and version bump.
