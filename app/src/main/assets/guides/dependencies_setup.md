# App Dependency Guide

Setting up dependencies correctly is crucial for a smooth Android development experience. This guide
covers essential Jetpack and third-party libraries that are commonly used in modern Android
development.

## Core Concepts

* **Modularization**: Consider using separate Gradle modules for different features or layers (e.g.,
  `app`, `data`, `domain`, `feature_x`) to manage dependencies more effectively.
* **Version Catalogs**: For larger projects, use Gradle version catalogs (libs.versions.toml) to
  manage dependency versions consistently across modules.
* **`build.gradle.kts` (or `build.gradle`):** Dependencies are declared in the `dependencies` block
  of your module-level `build.gradle.kts` (Kotlin DSL) or `build.gradle` (Groovy DSL) file.

## Jetpack Libraries

Jetpack is a suite of libraries to help developers follow best practices, reduce boilerplate code,
and write code that works consistently across Android versions and devices.

### 1. Lifecycle-Aware Components

* **Purpose**: Manage activity and fragment lifecycles gracefully. Prevents memory leaks and handles
  configuration changes.
* **Key components**: `ViewModel`, `LiveData`, `LifecycleObserver`.
* **Gradle Dependency (example)**:
   ```gradle
   // ViewModel
   implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
   // LiveData
   implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
   // Lifecycles only (without ViewModel or LiveData)
   implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
   // Annotation processor for @OnLifecycleEvent
   kapt("androidx.lifecycle:lifecycle-compiler:2.7.0") // or annotationProcessor for Java
   ```

### 2. Navigation

* **Purpose**: Simplifies implementing navigation, from simple button clicks to complex patterns
  like deep links and argument passing.
* **Key components**: `NavController`, Navigation graph (XML or Kotlin DSL).
* **Gradle Dependency (example)**:
   ```gradle
   implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
   implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
   ```

### 3. Compose

* **Purpose**: Android’s modern toolkit for building native UI. It simplifies and accelerates UI
  development on Android with less code, powerful tools, and intuitive Kotlin APIs.
* **Gradle Dependency (example - BOM recommended)**:
   ```gradle
   // Define Compose BOM (Bill of Materials) in your root build.gradle or libs.versions.toml
   // implementation(platform("androidx.compose:compose-bom:2024.05.00")) // Check for latest version

   implementation("androidx.compose.ui:ui")
   implementation("androidx.compose.material3:material3") // For Material Design 3
   implementation("androidx.compose.ui:ui-tooling-preview")
   debugImplementation("androidx.compose.ui:ui-tooling")
   ```

### 4. Room

* **Purpose**: A persistence library that provides an abstraction layer over SQLite to allow for
  more robust database access while harnessing the full power of SQLite.
* **Key components**: `Entity`, `DAO` (Data Access Object), `Database`.
* **Gradle Dependency (example)**:
   ```gradle
   implementation("androidx.room:room-runtime:2.6.1")
   kapt("androidx.room:room-compiler:2.6.1") // or annotationProcessor for Java
   // Optional - Kotlin Extensions and Coroutines support for Room
   implementation("androidx.room:room-ktx:2.6.1")
   ```

### 5. Hilt

* **Purpose**: A dependency injection library for Android that reduces the boilerplate of doing
  manual dependency injection in your project. Built on top of Dagger.
* **Key components**: `@HiltAndroidApp`, `@AndroidEntryPoint`, `@Inject`, `@Module`, `@Provides`.
* **Gradle Dependency (example)**:
   ```gradle
   implementation("com.google.dagger:hilt-android:2.51.1")
   kapt("com.google.dagger:hilt-compiler:2.51.1")
   // For Hilt Navigation Compose
   implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
   ```
* **Plugin Setup**: Add the Hilt plugin to your root `build.gradle.kts` and apply it in your
  app-level `build.gradle.kts`.

### 6. WorkManager

* **Purpose**: Manages deferrable, asynchronous tasks that are expected to run even if the app exits
  or the device restarts. Ideal for background work.
* **Key components**: `Worker`, `WorkRequest`, `WorkManager`.
* **Gradle Dependency (example)**:
   ```gradle
   implementation("androidx.work:work-runtime-ktx:2.9.0")
   ```

## Third-Party Libraries

These are popular and widely adopted libraries from the Android community.

### 1. Retrofit

* **Purpose**: A type-safe HTTP client for Android and Java by Square. Makes it easy to consume JSON
  or XML data which is parsed into Plain Old Java Objects (POJOs).
* **Gradle Dependency (example)**:
   ```gradle
   implementation("com.squareup.retrofit2:retrofit:2.11.0")
   // Converters for JSON (Gson or Moshi are common)
   implementation("com.squareup.retrofit2:converter-gson:2.11.0") // or converter-moshi
   ```

### 2. OkHttp

* **Purpose**: An efficient HTTP client for Android and Java. Retrofit uses OkHttp by default for
  network requests. It supports HTTP/2, connection pooling, GZIP, and response caching.
* **Gradle Dependency (example - often included by Retrofit, but can be used standalone)**:
   ```gradle
   implementation("com.squareup.okhttp3:okhttp:4.12.0")
   implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // For logging requests/responses
   ```

### 3. Coil / Glide

* **Purpose**: Image loading libraries for Android. They handle caching, transformations, and
  loading images from various sources (network, local files, resources).
* **Coil (Kotlin Coroutines based)**:
   ```gradle
   implementation("io.coil-kt:coil-compose:2.6.0") // For Compose
   implementation("io.coil-kt:coil:2.6.0")        // For Views
   ```
* **Glide**:
   ```gradle
   implementation("com.github.bumptech.glide:glide:4.16.0")
   kapt("com.github.bumptech.glide:compiler:4.16.0") // or annotationProcessor for Java
   ```

### 4. Kotlin Coroutines / Flow

* **Purpose**: For asynchronous programming in Kotlin. Coroutines simplify background tasks, and
  Flow provides a stream of data that can be collected asynchronously.
* **Gradle Dependency (example)**:
   ```gradle
   // Core Coroutines
   implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3") // Check for latest
   implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
   // Flow is part of kotlinx-coroutines-core
   ```

## Best Practices for Managing Dependencies

* **Keep them updated**: Regularly check for new versions to get bug fixes, performance
  improvements, and new features. Use tools like `gradle-versions-plugin` or Android Studio's
  built-in lint checks.
* **Minimize**: Only include dependencies you actually need to avoid increasing build times and app
  size unnecessarily.
* **Understand Transitive Dependencies**: Be aware of the dependencies your chosen libraries bring
  in. Use `gradlew :app:dependencies` to inspect the dependency tree.
* **Security**: Be cautious with less-known libraries. Check for open issues and community support.

This guide provides a starting point. The specific dependencies you need will vary based on your
project's requirements. Always refer to the official documentation for each library for the most
up-to-date information and advanced usage.
