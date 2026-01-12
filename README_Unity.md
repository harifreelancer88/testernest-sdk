# TesterNest Unity Android Wrapper

## Import into Unity
- Copy `unity-testernest-package/Assets/Testernest` into your Unity project's `Assets/` folder.
- Ensure the AARs are present in `Assets/Testernest/Plugins/Android/`:
  - `testernest-android-release.aar`
  - `testernest-core-release.aar`

## Player Settings
- **Minimum API Level**: 21
- **Target API Level**: 34 (or latest installed)

## Android Dependencies
The Android wrapper relies on AndroidX lifecycle libraries.

If you use **External Dependency Manager for Unity**, add a dependencies file or include these in your existing setup:
- `androidx.lifecycle:lifecycle-process:2.7.0`
- `androidx.lifecycle:lifecycle-runtime-ktx:2.7.0`

If you manage Gradle dependencies directly, add:

```gradle
dependencies {
    implementation "androidx.lifecycle:lifecycle-process:2.7.0"
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.7.0"
}
```

## Build/Copy the AARs
From the repo root:

PowerShell:
```powershell
.\unity-testernest-package\build-android-aar.ps1
```

Bash:
```bash
./unity-testernest-package/build-android-aar.sh
```

## Demo MonoBehaviour
```csharp
using UnityEngine;
using Testernest;

public class TesternestDemo : MonoBehaviour
{
    void Start()
    {
        Testernest.Init("YOUR_PUBLIC_KEY", enableLogs: true);
        Testernest.SetScreen("MainMenu");
        Testernest.Track("app_started", "{\"source\":\"unity\"}");
    }

    public void Connect(string code6)
    {
        Testernest.ConnectTester(code6);
    }

    public void FlushNow()
    {
        Testernest.Flush();
    }
}
```

## Test on Android
- Switch build target to Android.
- Build & Run on a device.
- Use `adb logcat` to view logs if `enableLogs` is true.
