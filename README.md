# Native Game Overlay

This is a native Android app built with Java and Gradle. It does not use Expo.

## Safety and scope

The app provides a visible, user-controlled floating shortcut with a persistent Fly speed control. The slider changes how quickly the floating button responds to dragging and is stored locally. It uses Android's official overlay permission and a foreground service with an ongoing notification. It does not inject code, modify the operating system, read other apps' private data, capture the screen, or bypass Android permissions.

## Build locally

```bash
gradle :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## GitHub build

The workflow in `.github/workflows/build-apk.yml` builds the debug APK on every push and uploads it as a downloadable workflow artifact. Pushing a tag such as `v1.0.0` also creates a GitHub release containing the APK.

On the phone, install the APK, open it, grant "display over other apps", then tap Start overlay.