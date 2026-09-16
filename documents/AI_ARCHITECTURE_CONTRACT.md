# Remmi AI Architecture Contract

**This document is mandatory. Follow it for every code change.**

## 1. Absolute Rules

* Preserve the existing architecture and folder structure.
* Make the smallest change necessary.
* **Do not delete existing implementations unless explicitly requested.**
* **Do not replace an existing implementation with a different architecture unless explicitly requested.**
* Do not reorganize, rename, migrate, or redesign existing code unless explicitly requested.
* Do not create new architectural layers, managers, frameworks, abstractions, or communication systems unless explicitly requested.
* Reuse existing implementations whenever possible.
* **NEVER modify, rewrite, shorten, delete, or otherwise change this contract file.**
* If unsure where something belongs, **do not invent a new location. Ask first.**

---

# 2. Main Project Structure

The project has three main areas:

```text
core/
ui/
plugins/
```

Their responsibilities are fixed:

```text
core/       → Application infrastructure
ui/         → User interface
plugins/    → Plugins and plugin-specific functionality
```

---

# 3. `core/` — Infrastructure

`core/` contains application infrastructure only.

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

Do not create unrelated files or folders directly inside `core/`.

---

## 3.1 Automation

```text
core/automation/
├── AutomationEngine.kt
└── <feature>/
```

* `AutomationEngine.kt` is the main automation script.
* Each automation feature has its own folder.
* Automation infrastructure belongs here.
* Do not create separate automation systems elsewhere.

---

## 3.2 Database

```text
core/database/
├── <main database service script>
└── <database implementation/service>/
```

The database system is **generic infrastructure**.

It must be capable of serving any current or future plugin.

### Database Service

The Database Service must **not contain plugin-specific logic**.

It must not know what a Task, Calendar Event, Note, or other plugin object means.

The Database Service receives generic database commands through the EventBus.

Example:

```text
CreateItemCommand
    table = "tasks"
    data = {
        title = "...",
        completed = false,
        ...
    }
```

The service only needs to understand:

* the requested database operation
* the target table
* the supplied data/details

The service then performs the generic database operation.

It must not contain methods such as:

```text
createTask()
createCalendarEvent()
createNote()
```

### Database Responsibilities

The database infrastructure owns:

* actual database access
* Room/database configuration
* DAOs
* database entities
* database connections
* queries
* persistence implementation

These are infrastructure details and must remain inside `core/database/`.

### Plugin Database Responsibilities

Plugins own:

* their plugin models
* their repositories
* their understanding of their own data
* the construction of database commands
* the data sent through those commands

Plugins **must not directly access**:

* Room
* DAOs
* database instances
* SQL
* database entities
* Supabase/database SDK implementations

Communication with the database is through the EventBus.

### Model vs Entity

A plugin model and a database entity are different responsibilities.

For example:

```text
plugins/Tasks/models/Task.kt
```

may represent the plugin's Task.

While:

```text
core/database/.../TaskEntity.kt
```

represents how that data is persisted.

Do not unnecessarily merge these responsibilities.

---

## 3.3 Android

```text
core/android/
├── <main Android service script>
└── <Android service>/
```

* The main Android service script belongs directly in `android/`.
* Each Android service has its own folder.
* Android platform functionality belongs here.
* Services must be generic enough to support any plugin.
* Plugins must use these services rather than implementing duplicate Android infrastructure.

---

## 3.4 EventBus

```text
core/eventBus/
├── <EventBus main/launch script>
└── <event type>/
```

* There is **exactly one EventBus**.
* The main EventBus script manages/launches communication.
* Each event type has its own folder.
* Do not create another EventBus or alternative communication system.

### Commands

Commands request an action.

Examples:

```text
CreateItemCommand
UpdateItemCommand
DeleteItemCommand
```

### Events

Events report something that happened.

Use the existing EventBus architecture for communication between independent parts of Remmi.

---

## 3.5 Host

```text
core/host/
└── RemmiHost.kt
```

Host contains only its host script.

---

## 3.6 Controller

```text
core/controller/
└── RemmiController.kt
```

Controller contains only its controller script.

The Controller coordinates the core infrastructure.

Do not put plugin business logic, database operations, Android operations, UI logic, or automation logic inside the Controller.

---

## 3.7 Models

```text
core/models/
└── <shared plugin model classes/interfaces>
```

`core/models/` contains shared plugin-derived model classes/interfaces that are required by both core and plugins.

Plugin-specific models remain inside their plugin.

---

# 4. `ui/` — User Interface

```text
ui/
├── screens/
├── popups/
└── components/
```

All application UI belongs here.

```text
screens/    → Screens
popups/     → Popups/dialogs
components/ → Reusable UI components
```

Do not put database, Android, automation, or other infrastructure here.

Do not move plugin business logic into `ui/`.

---

# 5. `plugins/` — Plugins

```text
plugins/
├── PluginA/
├── PluginB/
└── ...
```

Each plugin owns its own functionality.

Typical structure:

```text
plugins/
└── Tasks/
    ├── TasksPlugin.kt
    ├── Actions.kt
    ├── Repository.kt
    ├── Widgets.kt
    ├── models/
    └── ...
```

### Plugin Rules

* Plugin functionality stays inside its plugin.
* Plugin-specific models stay inside its plugin.
* Plugin repositories stay inside its plugin.
* Plugins use the existing core infrastructure.
* Plugins communicate with infrastructure through the EventBus.
* Plugins do not implement their own database, Android, automation, or EventBus infrastructure.
* Do not create cross-plugin feature folders.

---

# 6. Generic Service Principle

All core services must be **generic infrastructure**.

A service must provide functionality that can be used by multiple plugins rather than being designed around one specific plugin.

For example:

```text
Plugin
   ↓
Repository
   ↓
EventBus Command
   ↓
Generic Service
   ↓
Infrastructure
```

The plugin provides the **specific information**.

The core service provides the **generic operation**.

Do not make core services plugin-aware unless explicitly requested.

---

# 7. Database Load / Bootstrap Script

The database load/bootstrap script must always represent the **complete current database structure**.

Whenever a change:

* adds a plugin with persisted data
* removes a plugin with persisted data
* adds a table
* removes a table
* changes a table
* adds/removes/changes columns
* changes relationships
* changes constraints
* changes indexes
* changes any other persisted database structure

the AI **MUST update the database load/bootstrap script in the same change.**

The script must allow the database to be created from a clean state and be immediately ready for the current application.

**No manual database preparation should be required after running the script.**

Preserve all unrelated existing database definitions.

---

# 8. Existing Implementations Must Be Preserved

Do not delete or replace working implementations simply because another approach is considered more modern, standard, or cleaner.

### GlobalUIState

`GlobalUIState.kt` must retain the standard Compose `mutableStateOf` implementation.

If it has been changed to another state-flow/state-management architecture without explicit authorization, restore the original `mutableStateOf` implementation.

### Hilt / Dagger

Hilt, Dagger, and other unrequested dependency-injection frameworks are prohibited.

If introduced without explicit authorization:

* remove the dependencies
* remove configuration
* remove annotations
* remove DI modules
* restore the original construction/ownership architecture

Do not replace Hilt/Dagger with another DI framework.

---

# 9. No Unrequested Architecture

Do not introduce:

* Hilt
* Dagger
* Koin
* dependency-injection frameworks
* new managers
* new services
* new repositories
* new architectural layers
* new EventBuses
* alternative communication systems
* new frameworks
* new design-pattern infrastructure

unless explicitly requested.

**Do not add something merely because it is considered standard Android practice.**

---

# 10. Change Procedure

For every task:

1. Identify whether the change belongs to `core`, `ui`, or `plugins`.
2. Find the existing implementation.
3. Reuse it where possible.
4. Modify the minimum number of files.
5. Preserve existing implementations.
6. Keep the established folder structure.
7. Update the database load/bootstrap script if persisted data changes.
8. Build/test the project.
9. Do not modify unrelated systems.

---

# 11. Final Ownership Rules

```text
core/
    Infrastructure only

ui/
    UI only

plugins/
    Plugin functionality and plugin-specific models
```

Inside `core/`:

```text
automation/ → AutomationEngine + automation features

database/   → Generic database infrastructure + database services

android/    → Generic Android infrastructure + Android services

eventBus/   → One EventBus + event types

host/       → RemmiHost only

controller/ → RemmiController only

models/     → Shared plugin model classes/interfaces
```

## Final Principle

**Plugin-specific knowledge belongs in the plugin.**

**Generic infrastructure belongs in core.**

**UI belongs in ui.**

**Communication between independent systems goes through the EventBus.**

**The database service performs generic operations based on commands and their supplied data; it does not contain plugin-specific business logic.**

**Never create a new architectural location when an existing location already owns the responsibility.**
