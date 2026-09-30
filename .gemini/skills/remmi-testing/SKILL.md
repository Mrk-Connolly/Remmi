---
name: remmi-testing
description: Guidance for unit, integration, Compose UI, and persistence testing in Remmi. Covers test levels, observable behavior, AAA pattern, deterministic testing, launcher-critical test paths, and real test execution requirements. Activate when writing, updating, or debugging tests in Remmi.
---

# Remmi Testing Skill

## Testing Methodology

### 1. Core Testing Rule
Test observable behavior, not internal implementation details.
Use the **Arrange-Act-Assert (AAA)** pattern for clarity.

### 2. Test Levels & Strategy
* **Unit Tests (`app/src/test/`)**: Fast, deterministic tests for ViewModels, business logic, model transformations, automation engine rules, and plugin logic.
* **Integration Tests (`app/src/test/` or `app/src/androidTest/`)**: Test interaction between repositories, database services, and core components.
* **Compose UI Tests (`app/src/androidTest/`)**: Test composable rendering, user interaction, semantics, and navigation state using `createComposeRule()`.
* **Persistence Tests**: Verify Room migration, database service transactions, and memory provider syncing.

### 3. Launcher-Critical Test Paths
Ensure full coverage for critical workflows:
* Process startup & `RemmiController` initialization
* Home launcher rendering and state restore
* App launching and returning to Home
* App drawer search
* Plugin loading, opening, and switching
* Smooth list/grid scrolling
* Graceful subsystem failure handling (e.g. plugin error isolation)

### 4. Command Execution Guidelines
* Run JVM Unit Tests: `./gradlew :app:testDebugUnitTest`
* Run specific test class: `./gradlew :app:testDebugUnitTest --tests "com.remmi.app.core.eventBus.EventBusTest"`
* Run Android Instrumentation Tests: `./gradlew :app:connectedCheck` (if device/emulator is connected)

**NEVER report a test as passed unless it was actually executed.**
