# Memory & CPU Optimization Guide

## Memory Optimization

### Avoid Memory Leaks

Memory leaks are a common problem in Android development. They occur when an object is no longer
needed but is still referenced by another object, preventing the garbage collector from reclaiming
its memory. Over time, this can lead to an `OutOfMemoryError` and crash your app.

**Tool: LeakCanary**
LeakCanary is a memory leak detection library for Android. It automatically detects and reports
memory leaks in your app during development.

**Setup:**

1. Add the LeakCanary dependency to your `build.gradle` file:
   ```
   dependencies {
     debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.x' // Replace 2.x with the latest version
   }
   ```
2. (Optional) Customize LeakCanary's behavior in your `Application` class.

**Usage:**
LeakCanary will automatically show a notification when a memory leak is detected. Tap on the
notification to see the leak trace and identify the source of the leak.

**Common Causes of Memory Leaks:**

* **Static references to Activities or Views:** Static fields live for the entire application
  lifecycle and can hold references to destroyed Activities or Views.
* **Inner classes holding references to outer classes:** Non-static inner classes implicitly hold a
  reference to their outer class. If the inner class instance outlives the outer class instance (
  e.g., a background task), it can cause a leak.
* **Listeners and Callbacks:** Unregister listeners and callbacks in `onDestroy()` or `onStop()` to
  prevent them from holding references to destroyed components.
* **Bitmaps:** Large Bitmaps can consume a lot of memory. Ensure they are recycled when no longer
  needed using `bitmap.recycle()`. Be mindful of where you store them.

### Optimize Image Loading

Loading and displaying images efficiently is crucial for a smooth user experience and to prevent
`OutOfMemoryError`s.

**Libraries: Coil & Glide**
Coil and Glide are popular image loading libraries for Android that provide features like:

* **Caching:** Storing images in memory and/or disk to avoid re-downloading or re-processing them.
* **Resizing and Downsampling:** Loading images at the appropriate size for the display view to save
  memory.
* **Transformations:** Applying transformations like cropping, rounded corners, etc.
* **Placeholder and Error Drawables:** Showing placeholder images while loading or error images if
  loading fails.

**Coil Example:**

```
// In your ImageView
imageView.load("https://example.com/image.jpg") {
    crossfade(true)
    placeholder(R.drawable.placeholder)
    error(R.drawable.error)
    // Configure caching, transformations, etc.
    memoryCachePolicy(CachePolicy.ENABLED)
    diskCachePolicy(CachePolicy.ENABLED)
}
```

**Glide Example:**

```
Glide.with(context)
    .load("https://example.com/image.jpg")
    .placeholder(R.drawable.placeholder)
    .error(R.drawable.error)
    .diskCacheStrategy(DiskCacheStrategy.ALL) // Control caching
    .into(imageView)
```

**Best Practices for Image Loading:**

* **Use an Image Loading Library:** Don't reinvent the wheel. Libraries like Coil and Glide are
  optimized and battle-tested.
* **Load Images at the Right Size:** Don't load a 1024x1024 image into a 100x100 `ImageView`. Use
  the library's resizing capabilities.
* **Cache Images:** Utilize memory and disk caching to improve loading speed and reduce network
  usage. Configure cache sizes appropriately.
* **Release Resources:** Ensure that image loading libraries can release resources when Views are
  detached or Activities are destroyed. Most libraries handle this automatically.
* **Use Appropriate Image Formats:** Consider using WebP for smaller file sizes with good quality.
* **Optimize Your Images:** Compress images without significant quality loss before adding them to
  your app or serving them from a backend.

## CPU Optimization

High CPU usage can lead to poor performance, battery drain, and a sluggish user experience.

**Common Causes of High CPU Usage:**

* **Intensive computations on the main thread:** Performing long-running operations like network
  requests, database queries, or complex calculations on the main thread blocks UI updates.
* **Inefficient algorithms and data structures:** Choosing the wrong algorithm or data structure can
  lead to unnecessary CPU cycles.
* **Frequent or unnecessary updates:** Re-drawing views or re-calculating data too often.
* **Background tasks:** Poorly managed background tasks can consume CPU resources even when the app
  is not in the foreground.

**Tools for CPU Profiling:**

* **Android Studio CPU Profiler:** Helps you inspect your app's CPU usage in real-time and record
  method traces to identify performance bottlenecks.
* **Debug API:** Use `Debug.startMethodTracing()` and `Debug.stopMethodTracing()` to
  programmatically record method traces.
* **Systrace:** A command-line tool that collects and inspects timing information across all
  processes running on a device.

**Best Practices for CPU Optimization:**

* **Offload Work from the Main Thread:** Use Kotlin Coroutines, RxJava, `AsyncTask` (deprecated but
  still seen in older code), or `Executors` to move long-running operations to background threads.
  ```
  // Using Kotlin Coroutines
  viewModelScope.launch(Dispatchers.IO) {
      val result = performExpensiveOperation()
      withContext(Dispatchers.Main) {
          updateUi(result)
      }
  }
  ```
* **Optimize Algorithms and Data Structures:** Choose efficient algorithms and data structures for
  your specific needs. For example, use `HashMap` for fast lookups if an order isn't required,
  rather than iterating through a `List`.
* **Avoid Over-drawing:**
    * Use the **Hierarchy Viewer** and **Layout Inspector** to identify and reduce unnecessary view
      nesting.
    * Use `ClipRect` and `Canvas.save/restore` to draw only the necessary parts of custom views.
    * Enable **"Show GPU overdraw"** in developer options to visualize overdraw.
* **Optimize Layouts:**
    * Prefer `ConstraintLayout` for flat hierarchies.
    * Use `<merge>` and `<include>` tags to reduce layout nesting and reuse layouts.
    * Use `ViewStub` to lazily inflate views that are not immediately needed.
* **Efficient RecyclerView Usage:**
    * Implement `DiffUtil` for efficient updates when the underlying data changes.
    * Avoid complex calculations or view creations in `onBindViewHolder`.
    * Set `setHasFixedSize(true)` if the `RecyclerView` size doesn't change with adapter content
      changes.
* **Use Primitives Where Possible:** Prefer primitive types (e.g., `int`, `float`) over their boxed
  counterparts (e.g., `Integer`, `Float`) to avoid autoboxing overhead.
* **Batch Operations:** Group similar operations together (e.g., multiple database writes) to reduce
  overhead.
* **Optimize Background Work:** Use WorkManager for deferrable and guaranteed background tasks, and
  be mindful of how often these tasks run and the resources they consume.
* **Profile Your App:** Regularly use the Android Studio CPU Profiler to identify and address
  performance bottlenecks.

By focusing on these memory and CPU optimization techniques, you can build more performant, stable,
and battery-friendly Android applications.
