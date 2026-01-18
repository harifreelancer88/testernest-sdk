# React Native (Android)

Android-only for now. iOS support is planned.

## Install

```bash
npm i @testernest/react-native
```

```bash
yarn add @testernest/react-native
```

## Android setup

Ensure your Android repositories include `google()` and `mavenCentral()`.

## Usage

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
/>;
```

Render `<TesternestConnectPrompt />` once at the root of the app.

## Verify (logcat)

Windows CMD:

```bat
adb logcat -v time | findstr /i "Testernest: BOOTSTRAP Testernest: BATCH Testernest: CLAIM"
```

## Troubleshooting

- Prompt not showing: already connected or dismissed. Clear app data and relaunch.
- Invalid code: code expired or wrong app/publicKey. Generate a new 6-digit code.
- 401 invalid token: token expired/invalid. Re-init and verify `publicKey` + `baseUrl`.
