# Reducing Your App Size

A smaller app size can lead to more downloads, improved user retention, and reduced data usage for
your users. Here are some key strategies to reduce your app's size:

## 1. Use Android App Bundles (AAB)

Android App Bundles are a publishing format that includes all your app’s compiled code and
resources, but defers APK generation and signing to Google Play. Google Play’s Dynamic Delivery then
uses your app bundle to generate and serve optimized APKs for each user’s device configuration, so
they download only the code and resources they need to run your app.

**Benefits:**

* **Smaller app size for users:** Google Play optimizes APKs for specific device densities, ABIs,
  and languages.
* **Simplified release management:** You no longer need to build, sign, and manage multiple APKs for
  different device configurations.
* **Dynamic features:** Allows you to deliver features on-demand, rather than at install time,
  further reducing initial download size.

**How to implement:**

* Build your app as an AAB from Android Studio (Build > Build Bundle(s) / APK(s) > Build Bundle(s)).
* Upload the AAB to the Google Play Console.

## 2. Enable Code Shrinking with ProGuard/R8

ProGuard (for older Android Gradle plugin versions) or R8 (the default for newer versions) are tools
that shrink, obfuscate, and optimize your code.

* **Shrinking (Tree Shaking):** Detects and safely removes unused classes, fields, methods, and
  attributes from your app and its library dependencies. This is a highly effective way to reduce
  code size.
* **Optimization:** Optimizes the bytecode, removing unused instructions and inlining code where
  possible.
* **Obfuscation:** Renames classes, fields, and methods with short, meaningless names, making your
  code harder to reverse engineer and further reducing DEX file sizes.

**How to enable:**

In your module-level `build.gradle` (or `build.gradle.kts`) file, ensure `minifyEnabled` is set to
`true` for your release build type:

```
android {
    // ...
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

* `proguard-android-optimize.txt`: This is a default ProGuard/R8 configuration file provided by the
  Android SDK. It includes some baseline optimizations.
* `proguard-rules.pro`: This is where you define custom ProGuard/R8 rules. You'll need to add rules
  to prevent ProGuard/R8 from removing code that is actually needed (e.g., code accessed via
  reflection).

**Important Considerations:**

* **Thorough Testing:** After enabling `minifyEnabled`, thoroughly test your release builds. Code
  shrinking can sometimes remove code that your app relies on in ways that static analysis can't
  detect (like reflection).
* **Keep Rules (`-keep` directives):** You'll often need to add specific `-keep` rules in your
  `proguard-rules.pro` file to tell ProGuard/R8 not to remove or obfuscate certain classes, methods,
  or fields. This is common for:
    * Classes and methods accessed via reflection.
    * Enum `values()` and `valueOf()` methods if you iterate over them.
    * Classes that are serialized (e.g., with Gson or Moshi).
    * Native methods (JNI).
    * View constructors for XML layouts.

## 3. Enable Resource Shrinking

Resource shrinking works in conjunction with code shrinking. Once unused code has been removed,
resource shrinking can identify and remove resources (like drawables, layouts, and strings) that are
no longer referenced.

**How to enable:**

In your module-level `build.gradle` (or `build.gradle.kts`) file, set `shrinkResources` to `true`
for your release build type. This property only works if `minifyEnabled` is also `true`.

```
android {
    // ...
    buildTypes {
        release {
            minifyEnabled true
            shrinkResources true // Enable resource shrinking
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}

```

**How it works:**

1. **Code Shrinking:** ProGuard/R8 first removes unused code.
2. **Resource Collection:** The Gradle build system records all resources used by the app.
3. **Analysis:** It then compares the used resources with the app's packaged code. If a resource is
   not referenced by the remaining code (after code shrinking), it's considered unused.
4. **Removal:** Unused resources are removed from the APK.

**Safe Removal and Strict Mode:**

* By default, resource shrinking is "safe." It won't remove resources that are dynamically
  accessed (e.g., using `Resources.getIdentifier()`).
* You can use a "strict mode" by creating an XML file (e.g., `res/raw/keep.xml`) with a
  `<resources shrinkMode="strict" ...>` tag. In strict mode, you must explicitly mark resources to
  keep using `tools:keep` or `tools:discard` attributes. This offers more aggressive shrinking but
  requires more careful configuration.

**Example `keep.xml`:**

```
<?xml version="1.0"encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools"
    tools:keep="@layout/used_layout,@drawable/used_icon"
    tools:discard="@layout/unused_layout_by_default_unless_dynamic" />

```

By implementing these strategies, you can significantly reduce the size of your Android application,
leading to a better experience for your users.
