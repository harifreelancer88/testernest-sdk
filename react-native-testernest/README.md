# @testernest/react-native

React Native bridge for the Testernest Android SDK.

Only 6-digit connect is supported (no claim token).

## Install

```sh
yarn add @testernest/react-native
```

### Android setup

1) Ensure the Testernest Android SDK is available to Gradle. For this repo, the module is included locally.
2) Rebuild the app after installing the package.

If you are integrating outside this repo, add the Testernest Android SDK dependency to your app or a Maven repository that hosts it.

## Usage

```ts
import {
  init,
  track,
  flush,
  setCurrentScreen,
  connectTester,
  disconnectTester,
  getDebugSnapshot,
  TesternestConnectPrompt,
} from '@testernest/react-native';

await init({
  publicKey: 'YOUR_PUBLIC_KEY',
  baseUrl: 'https://myappcrew-tw.pages.dev',
  enableLogs: true,
});

track('signup_start', { plan: 'pro' });
setCurrentScreen('Checkout');
await connectTester('123456');
await flush();
const snapshot = await getDebugSnapshot();
await disconnectTester();
```

Auto connect prompt (1-line integration):

```tsx
<TesternestConnectPrompt
  publicKey="YOUR_PUBLIC_KEY"
  baseUrl="https://myappcrew-tw.pages.dev"
/>
```

## API

- `init({ publicKey, baseUrl?, enableLogs? }): Promise<void>`
- `track(name: string, properties?: Record<string, any>): void`
- `flush(): Promise<void>`
- `setCurrentScreen(screen: string | null): void`
- `connectTester(code6: string): Promise<void>`
- `disconnectTester(): Promise<void>`
- `getDebugSnapshot(): Promise<Record<string, any>>`
- `isConnected(): Promise<boolean>`
- `TesternestConnectPrompt`

## Build

```sh
cd android
./gradlew assembleRelease
```
