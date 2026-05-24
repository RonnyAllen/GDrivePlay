# GDrivePlay 🎬
Stream your Google Drive videos instantly with a gorgeous, high-end player experience. 

GDrivePlay is a premium, feature-rich Android video player designed to let you stream your personal media library directly from Google Drive. With a true-dark AMOLED interface and a vibrant amber-orange design, GDrivePlay delivers a cinematic experience right on your phone without taking up local storage.

---

## 🌟 Premium Features

- **🚀 Direct Google Drive Streaming**: Browse, search, and instantly stream any video file (`.mp4`, `.mkv`, `.avi`, `.webm`) from your cloud drive.
- **⚡ 200% Software Volume Booster**: Swiping up on the right side of the screen controls your system volume. Swiping past 100% engages a digital gain booster, raising quiet audio tracks up to 200% (+20dB) without clipping.
- **📱 Picture-in-Picture (PiP)**: Smoothly transition to a floating picture-in-picture mini-player when you go home or click the PiP button. Perfect for multitasking!
- **🖐️ Precision Swipe Gestures**:
  - **Left Screen Edge**: Swipe up/down to adjust screen brightness smoothly.
  - **Right Screen Edge**: Swipe up/down to adjust volume (and engage 200% boost).
  - **Center Screen**: Swipe horizontally to scrub through the timeline with live video seeks and exact timecode overlays.
- **🔄 Smart Playback Controls**: Loop modes (off, loop one, repeat all), shuffle playback, fast forward/rewind 10s taps, and customizable playback speeds (0.25x up to 2.0x).
- **📂 Press-and-Hold Context Menus**:
  - Long-press any file or folder to add it to your playlist queue.
  - Enter **Multi-Select Mode** to choose multiple videos and folders, and queue them all at once using the glassmorphic floating action bar.
- **📺 Chromecast & background Sync**: Play audio in the background or cast directly to your Smart TV over LAN with automatic token proxy security.
- **🌙 True AMOLED Black Mode**: Switch between premium charcoal surfaces and high-contrast true black themes in Settings to save battery on OLED screens.

---

## 📱 How to Use the App

### 1. Simple Set Up
1. Sign in securely with your Google Account when launching the app.
2. Grant read-only access to browse your Google Drive.

### 2. Gesture Guide (Video Player)
| Gesture | Location | Action |
| :--- | :--- | :--- |
| **Vertical Swipe** | Left 15% Edge | Adjust screen brightness (0% - 100%) |
| **Vertical Swipe** | Right 15% Edge | Adjust volume (0% - 100% system, 100% - 200% boost) |
| **Horizontal Swipe**| Center 70% | Scrub / seek through video with live updates |
| **Double Tap** | Left Half | Rewind 10 seconds |
| **Double Tap** | Right Half | Fast Forward 10 seconds |
| **Pinch Zoom** | Anywhere | Cycle aspect ratios (Fit, Fill, Stretch, 4:3, 16:9) |

### 3. File Browser Actions
- **Single Tap**: Open a folder, or play a video. (Playing a video automatically loads all videos in the active folder into the player queue so you can skip between them).
- **Long Press / Press & Hold**: Opens a context menu to bulk-add a folder/video to your playlist, view metadata details, or enter **Multi-Select Mode**.
- **Multi-Select Mode**: Checkboxes appear next to files/folders. Select multiple items and click **Add to Playlist** on the floating action bar to queue them.

---

## 🛠️ Build & Installation

### Option 1: Direct APK Installation (End User)
You can directly download and install the signed release APK to your phone:
1. Locate the packaged APK file: [app-release.apk](file:///C:/Users/rohan/.gradle-builds/GDrivePlay/app/outputs/apk/release/app-release.apk).
2. Transfer it to your phone and install it (make sure "Install from Unknown Sources" is enabled in settings).

### Option 2: Compile in Android Studio (Developer)
1. Open **Android Studio** (Hedgehog or newer) and select **Open** or **Import**.
2. Select the `GDrive Streaming app` workspace root directory.
3. Let Gradle compile dependencies and sync.
4. Press the green **Run** button to compile and deploy to your connected device.
