# Android Data Layer Setup Guide

The Data Layer is a crucial part of an Android application's architecture. It's responsible for
managing all application data, abstracting data sources from the rest of the app (like the UI layer
and domain layer), and providing a clean API for data access. This guide explores how to set up and
manage local and remote data sources, implement caching strategies, and follow best practices.

## 1. Role of the Data Layer

* **Abstracts Data Sources:** The rest of the app doesn't need to know whether data comes from a
  local database, a remote server, or a cache.
* **Single Source of Truth (SSoT):** Often, the data layer (specifically repositories) ensures that
  different parts of the app access consistent data.
* **Handles Data Operations:** Includes fetching, storing, updating, and deleting data.
* **Manages Network Calls and Local Storage:** Implements the specifics of interacting with
  databases, file systems, and network APIs.
* **Offline Support:** Crucial for providing a good user experience by caching data and allowing the
  app to function without an internet connection.

## 2. Components of the Data Layer

Typically, the data layer consists of:

* **Repositories:** Manage multiple data sources (local, remote) and expose data to the domain or UI
  layer.
* **Data Sources:**
    * **Local Data Sources:** Handle data stored on the device (e.g., Room database, DataStore,
      SharedPreferences).
    * **Remote Data Sources:** Handle data fetched from a network (e.g., REST APIs using
      Retrofit/Ktor, GraphQL).
* **Data Models/Entities:**
    * **Network DTOs (Data Transfer Objects):** Structures that match the network API responses.
    * **Database Entities:** Structures that define the schema of your local database.
    * **Domain Models:** Clean data structures used by the rest of the app, often mapped from DTOs
      or database entities.

## 3. Local Data Sources

Storing data locally is essential for performance, offline support, and preserving user data.

### a) Room Database

Room is an abstraction layer over SQLite that allows for more robust database access while
harnessing the full power of SQLite. It's part of Android Jetpack.

* **Key Components:**
    * **`@Entity`:** Represents a table in the database. Each instance is a row.
    * **`@Dao` (Data Access Object):** Contains methods to interact with the database (queries,
      inserts, updates, deletes).
    * **`@Database`:** Represents the database holder. It defines the list of entities and DAOs.
* **Benefits:**
    * Compile-time SQL query validation.
    * Reduces boilerplate code.
    * Integrates well with `LiveData` and Kotlin `Flow` for observable queries.
* **Basic Setup:**
    1. Add Room dependencies to `build.gradle`.
    2. Define your `@Entity` classes.
    3. Create `@Dao` interfaces with your database operations.
    4. Create a class that extends `RoomDatabase` and is annotated with `@Database`.

```
// Example Entity
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String
)

// Example DAO
@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
}

// Example Database
@Database(entities = [UserEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
```

### b) Jetpack DataStore

DataStore is a modern data storage solution that aims to replace `SharedPreferences`. It provides
two implementations:

* **Preferences DataStore:** Stores data in key-value pairs, similar to `SharedPreferences`, but
  with asynchronous API using Kotlin Coroutines and Flow. It does not provide type safety.
* **Proto DataStore:** Stores data as custom data types using Protocol Buffers. This provides type
  safety and is more efficient.

* **Benefits:**
    * Asynchronous API (prevents UI jank).
    * Uses Kotlin `Flow` for data emission.
    * Handles data migration from `SharedPreferences`.
    * Type safety with Proto DataStore.
* **Usage (Preferences DataStore):**

```
// 1. Create a DataStore instance
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// 2. Define keys
val EXAMPLE_COUNTER = intPreferencesKey("example_counter")

// 3. Write data
suspend fun incrementCounter(context: Context) {
    context.dataStore.edit { settings ->
        val currentCounterValue = settings[EXAMPLE_COUNTER] ?: 0
        settings[EXAMPLE_COUNTER] = currentCounterValue + 1
    }
}

// 4. Read data
fun getCounterFlow(context: Context): Flow<Int> {
    return context.dataStore.data
        .map { preferences ->
            preferences[EXAMPLE_COUNTER] ?: 0
        }
}
```

### c) SharedPreferences

`SharedPreferences` is suitable for storing small amounts of primitive data in key-value pairs.

* **When to Use:**
    * Simple user preferences (e.g., app settings, flags).
    * Small, unstructured data.
* **Limitations:**
    * Synchronous API (can block the UI thread if not handled carefully).
    * Not type-safe.
    * No built-in error handling for parsing.
    * Transactionality issues.
* **Recommendation:** For new development, prefer Jetpack DataStore.

## 4. Remote Data Sources

Fetching data from a network is a common requirement for most apps.

### a) Retrofit

Retrofit is a type-safe HTTP client for Android and Java by Square. It makes it easy to consume JSON
or XML (or other) web services.

* **Key Features:**
    * Turns HTTP API into a Kotlin/Java interface.
    * Uses annotations to describe HTTP requests (e.g., `@GET`, `@POST`, `@Path`, `@Query`).
    * Supports various converters (e.g., Gson, Moshi, Jackson) for serialization/deserialization.
    * Integrates with coroutines (`suspend` functions) and RxJava.
* **Basic Setup:**
    1. Add Retrofit and a converter (e.g., `retrofit2:converter-moshi`) dependencies.
    2. Define an interface for your API endpoints.
    3. Build a Retrofit instance.

```
// Example API Interface
interface ApiService {
    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): UserDto // UserDto is a data class for network response
}

// Example Retrofit Instance
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .addConverterFactory(MoshiConverterFactory.create())
    .build()

val apiService = retrofit.create(ApiService::class.java)
```

### b) Ktor Client

Ktor is a framework built by JetBrains for creating asynchronous servers and clients in Kotlin. The
Ktor HTTP client is a good alternative to Retrofit, especially in Kotlin-first projects.

* **Key Features:**
    * Kotlin-first design with extensive coroutine support.
    * Multiplatform (can be used in Android, iOS, JVM, JS).
    * Extensible with features like authentication, serialization, logging.
* **Basic Setup:**
    1. Add Ktor client dependencies (e.g., `ktor-client-core`, `ktor-client-cio` for engine,
       `ktor-client-content-negotiation`, `ktor-serialization-kotlinx-json`).
    2. Create an `HttpClient` instance.

```
// Example Ktor Client
val client = HttpClient(CIO) { // CIO is one of the available engines
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    // Other configurations like defaultRequest, logging, etc.
}

// Example GET request
suspend fun fetchUser(userId: String): UserDto { // UserDto for network response
    return client.get("https://api.example.com/users/$userId").body()
}
```

### c) GraphQL

For GraphQL APIs, you can:

* Use libraries like **Apollo Android**, which is a type-safe GraphQL client.
* Use Retrofit or Ktor to make POST requests to the GraphQL endpoint, managing the query body
  manually or with a lightweight helper.

## 5. Repository Pattern

The Repository pattern is a key architectural component in the Data Layer. It abstracts the data
sources from the rest of the application.

* **Responsibilities:**
    * Provides a clean API for data access to the Domain layer or ViewModels.
    * Manages which data source to use (e.g., fetch from network or local cache).
    * Can implement caching logic (deciding when to refresh data from the network).
    * Acts as a Single Source of Truth for a particular type of data.
* **Example:**

```
class UserRepository(
    private val remoteDataSource: UserRemoteDataSource, // e.g., using ApiService (Retrofit/Ktor)
    private val localDataSource: UserDao // e.g., Room DAO
) {
    fun getUser(userId: String): Flow<Resource<User>> { // User is a domain model
        return networkBoundResource(
            query = {
                // Read from local database (DAO) and map to domain model
                localDataSource.getUserById(userId).map { entity -> entity?.toDomainModel() }
            },
            fetch = {
                // Fetch from remote data source (API)
                remoteDataSource.fetchUser(userId)
            },
            saveFetchResult = { networkUserDto ->
                // Save network response to local database after mapping to entity
                localDataSource.insertUser(networkUserDto.toUserEntity())
            },
            shouldFetch = { localUser ->
                // Logic to decide if network fetch is needed (e.g., data is stale or not present)
                localUser == null // Simple example: fetch if not in DB
            }
        ).map { resource -> /* Map Resource<UserEntity> to Resource<User> if needed */ }
    }
}

// Resource class can be a sealed class to represent loading, success, error states
sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}

// networkBoundResource is a common utility function for this pattern.
```

## 6. Offline-First with Caching Strategies

An offline-first approach ensures that the app remains functional even without a stable internet
connection.

* **Core Idea:** The app primarily interacts with a local cache (e.g., Room database). This cache is
  the SSoT for UI display.
* **Data Synchronization:**
    * **Fetch data from network:** When needed (e.g., on app start, user pull-to-refresh, or data is
      stale).
    * **Save to local cache:** Always save fresh data from the network to the local cache.
    * **Display from local cache:** UI observes data from the local cache (e.g., Room `Flow`).
* **Caching Strategies:**
    * **Cache-Aside:** The application code first checks the cache. If data is present (cache hit),
      it's returned. If not (cache miss), the application fetches data from the data store (e.g.,
      network), stores it in the cache, and then returns it.
    * **Read-Through:** Similar to cache-aside, but the cache itself is responsible for fetching
      data from the data store if it's a cache miss. The application always talks to the cache.
    * **Write-Through:** Data is written to the cache and the underlying data store simultaneously.
      Ensures consistency but can have higher latency for write operations.
    * **Write-Behind (Write-Back):** Data is written to the cache, and the cache asynchronously
      writes it to the data store. Improves write performance but has a risk of data loss if the
      cache fails before writing to the store.
    * **Network-Bound Resource:** A common pattern in Android (shown in the `UserRepository`example)
      that combines fetching from network and saving/retrieving from a local cache.

## 7. Data Mapping

It's crucial to map data between different layers:

* **Network DTOs (Data Transfer Objects)** `->` **Domain Models:** When data comes from the network.
* **Database Entities** `->` **Domain Models:** When data is read from the local database.
* **Domain Models** `->` **Database Entities:** When data needs to be saved to the local database.

This separation ensures that:

* The network layer can change without affecting the domain layer.
* The database schema can change without affecting the domain layer.
* The domain layer works with clean, business-specific models.

## 8. Error Handling

* **Network Errors:** Handle exceptions from Retrofit/Ktor (e.g., `IOException`, `HttpException`).
  Expose these as specific error states (e.g., using a `Resource.Error` wrapper) to the upper
  layers.
* **Database Errors:** Handle `SQLiteException` and other database-related errors.
* **Repositories:** Should catch errors from data sources and transform them into a consistent error
  representation for the domain or UI layer.

## 9. Testing the Data Layer

* **DAOs (Room):** Use an in-memory Room database (`Room.inMemoryDatabaseBuilder()`) for testing DAO
  queries.
* **Remote Data Sources:**
    * Use mock web servers (e.g., MockWebServer by OkHttp) to test network interactions without
      hitting a real backend.
    * Verify request formation and parsing of mock responses.
* **Repositories:**
    * Use fakes or mocks for local and remote data sources.
    * Verify that the repository correctly interacts with its data sources and implements the
      caching/SSoT logic.

## Conclusion

A well-structured Data Layer is fundamental for building scalable, maintainable, and robust Android
applications. By carefully choosing your local and remote data sources, implementing a solid
repository pattern, and considering offline capabilities, you can create a reliable foundation for
your app's data management. Remember to prioritize clear separation of concerns, data mapping, and
comprehensive testing.
