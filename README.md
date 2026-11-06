# TVision Android Launcher (v0.1.0)

A lightweight, native Kotlin Android Home launcher prototype designed for a landscape tablet. It uses the system launcher activity list, launches apps, and supports touch plus standard Android D-pad/Enter navigation.

## Build
1. Open this folder in Android Studio (Giraffe or newer recommended).
2. Allow Gradle sync; use JDK 17.
3. Select the `app` run configuration and install on the tablet.
4. Press Home and choose **TVision**. Select **Always** to make it the default launcher.

## Package
`com.centurianrahul.tvision`

## Current prototype scope
- HOME + LAUNCHER intent filters
- Landscape, immersive full-screen home
- Automatically discovers launchable apps
- App icons and labels in a focusable grid
- Touch and D-pad/Enter activation
- Clock/date header and TV-style visual treatment

## Next iteration
Add editable favourites, a real Home Assistant tile/deep link, configurable rows, settings, screensaver, wallpaper selection, and accessibility/remote testing. Streaming-service content cannot be embedded without their supported apps/APIs; TVision launches the installed apps instead.

## Build APK through GitHub Actions (no local Android build required)
1. Create a GitHub repository and upload the contents of this project.
2. Open the repository's **Actions** tab.
3. Run **Build TVision Android APK** (or push to `main`/`master`).
4. Open the completed workflow run and download the `TVision-debug-apk` artifact.
5. Extract `app-debug.apk` and install it on the tablet. Android may ask you to allow installation from the browser or file manager used to open it.
