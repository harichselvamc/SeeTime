## See Time

A simple Kotlin Android app that lets users check and convert time between any IANA time zones.
All timezone offset and DST data is computed on-device (via the platform's `java.time`/tzdata), stored locally in Room, and refreshed instantly with no network access required.
Built with Android Studio and tested on a Motorola Edge 50 Pro.

**Website:** [harichselvamc.github.io/SeeTime](https://harichselvamc.github.io/SeeTime/) (project overview, screenshots, download links)

## Features

- Compare and convert time across any IANA time zone, fully offline
- Accurate DST handling — shows the real delta for each zone, not a guessed +1:00
- Home screen widget showing your primary time pair
- Long-press app shortcuts: quick-add a time pair, report an issue
- 12-hour / 24-hour format toggle in Settings
- Material You dynamic color + dark theme
- Drag to reorder, swipe to edit/delete

## Download

- **Google Play:** coming soon — [play.google.com/store/apps/details?id=com.harichselvamc.seetime](https://play.google.com/store/apps/details?id=com.harichselvamc.seetime)
- **GitHub Releases:** grab the signed APK from the [Releases](https://github.com/harichselvamc/SeeTime/releases) section (latest tag, `app-release.apk`). You may need to allow installation from unknown sources.

## Feedback

Found a bug or have a feature request? Open an [issue](https://github.com/harichselvamc/SeeTime/issues), use the "Report an issue" shortcut/setting in the app, or email harichselvamc@gmail.com.

## Screenshots

1. Home Screen
<img width="250" alt="Home Screen" src="https://github.com/user-attachments/assets/eb4d45af-10c2-4422-8d6c-46c087ac27d8" />

2. Add Time Zone
<img width="250" alt="Add Time Zone" src="https://github.com/user-attachments/assets/05920377-dedd-463b-9fa2-ddddb0eba1e5" />

3. Swipe Right to Delete (Home Screen)
<img width="250" alt="Swipe Right to Delete" src="https://github.com/user-attachments/assets/410552fa-0915-49f3-9814-8792e91aa286" />

4. Swipe Left to Edit
<img width="250" alt="Swipe Left to Edit" src="https://github.com/user-attachments/assets/cb91c9db-5a89-42a8-aa33-fd52083f98db" />
