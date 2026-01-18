# React Native (Android) Release Plan

Checklist for a good customer experience:

- RN package installs from npm without local Gradle module assumptions.
- Android build pulls `com.testernest:testernest-android` from Maven Central.
- `react-native.config.js` supports autolinking for Android.
- JS API surface matches docs: `init`, `track`, `flush`, `setCurrentScreen`, `connectTester`, `disconnectTester`, `getDebugSnapshot`, `isConnected`, `TesternestConnectPrompt`.
- Example app builds via `react-native-testernest/example` using Maven Central.
- Docs include install, init usage, connect prompt usage, verify (logcat), and troubleshooting.
- Version bumped and publish steps are documented.
