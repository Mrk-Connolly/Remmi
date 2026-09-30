# Remmi AI Testing Contract

## 1. Purpose

This contract defines how AI agents must test Remmi.

The goal is to produce tests that:

* verify real behavior
* catch regressions
* protect existing functionality
* test important user flows
* remain maintainable
* run reliably
* provide useful failure information

Testing must validate the existing application, not redesign it.

---

## 2. Core Rule

**Test the application as it exists.**

Do not modify production architecture merely to make testing easier.

Do not introduce:

* Hilt / Dagger / Koin
* new managers
* new services
* new repositories
* new EventBus systems
* new state-management architecture
* new navigation architecture
* new Gradle modules
* test-only production abstractions

unless explicitly requested.

---

## 3. Before Writing Tests

Before creating tests:

1. Inspect the relevant production code.
2. Understand the existing architecture.
3. Identify existing tests and testing conventions.
4. Identify the behavior that needs protection.
5. Determine the appropriate test level.
6. Reuse existing test utilities and fixtures.
7. Avoid duplicating existing coverage.

Do not immediately write tests without understanding the implementation.

---

## 4. Testing Pyramid

Prefer the lowest test level that can reliably verify the behavior.

```text
              UI / E2E
             ─────────
          Integration Tests
        ───────────────────
             Unit Tests
      ─────────────────────────
```

### Unit tests

Use for:

* business rules
* transformations
* validation
* calculations
* ViewModels
* state transitions
* plugin logic
* data mapping

Unit tests should be fast and deterministic.

### Integration tests

Use when behavior depends on multiple real components.

Examples:

* database + repository
* repository + ViewModel
* plugin + existing infrastructure
* persistence behavior
* Android framework integration

### UI tests

Use for important user-visible behavior.

Test:

* rendering
* interaction
* navigation
* state changes
* accessibility semantics
* important user flows

### End-to-end tests

Use selectively for critical complete workflows.

Do not create E2E tests for every feature.

---

## 5. What to Test

Tests should focus on **observable behavior**, not implementation details.

Prefer:

```text
Given a shopping list with three items,
when the user completes one item,
then the completed item is shown as completed
and the remaining count decreases.
```

Avoid tests that merely verify:

```text
privateMethodX() was called()
```

unless that interaction itself is the behavior being tested.

---

## 6. Test Naming

Test names must clearly describe:

**condition → action → expected result**

Examples:

```text
emptyShoppingList_showsEmptyState()

addingItem_addsItemToList()

completingItem_decreasesRemainingCount()

invalidContact_doesNotSave()

selectingPlugin_displaysPluginContent()
```

Use the project's existing naming convention when one already exists.

---

## 7. Arrange / Act / Assert

Where appropriate, structure tests as:

```text
Arrange
Act
Assert
```

Example:

```kotlin
// Arrange
val list = createShoppingList()

// Act
viewModel.addItem("Milk")

// Assert
assertThat(viewModel.items).contains("Milk")
```

Keep tests focused and easy to read.

---

## 8. Test Behavior, Not Implementation

A refactor that preserves behavior should not require rewriting large numbers of tests.

Tests should generally assert:

* resulting state
* returned values
* persisted data
* visible UI
* navigation outcome
* emitted user-visible events
* error behavior

Avoid unnecessarily asserting internal implementation structure.

---

## 9. Edge Cases

For meaningful functionality, consider:

* empty input
* missing data
* invalid input
* duplicate data
* boundary values
* very large values
* rapid repeated actions
* interrupted operations
* unavailable data
* permission denial
* permission granted
* network failure where applicable
* database failure where applicable
* lifecycle changes where applicable

Do not create artificial edge cases that cannot occur in the application.

---

## 10. State Testing

For stateful UI and ViewModels, test meaningful state transitions.

Example:

```text
Initial
  ↓
Loading
  ↓
Loaded

Initial
  ↓
Loading
  ↓
Error

Loaded
  ↓
User action
  ↓
Updated
```

Verify both the state and the resulting user-visible behavior.

---

## 11. Compose UI Testing

For Jetpack Compose UI, test behavior through semantics and user interaction.

Prefer:

* semantic queries
* visible text
* content descriptions
* roles
* state
* user actions

Examples:

```kotlin
onNodeWithText("Shopping").assertIsDisplayed()

onNodeWithText("Add item").performClick()

onNodeWithText("Milk").assertIsDisplayed()
```

Avoid relying on fragile implementation details such as:

* internal layout structure
* exact node hierarchy
* arbitrary coordinates
* implementation-specific tags unless necessary

Use test tags when a stable semantic target cannot otherwise be exposed.

---

## 12. Accessibility Testing

UI tests should verify important accessibility behavior.

Where relevant, check:

* content descriptions
* semantic roles
* enabled/disabled state
* selected state
* readable labels
* meaningful traversal
* touch interaction

A visually correct UI is not automatically an accessible UI.

---

## 13. Navigation Testing

Important navigation flows should verify:

```text
User action
    ↓
Expected destination
    ↓
Expected content/state
```

Test critical paths rather than every possible navigation combination.

Do not duplicate the entire navigation graph in tests.

---

## 14. Plugin Testing

Each plugin should have tests appropriate to its responsibility.

At minimum, test:

* primary functionality
* important state transitions
* invalid input
* empty state
* error state
* persistence where applicable
* interaction with shared infrastructure where applicable

Plugin tests must respect the existing plugin architecture.

Do not move plugin business logic merely to make it easier to test.

---

## 15. Data and Persistence Testing

Where Remmi persists data, test:

* creation
* retrieval
* modification
* deletion
* empty database behavior
* duplicate handling
* important relationships
* persistence across relevant lifecycle boundaries

Tests must use the existing persistence architecture.

Do not replace the real persistence mechanism with a new abstraction solely for testing.

---

## 16. Determinism

Tests must be deterministic.

Avoid:

* arbitrary delays
* real-time dependencies
* random values without controlled seeds
* external services unless explicitly testing integration
* tests that depend on execution order
* shared mutable test state

Prefer controlled:

* clocks
* test data
* coroutine execution
* temporary storage
* fake/stub external boundaries where the existing architecture permits it

---

## 17. Test Isolation

Each test should be independently runnable.

A test must not depend on:

* another test running first
* previous database state
* previous application state
* a particular test execution order

Clean up resources after tests.

---

## 18. External Dependencies

Do not make unit tests depend on:

* production servers
* real user accounts
* real network connectivity
* external APIs
* device-specific state

unless the test is intentionally an integration/E2E test.

Use appropriate existing test doubles where necessary.

Do not introduce a new production abstraction solely because a test would otherwise be difficult.

---

## 19. Permissions

Where functionality depends on Android permissions, test relevant permission states when practical:

```text
Permission granted
Permission denied
Permission revoked
```

The application should fail gracefully when permissions are unavailable.

---

## 20. Error Handling

Tests should verify that expected failures produce useful application behavior.

For example:

```text
Failure
  ↓
Application remains stable
  ↓
User receives understandable feedback
  ↓
Recovery is possible where appropriate
```

Do not test only successful paths.

---

## 21. Regression Testing

When fixing a bug:

1. Reproduce the bug.
2. Write a regression test that fails before the fix.
3. Implement the fix.
4. Verify the test passes.
5. Run relevant existing tests.

The regression test should prevent the same bug from silently returning.

---

## 22. New Features

For a new feature, tests should normally cover:

### Required

* primary success path
* important state changes
* important failure path
* relevant edge cases

### When applicable

* persistence
* navigation
* permissions
* accessibility
* lifecycle behavior
* plugin integration

Do not create large quantities of low-value tests merely to increase test count.

---

## 23. Test Quality

A good test should be:

* deterministic
* focused
* readable
* meaningful
* maintainable
* independent
* fast when possible
* resistant to unrelated refactoring

Avoid:

* duplicate tests
* giant tests covering many unrelated behaviors
* excessive mocking
* testing framework behavior instead of application behavior
* assertions with no meaningful relationship to the requirement

---

## 24. Coverage

Code coverage is a signal, not the objective.

Prioritize coverage of:

1. critical user flows
2. business rules
3. data integrity
4. important error paths
5. state transitions
6. regression-prone functionality

Do not add meaningless tests simply to increase a percentage.

---

## 25. Test Execution

After changing tests:

1. Run the most relevant test suite.
2. Run broader tests when practical.
3. Verify compilation.
4. Investigate failures rather than hiding them.
5. Report what was actually executed.

Never claim that tests passed unless they were actually run.

Clearly distinguish:

```text
Tests written
Tests executed
Tests passed
Tests failed
Tests not executed
```

---

## 26. Failure Reporting

When a test fails, report:

* test name
* failure reason
* affected functionality
* whether the failure appears related to the change
* relevant stack trace/context
* recommended next investigation

Do not simply rerun failing tests repeatedly without investigating the cause.

---

## 27. Test Changes Must Not Hide Production Problems

Do not modify tests merely to make them pass.

Do not weaken assertions because the implementation is incorrect.

Do not add excessive mocks that hide broken integration.

If the production behavior is wrong, fix the production behavior.

If the test expectation is wrong, explain why and correct the test.

---

## 28. Architecture Boundary

Testing does not authorize architectural changes.

Do not change:

```text
core/
database/
EventBus/
plugins/
repositories/
services/
managers/
navigation/
state management/
Gradle structure/
```

solely because testing is inconvenient.

Tests must adapt to the existing architecture unless the user explicitly requests architectural changes.

---

## 29. Test Task Procedure

For every testing task:

1. Read the Architecture Contract.
2. Read the UI Design Skill when UI is involved.
3. Inspect existing tests.
4. Inspect the relevant production implementation.
5. Identify the behavior to protect.
6. Choose the lowest appropriate test level.
7. Reuse existing test infrastructure.
8. Write focused tests.
9. Run the relevant tests.
10. Run broader regression tests when appropriate.
11. Report exact results.
12. Confirm whether production architecture was changed.

---

## 30. Final Principle

**Tests should protect behavior, not implementation.**

The purpose of testing Remmi is to make changes safer, detect regressions earlier, and provide confidence that the application continues to behave correctly.

A small set of meaningful, reliable tests is more valuable than a large collection of fragile tests.
