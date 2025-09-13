# Android Security Best Practices

This guide covers essential security practices for Android development, focusing on data protection and permissions.

## Secure Data Storage

Protecting sensitive user data is crucial. Android offers several ways to store data securely.

### 1. EncryptedSharedPreferences

For storing simple key-value pairs securely, `EncryptedSharedPreferences` provides a wrapper around `SharedPreferences` that automatically encrypts keys and values.

**Features:**

*   **Encryption:** Uses AES-256 GCM for keys and values.
*   **Key Management:** Leverages the Android Keystore system to protect the master key used for encryption.
*   **Ease of Use:** Similar API to `SharedPreferences`, making it easy to integrate.

**When to use:**

*   Storing sensitive preferences, settings, or small pieces of data like authentication tokens, API keys, or user credentials.

**Setup:**

1.  Add the security crypto library to your `build.gradle` file:
    ```gradle
    dependencies {
        implementation("androidx.security:security-crypto:1.0.0")
    }
    ```

2.  Initialize `EncryptedSharedPreferences`:
    ```kotlin
    import androidx.security.crypto.EncryptedSharedPreferences
    import androidx.security.crypto.MasterKeys

    // Create or retrieve the master key for encryption
    val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    // Initialize EncryptedSharedPreferences
    val sharedPreferences = EncryptedSharedPreferences.create(
        "secret_shared_prefs", // A unique filename for the SharedPreferences
        masterKeyAlias,
        applicationContext,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Use EncryptedSharedPreferences like regular SharedPreferences
    sharedPreferences.edit()
        .putString("user_token", "your_sensitive_token_here")
        .apply()

    val token = sharedPreferences.getString("user_token", null)
    ```

**Important Considerations:**

*   The Android Keystore system provides hardware-backed protection for master keys on supported devices, making it difficult to extract them.
*   Ensure that the filename for `EncryptedSharedPreferences` is unique and not easily guessable.

### 2. SQLCipher for Android

When you need to store larger amounts of structured data in a local database securely, SQLCipher offers full-database encryption. It's an open-source extension to SQLite that provides transparent 256-bit AES encryption of database files.

**Features:**

*   **Full Database Encryption:** Encrypts the entire database file, including schema, data, and metadata.
*   **Transparent Encryption:** Application code interacts with the database largely the same way as with a standard SQLite database.
*   **Cross-Platform:** Available for multiple platforms.
*   **Compatibility:** Can be used with Room Persistence Library with some additional setup.

**When to use:**

*   Storing sensitive structured data, such as user profiles, financial information, or health records, in a local SQLite database.

**Setup with Room:**

1.  Add SQLCipher and Room dependencies to your `build.gradle` file:
    ```gradle
    dependencies {
        implementation("androidx.room:room-runtime:2.6.1") // Use the latest Room version
        ksp("androidx.room:room-compiler:2.6.1") // Or annotationProcessor for Java
        implementation("net.zetetic:android-database-sqlcipher:4.5.0@aar") // Use the latest SQLCipher version
        implementation("androidx.sqlite:sqlite-framework:2.4.0") // For SafeHelperFactory
    }
    ```

2.  Create a `SupportSQLiteOpenHelper.Factory` that uses SQLCipher:
    ```kotlin
    import androidx.sqlite.db.SupportSQLiteDatabase
    import androidx.sqlite.db.SupportSQLiteOpenHelper
    import net.sqlcipher.database.SQLiteDatabase
    import net.sqlcipher.database.SupportFactory

    val passphrase = "your-secure-passphrase".toByteArray() // Store this securely, e.g., using Android Keystore
    val factory = SupportFactory(passphrase)
    ```
    **Note:** The passphrase needs to be securely managed. Storing it directly in code is not recommended for production apps. Consider deriving it or fetching it from a secure location, potentially protected by the Android Keystore.

3.  Configure Room to use this factory:
    ```kotlin
    import androidx.room.Room

    val db = Room.databaseBuilder(
        applicationContext,
        MyEncryptedDatabase::class.java, "my_encrypted_database.db"
    )
    .openHelperFactory(factory) // Set the SQLCipher factory
    .build()
    ```

**Important Considerations:**

*   **Passphrase Management:** The security of SQLCipher heavily relies on the secrecy of the passphrase. Use strong passphrases and protect them diligently. The Android Keystore can be used to protect the passphrase itself.
*   **Performance:** Encryption and decryption can introduce some performance overhead, especially for large databases or complex queries. Test thoroughly.
*   **Key Derivation:** Use robust key derivation functions (e.g., PBKDF2) if generating keys from user passwords.

## Permission Best Practices

Requesting and handling permissions correctly is vital for user privacy and app functionality.

### 1. Request Permissions at Runtime

For permissions that are classified as "dangerous" (e.g., accessing location, camera, contacts), you must request them from the user at runtime on Android 6.0 (API level 23) and higher.

**Steps:**

1.  **Declare Permissions in Manifest:**
    ```xml
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.READ_CONTACTS" />
    ```

2.  **Check for Existing Permissions:**
    Before performing an operation that requires a permission, check if your app already has it.
    ```kotlin
    import android.Manifest
    import android.content.pm.PackageManager
    import androidx.core.content.ContextCompat

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
        // Permission is already granted, proceed with camera operation
    } else {
        // Permission is not granted, request it
        requestCameraPermission()
    }
    ```

3.  **Request Permissions:**
    If the permission is not granted, request it from the user. Provide context or a rationale before making the request, especially if the permission isn't obvious for your app's functionality.
    ```kotlin
    import androidx.core.app.ActivityCompat

    private const val CAMERA_PERMISSION_REQUEST_CODE = 101

    private fun requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            // Show an explanation to the user *asynchronously*
            // This could be a dialog or a snackbar
            // After the user sees the explanation, try again to request the permission.
            AlertDialog.Builder(this)
                .setTitle("Camera Permission Needed")
                .setMessage("This app needs the Camera permission to take pictures.")
                .setPositiveButton("OK") { _, _ ->
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.CAMERA),
                        CAMERA_PERMISSION_REQUEST_CODE)
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .create().show()
        } else {
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE)
        }
    }
    ```

4.  **Handle Permission Result:**
    Override `onRequestPermissionsResult` to handle the user's response.
    ```kotlin
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Camera permission granted
                    openCamera()
                } else {
                    // Permission denied. Disable the functionality that depends on this permission.
                    // Optionally, inform the user that the functionality is unavailable.
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
            // Handle other permission requests
        }
    }
    ```

**Best Practices for Runtime Permissions:**

*   **Request in Context:** Only ask for permissions when they are needed for a specific feature the user is trying to access.
*   **Explain Why:** If the need for a permission isn't immediately obvious, provide a clear explanation to the user before showing the system permission dialog. Use `shouldShowRequestPermissionRationale()`.
*   **Handle Denial Gracefully:** If a user denies a permission, your app should continue to function without the feature that requires it. Don't block the user or repeatedly ask for the permission if they've denied it with "Don't ask again."
*   **Don't Request Unnecessary Permissions:** Only declare and request permissions that your app truly needs.
*   **Use the ActivityResult API:** For a more modern and recommended approach to requesting permissions, consider using the `ActivityResultContracts.RequestPermission` and `ActivityResultContracts.RequestMultiplePermissions` contracts.

### 2. Scoped Storage

Starting with Android 10 (API level 29), apps targeting this version or higher are given scoped access to external storage by default. This means apps have unrestricted access only to their app-specific directories on external storage and to specific types of media that the app has created.

**Key Concepts:**

*   **App-Specific Directory:** Each app has a private directory on external storage (e.g., `Android/data/your.package.name/files/`) where it can freely read and write files. No special permissions are needed for this.
*   **MediaStore:** For accessing shared media files (photos, videos, audio) created by other apps, use the `MediaStore` API. You'll typically need `READ_EXTERNAL_STORAGE` (or more granular media permissions like `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_AUDIO` on Android 13+) for read access and `WRITE_EXTERNAL_STORAGE` (only for apps targeting Android 9 or lower, or with special app access) for modification/deletion of other apps' media.
*   **Storage Access Framework (SAF):** For letting users browse and select files (documents, and other non-media files) from shared storage or cloud providers, use the Storage Access Framework (e.g., `ACTION_OPEN_DOCUMENT`, `ACTION_CREATE_DOCUMENT`).

**Benefits of Scoped Storage:**

*   **Better Attribution:** It's clearer which app owns which files.
*   **Reduced App Clutter:** Uninstalling an app can more effectively clean up the files it created in its app-specific directory.
*   **Enhanced User Privacy:** Apps have less access to potentially sensitive files on external storage by default.

**Adapting to Scoped Storage:**

*   Migrate files from legacy external storage locations to your app-specific directory or use `MediaStore` for appropriate media types.
*   If your app needs to access files outside its app-specific directory or shared media collections, use the Storage Access Framework.
*   For apps targeting Android 11 (API level 30) or higher, `WRITE_EXTERNAL_STORAGE` permission no longer grants general write access to external storage, even if requested. You must use `MediaStore` or SAF for modifications.
*   The `MANAGE_EXTERNAL_STORAGE` permission grants broad access but is intended for specific app categories (e.g., file managers) and requires Google Play approval. Most apps should not need this.

## General Security Tips

*   **Input Validation:** Always validate data received from external sources (user input, network responses) to prevent injection attacks (e.g., SQL injection, if not using Room or similar ORMs properly).
*   **Secure Network Communication:** Use HTTPS (TLS/SSL) for all network communications to encrypt data in transit. Implement certificate pinning for high-security applications.
*   **Code Obfuscation:** Use ProGuard or R8 to obfuscate your code, making it harder for reverse engineers.
*   **WebView Security:**
    *   Be cautious with `setJavaScriptEnabled(true)`. If enabled, ensure you're not loading untrusted web content or exposing sensitive native interfaces.
    *   Use `WebViewAssetLoader` to securely load local assets.
*   **Dependency Updates:** Keep your app's dependencies (libraries, SDKs) up to date to patch known vulnerabilities.
*   **Principle of Least Privilege:** Grant only the minimum necessary permissions and access rights to components and code.
*   **Regular Security Audits:** Conduct security testing and code reviews to identify and address vulnerabilities.

By following these guidelines, you can significantly improve the security posture of your Android applications and protect your users' data.
