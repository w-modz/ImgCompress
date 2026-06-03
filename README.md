# Image Compress

An Android app built with Kotlin and Jetpack Compose for compressing images before sharing, uploading, or storage. You can target a specific file size in MB or reduce by a percentage.

<table>
  <tr>
    <td>

## Features

- Pick images from your device gallery
- Two compression modes: flat size (MB) or percentage-based reduction
- Preview images before compressing
- Saves output automatically to your gallery
- Material 3 UI
- Supports Android 8.0 (API 26) and newer

    </td>
    <td>
      <img src="https://github.com/w-modz/ImgCompress/blob/main/Screenshot_ImgCompress.jpg" alt="App Screenshot" width="300"/>
    </td>
  </tr>
</table>

## Tech Stack

- [Kotlin](https://kotlinlang.org/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material 3](https://m3.material.io/)
- [Coil](https://github.com/coil-kt/coil) — image loading

## Getting Started

### Prerequisites

- Android Studio Giraffe or newer
- Android SDK 26+
- Java 11 (configured in Android Studio)
- A physical device or emulator

### Setup

```bash
git clone https://github.com/yourusername/ImgCompress.git
cd ImgCompress
```

Open the project in Android Studio via **File → Open**, then wait for Gradle to sync.

### Building

Via the IDE: **Build → Make Project**

Via command line:
```bash
./gradlew build
```

### Running

**Emulator** — Open Device Manager, create a virtual device targeting API 26+, and hit Run.

**Physical device** — Enable Developer Options and USB Debugging, connect via USB, authorize the connection if prompted, then hit Run.
