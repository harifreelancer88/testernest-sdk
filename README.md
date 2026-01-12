# TesterNest Android Native SDK

Production-ready Android Native SDK (Kotlin) for Testernest/MyAppCrew mobile tracking. This mirrors the Flutter SDK behavior and endpoints.

## Native Android (Kotlin) Quickstart
Get a tester connected in under 5 minutes.

1) Add the dependency (Maven preferred; for now use `mavenLocal()` for development. This will move to Maven Central/GitHub Packages.)

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.testernest:testernest-android:0.1.0")
}
```

2) Initialize in your `Application`:

```kotlin
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

3) Auto-attach the connect prompt in your Activity:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onResume() {
        super.onResume()
        Testernest.attachAutoConnectPrompt(this)
    }
}
```

What the tester sees:
- On first open, a 6-digit connect prompt appears.
- After connecting once, the prompt never shows again.

How to verify (logcat):

Windows CMD (findstr):

```bat
adb logcat -v time | findstr /i "Testernest: BOOTSTRAP Testernest: BATCH Testernest: CLAIM"
```

macOS/Linux (grep -E):

```bash
adb logcat -v time | grep -E "Testernest: (BOOTSTRAP|BATCH|CLAIM)"
```

Note: On first open you should see BOOTSTRAP 200, then BATCH 200, and after entering code CLAIM 200.

Troubleshooting:
- Prompt not showing: `adb shell pm clear <package>`
- "Invalid code": regenerate a new code in the dashboard and confirm the app/publicKey
- 401 invalid token: bootstrap retry is automatic

## Publishing (maintainers)

1) Add to `%USERPROFILE%\.gradle\gradle.properties` (examples only):

```properties
centralPortalUsername=...
centralPortalPassword=...
signing.gnupg.keyName=BB1578FE40BB3C12
signing.gnupg.passphrase=...
```

2) Ensure the project `gradle.properties` POM metadata entries are set to real values before publishing (POM_URL, POM_SCM_URL, POM_LICENSE_NAME, etc).

3) Publish:

```bat
.\gradlew clean publishAggregationToCentralPortal
```

4) With USER_MANAGED, after upload go to Central Portal → Deployments → Publish.

## React Native (Android)

Install + add one line and testers see the prompt automatically:

```tsx
import { TesternestConnectPrompt } from '@testernest/react-native';

<TesternestConnectPrompt
  publicKey="YOUR_PUBLIC_KEY"
  baseUrl="https://myappcrew-tw.pages.dev"
/>
```

API surface:
- `init(context, publicKey, baseUrl, enableLogs)`
- `logEvent(name, properties)`
- `flushNow()`
- `setCurrentScreen(screen)`
- `connectTester(code6)`
- `connectFromText(input, publicKeyOverride)`
- `disconnectTester()`
- `isInitialized()`
- `isTesterConnected()`
- `attachAutoConnectPrompt(activity, config?)`
- `getDebugSnapshot()`

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
