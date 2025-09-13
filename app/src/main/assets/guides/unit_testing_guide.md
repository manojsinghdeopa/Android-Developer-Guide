# Unit Testing in Android

Unit testing is a fundamental practice in software development that involves testing individual
components or units of source code in isolation. In Android development, unit tests are crucial for
ensuring the correctness, reliability, and maintainability of your app. This guide provides a
comprehensive overview of unit testing in Android, focusing on JUnit, Mockito, and Kotlinx
Coroutines Test.

## Table of Contents

1. [Introduction to Unit Testing](#introduction-to-unit-testing)
    * [Why Unit Test?](#why-unit-test)
    * [What to Test?](#what-to-test)
    * [Types of Tests in Android](#types-of-tests-in-android)
2. [Setting up Your Environment](#setting-up-your-environment)
    * [Configuring Gradle Dependencies](#configuring-gradle-dependencies)
    * [Directory Structure](#directory-structure)
3. [Writing Unit Tests with JUnit](#writing-unit-tests-with-junit)
    * [Basic JUnit Test Structure](#basic-junit-test-structure)
    * [Common JUnit Annotations](#common-junit-annotations)
    * [Assertions](#assertions)
    * [Example: Testing a Simple Utility Class](#example-testing-a-simple-utility-class)
4. [Mocking Dependencies with Mockito](#mocking-dependencies-with-mockito)
    * [Why Mocking?](#why-mocking)
    * [Adding Mockito to Your Project](#adding-mockito-to-your-project)
    * [Creating Mocks](#creating-mocks)
    * [Stubbing Method Calls](#stubbing-method-calls)
    * [Verifying Interactions](#verifying-interactions)
    * [Argument Matchers](#argument-matchers)
    * [Example: Testing a Class with Dependencies](#example-testing-a-class-with-dependencies)
5. [Testing Kotlin Coroutines with Kotlinx Coroutines Test](#testing-kotlin-coroutines-with-kotlinx-coroutines-test)
    * [Challenges in Testing Coroutines](#challenges-in-testing-coroutines)
    * [Setting up Kotlinx Coroutines Test](#setting-up-kotlinx-coroutines-test)
    * [Using `TestCoroutineDispatcher` and
      `TestCoroutineScope`](#using-testcoroutinedispatcher-and-testcoroutinescope)
    * [Controlling Virtual Time with
      `runBlockingTest`](#controlling-virtual-time-with-runblockingtest)
    * [Testing `Flow`](#testing-flow)
    * [Example: Testing a ViewModel with Coroutines](#example-testing-a-viewmodel-with-coroutines)
6. [Best Practices for Unit Testing](#best-practices-for-unit-testing)
    * [Write Small, Focused Tests](#write-small-focused-tests)
    * [Test One Thing at a Time](#test-one-thing-at-a-time)
    * [Make Tests Independent and Repeatable](#make-tests-independent-and-repeatable)
    * [Follow the Arrange-Act-Assert (AAA) Pattern](#follow-the-arrange-act-assert-aaa-pattern)
    * [Name Tests Clearly and Descriptively](#name-tests-clearly-and-descriptively)
    * [Keep Tests Fast](#keep-tests-fast)
    * [Refactor Tests as You Refactor Code](#refactor-tests-as-you-refactor-code)
    * [Aim for High Test Coverage (But Don't Obsess)](#aim-for-high-test-coverage-but-dont-obsess)
7. [Running Unit Tests](#running-unit-tests)
    * [From Android Studio](#from-android-studio)
    * [Using Gradle](#using-gradle)
8. [Conclusion](#conclusion)

---

## 1. Introduction to Unit Testing

### Why Unit Test?

* **Early Bug Detection:** Catch bugs early in the development cycle, making them cheaper and easier
  to fix.
* **Improved Code Quality:** Writing testable code often leads to better-designed, more modular, and
  maintainable code.
* **Facilitates Refactoring:** Confidently refactor code knowing that tests will catch regressions.
* **Documentation:** Tests serve as a form of documentation, illustrating how individual components
  are intended to be used.
* **Faster Feedback Loop:** Unit tests run quickly, providing rapid feedback on code changes.

### What to Test?

Focus on testing the business logic of your application. This includes:

* Methods with conditional logic (if/else, switch statements).
* Methods that perform calculations or transformations.
* Boundary conditions (e.g., null inputs, empty lists, extreme values).
* Error handling and exception throwing.
* Interactions between objects (using mocks).

Avoid testing:

* Trivial getters and setters (unless they contain logic).
* Third-party library code (assume it's already tested).
* Android Framework components directly in local unit tests (use instrumented tests or mocking for
  these).

### Types of Tests in Android

* **Local Unit Tests:** Run on your local development machine (JVM). They are fast and ideal for
  testing logic that doesn't depend on the Android framework. JUnit is the primary framework for
  this.
* **Instrumented Tests:** Run on an Android device or emulator. They have access to Android
  framework APIs and are suitable for testing UI interactions, components like Activities, Services,
  and Content Providers.

This guide focuses on **Local Unit Tests**.

---

## 2. Setting up Your Environment

### Configuring Gradle Dependencies

You'll need to add dependencies for JUnit, Mockito, and Kotlinx Coroutines Test to your
`build.gradle` file (usually the module-level `app/build.gradle.kts` or `app/build.gradle`).

```kotlin
// app/build.gradle.kts

dependencies {
    // JUnit 4 (the default test runner in Android Studio)
    testImplementation("junit:junit:4.13.2")

    // Mockito Core for mocking objects
    testImplementation("org.mockito:mockito-core:5.10.0") // Check for the latest version
    // Mockito-Kotlin for better Kotlin integration (optional but recommended)
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1") // Check for the latest version

    // Kotlinx Coroutines Test for testing coroutines
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3") // Align with your coroutines version

    // For AndroidX Test specific utilities if needed for local tests (e.g. InstantTaskExecutorRule)
    testImplementation("androidx.arch.core:core-testing:2.2.0")
}
```

**Note:** Always check for the latest versions of these libraries.

### Directory Structure

By default, Android Studio places local unit tests in the `app/src/test/java` (or
`app/src/test/kotlin`) directory.

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/myapp/
│   │   │   └── MyClass.kt
│   ├── test/
│   │   ├── java/com/example/myapp/
│   │   │   └── MyClassTest.kt
```

---

## 3. Writing Unit Tests with JUnit

JUnit is a widely-used testing framework for Java and Kotlin.

### Basic JUnit Test Structure

A JUnit test class is typically a regular class with methods annotated with `@Test`.

```kotlin
import org.junit.Test
import org.junit.Assert.* // For assertions

class MyClassTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}
```

### Common JUnit Annotations

* `@Test`: Marks a method as a test method.
* `@Before`: Methods annotated with `@Before` are executed before each test method in the class.
  Useful for setting up common test conditions.
* `@After`: Methods annotated with `@After` are executed after each test method. Useful for cleaning
  up resources.
* `@BeforeClass`: Methods annotated with `@BeforeClass` are executed once before any test methods in
  the class. Must be static (or `@JvmStatic` in Kotlin companion object).
* `@AfterClass`: Methods annotated with `@AfterClass` are executed once after all test methods in
  the class. Must be static (or `@JvmStatic` in Kotlin companion object).
* `@Ignore`: Marks a test method or class to be ignored during test execution.
* `@Rule`: Allows for more flexible and reusable setup/teardown logic through test rules (e.g.,
  `InstantTaskExecutorRule` for LiveData).

### Assertions

Assertions are used to verify that the actual outcome of a test matches the expected outcome. JUnit
provides a set of assertion methods in the `org.junit.Assert` class.

Common assertions:

* `assertEquals(expected, actual)`: Checks if two values are equal.
* `assertNotEquals(unexpected, actual)`: Checks if two values are not equal.
* `assertTrue(condition)`: Checks if a condition is true.
* `assertFalse(condition)`: Checks if a condition is false.
* `assertNull(object)`: Checks if an object is null.
* `assertNotNull(object)`: Checks if an object is not null.
* `assertSame(expected, actual)`: Checks if two object references point to the same object.
* `assertNotSame(unexpected, actual)`: Checks if two object references point to different objects.
* `assertArrayEquals(expectedArray, actualArray)`: Checks if two arrays are equal.
* `fail(message)`: Fails a test immediately.

### Example: Testing a Simple Utility Class

Let's say you have a simple `StringHelper` class:

```kotlin
// src/main/java/com/example/myapp/StringHelper.kt
package com.example.myapp

class StringHelper {
    fun reverseString(input: String?): String? {
        if (input == null) return null
        return input.reversed()
    }

    fun isPalindrome(input: String?): Boolean {
        if (input == null) return false
        val cleanInput = input.lowercase().filter { it.isLetterOrDigit() }
        return cleanInput == cleanInput.reversed()
    }
}
```

Here's how you might test it:

```kotlin
// src/test/java/com/example/myapp/StringHelperTest.kt
package com.example.myapp

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class StringHelperTest {

    private lateinit var stringHelper: StringHelper

    @Before
    fun setUp() {
        stringHelper = StringHelper()
    }

    @Test
    fun reverseString_nullInput_returnsNull() {
        val actual = stringHelper.reverseString(null)
        assertNull("Reversing null should return null", actual)
    }

    @Test
    fun reverseString_emptyInput_returnsEmpty() {
        val actual = stringHelper.reverseString("")
        assertEquals("Reversing empty string should return empty string", "", actual)
    }

    @Test
    fun reverseString_validInput_returnsReversed() {
        val actual = stringHelper.reverseString("hello")
        assertEquals("Reversing 'hello' should return 'olleh'", "olleh", actual)
    }

    @Test
    fun isPalindrome_nullInput_returnsFalse() {
        assertFalse("Null string should not be a palindrome", stringHelper.isPalindrome(null))
    }

    @Test
    fun isPalindrome_emptyInput_isTrue() { // An empty string can be considered a palindrome
        assertTrue("Empty string should be a palindrome", stringHelper.isPalindrome(""))
    }

    @Test
    fun isPalindrome_simplePalindrome_returnsTrue() {
        assertTrue("'madam' should be a palindrome", stringHelper.isPalindrome("madam"))
    }

    @Test
    fun isPalindrome_nonPalindrome_returnsFalse() {
        assertFalse("'hello' should not be a palindrome", stringHelper.isPalindrome("hello"))
    }

    @Test
    fun isPalindrome_withSpacesAndCases_returnsTrue() {
        assertTrue(
            "'A man, a plan, a canal: Panama' should be a palindrome",
            stringHelper.isPalindrome("A man, a plan, a canal: Panama")
        )
    }
}
```

---

## 4. Mocking Dependencies with Mockito

Mockito is a popular mocking framework for Java and Kotlin that allows you to create test double
objects (mocks) for dependencies.

### Why Mocking?

* **Isolation:** Test a unit of code in isolation by replacing its dependencies with mocks. This
  ensures that the test focuses only on the behavior of the unit under test.
* **Control:** Mocks provide control over the behavior of dependencies. You can specify what a mock
  should return when its methods are called or verify that certain methods were called.
* **Speed:** Avoids the need for real, potentially slow or complex, dependencies (e.g., network
  calls, database access).
* **Testability of Untestable Code:** Sometimes dependencies are hard to instantiate or configure in
  a test environment. Mocks can stand in for them.

### Adding Mockito to Your Project

Ensure you have the Mockito dependencies in your `build.gradle` file (as shown in the setup
section). Using `mockito-kotlin` is highly recommended for a better experience with Kotlin.

### Creating Mocks

You can create mocks using `Mockito.mock()` or the `mock()` function from `mockito-kotlin`.

```kotlin
import org.mockito.Mockito.mock
import org.mockito.kotlin.mock // Preferred for Kotlin

// Example dependency
interface UserRepository {
    fun getUser(id: String): User?
    fun saveUser(user: User)
}

data class User(val id: String, val name: String)

class MyViewModelTest {

    // Using Mockito core
    // private val userRepositoryMock: UserRepository = mock(UserRepository::class.java)

    // Using mockito-kotlin (more concise)
    private val userRepositoryMock: UserRepository = mock()

    @Test
    fun someTest() {
        // ... use userRepositoryMock
    }
}
```

### Stubbing Method Calls

Stubbing involves defining the behavior of a mock object's methods. You can specify what a method
should return or what exception it should throw when called with certain arguments.

The `when(...).thenReturn(...)` syntax from Mockito, or `whenever(...).thenReturn(...)` from
`mockito-kotlin`, is commonly used.

```kotlin
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.junit.Test
import org.junit.Assert.*

class MyViewModelTest {
    private val userRepositoryMock: UserRepository = mock()
    private val SUT: MyViewModel // System Under Test

    init {
        SUT = MyViewModel(userRepositoryMock)
    }

    @Test
    fun getUserName_userExists_returnsUserName() {
        val userId = "123"
        val expectedUser = User(userId, "Test User")

        // Stubbing: When getUser(userId) is called on the mock, return expectedUser
        whenever(userRepositoryMock.getUser(userId)).thenReturn(expectedUser)

        val userName = SUT.getUserName(userId)

        assertEquals("Test User", userName)
    }

    @Test
    fun getUserName_userDoesNotExist_returnsNull() {
        val userId = "nonexistent"

        // Stubbing: When getUser(userId) is called, return null
        whenever(userRepositoryMock.getUser(userId)).thenReturn(null)

        val userName = SUT.getUserName(userId)

        assertNull(userName)
    }

    @Test(expected = RuntimeException::class) // Or use assertThrows with JUnit Jupiter
    fun doSomething_repositoryThrowsException() {
        val userId = "123"
        whenever(userRepositoryMock.getUser(userId)).thenThrow(RuntimeException("Database error"))

        SUT.performDangerousOperation(userId) // This method internally calls getUser
    }
}

// Class being tested
class MyViewModel(private val userRepository: UserRepository) {
    fun getUserName(userId: String): String? {
        return userRepository.getUser(userId)?.name
    }

    fun performDangerousOperation(userId: String) {
        userRepository.getUser(userId) // This might throw if stubbed to do so
    }
}
```

### Verifying Interactions

Verification ensures that certain methods on a mock object were called with the expected arguments,
or a specific number of times.

Use `Mockito.verify()` or `verify()` from `mockito-kotlin`.

```kotlin
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.Test

class RegistrationServiceTest {
    private val userRepositoryMock: UserRepository = mock()
    private val notificationServiceMock: NotificationService = mock() // Another dependency
    private val registrationService: RegistrationService

    interface NotificationService {
        fun sendWelcomeEmail(email: String)
    }

    init {
        registrationService = RegistrationService(userRepositoryMock, notificationServiceMock)
    }

    @Test
    fun registerUser_savesUserAndSendsNotification() {
        val user = User("1", "John Doe")
        val email = "john.doe@example.com"

        // Act
        registrationService.registerUser(user, email)

        // Verify
        // Check if userRepository.saveUser(user) was called exactly once
        verify(userRepositoryMock).saveUser(user)

        // Check if notificationService.sendWelcomeEmail(email) was called
        verify(notificationServiceMock).sendWelcomeEmail(email)
    }
}

// Class being tested
class RegistrationService(
    private val userRepository: UserRepository,
    private val notificationService: NotificationService
) {
    fun registerUser(user: User, email: String) {
        userRepository.saveUser(user)
        notificationService.sendWelcomeEmail(email)
    }
}
```

### Argument Matchers

Argument matchers allow for flexible verification or stubbing when you don't care about the exact
argument value or when it's hard to predict.

Mockito provides matchers like `any()`, `anyString()`, `anyInt()`, `eq()`, `argThat()`.
`mockito-kotlin` provides typed versions like `any<String>()`.

```kotlin
import org.mockito.kotlin.*
import org.junit.Test

class ArgumentMatcherTest {
    interface MyApi {
        fun complexCall(id: Int, name: String, config: Map<String, Any>): String
        fun processUser(user: User)
    }

    private val myApiMock: MyApi = mock()

    @Test
    fun testWithMatchers() {
        // Stubbing with matchers
        whenever(myApiMock.complexCall(anyInt(), eq("test"), anyMap())).thenReturn("Success")

        val result = myApiMock.complexCall(10, "test", mapOf("key" to "value"))
        assertEquals("Success", result)

        // Verification with matchers
        val testUser = User("id1", "Test User")
        myApiMock.processUser(testUser)

        // Verify processUser was called with any User object
        verify(myApiMock).processUser(any())

        // Verify processUser was called with a User object whose name is "Test User"
        verify(myApiMock).processUser(argThat { user -> user.name == "Test User" })
    }
}
```

### Example: Testing a Class with Dependencies

Consider a `AuthManager` that depends on a `CredentialsValidator` and `TokenGenerator`.

```kotlin
// Dependencies
interface CredentialsValidator {
    fun validate(username: String, pass: String): Boolean
}

interface TokenGenerator {
    fun generateToken(username: String): String
}

// Class to test
class AuthManager(
    private val validator: CredentialsValidator,
    private val tokenGenerator: TokenGenerator
) {
    fun login(username: String, pass: String): String? {
        if (validator.validate(username, pass)) {
            return tokenGenerator.generateToken(username)
        }
        return null
    }
}

// Test Class
import org . junit . Test
        import org . junit . runner . RunWith
        import org . mockito . InjectMocks
        import org . mockito . Mock
        import org . mockito . junit . MockitoJUnitRunner
        import org . mockito . kotlin . * // Use this for whenever, verify, etc.
        import org . junit . Assert . *

        @RunWith(MockitoJUnitRunner::class) // Initializes mocks annotated with @Mock and injects them
        class AuthManagerTest {

            @Mock
            private lateinit var mockValidator: CredentialsValidator

            @Mock
            private lateinit var mockTokenGenerator: TokenGenerator

            @InjectMocks // Creates an instance of AuthManager and injects @Mock fields
            private lateinit var authManager: AuthManager

            @Test
            fun `login with valid credentials returns token`() {
                // Arrange
                val username = "testUser"
                val password = "password123"
                val expectedToken = "fakeToken123"

                whenever(mockValidator.validate(username, password)).thenReturn(true)
                whenever(mockTokenGenerator.generateToken(username)).thenReturn(expectedToken)

                // Act
                val token = authManager.login(username, password)

                // Assert
                assertEquals(expectedToken, token)
                verify(mockValidator).validate(username, password)
                verify(mockTokenGenerator).generateToken(username)
            }

            @Test
            fun `login with invalid credentials returns null`() {
                // Arrange
                val username = "testUser"
                val password = "wrongPassword"

                whenever(mockValidator.validate(username, password)).thenReturn(false)

                // Act
                val token = authManager.login(username, password)

                // Assert
                assertNull(token)
                verify(mockValidator).validate(username, password)
                verify(
                    mockTokenGenerator,
                    never()
                ).generateToken(any()) // Ensure token generator was not called
            }
        }
```

**Note:** The `@RunWith(MockitoJUnitRunner::class)` annotation automatically initializes fields
annotated with `@Mock` and injects them into fields annotated with `@InjectMocks`. This reduces
boilerplate.

---

## 5. Testing Kotlin Coroutines with Kotlinx Coroutines Test

Testing code that uses Kotlin coroutines requires special handling due to their asynchronous nature.
The `kotlinx-coroutines-test` library provides utilities to make testing coroutines more
straightforward and reliable.

### Challenges in Testing Coroutines

* **Asynchronicity:** Coroutines execute asynchronously, making it hard to deterministically test
  their outcomes.
* **Dispatchers:** Coroutines rely on `CoroutineDispatcher`s (like `Dispatchers.Main`,
  `Dispatchers.IO`). In tests, you need to control these dispatchers.
* **Timing:** Tests should not rely on `Thread.sleep()` as it makes them slow and flaky.

### Setting up Kotlinx Coroutines Test

Ensure you have the `kotlinx-coroutines-test` dependency in your `build.gradle` file.

### Using `TestCoroutineDispatcher` and `TestCoroutineScope` (Legacy - pre `1.6.0`) /
`StandardTestDispatcher` and `TestScope` (Recommended `1.6.0+`)

The `kotlinx-coroutines-test` library provides:

* `TestDispatcher` (e.g., `StandardTestDispatcher`, `UnconfinedTestDispatcher`): These dispatchers
  allow you to control the execution of coroutines in tests. `StandardTestDispatcher` gives you
  fine-grained control over virtual time.
* `TestScope`: A `CoroutineScope` that uses a `TestDispatcher` and provides more control for
  testing.

**For `kotlinx-coroutines-test` 1.6.0 and later (Recommended):**

```kotlin
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.* // Import TestScope, runTest, StandardTestDispatcher etc.
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi // Required for many coroutines test APIs
class MyCoroutineViewModelTest {

    // Rule to swap the main dispatcher with a TestDispatcher
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule() // See MainCoroutineRule implementation below

    private lateinit var viewModel: MyCoroutineViewModel

    @Test
    fun `fetchData success updates liveData`() = runTest { // runTest provides a TestScope
        // Arrange
        val fakeData = "Fake Data"
        val repository = FakeRepository(fakeData) // A test double for your repository
        viewModel = MyCoroutineViewModel(
            repository,
            mainCoroutineRule.testDispatcher
        ) // Inject TestDispatcher

        // Act
        viewModel.fetchData()
        // advanceUntilIdle() // If using StandardTestDispatcher explicitly and need to run pending tasks

        // Assert
        assertEquals(fakeData, viewModel.data.value) // Assuming LiveData or StateFlow
    }
}

// Example ViewModel
import androidx . lifecycle . LiveData
        import androidx . lifecycle . MutableLiveData
        import androidx . lifecycle . ViewModel
        import androidx . lifecycle . viewModelScope
        import kotlinx . coroutines . CoroutineDispatcher
        import kotlinx . coroutines . Dispatchers
        import kotlinx . coroutines . launch

interface DataRepository {
    suspend fun fetchData(): String
}

class MyCoroutineViewModel(
    private val repository: DataRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main // Default to Main
) : ViewModel() {
    private val _data = MutableLiveData<String>()
    val data: LiveData<String> = _data

    fun fetchData() {
        viewModelScope.launch(dispatcher) { // Use injected dispatcher
            val result = repository.fetchData()
            _data.value = result
        }
    }
}

// Fake Repository for testing
class FakeRepository(private val cannedResponse: String) : DataRepository {
    override suspend fun fetchData(): String {
        kotlinx.coroutines.delay(100) // Simulate network delay
        return cannedResponse
    }
}

// MainCoroutineRule for swapping Dispatchers.Main
@ExperimentalCoroutinesApi
class MainCoroutineRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher() // Or UnconfinedTestDispatcher()
) : org.junit.rules.TestWatcher() { // TestWatcher from JUnit
    override fun starting(description: org.junit.runner.Description) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: org.junit.runner.Description) {
        super.finished(description)
        Dispatchers.resetMain()
    }
}

// If using LiveData, you might also need InstantTaskExecutorRule
// import androidx.arch.core.executor.testing.InstantTaskExecutorRule
// @get:Rule
// val instantTaskExecutorRule = InstantTaskExecutorRule()
```

### Controlling Virtual Time with `runTest`

`runTest` is the primary way to write tests for suspending functions. It creates a `TestScope` and
automatically advances a virtual clock, making delays and asynchronous operations complete
immediately for testing purposes.

* `runTest { ... }`: The coroutines inside this block will run on a `TestDispatcher`. `delay` calls
  will auto-advance.
* `advanceTimeBy(delayTimeMillis: Long)`: Manually advances the virtual clock by a specified amount.
* `advanceUntilIdle()`: Advances the virtual clock until there are no more pending tasks in the
  dispatcher.
* `StandardTestDispatcher()`: The scheduler for this dispatcher needs to be explicitly advanced.
  `runTest` does this automatically at the end of the test block.
* `UnconfinedTestDispatcher()`: Executes coroutines eagerly. Be cautious as it can lead to different
  execution orders than production dispatchers.

### Testing `Flow`

`kotlinx-coroutines-test` also makes testing Kotlin `Flow` easier.

```kotlin
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@ExperimentalCoroutinesApi
class MyFlowDataSourceTest {

    @Test
    fun `getData emits correct item`() = runTest {
        val dataSource = MyFlowDataSource()
        val result = dataSource.getData().first() // Collect the first emitted item
        assertEquals("Hello from Flow", result)
    }
}

class MyFlowDataSource {
    fun getData() = flowOf("Hello from Flow")
}
```

For more complex flow testing (multiple emissions, completion, errors), you can collect into a list
or use libraries like Turbine from CashApp.

### Example: Testing a ViewModel with Coroutines (using `runTest` and `MainCoroutineRule`)

This builds upon the `MyCoroutineViewModel` example above, showcasing a more complete test.

```kotlin
import androidx.arch.core.executor.testing.InstantTaskExecutorRule // For LiveData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class MyCoroutineViewModelAdvancedTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule() // Handles Dispatchers.Main

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule() // For LiveData synchronous updates

    private lateinit var viewModel: MyCoroutineViewModel
    private lateinit var mockRepository: DataRepository // Mock the repository

    @Before
    fun setUp() {
        mockRepository = mock()
        // Pass the testDispatcher from the rule to the ViewModel
        viewModel = MyCoroutineViewModel(mockRepository, mainCoroutineRule.testDispatcher)
    }

    @Test
    fun `fetchData success - repository returns data - updates LiveData`() = runTest {
        // Arrange
        val expectedData = "Successful Data"
        whenever(mockRepository.fetchData()).thenReturn(expectedData)

        // Act
        viewModel.fetchData()
        // runTest will auto-advance time for delays in fetchData and its launch block.
        // If fetchData launched on a different dispatcher than the one runTest uses,
        // you might need advanceUntilIdle() on that specific dispatcher if it's a StandardTestDispatcher.
        // However, since we inject mainCoroutineRule.testDispatcher, it's controlled.

        // Assert
        assertEquals(expectedData, viewModel.data.value)
    }

    @Test
    fun `fetchData failure - repository throws exception - LiveData not updated (or error state)`() =
        runTest {
            // Arrange
            val initialValue =
                viewModel.data.value // Assuming it might be null or some initial state
            val errorMessage = "Network Error"
            whenever(mockRepository.fetchData()).thenThrow(RuntimeException(errorMessage))

            // Act
            viewModel.fetchData()

            // Assert
            // Depending on your ViewModel's error handling, assert the LiveData state.
            // For this simple ViewModel, it might remain unchanged or you might have an error LiveData.
            assertEquals(initialValue, viewModel.data.value)
            // Or: assertNotNull(viewModel.error.value)
            // Or: verify an error logging mechanism was called
        }
}
```

---

## 6. Best Practices for Unit Testing

* **Write Small, Focused Tests:** Each test method should verify a single piece of functionality or
  a single scenario. This makes tests easier to understand, debug, and maintain.
* **Test One Thing at a Time:** Avoid testing multiple concerns within a single test method.
* **Make Tests Independent and Repeatable:** Tests should not depend on the order in which they are
  run, nor should they rely on external state that might change. Each test should set up its own
  environment and clean up afterward if necessary.
* **Follow the Arrange-Act-Assert (AAA) Pattern:**
    * **Arrange:** Set up the test environment, initialize objects, and prepare mock dependencies.
    * **Act:** Execute the method or unit of code being tested.
    * **Assert:** Verify that the outcome of the action is as expected using assertion methods.
* **Name Tests Clearly and Descriptively:** Test names should clearly indicate what is being tested
  and the expected outcome (e.g., `sum_positiveNumbers_returnsCorrectSum`,
  `login_invalidCredentials_returnsNull`).
* **Keep Tests Fast:** Unit tests should run quickly to provide rapid feedback. Avoid time-consuming
  operations like network calls or database access (use mocks instead).
* **Refactor Tests as You Refactor Code:** When you refactor production code, update the
  corresponding tests to reflect the changes. Tests should be treated as first-class citizens.
* **Aim for High Test Coverage (But Don't Obsess):** While high test coverage is generally good,
  focus on testing critical and complex parts of your application. 100% coverage doesn't guarantee
  bug-free code if the tests themselves are not well-written.
* **Test Public APIs:** Focus on testing the public contract of your classes, not their private
  implementation details. This makes tests less brittle to refactoring.
* **Use Test Doubles Effectively:** Understand when to use mocks, stubs, fakes, or spies.

---

## 7. Running Unit Tests

### From Android Studio

* **Run a single test method:** Click the green play icon in the gutter next to the test method.
* **Run all tests in a class:** Click the green play icon in the gutter next to the test class name.
* **Run all tests in a directory/module:** Right-click on the test directory (e.g., `src/test/java`)
  or module and select "Run tests in '...'".
* **Test results** are displayed in the "Run" tool window.

### Using Gradle

You can run unit tests from the command line using Gradle wrapper:

* Run all unit tests for all variants:
  ```bash
  ./gradlew test
  ```
* Run unit tests for a specific build variant (e.g., debug):
  ```bash
  ./gradlew testDebugUnitTest 
  ```
  (The exact task name might vary based on your project structure and build types/flavors).
* Run a specific test class:
  ```bash
  ./gradlew testDebugUnitTest --tests "com.example.myapp.MyClassTest"
  ```
* Run a specific test method:
  ```bash
  ./gradlew testDebugUnitTest --tests "com.example.myapp.MyClassTest.mySpecificTestMethod"
  ```
* HTML test reports are generated in `app/build/reports/tests/testDebugUnitTest/` (path may vary).

---

## 8. Conclusion

Unit testing is an indispensable part of modern Android development. By leveraging frameworks like
JUnit and Mockito, and tools like `kotlinx-coroutines-test` for asynchronous code, you can build
more robust, maintainable, and reliable applications. Adopting good testing practices will
significantly improve your development workflow and the overall quality of your codebase. Remember
to write tests for new features and bug fixes, and to maintain your test suite as your application
evolves.
