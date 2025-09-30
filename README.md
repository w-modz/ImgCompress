# 📷 Image Compress App

<table>
  <tr>
    <td>
      The <b>Image Compressor App</b> is an Android application built with <b>Kotlin</b> and <b>Jetpack Compose</b> that allows you to quickly compress images by either:  
      <br><br>
      • 📏 Setting a <b>target size in MB</b><br>
      • 📉 Choosing a <b>percentage reduction</b><br><br>
      This tool is helpful for reducing photo sizes before sharing, uploading, or storing them.
    </td>
    <td>
      <img src="" alt="App Screenshot" width="800"/>
    </td>
  </tr>
</table>


---

## ✨ Features
- 📂 Pick images directly from your device gallery.  
- ⚖️ Choose between:
  - Flat size compression (`MB`), or
  - Percentage-based compression (`%`).  
- 🔄 Preview selected images before compressing.  
- 💾 Saves compressed images automatically to your gallery.  
- 🎨 Modern UI built with **Material 3** and **Jetpack Compose**.
- 📱 Compatible with Android 8.0 Oreo and newer.

---

## 🛠️ Tech Stack
- [Kotlin](https://kotlinlang.org/)  
- [Jetpack Compose](https://developer.android.com/jetpack/compose)  
- [Material 3](https://m3.material.io/)  
- [Coil](https://github.com/coil-kt/coil) for image loading  

---


## 🚀 Getting Started

Follow these steps to build, install, and launch **WiFiMeasure** using **Android Studio**.

---

### 1️⃣ Prerequisites
- [Android Studio](https://developer.android.com/studio) (Giraffe or newer recommended)
- Android SDK 26 or newer
- Java 11 installed (configured in Android Studio)
- An Android device (real or emulator)

### 2️⃣ Clone the Repository
```bash
git clone https://github.com/yourusername/WiFiMeasure-App.git
cd WiFiMeasure-App
```

### 3️⃣ Open in Android Studio

- Launch **Android Studio**.
- Select **File → Open** and choose the project folder.
- Wait for Gradle to sync all dependencies.


### 4️⃣ Build the Project

  You can build via the **UI** or **command line**:

  ### UI Method
  1. Go to **Build → Make Project**.
  
  ### Command Line Method
  ```bash
  ./gradlew build
  ```
  
### 5️⃣ Run the App
Option A – Android Emulator

    - In Android Studio, open Device Manager.

    - Create and start a virtual device (API 26+).

    - Click Run ▶ in the toolbar or use:

Option B – Physical Device (USB Debugging)

    - Enable Developer Options and USB Debugging on your device.

    - Connect your device via USB.

    - Authorize the connection if prompted.

    - Click Run ▶ in Android Studio or run:

✅ The app should now install and launch on your selected device!
