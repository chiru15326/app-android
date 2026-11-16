# My Study Planner – Android project

Offline Android study planner for the 26-week Senior .NET Full Stack timetable.

## Requirements
- Android Studio (a recent stable release that supports Android Gradle Plugin 8.10.1)
- JDK 17
- Android SDK Platform 36
- Internet access on the development computer for Gradle dependencies on the first build

## Build APK
1. Extract this ZIP.
2. Open the `MyStudyPlannerAndroid` folder in Android Studio.
3. Allow Gradle sync and install missing SDK components if prompted.
4. Select Build > Build Bundle(s) / APK(s) > Build APK(s).
5. Android Studio will create the debug APK at `app/build/outputs/apk/debug/app-debug.apk`.
6. Copy the APK to your Vivo phone and install it. If prompted, allow installing apps from that file manager/browser.

## Features
- 182 dated daily entries from 17 Nov 2026 through 17 May 2027.
- Daily availability aligned to actual calendar weekdays: Monday 2h; Tuesday–Thursday 4h; Friday 2h; Saturday 2h; Sunday 2h (20 hours per Monday–Sunday week). The first 7-day course block runs Tuesday–Monday because the confirmed start date is Tuesday, 17 Nov 2026.
- Home dashboard, schedule by week, completion tracking, light/dark mode.
- Local progress storage using Android SharedPreferences.
- Daily reminder enabled by default at 7:00 AM; it is scheduled on first app launch and after device reboot. Change the reminder time or turn it off in Settings.
- No internet permission is requested by the app. Gradle needs internet on the build computer to download dependencies.

## Notes
- The schedule is bundled locally as `app/src/main/assets/schedule.json`.
- Reminder delivery uses WorkManager and is not exact-to-the-minute; Android battery optimization can delay it.
- On Android 13+, grant notification permission when prompted. If denied, reminders will not be visible.
- This source project has not been compiled or device-tested in this environment. Build and verify it in Android Studio before installing on your Vivo device.

## Build APK without installing Android Studio (GitHub Actions)
1. Create a GitHub account and a new repository (for example, `MyStudyPlannerAndroid`).
2. Upload the contents of this project folder to the repository root (not the outer ZIP file).
3. Open the repository's **Actions** tab and select **Build Android APK**.
4. Select **Run workflow** if it has not started automatically, then wait for the run to finish successfully.
5. Open the completed run and download the **MyStudyPlanner-debug-apk** artifact. Extract it to get `app-debug.apk`.
6. Transfer `app-debug.apk` to the Android phone and install it. Allow installation from the app used to open the APK if Android asks.

This workflow builds a debug APK in GitHub's cloud runner; it does not create a signed Play Store release. Keep the repository private if you do not want the source code public. Do not commit passwords, signing keys, or other secrets.
