# Android Architecture Guide

Choosing a robust and scalable architecture is crucial for building maintainable and testable
Android applications. This guide explores common architectural patterns and best practices for
organizing your codebase.

## 1. Why is Architecture Important?

A well-defined architecture:

* **Improves Maintainability:** Makes it easier to understand, modify, and extend the codebase.
* **Enhances Testability:** Separates concerns, allowing for easier unit and integration testing.
* **Facilitates Collaboration:** Provides a clear structure for team members to work on different
  parts of the app simultaneously.
* **Increases Scalability:** Allows the app to grow in complexity without becoming unmanageable.
* **Reduces Bugs:** Enforces separation of concerns, minimizing the chances of unintended side
  effects.

## 2. Common Architectural Patterns

Several architectural patterns are popular in Android development. The choice often depends on the
project's complexity, team familiarity, and specific requirements.

### a) Model-View-ViewModel (MVVM)

MVVM is one of the most widely adopted architectures in modern Android development, especially with
Jetpack Compose and Data Binding.

* **Model:** Represents the data and business logic of the application. It's responsible for
  retrieving, storing, and manipulating data. This can include repositories, use cases, data
  sources (network, database), etc.
* **View:** The UI layer (Activities, Fragments, Composable functions). It observes the ViewModel
  for data changes and updates the UI accordingly. It also forwards user interactions to the
  ViewModel. The View should be as "dumb" as possible, containing minimal logic.
* **ViewModel:** Acts as a bridge between the Model and the View. It holds and prepares UI-related
  data for the View and handles user interactions. The ViewModel is lifecycle-aware and survives
  configuration changes (like screen rotations). It exposes data via observable data holders (e.g.,
  `LiveData`, `StateFlow`).

**Key Benefits of MVVM:**

* Excellent for UI-heavy applications.
* Good separation of concerns.
* Highly testable ViewModels.
* Integrates well with Android Jetpack components.

### b) Clean Architecture

Clean Architecture, proposed by Robert C. Martin (Uncle Bob), emphasizes separation of concerns by
dividing the software into layers with strict dependency rules. The core idea is that inner layers
should not know anything about outer layers.

**Typical Layers:**

1. **Entities (Innermost):** Represent core business objects and rules. These are plain Kotlin/Java
   objects and are independent of any other layer.
2. **Use Cases (Interactors):** Contain application-specific business logic. They orchestrate the
   flow of data between Entities and the outer layers (e.g., fetching data from a repository and
   preparing it for presentation).
3. **Interface Adapters:** Convert data from the format most convenient for Use Cases and Entities
   to the format most convenient for external agencies like the database or the web. This layer
   includes Presenters (for MVP), ViewModels (for MVVM), and Gateways/Repositories.
4. **Frameworks & Drivers (Outermost):** Consists of frameworks and tools like the UI (Activities,
   Fragments, Compose), database implementations, network clients, etc.

**Dependency Rule:** Source code dependencies can only point inwards. Nothing in an inner circle can
know anything at all about something in an outer circle.

**Key Benefits of Clean Architecture:**

* Maximum separation of concerns.
* High testability and maintainability.
* Framework independence (business logic is not tied to Android framework).
* Scalability for large and complex applications.

**Common Implementation in Android:** Often combined with MVVM for the presentation layer.

* **Domain Layer:** Contains Entities and Use Cases. Pure Kotlin/Java module.
* **Data Layer:** Contains Repository implementations, data sources (network, local database), and
  data mapping logic.
* **Presentation Layer (UI):** Contains Activities, Fragments, ViewModels (following MVVM), and
  UI-specific logic. Depends on the Domain layer.

### c) Model-View-Intent (MVI)

MVI is a reactive architectural pattern that focuses on a unidirectional data flow and immutable
states.

* **Model:** Represents the state of the UI. It's typically an immutable data class.
* **View:** Renders the Model (state) and captures user Intents.
* **Intent:** Represents a user's intention to perform an action (e.g., "load data," "refresh
  page," "click button"). These are not Android `Intent`s.

**Unidirectional Data Flow:**

1. User interacts with the View, generating an Intent.
2. The Intent is processed (often by a ViewModel or Presenter-like component).
3. This processing might involve fetching data or business logic, resulting in a new Model (state).
4. The View observes the Model and re-renders itself with the new state.

**Key Benefits of MVI:**

* Predictable state management.
* Easier debugging due to clear data flow.
* Well-suited for reactive programming paradigms (e.g., using RxJava or Kotlin Flows).
* Improved testability.

### d) Model-View-Presenter (MVP) - Less Common Now

MVP was a popular pattern before MVVM gained traction with Android Jetpack.

* **Model:** Same as in MVVM.
* **View:** Passive interface that displays data and routes user commands to the Presenter.
  Typically an Activity or Fragment implementing a View interface.
* **Presenter:** Retrieves data from the Model and updates the View. It also handles user
  interactions from the View. There's a one-to-one relationship between a View and a Presenter, with
  the Presenter holding a reference to the View interface.

**Challenges with MVP:**

* Can lead to tight coupling between View and Presenter if not carefully managed.
* Presenter can become a large class if it handles too much.
* Lifecycle management can be more complex compared to MVVM's `ViewModel`.

## 3. Organizing Your Project by Modules

For larger applications, organizing your code into modules (Gradle modules) based on features or
layers provides significant benefits:

* **Improved Build Times:** Gradle can build modules in parallel and cache unchanged modules.
* **Better Encapsulation:** Clear boundaries between different parts of your app.
* **Reusability:** Modules can potentially be reused across different projects.
* **Team Scalability:** Different teams can own and work on different modules independently.

**Common Module Structures:**

### a) Layer-Based Modules:

Often seen with Clean Architecture:

* `:app` (Presentation Layer): Contains Activities, Fragments, ViewModels, UI elements. Depends on
  `:domain` and sometimes `:data` (for DI or specific cases).
* `:domain` (Domain Layer): Contains Use Cases, Entities, and Repository interfaces. Pure
  Kotlin/Java module. No Android framework dependencies.
* `:data` (Data Layer): Contains Repository implementations, network API clients, database DAOs,
  mappers. Depends on `:domain` (to implement interfaces).

### b) Feature-Based Modules:

Each significant feature of the app gets its own module.

* `:app` (Core/Shell): Contains the main application class, dependency injection setup, and
  navigation between feature modules.
* `:core` or `:common`: Contains shared utilities, base classes, UI components, etc., used by
  multiple feature modules.
* `:feature_login`
* `:feature_profile`
* `:feature_settings`
* `:feature_home`

Each feature module might internally follow a pattern like MVVM or be structured into its own
mini-layers (e.g., `ui`, `domain`, `data` sub-packages within the feature module). Feature modules
typically depend on `:core` and expose their functionality to the `:app` module for integration.

**Hybrid Approach:** You can also combine layer-based and feature-based modularization. For example,
have top-level `data` and `domain` modules, and then multiple feature modules for the presentation
layer.

## 4. Key Considerations

* **Dependency Injection (DI):** Use a DI framework like Hilt or Koin to manage dependencies between
  classes and layers. This promotes loose coupling and testability.
* **Single Source of Truth (SSOT):** Ensure that for any piece of data, there's a single,
  authoritative source. Repositories often serve this role.
* **State Management:** Carefully manage UI state, especially with configuration changes and
  asynchronous operations. Jetpack ViewModels, `SavedStateHandle`, `LiveData`, and Kotlin
  `StateFlow`/`SharedFlow` are valuable tools.
* **Testing:** Your architecture should make it easy to write unit tests for business logic (
  ViewModels, Use Cases, Repositories) and UI tests for user interactions.

## Conclusion

Choosing the right architecture is a foundational step in Android development. While MVVM is a
strong default for many projects, especially with Jetpack Compose, understanding principles from
Clean Architecture and patterns like MVI can help you build more robust, scalable, and maintainable
applications. Modularization further enhances these benefits for larger projects. Start simple, and
evolve your architecture as your application grows in complexity.
