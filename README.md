# DayFlow · Plan better. Do more.

An offline-first Android day planner in **Kotlin + Jetpack Compose**.

## What it does

- Create, edit, complete and delete tasks with notes, due dates and times.
- See **Today**, **Upcoming**, **All tasks**, and **Done** views.
- Organize tasks as Study, Work, Personal or Health, with priorities.
- Set notifications at the time of the event, 10 minutes before or 30 minutes before.
- Set daily and weekly repeating plans.
- Reschedule reminders on boot, device time changes and app updates.
- Track your day's completion progress with a clear modern dark dashboard.
- Store your tasks privately on your phone; no account, server, Firebase, or internet required.

## Get an APK without Android Studio

1. Put these files at the **root** of a GitHub repository (including `.github/workflows/android-apk.yml`).
2. Go to **Actions** in GitHub and open **Build DayFlow APK**.
3. Click **Run workflow** if there isn't already an automatic run.
4. After the workflow is successful, open that run and choose the **DayFlow-APK** artifact.
5. Extract the ZIP and install `app-debug.apk` on your Android device.

The workflow produces a **debug APK for personal installation/testing**, not a release-signed Play Store app. The build is configured but has not been verified by a CI run in this environment.

### Setup reminders

Accept the **notifications** permission when prompted. On newer Android versions, also grant the app **Alarms & reminders** permission for precise alarm timing. Without that permission the app uses inexact Android alarms, which may be delayed. Battery restrictions, notification settings, DND, and force-stopping the app can also block reminders.

## Local build

Requirements: JDK 17, Android SDK Platform 35, Gradle 8.9.

```sh
gradle :app:assembleDebug
```

APK location: `app/build/outputs/apk/debug/app-debug.apk`.

## Data/privacy

Tasks are stored locally in Android SharedPreferences as JSON. No network permission is requested. No cloud backup service is built in. Data may be lost if the app is uninstalled or app storage is cleared. Some Android backup services may back up app data according to device settings.

## Limitations / planned additions

This version does not include calendar sync, cloud sync, home-screen widgets, snooze actions, or multi-device collaboration. Recurring reminders automatically advance to the next occurrence after firing, and completing a recurring plan starts the next occurrence.
