# Dependency Injection in Android: Hilt and Dagger Guide

Dependency Injection (DI) is a design pattern that promotes loose coupling and testability in
software applications. In Android development, DI frameworks like Dagger and Hilt are widely used to
manage dependencies effectively. This guide provides a comprehensive overview of DI, Dagger, and
Hilt, helping you build scalable and maintainable Android apps.

## 1. What is Dependency Injection?

Dependency Injection means that an object does not create its own dependencies (other objects it
relies on to function). Instead, these dependencies are provided or "injected" into the object from
an external source.

**Core Idea:** "Don't call us, we'll call you." (Hollywood Principle applied to object creation).

**Example without DI:**

```kotlin
class Car {
    private val engine = Engine() // Car creates its own Engine dependency

    fun start() {
        engine.start()
    }
}
```

**Example with Manual DI (Constructor Injection):**

```kotlin
class Car(private val engine: Engine) { // Engine is passed in (injected)
    fun start() {
        engine.start()
    }
}

// Usage:
val engine = RealEngine()
val car = Car(engine) // Dependency is provided externally
```

### Why Use Dependency Injection?

* **Decoupling:** Objects are not responsible for creating their dependencies. This reduces coupling
  between classes. Changes to how a dependency is created don't affect the class that uses it.
* **Testability:** Dependencies can be easily replaced with mock or fake implementations during
  testing. This allows for isolated unit tests.
* **Reusability:** Loosely coupled components are often more reusable in different parts of the
  application or even in other projects.
* **Readability and Maintainability:** Clearer separation of concerns makes code easier to
  understand, manage, and scale. DI frameworks can centralize dependency management.
* **Scalability:** As applications grow, managing dependencies manually becomes cumbersome. DI
  frameworks automate this process.

### Types of Dependency Injection:

* **Constructor Injection:** Dependencies are provided through the class constructor. This is the
  most common and recommended type.
* **Field Injection (or Setter Injection):** Dependencies are provided through public setters or
  directly into public fields after the object is constructed. Often used in Android framework
  classes where you don't control instantiation (e.g., Activities, Fragments).
* **Method Injection:** Dependencies are passed as parameters to a specific method that requires
  them.

## 2. Dagger 2

Dagger 2 is a popular, fully static, compile-time dependency injection framework for Java, Kotlin,
and Android. It's maintained by Google and is a robust solution for managing complex dependency
graphs.

### Core Dagger 2 Annotations and Concepts:

* **`@Inject`**:
    * On a constructor: Tells Dagger how to create instances of that class.
    * On a field: Requests Dagger to inject an instance of that type into the field.
    * On a method: Dagger will call this method after construction (rarely used now).
* **`@Module`**: Classes that provide dependencies that cannot be constructor-injected. This
  includes:
    * Interfaces (Dagger doesn't know which implementation to use).
    * Third-party classes (you don't own the code).
    * Objects that require runtime configuration (e.g., building an OkHttpClient with specific
      interceptors).
* **`@Provides`**: Methods within a `@Module` that tell Dagger how to create and provide instances
  of a dependency.
* **`@Component`**: An interface (or abstract class) that acts as the injector. It connects
  modules (providers of dependencies) with the objects requesting dependencies.
    * It defines which dependencies it can provide (by exposing provision methods) and where it can
      inject them.
    * Dagger generates an implementation of this interface (e.g., `DaggerMyComponent`).
* **`@Singleton` and Scopes (`@Scope`)**: Annotations used to control the lifecycle of provided
  instances.
    * `@Singleton`: Ensures only one instance of a dependency is created and reused within the scope
      of the component.
    * Custom scopes can be defined for finer-grained lifecycle management (e.g., `@ActivityScope`,
      `@UserScope`).

### Basic Dagger 2 Setup:

1. **Add Dependencies:**
   ```gradle
   // In build.gradle (Module)
   dependencies {
       implementation 'com.google.dagger:dagger:2.x'
       kapt 'com.google.dagger:dagger-compiler:2.x' // Or annotationProcessor for Java
   }
   ```
2. **Create a Module:**
   ```kotlin
   @Module
   class AppModule(private val applicationContext: Context) {
       @Provides
       @Singleton
       fun provideApplicationContext(): Context = applicationContext

       @Provides
       @Singleton
       fun provideSharedPreferences(context: Context): SharedPreferences {
           return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
       }
   }
   ```
3. **Create a Component:**
   ```kotlin
   @Singleton
   @Component(modules = [AppModule::class])
   interface AppComponent {
       // Expose dependencies to be used by other components or for explicit retrieval
       fun applicationContext(): Context
       fun sharedPreferences(): SharedPreferences

       // Inject dependencies into a target class
       fun inject(mainActivity: MainActivity)
   }
   ```
4. **Initialize the Component:** Typically in your `Application` class.
   ```kotlin
   class MyApplication : Application() {
       lateinit var appComponent: AppComponent

       override fun onCreate() {
           super.onCreate()
           appComponent = DaggerAppComponent.builder()
               .appModule(AppModule(applicationContext))
               .build()
       }
   }
   ```
5. **Inject Dependencies:**
   ```kotlin
   class MainActivity : AppCompatActivity() {
       @Inject lateinit var sharedPreferences: SharedPreferences

       override fun onCreate(savedInstanceState: Bundle?) {
           super.onCreate(savedInstanceState)
           (application as MyApplication).appComponent.inject(this) // Perform injection
           // Now sharedPreferences can be used
       }
   }
   ```

### Pros of Dagger 2:

* **Compile-time safety:** Dependency graph validation happens at compile time, catching errors
  early.
* **Performance:** No reflection involved at runtime, leading to better performance compared to
  runtime DI frameworks.
* **Power and Flexibility:** Highly configurable for complex scenarios.
* **Well-established:** Large community and plenty of resources.

### Cons of Dagger 2:

* **Steep learning curve:** Can be complex to understand and set up initially.
* **Boilerplate code:** Requires writing modules, components, and manual injection calls for Android
  framework classes.
* **Build times:** Annotation processing can increase build times, especially in large projects.

## 3. Hilt

Hilt is Jetpack's recommended dependency injection solution for Android. It's built on top of Dagger
and aims to simplify Dagger usage in Android applications by providing a standard way to use DI and
reducing boilerplate.

### Why Hilt?

* **Simplified Dagger:** Reduces the amount of boilerplate code needed for Dagger in Android.
* **Standardized Components:** Provides predefined components and scopes that integrate with Android
  framework classes.
* **Easy Setup:** Less configuration required compared to Dagger.
* **Jetpack Integration:** Works seamlessly with other Jetpack libraries like ViewModel, Navigation,
  and WorkManager.
* **Android-Specific:** Designed specifically for Android development challenges.

### Core Hilt Annotations and Concepts:

* **`@HiltAndroidApp`**: Annotate your `Application` class. This triggers Hilt's code generation,
  including a base class for your application that supports DI.
* **`@AndroidEntryPoint`**: Annotate Android framework classes (Activities, Fragments, Views,
  Services, BroadcastReceivers) to make them DI containers. Hilt will automatically generate the
  necessary Dagger components for these classes.
* **`@Inject`**: Same as in Dagger. Used for constructor injection or field injection (for
  dependencies provided by Hilt).
* **`@Module`**: Same as in Dagger. Used to provide instances of types that cannot be
  constructor-injected.
* **`@InstallIn(Component::class)`**: Annotate Hilt modules to specify which Hilt-generated
  component they belong to. This determines the scope and lifecycle of the bindings in that module.
    * **Standard Hilt Components:**
        * `SingletonComponent`: For application-level singletons.
        * `ActivityRetainedComponent`: For dependencies that should survive configuration changes (
          used by `@HiltViewModel`).
        * `ViewModelComponent`: For dependencies specific to a ViewModel.
        * `ActivityComponent`: For dependencies scoped to an Activity.
        * `FragmentComponent`: For dependencies scoped to a Fragment.
        * `ViewComponent`: For dependencies scoped to a View.
        * `ViewWithFragmentComponent`: For dependencies scoped to a View annotated with
          `@WithFragmentBindings`.
        * `ServiceComponent`: For dependencies scoped to a Service.
* **`@Provides`**: Same as in Dagger. Used within Hilt modules to tell Hilt how to provide
  instances.
* **`@Binds`**: Used within Hilt modules to tell Hilt which implementation to use for an interface.
  More efficient than `@Provides` for this purpose as it doesn't generate a factory method.
  ```kotlin
  @Module
  @InstallIn(SingletonComponent::class)
  abstract class AnalyticsModule {
      @Binds
      abstract fun bindAnalyticsService(
          impl: FirebaseAnalyticsService
      ): AnalyticsService // AnalyticsService is an interface, FirebaseAnalyticsService is its impl
  }
  ```
* **`@HiltViewModel`** (previously `@ViewModelInject`): Annotate `ViewModel` classes to make them
  injectable with Hilt. Hilt will create a `ViewModelProvider.Factory` for you.
* **`@EntryPoint`**: Used to access dependencies from classes not directly supported by Hilt (e.g.,
  content providers, or when you need to get dependencies from a component an `@AndroidEntryPoint`
  doesn't give you access to).
* **`@DefineComponent` / `@DefineComponent.Builder`**: For creating custom Hilt components (less
  common, for advanced use cases).

### Basic Hilt Setup:

1. **Add Dependencies:**
   ```gradle
   // In build.gradle (Project)
   plugins {
       id 'com.google.dagger.hilt.android' version '2.x' apply false // Hilt Gradle plugin
   }

   // In build.gradle (Module)
   plugins {
       id 'kotlin-kapt'
       id 'com.google.dagger.hilt.android'
   }

   android {
       // ...
       compileOptions {
           sourceCompatibility JavaVersion.VERSION_1_8
           targetCompatibility JavaVersion.VERSION_1_8
       }
   }

   dependencies {
       implementation "com.google.dagger:hilt-android:2.x"
       kapt "com.google.dagger:hilt-compiler:2.x"

       // For @HiltViewModel
       implementation "androidx.hilt:hilt-lifecycle-viewmodel:1.x.x" // Check latest version
       kapt "androidx.hilt:hilt-compiler:1.x.x" // Check latest version
   }

   // Allow references to generated code
   kapt {
       correctErrorTypes true
   }
   ```
2. **Annotate Application Class:**
   ```kotlin
   @HiltAndroidApp
   class MyApplication : Application() {
       // No need to create component manually
   }
   ```
   And register it in `AndroidManifest.xml`: `<application android:name=".MyApplication" ...>`
3. **Create a Module (Example):**
   ```kotlin
   @Module
   @InstallIn(SingletonComponent::class) // Scoped to application lifecycle
   object NetworkModule {
       @Provides
       @Singleton // Ensure only one instance of Retrofit
       fun provideRetrofit(): Retrofit {
           return Retrofit.Builder()
               .baseUrl("https://api.example.com/")
               .addConverterFactory(MoshiConverterFactory.create())
               .build()
       }

       @Provides
       fun provideApiService(retrofit: Retrofit): ApiService {
           return retrofit.create(ApiService::class.java)
       }
   }
   ```
4. **Inject into Android Classes:**
   ```kotlin
   @AndroidEntryPoint // Mark Activity for injection
   class MainActivity : AppCompatActivity() {

       @Inject lateinit var apiService: ApiService // Field injection

       override fun onCreate(savedInstanceState: Bundle?) {
           super.onCreate(savedInstanceState)
           // No manual injection call needed like (application as MyApplication).appComponent.inject(this)
           // Hilt handles it automatically.
           // apiService is ready to use here.
       }
   }
   ```
5. **Inject ViewModels:**
   ```kotlin
   @HiltViewModel
   class MyViewModel @Inject constructor(
       private val userRepository: UserRepository
   ) : ViewModel() {
       // ...
   }

   // In Activity/Fragment:
   @AndroidEntryPoint
   class MyActivity : AppCompatActivity() {
       private val viewModel: MyViewModel by viewModels() // Hilt provides the factory
   }
   ```

### Benefits of Hilt:

* **Reduced Boilerplate:** Significantly less setup and boilerplate compared to Dagger.
* **Standardization:** Provides a standard set of components and scopes tailored for Android.
* **Ease of Use:** Lower learning curve than raw Dagger.
* **Improved Build Times (potentially):** By being more constrained, Hilt can sometimes offer better
  build performance through more targeted component generation.
* **Testability:** Excellent support for testing with easy dependency replacement.

## 4. Dagger vs. Hilt: Which to Choose?

* **For new Android projects:** **Hilt is generally the recommended choice.** It simplifies DI,
  integrates well with the Android ecosystem, and reduces the learning curve associated with Dagger.
* **For existing Dagger projects:** Migrating to Hilt can be beneficial but might require effort.
  Hilt can interoperate with existing Dagger code, allowing for incremental migration.
* **For pure Kotlin/Java libraries (non-Android):** Dagger 2 is still the go-to option as Hilt is
  Android-specific.
* **For very complex, non-standard DI needs in Android:** While Hilt covers most Android use cases,
  Dagger's full flexibility might be needed in rare, highly custom scenarios.

## 5. Best Practices for DI (Hilt/Dagger)

* **Prefer Constructor Injection:** It makes dependencies explicit and ensures objects are
  initialized with valid dependencies.
* **Use Field Injection Sparingly:** Primarily for Android framework classes where you don't control
  instantiation (e.g., `@AndroidEntryPoint` classes).
* **Keep Modules Focused:** Modules should have a clear responsibility (e.g., `NetworkModule`,
  `DatabaseModule`).
* **Understand Scopes:** Use scopes (`@Singleton`, `@ActivityScoped`, etc.) correctly to manage the
  lifecycle of your dependencies and avoid memory leaks or unintended sharing of state.
* **Interfaces for Dependencies:** Depend on abstractions (interfaces) rather than concrete
  implementations. This allows for easier swapping of implementations (e.g., for testing or
  different build variants). Use `@Binds` in Hilt/Dagger for this.
* **Single Responsibility Principle:** Classes should have one reason to change. DI helps achieve
  this by separating object creation from object usage.

## 6. Testing with Hilt/Dagger

Both Hilt and Dagger provide excellent support for testing.

### Testing with Hilt:

Hilt offers dedicated testing APIs that simplify replacing dependencies in tests.

1. **Add Testing Dependencies:**
   ```gradle
   androidTestImplementation "com.google.dagger:hilt-android-testing:2.x"
   kaptAndroidTest "com.google.dagger:hilt-compiler:2.x"
   ```
2. **Custom Test Runner:**
   ```kotlin
   // src/androidTest/java/com/example/MyCustomTestRunner.kt
   class MyCustomTestRunner : AndroidJUnitRunner() {
       override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
           return super.newApplication(cl, HiltTestApplication::class.java.name, context)
       }
   }
   // Update build.gradle: testInstrumentationRunner "com.example.MyCustomTestRunner"
   ```
3. **Use `@HiltAndroidTest`:** Annotate your UI test classes.
4. **Replace Bindings:**
    * **`@UninstallModules`**: Remove specific Hilt modules for a test.
    * **Test-specific Modules**: Provide fake or mock implementations in modules within your
      `androidTest` source set.
    * **`@BindValue`**: Easily replace a field in your test with a mock or fake.

```kotlin
@HiltAndroidTest
class MyActivityTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue // Replaces the real AnalyticsService with this mock
    @JvmField // If the original is a field injection in a Kotlin class
    val mockAnalyticsService: AnalyticsService = mock(AnalyticsService::class.java)

    @Test
    fun testSomething() {
        launchActivity<MyActivity>()
        // Perform assertions, mockAnalyticsService will be injected
        verify(mockAnalyticsService).trackScreen("MyActivityScreen")
    }
}
```

### Testing with Dagger:

* **Test Components:** Create separate Dagger components for tests that use test modules providing
  mock/fake dependencies.
* **JUnit Rules:** Use JUnit rules to manage the lifecycle of test components.

## 7. Conclusion

Dependency Injection, particularly with Hilt, is a cornerstone of modern Android development. It
significantly improves code quality, testability, and maintainability. While Dagger 2 remains a
powerful tool, Hilt's simplification and Android-specific focus make it the preferred choice for
most Android applications. By understanding the core concepts and applying best practices, you can
leverage DI to build robust and scalable Android apps.
