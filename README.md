# Listening Stats Android Concept

A personal Android app that visualizes music listening statistics from **Last.fm** and **stats.fm** — built as a learning project to improve Kotlin/Jetpack Compose skills.

## Features

- **Dashboard**: Top tracks, artists, albums + recently played with album art
- **Top Items**: Browse top tracks/artists/albums filtered by time range
- **Recent Tracks**: Full recently played history with album art
- **Activity**: GitHub-style heatmap, hourly/weekly/monthly breakdowns
- **Album Art**: Deezer API fallback when Last.fm has no cover

## Concept

This is a **learning / portfolio project**, not a production app. The goal was to explore:

- Jetpack Compose UI + Material3 design system
- Retrofit + kotlinx.serialization for API clients
- ViewModel + StateFlow + coroutines architecture
- Room caching and Coil image loading
- Adaptive icons and Android theming

## Supported Devices

- **Android 8.0 (API 26)** and above
- Optimized for phones; tablet support is basic
- arm64-v8a, armeabi-v7a, x86_64

## Download

Grab the latest APK from [Releases](https://github.com/Death-Watcher/listening-stats-android-concept/releases).

## Build from Source

```bash
git clone https://github.com/Death-Watcher/listening-stats-android-concept.git
cd listening-stats-android-concept
export JAVA_HOME=/path/to/jdk17
export ANDROID_HOME=/path/to/android-sdk
./gradlew assembleDebug
```

## Requirements

- **Last.fm account** + [API key](https://www.last.fm/api)
- **stats.fm account** (optional, provides additional heatmap data)

## APIs Used

- [Last.fm API](https://www.last.fm/api) – play counts, album art, artist names
- [Deezer API](https://developers.deezer.com/api) – album art fallback
- [stats.fm API](https://stats.fm/api) – stream timestamps (heatmap only)

## License

MIT
