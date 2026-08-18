# Implementation Plan - Core Architecture Restructuring

I will restructure the Remmi project to follow the requested hierarchy, separating the system environment (Host) from the running instance (Runtime) and the user interface (App).

## User Review Required

> [!IMPORTANT]
> This refactoring moves `RemmiHost` and `RemmiRuntime` from being UI-centric components to being the core logic and state owners of the application.
> - `RemmiHost` will be initialized in `MainActivity` and passed down to the UI.
> - New components `ServiceManager` and `EventBus` will be introduced to standardize service access and event handling.

## Proposed Changes

### [Component] Core Logic & Environment

#### [NEW] [EventBus.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/events/EventBus.kt)
- Implement a simple `EventBus` using Kotlin `SharedFlow` for asynchronous event distribution.

#### [NEW] [ServiceManager.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/service/ServiceManager.kt)
- Create a `ServiceManager` to centralize and manage the lifecycle of core services like `AndroidService`.

#### [MODIFY] [RemmiRuntime.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/runtime/RemmiRuntime.kt)
- Transform `RemmiRuntime` into a class that initializes and holds:
    - `PluginManager`
    - `ServiceManager`
    - `EventManager`
    - `EventBus`
- It will no longer depend on `RuntimeContext` but will be the context itself.

#### [MODIFY] [RemmiHost.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/host/RemmiHost.kt)
- Transform `RemmiHost` into a class that "owns the environment".
- It will hold:
    - `RemmiRuntime`
    - `AutomationEngine`
- It will manage the startup and shutdown of the entire Remmi system.

### [Component] User Interface

#### [MODIFY] [MainActivity.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/MainActivity.kt)
- Instantiate `RemmiHost` once in `onCreate`.
- Pass the `RemmiHost` instance to `RemmiApp`.

#### [MODIFY] [RemmiApp.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/RemmiApp.kt)
- Take `RemmiHost` as a parameter.
- Use `CompositionLocal` (optional) or direct passing to provide the Host/Runtime state to `AppNavigation` and screens.

#### [MODIFY] [AppNavigation.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/navigation/AppNavigation.kt)
- Update to consume the new `RemmiRuntime` or `RemmiHost` instead of the old `Context` or `RuntimeContext`.

### [Cleanup]
- Remove `RuntimeContext.kt` if it becomes redundant after the refactor.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure all type mismatches and missing imports are resolved.

### Manual Verification
- Launch the app and verify that plugins are still loaded correctly (logged in Logcat).
- Verify that navigation works and screens can access the plugin data.
- Check that the `AutomationEngine` still receives events through the new hierarchy.
