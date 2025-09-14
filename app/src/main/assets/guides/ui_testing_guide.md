# UI Testing in Android

Effective UI testing is crucial for delivering high-quality Android applications. It helps ensure
that your app's user interface behaves as expected across different devices and scenarios. Android
offers robust frameworks for UI testing, catering to both traditional XML-based views and modern
Jetpack Compose UIs.

## Key UI Testing Frameworks

Two primary frameworks dominate Android UI testing:

1. **Espresso:** Used for testing UIs built with Android's traditional XML layouts.
2. **Compose Testing API:** Specifically designed for testing UIs built with Jetpack Compose.

## 1. Espresso (for XML-based UIs)

Espresso is a powerful testing framework that is part of the Android Testing Support Library. It
provides a concise and readable API for UI testing, focusing on three main components:

* **ViewMatchers:** Allow you to find views in the current view hierarchy.
    * Examples: `withId(R.id.my_button)`, `withText("Submit")`, `withHint("Enter your name")`
* **ViewActions:** Allow you to interact with the found views.
    * Examples: `click()`, `typeText("Hello")`, `scrollTo()`, `swipeLeft()`
* **ViewAssertions:** Allow you to assert the state of a view.
    * Examples: `matches(isDisplayed())`, `matches(withText("Success"))`, `doesNotExist()`

### Setting up Espresso

To use Espresso, you need to add the following dependencies to your app's `build.gradle` file:

```
android {
    defaultConfig {
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test:runner:1.5.2'
    androidTestImplementation 'androidx.test:rules:1.5.0' // For ActivityScenarioRule
}
```

*Note: Always check for the latest versions of these libraries.*

### Basic Espresso Test Structure

Espresso tests are typically written in Kotlin (or Java) and reside in the `androidTest` directory
of your module.

```
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MyActivityTest {

    @get:Rule
    var activityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testLoginSuccess() {
        // Type text into an EditText
        onView(withId(R.id.username_edittext))
            .perform(typeText("testuser"))

        // Type text into another EditText
        onView(withId(R.id.password_edittext))
            .perform(typeText("password123"))

        // Click on a Button
        onView(withId(R.id.login_button))
            .perform(click())

        // Check if a TextView displays the success message
        onView(withId(R.id.status_textview))
            .check(matches(withText("Login Successful")))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testLoginFailure() {
        onView(withId(R.id.username_edittext)).perform(typeText("wronguser"))
        onView(withId(R.id.password_edittext)).perform(typeText("wrongpassword"))
        onView(withId(R.id.login_button)).perform(click())
        onView(withId(R.id.status_textview)).check(matches(withText("Login Failed")))
    }
}
```

### Key Espresso Concepts

* **`onView()`:** The entry point for interacting with a view. It takes a `ViewMatcher` as an
  argument.
* **`perform()`:** Executes one or more `ViewAction`s on the matched view.
* **`check()`:** Verifies the state of the matched view using a `ViewAssertion`.
* **`ActivityScenarioRule`:**  A JUnit rule that provides functional testing of a single activity.
  It launches the activity before each test and finishes it after.
* **Synchronization:** Espresso automatically synchronizes test actions with the UI thread, meaning
  it waits for UI operations to complete before performing the next action or assertion. This
  greatly reduces flakiness.
* **Idling Resources:** For operations that happen in the background (e.g., network requests,
  database operations), Espresso might not know when to wait. `IdlingResource` is a mechanism to
  tell Espresso to wait for these long-running operations.

### Best Practices for Espresso Testing

* **Use unique IDs:** Assign unique IDs to your views in XML layouts for easy and reliable matching.
* **Keep tests focused:** Each test method should verify a specific piece of functionality.
* **Avoid `Thread.sleep()`:** Rely on Espresso's synchronization and Idling Resources instead of
  manual sleeps, which make tests flaky and slow.
* **Test different scenarios:** Cover success cases, error cases, and edge cases.
* **Organize your tests:** Group related tests into classes.
* **Run tests regularly:** Integrate UI tests into your CI/CD pipeline.

## 2. Compose Testing API (for Jetpack Compose UIs)

Jetpack Compose has its own dedicated testing API that is designed to work seamlessly with
composable functions. It allows you to find composables, interact with them, and verify their state
and properties.

### Setting up Compose Testing

Add the following dependencies to your app's `build.gradle` file:

```
dependencies {
    // Core Compose testing
    androidTestImplementation "androidx.compose.ui:ui-test-junit4:1.6.0" // Check for latest version

    // For debugging tests
    debugImplementation "androidx.compose.ui:ui-test-manifest:1.6.0" // Check for latest version
}
```

### Basic Compose Test Structure

Compose tests also reside in the `androidTest` directory. They use a `ComposeTestRule`.

```
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MyComposeScreenTest {

    @get:Rule
    val composeTestRule =
        createComposeRule() // Or createAndroidComposeRule<MyActivity>() if testing an Activity

    @Test
    fun testGreetingMessage() {
        // Set the content for the test
        composeTestRule.setContent {
            // Your Composable function under test
            // Example: GreetingScreen("Android")
        }

        // Find a composable by its text
        composeTestRule.onNodeWithText("Hello, Android!")
            .assertIsDisplayed() // Assert that it's visible

        // Find a composable by its test tag (recommended)
        composeTestRule.onNodeWithTag("myButton")
            .performClick() // Perform a click

        // Assert a composable's property
        composeTestRule.onNodeWithText("Button Clicked")
            .assertExists() // Assert that a node with this text now exists
    }
}
```

### Key Compose Testing Concepts

* **`ComposeTestRule`:** The JUnit rule for testing Compose UIs.
    * `createComposeRule()`: Use this when you want to test composables in isolation, without
      launching an Activity. You'll typically use `setContent` to display your UI.
    * `createAndroidComposeRule<YourActivity>()`: Use this when you need to test composables within
      an Activity context. It launches the specified Activity.
* **Finders (SemanticsNodeInteraction):** Used to locate one or more composables (nodes in the
  semantics tree).
    * `onNodeWithText("text")`: Finds a node containing the given text.
    * `onNodeWithTag("testTag")`: Finds a node with a specific test tag (set using
      `Modifier.testTag("testTag")`). This is the **recommended** way to find composables for
      testing as it's less prone to breakages due to text changes.
    * `onNodeWithContentDescription("description")`: Finds a node by its content description.
    * `onAllNodesWithText("text")`: Finds all nodes containing the given text.
* **Actions:** Used to interact with the found composables.
    * `performClick()`
    * `performTextInput("text")`
    * `performScrollTo()`
    * `performGesture { swipeLeft() }`
* **Assertions:** Used to verify the state or properties of composables.
    * `assertIsDisplayed()`
    * `assertIsNotDisplayed()`
    * `assertExists()`
    * `assertDoesNotExist()`
    * `assertTextEquals("expected text")`
    * `assertIsEnabled()`, `assertIsNotEnabled()`
* **Semantics Tree:** Compose testing relies on the Semantics Tree, which describes your UI in a way
  that's understandable by testing tools and accessibility services. You can add semantics
  properties to your composables using `Modifier.semantics`.
* **Test Tags:** Use `Modifier.testTag("yourTestTag")` to assign a unique identifier to your
  composables. This makes them easier and more reliable to find in tests.

```
// In your Composable function
Button(
    onClick = { /* ... */ },
    modifier = Modifier.testTag("loginButton") // Assign a test tag
) {
    Text("Login")
}

// In your test
composeTestRule.onNodeWithTag("loginButton").performClick()
```

* **Synchronization:** Similar to Espresso, the Compose testing framework handles synchronization
  automatically for most cases. It waits for the UI to be idle before proceeding with the test.
* **`waitUntil`:** For more complex synchronization needs, you can use
  `composeTestRule.waitUntil {}` to wait for a specific condition to become true.

### Best Practices for Compose Testing

* **Use Test Tags:** Prefer `onNodeWithTag` over `onNodeWithText` for more robust tests.
* **Isolate Composable Tests:** When possible, test individual composables in isolation using
  `createComposeRule()` and `setContent`. This makes tests faster and less flaky.
* **Test State Changes:** Verify that your UI reacts correctly to state changes.
* **Test User Interactions:** Simulate user interactions like clicks, scrolls, and text input.
* **Verify Visuals and Content:** Ensure that the correct information is displayed and that elements
  are visible or hidden as expected.
* **Keep Tests Concise:** Each test should focus on a specific aspect of the composable or screen.
* **Leverage Semantics:** Understand and use semantics properties to make your composables more
  testable and accessible.

## General UI Testing Best Practices (Applicable to both)

* **Hermetic Tests:** Tests should be independent and not rely on the state left by previous tests.
* **Readable and Maintainable:** Write clean, well-documented test code.
* **Test Real User Flows:** Focus on testing scenarios that a real user would encounter.
* **Don't Test Business Logic:** UI tests should focus on UI behavior. Unit tests are better suited
  for testing business logic.
* **Mock Dependencies:** Mock external dependencies (like network APIs, databases) to make tests
  faster, more reliable, and focused on UI interactions. Frameworks like Mockito can be used.
* **Run on Different Configurations:** Test your UI on various screen sizes, orientations, and API
  levels if possible. Emulators and real devices can be used.
* **CI Integration:** Integrate your UI tests into your Continuous Integration (CI) pipeline to
  catch regressions early.

## Conclusion

UI testing is an indispensable part of Android development. By leveraging Espresso for XML-based UIs
and the Compose Testing API for Jetpack Compose, you can build robust and reliable tests that ensure
your application's user interface functions correctly and provides a great user experience. Remember
to follow best practices and keep your tests focused, readable, and maintainable.
