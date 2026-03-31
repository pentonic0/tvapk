# Stream Player TV — Android Studio Project

An Android TV app that fetches live channels from a JSON feed and plays HLS/DASH streams with optional ClearKey DRM.

## Setup Instructions

### 1. Add the Gradle Wrapper JAR
The `gradle-wrapper.jar` cannot be distributed in source form. After opening the project in Android Studio, it will prompt you to download it automatically — just click **OK** when asked.

Alternatively, run from the project root:
```
gradle wrapper --gradle-version 8.6
```

### 2. Set your Android SDK path
Edit `local.properties` and set:
```
sdk.dir=/Users/yourname/Library/Android/sdk   # macOS
sdk.dir=C:\\Users\\yourname\\AppData\\Local\\Android\\sdk   # Windows
```

### 3. Open in Android Studio
- Open Android Studio → **File → Open** → select this folder
- Let Gradle sync complete
- Connect an Android TV device or start a TV emulator (API 23+)
- Click **Run**

## Features
- 📺 TV-optimised grid layout with D-pad navigation
- 🔴 LIVE badge on all channels
- ▶ HLS & DASH playback via ExoPlayer (Media3)
- 🔑 ClearKey DRM support
- ⌨️ Full remote control: D-pad, Enter, Back, Play/Pause, Stop
- 🖼 Glide thumbnail loading with placeholder
- Auto-focus first channel on launch

## Channel Data Source
`https://raw.githubusercontent.com/pentonic0/mpd/main/link.json`

## Tech Stack
- Kotlin + ViewBinding
- AndroidX Leanback (TV UI)
- Media3 ExoPlayer (HLS + DASH + DRM)
- Glide (image loading)
- Gson (JSON parsing)
- Kotlin Coroutines + LiveData
