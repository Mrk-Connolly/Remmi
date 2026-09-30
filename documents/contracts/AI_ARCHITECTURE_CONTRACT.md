# Remmi Architecture Contract

## 1. Core Principle

Remmi must remain **small, efficient, strong, predictable, and easy to understand**.

The architecture should prefer:

* Few strong systems
* Clear ownership
* Minimal dependencies
* Minimal communication hops
* Minimal duplicated state
* Minimal unnecessary work
* Measurable performance
* Graceful failure
* Reuse of existing infrastructure

Do not add architecture merely because the application is becoming larger.

**A larger application should not require a more complicated core.**

---

# 2. Primary Architecture

```text
Remmi
│
├── core/
├── ui/
└── plugins/
```

### Core

Provides fundamental Remmi infrastructure and Android integration.

### UI

Provides the Remmi user experience.

### Plugins

Provide Remmi functionality and feature-specific behavior.

The three areas must have clearly separated responsibilities.

---

# 3. Core

Core contains only systems fundamental to Remmi itself.

```text
core/
├── automation/
├── database/
├── android/
├── eventBus/
├── host/
├── controller/
└── models/
```

## Core responsibilities

### `core/automation/`

Shared automation infrastructure.

Automation must not contain plugin-specific business logic.

### `core/database/`

Generic persistence infrastructure.

Core owns:

* Room
* database connection
* DAOs
* database entities
* queries
* persistence implementation
* generic database commands

Plugins own:

* plugin models
* plugin repositories
* plugin understanding of their data
* construction of appropriate database commands

Plugins must **not** directly access:

* Room
* DAOs
* database instances
* SQL
* database entities
* database implementation details

### `core/eventBus/`

There is exactly **one EventBus**.

It provides communication between independent Remmi systems.

EventBus must remain simple.

It must not become:

* a business-logic engine
* a database layer
* an Android abstraction layer
* a plugin manager
* a state-management framework

### `core/host/`

Contains the Remmi runtime/application host.

### `core/controller/`

Coordinates Core systems.

The Controller must not contain:

* plugin business logic
* database operations
* Android implementation
* UI logic
* automation implementation

### `core/models/`

Contains only shared models/interfaces required across architectural boundaries.

Plugin-specific models remain inside their plugins.

---

# 4. Android Systems

Android functionality belongs inside:

```text
core/android/
```

Android integrations are **capability systems**, not new architectural layers.

Possible capabilities include:

```text
core/android/
├── apps/
├── launcher/
├── notifications/
├── widgets/
├── permissions/
├── intents/
├── share/
├── quickSettings/
├── roles/
└── ...
```

These are examples of organization, not permission to create unnecessary systems.

Each Android capability should be independently responsible for:

* Android APIs
* Android lifecycle
* Android-specific state
* Android-specific implementation
* Android callbacks
* conversion between Android information and Remmi information

An Android capability must not contain plugin business logic.

---

# 5. Android Services Are Capability Systems

Android services are no longer treated as generic "read/write" services.

They are independent systems that understand and manage their specific Android capability.

For example:

```text
Notification System
    ↓
Android Notification APIs
    ↓
Android

Android Apps System
    ↓
PackageManager
    ↓
Android
```

The Android system converts Android-specific information into appropriate Remmi-facing state/events.

It may maintain its own internal state when necessary.

Avoid creating one giant:

```text
AndroidService
```

that knows everything about Android.

Prefer focused capability systems.

---

# 6. EventBus Communication

EventBus is the communication backbone between **independent Remmi systems**.

For example:

```text
Android
   ↓
Android Capability
   ↓
EventBus
   ↓
Plugin / UI / Automation
```

or:

```text
Plugin
   ↓
EventBus
   ↓
Android Capability
   ↓
Android
```

However:

**EventBus is not required for every function call.**

Do not turn simple synchronous/local operations into unnecessary:

```text
request → EventBus → handler → EventBus → response
```

Use direct calls when systems are appropriately coupled and the operation is simple.

Use EventBus when independent systems need to communicate or when an event/command should cross an architectural boundary.

---

# 7. Home / Launcher

Home is primarily a **Remmi UI system**, not an Android infrastructure system.

Conceptually:

```text
ui/
└── home/
```

Home owns:

* Home layout
* Home UI state
* gestures
* user interaction
* plugin presentation
* app presentation
* launcher experience

Home must not contain Android implementation details such as:

* PackageManager
* NotificationListenerService implementation
* AppWidgetHost implementation
* Android permission internals
* Android launcher lifecycle implementation

Those belong in `core/android`.

The distinction is:

```text
Home
= What the user sees and interacts with

Android Launcher Capability
= How Remmi participates in Android's HOME system
```

---

# 8. Plugins

Plugins are Remmi's feature systems.

Examples:

```text
plugins/
├── Tasks/
├── Calendar/
├── Notes/
├── Shopping/
├── Contacts/
└── ...
```

A plugin owns its:

* feature logic
* plugin models
* repository
* actions
* widgets
* plugin-specific state
* plugin-specific behavior

Plugins must not duplicate Core infrastructure.

Plugins must not directly implement Android platform infrastructure that belongs in `core/android`.

Plugins should use the existing Core systems and Android capabilities.

---

# 9. Ownership Rule

Every responsibility must have **one clear owner**.

Examples:

```text
Database        → Database Core
Communication   → EventBus
Coordination    → Controller
Automation      → Automation Core
Android APIs    → Android Capability
Home UI         → UI/Home
Tasks logic     → Tasks Plugin
Calendar logic  → Calendar Plugin
```

Do not create duplicate ownership.

If two systems appear to own the same responsibility, the architecture should be reconsidered.

---

# 10. State

Use `StateFlow` / `Flow` for observable state where appropriate.

The distinction is:

```text
StateFlow
    = What is the current state?
```

and:

```text
EventBus
    = Something happened / something is requested.
```

State should have a clear owner.

Avoid creating a giant global application state.

Existing global UI state may remain where it is already part of the working architecture, but it must not become a dumping ground for plugin, database, Android, or automation state.

---

# 11. Main Thread

The main thread is reserved for UI work.

Do not block it with:

* database operations
* filesystem operations
* network operations
* expensive Android queries
* parsing
* CPU-heavy computation
* synchronization
* large data transformations

Use Kotlin Coroutines and appropriate dispatchers for background work.

The Home experience must remain responsive.

---

# 12. Startup

Remmi is intended to be a major Android entry point and potentially the user's launcher.

Startup is therefore a critical path.

The startup sequence should conceptually be:

```text
Launch
 ↓
Show Home
 ↓
Make Home interactive
 ↓
Load essential Home information
 ↓
Initialize secondary systems
 ↓
Initialize non-critical functionality
```

Do not require every plugin and every Android capability to fully initialize before Home becomes usable.

Use lazy/deferred initialization where appropriate.

---

# 13. Failure Isolation

Failure in one subsystem must not unnecessarily bring down Remmi.

For example:

```text
Calendar fails
     ↓
Home continues

Shopping fails
     ↓
Home continues

Notification capability fails
     ↓
Home continues

Automation fails
     ↓
Home continues

One plugin fails
     ↓
Other plugins continue
```

The primary Home/launcher experience has priority.

Secondary functionality should fail and recover independently whenever practical.

---

# 14. Graceful Degradation

Remmi should remain useful even when optional functionality is unavailable.

The architecture should allow:

```text
Home
 ↓
App launching
 ↓
Core functionality
 ↓
Plugins
 ↓
Android integrations
 ↓
Automation / secondary systems
```

A failure at a lower level should not automatically destroy higher-level functionality.

---

# 15. Performance

Performance is an architectural requirement, not a final optimization phase.

Remmi should minimize:

* unnecessary work
* unnecessary allocations
* unnecessary database queries
* unnecessary Android queries
* unnecessary EventBus hops
* duplicated state
* duplicated processing
* unnecessary initialization

Performance should be **measured**, not assumed.

---

# 16. Technology Stack

Remmi should use a small, cohesive technology stack.

### Primary

* Kotlin
* Jetpack Compose
* Kotlin Coroutines
* Kotlin Flow / StateFlow
* Room
* Existing EventBus
* WorkManager where appropriate

### Android

Normal Android APIs and platform components are used inside `core/android`.

### Performance

* R8
* Baseline Profiles
* Macrobenchmark
* Perfetto / Android tracing

These technologies complement the architecture rather than creating another architectural layer.

---

# 17. WorkManager

WorkManager is for reliable, deferrable background work.

Appropriate examples include:

* periodic synchronization
* maintenance
* cleanup
* retryable background operations

Do not use WorkManager merely to perform an operation that needs an immediate response.

Immediate operations should normally use direct APIs/coroutines.

---

# 18. Baseline Profiles

Remmi should maintain Baseline Profiles for critical user journeys.

Important journeys include:

```text
Cold launch
 ↓
Home display
 ↓
Home becomes interactive
 ↓
App drawer
 ↓
App search
 ↓
Application launch
 ↓
Return to Home
 ↓
Plugin interaction
```

Baseline Profiles should focus on the actual Remmi experience.

---

# 19. Macrobenchmark

Macrobenchmark should measure important user journeys.

At minimum, benchmark:

* startup
* Home
* app drawer
* application search
* application launching
* returning to Home
* plugin switching
* scrolling
* other performance-critical interactions

Performance changes should be measured against previous behavior.

Do not rely solely on subjective "feels faster" judgments.

---

# 20. Perfetto

Perfetto / Android system tracing is the diagnostic tool for investigating real performance problems.

The process should be:

```text
Benchmark
 ↓
Identify regression
 ↓
Trace
 ↓
Find bottleneck
 ↓
Fix
 ↓
Benchmark again
```

Do not add complexity merely because a performance problem is suspected.

Measure first.

---

# 21. R8 / Release Optimization

Production performance must be evaluated using release-like builds.

Performance testing should account for:

* R8
* shrinking
* optimization
* Baseline Profiles
* production configuration

Debug performance must not be treated as representative of the final application.

---

# 22. Performance Regression Protection

Performance should be part of development, not something checked only before release.

When a significant feature is added or changed, consider whether it affects:

* startup
* Home rendering
* scrolling
* memory
* database performance
* plugin switching
* Android integration performance
* app launching

Macrobenchmark should be used where the affected behavior can be measured.

The goal is to prevent Remmi from becoming progressively slower as functionality grows.

---

# 23. Technologies Not Automatically Allowed

Do not introduce the following merely because the project is growing:

* Hilt
* Dagger
* Koin
* additional EventBus systems
* additional state-management frameworks
* unnecessary navigation frameworks
* additional database technologies
* generic Manager layers
* generic Service layers
* duplicate infrastructure
* new Gradle modules
* new architecture layers

A new technology must solve a demonstrated problem that the existing architecture cannot reasonably solve.

---

# 24. Core Simplicity Rule

Before adding anything to Core, ask:

1. Is this fundamental to Remmi?
2. Does more than one system genuinely require it?
3. Does it belong in Core?
4. Does an existing Core system already solve the problem?
5. Is the abstraction actually necessary?
6. Will it improve the architecture rather than merely increase its size?

If not, do not add it.

**Core should provide a small number of strong primitives rather than a large collection of services.**

---

# 25. Architectural Direction

The intended relationship is:

```text
                         REMMI
                           │
             ┌─────────────┼─────────────┐
             │             │             │
             UI          PLUGINS        CORE
             │             │             │
          Home/etc.   Tasks/Calendar/    │
                       Notes/etc.        │
                                         │
                         ┌───────────────┤
                         │       │       │
                     Database EventBus Android
                                         │
                              Android Capabilities
```

The responsibilities are:

```text
Plugins
    → What Remmi does

UI
    → How the user interacts with Remmi

Core
    → What Remmi fundamentally needs

Android Systems
    → How Remmi interacts with Android

EventBus
    → How independent Remmi systems communicate
```

---

# 26. Fundamental Rule

The architecture should become **more capable without becoming proportionally more complicated**.

When Remmi gains a new capability:

```text
New capability
      ↓
Find its existing owner
      ↓
Reuse existing infrastructure
      ↓
Add the smallest required implementation
      ↓
Keep ownership clear
      ↓
Measure performance if the critical path is affected
```

Do not solve growth by continuously adding layers.

**The strength of Remmi comes from clear boundaries, single ownership, isolated failures, minimal communication, fast startup, and a small Core.**
