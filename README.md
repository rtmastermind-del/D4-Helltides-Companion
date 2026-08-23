# D4 Helltide Companion

Android companion app prototype for Diablo IV Helltides.

## Version 0.2.0

Features:
- Native Helltide countdown based on the 55-minute active / 5-minute break schedule.
- Built-in live Helltides.com map in an Android WebView.
- Aberrant Cinder tracker with a 250-Cinder Mystery Chest target.
- Helltide priority guide.
- Quick links to Helltides.com event schedule and World Boss tracker.
- Dark phone-first interface designed for quick use while playing.

## Build

1. Open the `D4HelltideCompanion` folder in Android Studio.
2. Allow Gradle sync to finish.
3. Run on an Android emulator or connected phone (Android 8.0 / API 26 or newer).

The Live Map requires an internet connection. The countdown works offline.

## Data / attribution

This is an unofficial fan companion. Diablo IV and associated trademarks/assets are the property of Blizzard Entertainment. The app does not bundle or redistribute Helltides.com map assets; it loads the public Helltides.com site in a WebView for current map/event data.

## Build without Android Studio
This project includes a GitHub Actions workflow at `.github/workflows/build-apk.yml`.
Upload the project to a GitHub repository, open the Actions tab, run **Build Android APK**, then download the **D4-Helltide-Companion-APK** artifact. No Android Studio is required.
