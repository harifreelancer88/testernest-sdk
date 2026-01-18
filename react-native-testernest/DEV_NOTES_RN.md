# React Native Dev Notes (Android)

Install deps:

```sh
yarn install
```

Build and install the example app:

```sh
cd example/android
gradlew installDebug
```

Verify network flow (Windows CMD):

```bat
adb logcat -v time | findstr /i "Testernest: BOOTSTRAP Testernest: BATCH Testernest: CLAIM"
```

Reset connect prompt and retest:

```bat
adb shell pm clear com.testernest.example
```
