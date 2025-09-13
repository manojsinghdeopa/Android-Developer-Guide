# Tools & Environment Setup 🛠️

## Installation

1.  **Android Studio:** Download and install the latest version of Android Studio from the [official website](https://developer.android.com/studio).
2.  **Android SDK:** During the Android Studio installation, the setup wizard will guide you through installing the necessary Android SDK components. Make sure to install the latest SDK platform.
3.  **Emulator/Physical Device:**
    *   **Emulator:** In Android Studio, go to `Tools > AVD Manager` to create and manage Android Virtual Devices (AVDs). Choose a system image that matches your target Android version.
    *   **Physical Device:** Enable Developer Options and USB Debugging on your Android device. Connect it to your computer via USB, and Android Studio should recognize it.

## Configuration

1.  **Gradle:** Gradle is the build system used by Android Studio. It's automatically configured when you create a new project.
2.  **Project Structure (`build.gradle.kts`):**
    *   The `build.gradle.kts` file (usually found in the `app` module and the project root) is where you manage project dependencies and build configurations.
    *   **Dependencies:** To add libraries, use the `dependencies` block in your module-level `build.gradle.kts` file. For example:
        ```kotlin
        dependencies {
            implementation("androidx.core:core-ktx:1.9.0") // Example dependency
            implementation("com.google.android.material:material:1.10.0") // Example dependency
            // Add other dependencies here
        }
        ```
    *   After adding or modifying dependencies, Android Studio will prompt you to sync your project with the Gradle files. Click "Sync Now".

This setup will provide you with the necessary tools and a basic project structure to start developing Android applications.
