# Battery Optimization Guide

Optimizing battery life is crucial for a good user experience in Android applications. Excessive battery drain can lead to users uninstalling your app. This guide focuses on two key areas: minimizing background work and effectively using `JobScheduler` (for API levels 21+) or `WorkManager` (the recommended solution).

## 1. Minimize Background Work

Unnecessary background processing is a major source of battery drain. Here's how to minimize it:

*   **Identify and Justify Background Tasks:**
    *   Carefully evaluate if a task truly needs to run in the background.
    *   Can the task be deferred until the device is charging or connected to Wi-Fi?
    *   Can you use foreground services with a user-visible notification for tasks that need immediate user awareness?

*   **Reduce Frequency and Duration:**
    *   If a task must run in the background, make it as infrequent as possible.
    *   Optimize your background code to complete quickly.

*   **Network Requests:**
    *   Batch network requests instead of making many small ones.
    *   Use libraries like Retrofit with OkHttp that support caching and request compression.
    *   Avoid polling for updates. Use Firebase Cloud Messaging (FCM) for real-time updates when possible.

*   **Location Updates:**
    *   Request location updates only when necessary and for the shortest duration possible.
    *   Choose the appropriate location accuracy (e.g., `PRIORITY_BALANCED_POWER_ACCURACY` over `PRIORITY_HIGH_ACCURACY` unless absolutely needed).
    *   Remove location updates when they are no longer required using `fusedLocationProviderClient.removeLocationUpdates(locationCallback)`.
    *   Consider using passive location listening if your app can benefit from location updates requested by other apps.

*   **Wake Locks:**
    *   Avoid using `PowerManager.WakeLock` unless absolutely essential. They prevent the CPU from sleeping and can significantly drain the battery.
    *   If you must use a wake lock, use the minimum level necessary and release it as soon as the task is complete with `wakelock.release()`.
    *   Prefer `JobScheduler` or `WorkManager` which handle wake locks more efficiently.

*   **Background Services:**
    *   For API level 26 and higher, background service execution is restricted. Use `JobScheduler` or `WorkManager` instead for most background tasks.
    *   If you need a service that runs indefinitely and is noticeable to the user, use a foreground service.

## 2. Use JobScheduler / WorkManager Effectively

`JobScheduler` (API 21+) and `WorkManager` (part of Android Jetpack, recommended) are designed to help you perform background work in a battery-efficient way. They allow the system to batch jobs and defer them based on device conditions.

### WorkManager (Recommended)

`WorkManager` is the recommended library for deferrable background work. It's backward-compatible to API level 14 and takes into account power-saving features like Doze mode.

**Key Concepts:**

*   **Worker:** Defines the actual work to be done in the `doWork()` method.
*   **WorkRequest:** Defines how and when the work should be run.
    *   `OneTimeWorkRequest`: For tasks that run once.
    *   `PeriodicWorkRequest`: For tasks that repeat at intervals (minimum 15 minutes).
*   **Constraints:** Define the conditions under which the work should run (e.g., device charging, network available, battery not low).
*   **WorkManager (the class):** Enqueues and manages `WorkRequest`s.

**Example (Simple One-Time Work):**

```
// Define a Worker
class MyUploadWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        // Your background task logic here (e.g., upload data)
        uploadData()
        return Result.success() // or Result.failure() or Result.retry()
    }

    private fun uploadData() {
        // Simulate uploading data
        Thread.sleep(1000)
        Log.d("MyUploadWorker", "Data upload complete.")
    }
}

// Create Constraints
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.UNMETERED) // e.g., Wi-Fi
    .setRequiresCharging(true)
    .build()

// Create a WorkRequest
val uploadWorkRequest: WorkRequest =
    OneTimeWorkRequestBuilder<MyUploadWorker>()
        .setConstraints(constraints)
        .build()

// Enqueue the WorkRequest
WorkManager.getInstance(context).enqueue(uploadWorkRequest)
```

**Best Practices for WorkManager:**

*   **Use Constraints:** Always define appropriate constraints to ensure your work runs only when optimal (e.g., device charging, on Wi-Fi). This is key for battery saving.
*   **Keep Workers Lightweight:** Workers should be focused on a specific task and complete it quickly. For long-running operations, consider breaking them into smaller chunks or using a foreground service if user interaction is involved.
*   **Handle Failure and Retries:** Implement `Result.retry()` in your `Worker` if the task can be retried. `WorkManager` provides configurable backoff policies.
*   **Idempotent Workers:** Design your workers to be idempotent, meaning running them multiple times has the same effect as running them once. This helps handle retries gracefully.
*   **Unique Work:** Use `enqueueUniqueWork()` or `enqueueUniquePeriodicWork()` to prevent duplicate work requests.
*   **Observe Work Status:** Use `WorkManager.getWorkInfoByIdLiveData()` or similar methods to observe the status of your work and update UI or perform other actions accordingly.
*   **Testing:** `WorkManager` provides testing artifacts to help you test your workers.

### JobScheduler (API 21+)

If you cannot use `WorkManager`, `JobScheduler` is the native Android system service for scheduling background tasks.

**Key Concepts:**

*   **JobService:** A service that performs the scheduled task. You extend `JobService` and implement `onStartJob()` and `onStopJob()`.
*   **JobInfo:** Specifies the conditions and parameters for the job (e.g., periodicity, network requirements, charging state).
*   **JobScheduler (system service):** Schedules jobs using `JobInfo`.

**Example:**

```
// MyJobService.kt
class MyJobService : JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d("MyJobService", "Job started")
        // Perform your background task here
        // Signal completion: jobFinished(params, false) for success, true to reschedule
        jobFinished(params, false)
        return true // True if your service needs to process the work on a separate thread.
                    // False if work is completed by the time this method returns.
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d("MyJobService", "Job stopped / cancelled")
        return true // True to reschedule if the job was interrupted
    }
}

// Scheduling the job (e.g., in an Activity or BroadcastReceiver)
val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
val componentName = ComponentName(this, MyJobService::class.java)
val jobInfo = JobInfo.Builder(123, componentName) // 123 is a unique job ID
    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
    .setRequiresCharging(true)
    .setPeriodic(15 * 60 * 1000) // Minimum 15 minutes for periodic jobs
    .build()

val resultCode = jobScheduler.schedule(jobInfo)
if (resultCode == JobScheduler.RESULT_SUCCESS) {
    Log.d("Scheduler", "Job scheduled successfully!")
} else {
    Log.d("Scheduler", "Job scheduling failed.")
}
```

**Best Practices for JobScheduler:**

*   **Specify Constraints:** Use `setRequiredNetworkType()`, `setRequiresCharging()`, `setRequiresDeviceIdle()` etc., to define when your job should run.
*   **Handle `onStopJob()`:** Properly handle `onStopJob()` to clean up resources and decide if the job needs to be rescheduled.
*   **`jobFinished()`:** Call `jobFinished(params, wantsReschedule)` to tell the system whether the job completed successfully and if it needs to be rescheduled (e.g., if it failed and you want to retry).
*   **Job IDs:** Use unique and meaningful job IDs.
*   **Permissions:** Ensure you have the `RECEIVE_BOOT_COMPLETED` permission if you need to reschedule jobs after a device reboot (and handle the `BOOT_COMPLETED` broadcast).

## Summary

*   **Aggressively minimize background work.** Question every background task.
*   **Prefer `WorkManager`** for deferrable background tasks. It's more robust, feature-rich, and handles compatibility and system optimizations like Doze mode automatically.
*   Use `JobScheduler` if `WorkManager` is not an option (older projects, specific needs not covered by WorkManager).
*   **Always use constraints** (`Constraints` in WorkManager, `JobInfo` setters in JobScheduler) to ensure tasks run under battery-friendly conditions.
*   Make your background logic **efficient and quick** to execute.
*   **Test your background tasks** thoroughly on different devices and Android versions, paying close attention to battery consumption.

By following these guidelines, you can create Android applications that are respectful of the user's battery life, leading to a better overall user experience.
