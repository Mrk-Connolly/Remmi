# REMMI — MASTER ARCHITECTURE CONTRACT

## 1. Purpose

Remmi is a long-running Android application evolving into a launcher/home experience with integrated plugins and Android system capabilities.

The architecture must prioritize:

1. Reliability
2. Responsiveness
3. Simplicity
4. Clear ownership
5. Failure isolation
6. Maintainability
7. Measurable performance

The architecture must become more capable without becoming proportionally more complicated.

---

# 2. Governing Principle

> Prefer the smallest correct change that preserves the existing architecture, performance, reliability, ownership boundaries, and working behavior.

A feature request does not authorize an architectural redesign.

Do not introduce abstractions, frameworks, layers, managers, services, repositories, modules, or state-management systems unless the existing architecture cannot correctly support the requirement.

---

# 3. Instruction Priority

When instructions conflict, use this order:

1. Android platform requirements
2. This Remmi Architecture Contract
3. Existing working Remmi implementation
4. Explicit feature request
5. Remmi UI Design rules
6. Remmi Testing rules
7. Developer convenience or refactoring ideas

Never sacrifice an established architectural boundary merely because another implementation is easier.

---

# 4. Primary Structure

Remmi consists of three primary areas:

```
Remmi/
├── core/
├── ui/
└── plugins/
```

Their responsibilities are distinct.

---

# 5. Core

Core contains fundamental infrastructure required by Remmi itself.

Current core structure:

```
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

Core may provide:

* EventBus
* Controller
* Host
* Database infrastructure
* Automation infrastructure
* Generic Android capability infrastructure
* Shared models/interfaces genuinely required across boundaries

Core must not contain:

* Plugin business logic
* Plugin-specific repositories
* Plugin-specific models
* Plugin UI
* Feature-specific workflows
* Duplicate infrastructure
* Generic "manager" layers
* Convenience abstractions without a demonstrated need

Core should provide a small number of strong primitives.

---

# 6. UI

UI contains Remmi presentation.

```
ui/
├── screens/
├── popups/
├── components/
└── home/
```

Home is part of Remmi UI.

Home may coordinate presentation and user interaction, but must not directly implement Android infrastructure.

Home must not directly depend on:

* PackageManager
* NotificationListenerService internals
* AppWidgetHost internals
* Android service implementation details
* Room/DAO implementation
* Plugin internal implementation

Home communicates with the owning system through appropriate interfaces/state/events.

---

# 7. Plugins

Plugins are Remmi's feature applications.

A plugin owns:

* Plugin-specific behavior
* Plugin-specific models
* Plugin-specific repositories
* Plugin-specific actions
* Plugin-specific UI/widgets where appropriate
* Plugin-specific interpretation of its data

A plugin must not:

* Access Room directly
* Access DAOs directly
* Access database instances directly
* Access another plugin's internal implementation
* Implement Android system infrastructure that belongs in core/android
* Duplicate core infrastructure

Plugins communicate with other independent systems through the existing EventBus or appropriate shared interfaces.

---

# 8. Database Ownership

Database infrastructure belongs to:

```
core/database/
```

Core owns:

* Room configuration
* Database instance
* DAO infrastructure
* Database entities
* Queries
* Connections
* Persistence implementation
* Schema/migration infrastructure

Plugins own:

* Their domain models
* Their repositories
* Their understanding of their data
* Construction of generic database commands

Plugins must never directly access Room, DAOs, database instances, SQL, or database entities.

Whenever persisted schema changes, the database bootstrap/load mechanism must also be updated.

---

# 9. Android Capability Infrastructure

Android-specific infrastructure belongs under:

```
core/android/
```

Examples include:

* Launcher integration
* Installed application discovery
* Notifications
* Widgets
* Permissions
* Intents
* Sharing
* Quick Settings
* Android roles
* Other Android capability bridges

Each capability should be as self-contained as practical.

An Android capability owns interaction with the Android platform.

The rest of Remmi should consume the capability rather than reproducing Android implementation details.

---

# 10. Home / Launcher

Home is primarily a Remmi UI system.

Conceptually:

```
Android
   ↓
core/android/launcher
   ↓
Remmi systems
   ↓
ui/home
```

The Android launcher integration handles Android's HOME role and platform interaction.

The Home UI handles:

* Home presentation
* User interaction
* Launcher content presentation
* App launching through the appropriate capability
* Plugin presentation
* Assistant presentation
* Gestures
* Home customization

Home must remain independent from plugin failures.

---

# 11. EventBus

Remmi has one EventBus.

The EventBus exists for communication between independent systems.

Use EventBus for:

* Cross-system commands
* Cross-system events
* Asynchronous communication
* Notifications between independently owned systems

Do not use EventBus for every function call.

For local, synchronous, tightly coupled operations, a direct call/interface is preferable.

The EventBus must remain simple and predictable.

---

# 12. State

Use StateFlow/Flow for observable state.

Use StateFlow when a system owns current state that other components need to observe.

Do not turn EventBus into a state-management framework.

Do not introduce another state-management library.

State belongs to the system that owns it.

---

# 13. Controller

RemmiController coordinates core systems.

It must not become a god object.

The Controller must not contain:

* Plugin business logic
* Database operations
* Android implementation logic
* UI logic
* Automation implementation logic

It coordinates; it does not own unrelated behavior.

---

# 14. Main Thread

The main thread is reserved for UI responsiveness and short Android framework interactions.

Never perform avoidable:

* Database work
* File I/O
* Network work
* Expensive computation
* Large collection processing
* Blocking calls

on the main thread.

Use Kotlin coroutines and appropriate dispatchers.

---

# 15. Startup

Startup must be optimized for perceived responsiveness.

Priority:

```
Process starts
    ↓
Home becomes usable
    ↓
Secondary systems initialize
    ↓
Non-critical data/features load
```

Do not block Home startup on non-critical plugins or services.

Prefer lazy initialization where appropriate.

---

# 16. Failure Isolation

A failure in one subsystem must not unnecessarily bring down unrelated systems.

For example:

```
Calendar failure
     ↓
Calendar unavailable
```

must not become:

```
Calendar failure
     ↓
Remmi Home crashes
```

Important boundaries must fail gracefully.

The application should degrade rather than collapse.

---

# 17. Performance

Performance is an architectural constraint.

Avoid unnecessary:

* allocations
* database queries
* Android system queries
* EventBus hops
* recompositions
* repeated initialization
* background work
* retained objects
* cache growth

Measure before optimizing when practical.

Critical user journeys include:

* Startup
* Home rendering
* App drawer
* App search
* App launching
* Returning to Home
* Plugin switching
* Scrolling
* Widget interaction

Use the existing technology stack:

* Kotlin
* Jetpack Compose
* Coroutines
* Flow / StateFlow
* Room
* EventBus
* WorkManager where appropriate
* R8
* Baseline Profiles
* Macrobenchmark
* Perfetto / Android tracing

Do not add another framework for performance work unless a demonstrated requirement demands it.

---

# 18. Reliability

Remmi is a continuously available launcher-like application.

Therefore reliability requirements are stronger than for a conventional feature application.

Pay particular attention to:

* lifecycle handling
* process recreation
* service registration/unregistration
* coroutine cancellation
* listener cleanup
* receiver cleanup
* memory leaks
* unbounded collections
* repeated initialization
* Android permission changes
* default launcher state
* low-memory conditions

---

# 19. Security and Privacy

Use least privilege.

Do not request unnecessary Android permissions.

Never:

* hardcode secrets
* log sensitive user information
* expose sensitive information through debug logging
* add exported components without justification
* bypass Android permission boundaries
* store sensitive information unnecessarily

Validate external intents and Android inputs where applicable.

---

# 20. Technology Restrictions

Do not introduce by default:

* Hilt
* Dagger
* Koin
* another EventBus
* another state-management framework
* another database technology
* unnecessary navigation frameworks
* generic manager/service layers
* new Gradle modules
* duplicate infrastructure

A new dependency requires a demonstrated problem that the existing stack cannot reasonably solve.

---

# 21. Existing Working Code

Do not rewrite working systems merely because another implementation appears cleaner.

Before changing code:

1. Search the repository.
2. Locate existing functionality.
3. Identify its owner.
4. Understand its lifecycle.
5. Determine whether it can be extended.
6. Reuse it when possible.

Preserve working behavior.

---

# 22. Architecture Decision Process

For every new piece of code:

```
Is it UI?
    → ui/

Is it plugin-specific?
    → plugins/<Plugin>/

Is it Android-specific infrastructure?
    → core/android/

Is it fundamental shared infrastructure?
    → core/

Is it communication between independent systems?
    → existing EventBus

Is it observable owned state?
    → owning system's Flow/StateFlow

Does it fit nowhere?
    → STOP and reassess
```

Never create a new architectural location simply because the existing locations are inconvenient.

---

# 23. STOP Conditions

Stop implementation and reassess when:

* ownership is ambiguous
* a new architectural layer appears necessary
* a new manager/service is being proposed
* database migration safety is unclear
* Android lifecycle behavior is uncertain
* a change crosses an established boundary
* a feature requires breaking an existing contract
* a working implementation would need to be replaced without necessity

Do not silently make the architectural decision.

---

# 24. Verification

A change is not complete because the code was written.

Verify:

* compilation
* relevant tests
* architecture boundaries
* lifecycle behavior
* failure behavior
* performance impact
* database migration/bootstrap requirements
* security/privacy impact

Never claim a test passed unless it was actually run.

---

# 25. Definition of Done

A change is complete when:

* correct owner was used
* existing infrastructure was reused
* no unnecessary dependencies were introduced
* architecture boundaries remain intact
* code compiles
* relevant tests pass
* failure behavior was considered
* lifecycle behavior was considered
* performance impact was considered
* persistence changes were handled safely
* required documentation was updated
* no unrelated changes were introduced

---

# 26. Final Rule

Remmi should remain understandable from its folder structure.

If an experienced developer cannot reasonably determine where a feature belongs by looking at the architecture, the architecture is becoming too complicated.
