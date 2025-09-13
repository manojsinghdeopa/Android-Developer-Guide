# Concurrency with Kotlin Coroutines and Flow

## 1. Introduction to Concurrency

Concurrency is the ability of a system to perform multiple tasks or processes seemingly
simultaneously. In Android development, effective concurrency management is crucial for creating
responsive and smooth user experiences. Without it, long-running operations like network requests,
database access, or complex computations can block the main thread, leading to Application Not
Responding (ANR) errors and a poor user experience.

## 2. Traditional Concurrency Challenges

Historically, managing concurrency in Android involved:

* **AsyncTask:** While simpler for some use cases, it suffered from context leaks and complexity
  with configuration changes.
* **Threads and Handlers:** Manually managing threads is error-prone, leading to complex
  synchronization logic, potential race conditions, and deadlocks.
* **Callbacks (Callback Hell):** Deeply nested callbacks for sequential asynchronous operations made
  code hard to read and maintain.
* **Third-party libraries (e.g., RxJava):** Powerful but can have a steep learning curve and add
  overhead.

## 3. Introduction to Kotlin Coroutines

Kotlin Coroutines provide a modern, powerful, and simpler way to manage asynchronous operations and
concurrency. They allow you to write asynchronous code in a sequential manner, making it more
readable and maintainable.

### What are Coroutines?

Coroutines can be thought of as lightweight, cooperatively multitasked "threads." They execute on a
pool of actual threads but can be suspended and resumed without blocking the underlying thread. This
makes them highly efficient for I/O-bound operations and allows for a massive number of coroutines
to run concurrently.

### Benefits of Coroutines

* **Structured Concurrency:** Coroutines are typically launched within a `CoroutineScope`, which
  manages their lifecycle. If a scope is cancelled, all coroutines within it are automatically
  cancelled, preventing leaks and simplifying resource management.
* **Readability:** Asynchronous code looks like synchronous code, eliminating callback hell.
* **Reduced Boilerplate:** Less code is needed compared to traditional threading or callback-based
  approaches.
* **Integration with Jetpack:** Widely adopted in Jetpack libraries (e.g., ViewModel, Lifecycle,
  Room, DataStore).

### Core Components

* **`suspend` functions:** These are special functions that can be paused (suspended) and resumed
  later. They can only be called from other `suspend` functions or within a coroutine.
  ```kotlin
  suspend fun fetchData(): String {
      delay(1000L) // Simulate network delay (suspending function)
      return "Data fetched"
  }
  ```
* **Coroutine Builders:** These functions are used to launch new coroutines.
    * `launch`: Starts a new coroutine that "fires and forgets" – it doesn't return a result to the
      caller. Used for tasks like updating UI or making a simple network call without expecting an
      immediate result.
      ```kotlin
      viewModelScope.launch {
          val data = fetchData()
          // Update UI with data
      }
      ```
    * `async`: Starts a new coroutine that computes a result and returns a `Deferred<T>`. You can
      then call `await()` on the `Deferred` object to get the result (suspending until the result is
      available). Used when you need to perform an asynchronous operation and get its result back.
      ```kotlin
      viewModelScope.launch {
          val deferredData: Deferred<String> = async {
              fetchDataFromServer()
          }
          val result = deferredData.await() // Suspend until data is ready
          // Use the result
      }
      ```
    * `runBlocking`: A coroutine builder that blocks the current thread until the coroutine
      completes. It's mainly used in main functions and tests, not typically in Android UI code.

* **Dispatchers:** Determine the thread or thread pool the coroutine will run on.
    * `Dispatchers.Main`: Runs coroutines on the Android main UI thread. Use this for UI updates.
    * `Dispatchers.IO`: Optimized for I/O-bound tasks like network requests, disk operations (
      reading/writing files, database access).
    * `Dispatchers.Default`: Optimized for CPU-intensive tasks like sorting large lists or
      performing complex calculations.
    * `Dispatchers.Unconfined`: Runs the coroutine on the current thread but resumes on whatever
      thread the suspending function used. Use with caution.

  You can switch dispatchers using `withContext`:
  ```kotlin
  suspend fun processData(): String {
      val rawData = withContext(Dispatchers.IO) {
          // Perform network or disk operation
          fetchDataFromServer()
      }
      val processedData = withContext(Dispatchers.Default) {
          // Perform CPU-intensive work
          heavyComputation(rawData)
      }
      return processedData
  }
  ```

* **`CoroutineScope`:** Defines the lifecycle of coroutines. When a scope is cancelled, all
  coroutines launched within it are also cancelled.
    * `viewModelScope`: Predefined scope in Android ViewModels. Coroutines launched in this scope
      are automatically cancelled when the ViewModel is cleared.
    * `lifecycleScope`: Predefined scope tied to a Lifecycle (e.g., Activity, Fragment). Coroutines
      are cancelled when the Lifecycle is destroyed.
    * Custom scopes: You can create your own scopes for more fine-grained control.

## 4. Kotlin Flow

Kotlin Flow is a type built on top of coroutines for representing an **asynchronous stream of data
**. It emits multiple values sequentially over time, as opposed to `suspend` functions that return a
single value.

### What is Flow?

A Flow is conceptually a stream of data that can be computed asynchronously. Values are emitted from
the flow, and a collector (terminal operator) consumes these values. Flows are **cold streams**,
meaning the code inside a flow builder doesn't run until a terminal operator (like `collect`) is
called on the flow.

### Why use Flow?

* **Asynchronous sequence:** Naturally represents data that arrives over time (e.g., location
  updates, database changes, user input events).
* **Backpressure support:** Handles situations where an emitter produces data faster than a
  collector can consume it.
* **Structured concurrency:** Integrates seamlessly with coroutines and their cancellation
  mechanism.
* **Rich set of operators:** Provides numerous operators for transforming, filtering, and combining
  flows, similar to RxJava.

### Creating Flows

* **`flow {}` builder:** The most common way to create a flow.
  ```kotlin
  fun countNumbers(limit: Int): Flow<Int> = flow {
      for (i in 1..limit) {
          delay(100L) // Simulate some work
          emit(i)     // Emit the next value
      }
  }
  ```
* **`.asFlow()` extension functions:** Convert various types (e.g., `List`, `Iterable`, `Array`,
  `Sequence`) into flows.
  ```kotlin
  val numberFlow = (1..5).asFlow()
  ```
* **`flowOf(...)`:** Creates a flow that emits a fixed set of values.
  ```kotlin
  val fixedFlow = flowOf("A", "B", "C")
  ```

### Collecting Flows

To get the values from a flow, you use a **terminal operator**. The most common one is `collect`.
`collect` is a suspending function, so it must be called from a coroutine or another `suspend`
function.

```kotlin
viewModelScope.launch {
    countNumbers(5).collect { number ->
        Log.d("FlowExample", "Collected: $number")
    }
}
```

### Flow Operators

Flow provides a rich set of operators to transform and manipulate data streams. These operators are
intermediate, meaning they return a new Flow and don't trigger the flow execution themselves.

* **Transformation Operators:**
    * `map`: Transforms each emitted value.
      ```kotlin
      usersFlow.map { user -> user.name.uppercase() }
      ```
    * `transform`: More generic transformation, can emit multiple values or skip some.
* **Filtering Operators:**
    * `filter`: Emits only values that satisfy a predicate.
      ```kotlin
      numberFlow.filter { it % 2 == 0 } // Emit only even numbers
      ```
    * `take`: Takes only the first N emissions.
* **Combining Operators:**
    * `zip`: Combines emissions from two flows. Emits when both flows have emitted a new value.
      ```kotlin
      val flowA = flowOf(1, 2)
      val flowB = flowOf("A", "B", "C")
      flowA.zip(flowB) { num, char -> "$num$char" } // Emits "1A", "2B"
      ```
    * `combine`: Combines the latest values from multiple flows. Emits whenever any of the source
      flows emit a new value (once all have emitted at least one).
      ```kotlin
      flowA.combine(flowB) { num, char -> "$num$char" }
      // Example emissions: (if A emits 1, B emits 'X') -> "1X"
      // (if A then emits 2) -> "2X"
      // (if B then emits 'Y') -> "2Y"
      ```
* **Flattening Operators:** For flows that emit other flows (e.g., `Flow<Flow<T>>`).
    * `flatMapConcat`: Concatenates the inner flows sequentially.
    * `flatMapMerge`: Merges inner flows concurrently.
    * `flatMapLatest`: Collects from the latest inner flow, cancelling the previous one.
* **Context and Dispatchers:**
    * `flowOn(Dispatcher)`: Changes the dispatcher for the upstream operations (the flow builder and
      preceding operators).
      ```kotlin
      fun dataFromNetwork(): Flow<String> = flow {
          // This runs on Dispatchers.IO
          emit(api.fetchData())
      }.flowOn(Dispatchers.IO)
      ```

### Error Handling

* **`catch` operator:** Catches exceptions from the upstream flow.
  ```kotlin
  dataFromNetwork()
      .catch { e ->
          Log.e("FlowError", "Error fetching data: ${e.message}")
          emit("Default Data on Error") // Optionally emit a fallback value
      }
      .collect { data ->
          // process data
      }
  ```
* Imperative `try-catch` within the `collect` block (handles exceptions in the collector or terminal
  operators).

### Buffering

Flows are sequential by default. Operators and the collector run on the same coroutine. Buffering
operators can change this behavior.

* `buffer()`: Runs the flow emitter in a separate coroutine, allowing the emitter to produce values
  while the collector is processing.
* `conflate()`: If the collector is slow, `conflate` drops intermediate values and only processes
  the latest one.
* `collectLatest()`: If a new value is emitted while the previous one is still being processed by
  the collector's block, the processing of the old value is cancelled, and the new value is
  processed.

## 5. Coroutines + Flow in Android

### ViewModelScope and LifecycleScope

* **`viewModelScope`:** The recommended scope for launching coroutines related to `ViewModel` logic.
  Automatically cancelled when the `ViewModel` is cleared.
* **`lifecycleScope`:** Tied to an Activity/Fragment lifecycle. Use
  `lifecycleScope.launchWhenStarted` or `lifecycleScope.launchWhenResumed` to execute code only when
  the lifecycle is in a specific state.

### Using Flow with LiveData, StateFlow, and SharedFlow

* **`LiveData`:**
    * `liveData` builder: A coroutine builder that produces `LiveData`.
  ```kotlin
  val userData: LiveData<User> = liveData(Dispatchers.IO) {
      emit(repository.fetchUser())
  }
  ```
    * `.asLiveData()`: Converts a Flow to LiveData.
* **`StateFlow`:** A hot flow that holds a single, updatable value (state). Ideal for representing
  UI state.
  ```kotlin
  // In ViewModel
  private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  fun fetchData() {
      viewModelScope.launch {
          _uiState.value = UiState.Loading
          try {
              val data = repository.getData().first() // Example: get first emission
              _uiState.value = UiState.Success(data)
          } catch (e: Exception) {
              _uiState.value = UiState.Error(e.message ?: "Unknown error")
          }
      }
  }
  ```
  In your Activity/Fragment, collect `StateFlow` using `lifecycleScope` and `collectLatest` or
  `repeatOnLifecycle`:
  ```kotlin
  // In Fragment's onViewCreated
  viewLifecycleOwner.lifecycleScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
          viewModel.uiState.collect { state ->
              // Update UI based on state
          }
      }
  }
  ```
* **`SharedFlow`:** A hot flow for broadcasting values to multiple collectors. Useful for one-time
  events.
  ```kotlin
  // In ViewModel
  private val _events = MutableSharedFlow<Event>()
  val events: SharedFlow<Event> = _events.asSharedFlow()

  fun triggerEvent() {
      viewModelScope.launch {
          _events.emit(Event.ShowSnackbar("Action performed!"))
      }
  }
  ```

### Network Calls and Database Operations

Coroutines and Flow are excellent for these tasks.

```kotlin
// Repository
class UserRepository(private val userDao: UserDao, private val apiService: ApiService) {
    fun getUserData(userId: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            // Try fetching from network first
            val networkUser = apiService.fetchUser(userId) // suspend function
            userDao.insert(networkUser.toEntity()) // suspend function
            emit(
                Resource.Success(
                    userDao.getUser(userId).first().toDomainModel()
                )
            ) // Assuming userDao.getUser returns Flow
        } catch (e: Exception) {
            // Network failed, try fetching from DB
            val localUser = userDao.getUser(userId).firstOrNull()?.toDomainModel()
            if (localUser != null) {
                emit(Resource.Success(localUser))
            } else {
                emit(Resource.Error("User not found and network request failed: ${e.message}"))
            }
        }
    }.flowOn(Dispatchers.IO) // Ensure all operations run on IO dispatcher
}
```

### Best Practices

* **Structured Concurrency:** Always launch coroutines in an appropriate `CoroutineScope` (e.g.,
  `viewModelScope`, `lifecycleScope`) to ensure proper cancellation.
* **Cancellation:** Coroutines are cancellable. Ensure your `suspend` functions are cooperative with
  cancellation by periodically checking `isActive` or using cancellable suspending functions from
  `kotlinx.coroutines` (like `delay`, `yield`).
* **Dispatcher Usage:**
    * Use `Dispatchers.Main` for UI updates.
    * Use `Dispatchers.IO` for network/disk operations.
    * Use `Dispatchers.Default` for CPU-intensive work.
    * Avoid blocking `Dispatchers.Main`.
    * Use `withContext` to switch dispatchers for specific blocks of code.
* **Expose `StateFlow` or `SharedFlow` from ViewModels:** For UI state and events, instead of
  exposing `MutableStateFlow` or `MutableSharedFlow`.
* **Flows are Cold:** Remember that flow builders (`flow {}`) are cold and only execute when a
  terminal operator is applied.
* **Error Handling:** Use `catch` operator in Flows or `try-catch` in coroutines to handle
  exceptions gracefully.
* **Testing:** `kotlinx-coroutines-test` library provides utilities like `TestCoroutineDispatcher`,
  `runBlockingTest` (deprecated in favor of `runTest`) for testing coroutines and flows.

## 6. Examples

### Example 1: Simple Background Task with `launch`

```kotlin
// In an Activity or Fragment
lifecycleScope.launch {
    Log.d("CoroutineExample", "Starting background task...")
    withContext(Dispatchers.IO) {
        // Simulate a long-running task
        delay(2000)
    }
    Log.d("CoroutineExample", "Background task finished.")
    // Update UI if needed (already on Main dispatcher if launched from lifecycleScope without changing context)
    // textView.text = "Task Completed" (if lifecycleScope uses Dispatchers.Main.immediate)
}
```

If `lifecycleScope` isn't configured to use `Dispatchers.Main.immediate` by default for the launch,
ensure UI updates are wrapped in `withContext(Dispatchers.Main)`. Most commonly,
`lifecycleScope.launch` uses `Dispatchers.Main.immediate`.

### Example 2: Fetching Data with `async` and `await`

```kotlin
// In a ViewModel
fun fetchTwoThings() {
    viewModelScope.launch {
        val deferredResult1 = async(Dispatchers.IO) { apiService.fetchDataPart1() }
        val deferredResult2 = async(Dispatchers.IO) { apiService.fetchDataPart2() }

        try {
            val result1 = deferredResult1.await()
            val result2 = deferredResult2.await()
            // Combine results and update UI state
            _uiState.value = UiState.Success(result1 + result2)
        } catch (e: Exception) {
            _uiState.value = UiState.Error("Failed to fetch data: ${e.message}")
        }
    }
}
```

### Example 3: Using Flow to Observe Database Changes (Room example)

```kotlin
// In DAO (Data Access Object) with Room
@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY name ASC")
    fun getAllItems(): Flow<List<Item>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item)
}

// In ViewModel
valallItems: StateFlow<List<Item>> = itemDao.getAllItems()
.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000), // Keep collecting for 5s after last subscriber
    initialValue = emptyList()
)

fun addItem(name: String) {
    viewModelScope.launch(Dispatchers.IO) {
        itemDao.insertItem(Item(name = name))
    }
}
```

### Example 4: Combining Multiple Flow Sources

```kotlin
// In ViewModel
val queryFlow = MutableStateFlow("")
val sortOrderFlow = MutableStateFlow(SortOrder.ASCENDING)

@OptIn(ExperimentalCoroutinesApi::class)
val searchResults: Flow<List<SearchResult>> = queryFlow
    .debounce(300) // Wait for 300ms of no new emissions before processing
    .combine(sortOrderFlow) { query, sortOrder ->
        Pair(query, sortOrder)
    }
    .flatMapLatest { (query, sortOrder) ->
        if (query.isBlank()) {
            flowOf(emptyList())
        } else {
            repository.searchItems(query, sortOrder) // This returns Flow<List<SearchResult>>
        }
    }
    .catch { e ->
        Log.e("Search", "Error in search results flow: $e")
        emit(emptyList()) // Emit empty list on error
    }
```

## 7. Conclusion

Kotlin Coroutines and Flow provide a powerful, expressive, and efficient way to handle concurrency
and asynchronous data streams in Android applications. By embracing structured concurrency,
simplifying complex asynchronous logic, and offering a rich set of operators, they significantly
improve code readability, maintainability, and overall developer productivity. Adopting them is a
key step towards building modern, responsive, and robust Android apps.
