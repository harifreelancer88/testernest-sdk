# React Native Bridge Audit (TesterNest SDK)

## What I checked
- Repo top-level inventory (folders + files).
- Presence of React Native-related folders and files.
- Keyword scan for RN bridge APIs, modules, and Codegen.
- React Native package metadata and entry points.
- Android native module implementation and dependencies.
- Example React Native app wiring and build config.
- Documentation references for RN usage.

## What exists
- React Native package folder: `react-native-testernest/`.
- Package name/version/entry points:
  - `name`: `@testernest/react-native`
  - `version`: `0.1.0`
  - Entry points: `main`/`react-native`/`types` -> `src/index.ts`.
- JS API surface (from `react-native-testernest/src/native.ts`):
  - `init`, `track`, `flush`, `setCurrentScreen`, `connectTester`, `disconnectTester`, `getDebugSnapshot`, `isConnected`.
  - UI helper: `TesternestConnectPrompt`.
- Android native module:
  - `TesternestPackage` implements `ReactPackage`.
  - `TesternestModule` implements `ReactContextBaseJavaModule` with RN bridge methods.
  - Calls into `com.testernest.android.Testernest` and forwards `publicKey`, `baseUrl`, `enableLogs` via `init`.
  - Gradle dependency: `implementation project(':testernest-android')`.
- Example RN app:
  - `react-native-testernest/example` with Android project and `react-native run-android` script.
  - Android settings include the local RN package + `testernest-android` + `testernest-core` modules.
- Autolinking config:
  - `react-native-testernest/react-native.config.js` declares Android `sourceDir` for autolinking.
- Docs:
  - `react-native-testernest/README.md` with install/usage/API.
  - Root `README.md` includes RN quickstart.

## What does NOT exist (or appears missing)
- iOS native module (no iOS folder or bridging code in RN package).
- TurboModule/Codegen setup (no `codegenConfig`, specs, or `TurboModule` usage found).
- `claim` or `trackEvent` JS API functions (only `connectTester` + `track`).

## Gaps + recommended next step (MVP)
- If Android-only is acceptable, the bridge is already functional; the main gap is iOS support.
- MVP to add iOS support would include:
  - `react-native-testernest/ios/` native module + podspec.
  - iOS bridging module exposing the same API as Android.
  - Update `react-native-testernest/react-native.config.js` for iOS, and update docs.

## Quick inventory (top-level)
- Folders: `.git`, `.gradle`, `build`, `docs`, `gradle`, `react-native-testernest`, `sample-native`, `testernest-android`, `testernest-core`, `unity-testernest-package`, `_evidence`.
- Files: `.gitignore`, `build.gradle.kts`, `gradle.properties`, `gradlew`, `gradlew.bat`, `LICENSE`, `package-lock.json`, `README.md`, `README_Unity.md`, `settings.gradle.kts`.