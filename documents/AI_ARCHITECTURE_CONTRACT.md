# Remmi AI Architecture Contract

**This document is mandatory. Follow it for every code change.**

---

# 1. Absolute Rules

* Preserve the existing architecture and folder structure.
* Make the smallest change necessary.
* **Do not delete existing implementations unless explicitly requested.**
* **Do not replace an existing implementation with a different architecture unless explicitly requested.**
* Do not reorganize, rename, migrate, or redesign existing code unless explicitly requested.
* Do not create new architectural layers, managers, frameworks, abstractions, or communication systems unless explicitly requested.
* Reuse existing implementations whenever possible.
* **NEVER modify, rewrite, shorten, delete, or otherwise change this contract file.**
* If unsure where something belongs, **do not invent a new location. Ask first.**

### UI-specific rule

Using or migrating to Material 3 does **not** automatically authorize a UI redesign.

A Material 3 migration means:

* use Material 3 components where appropriate
* use Material 3 theming
* use Material 3 typography
* use Material 3 colors
* use Material 3 shapes
* use Material 3 surfaces and elevation appropriately

It does **not** mean:

* redesigning existing screens
* changing navigation
* changing application behavior
* changing plugin ownership
* creating new UI architecture
* replacing existing custom components without need
* introducing new navigation patterns unless explicitly requested

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

# 5. Material 3 UI System

Remmi uses **Jetpack Compose with Material 3** as its UI design system.

Material 3 belongs to the `ui/` responsibility.

Use:

```kotlin
androidx.compose.material3.*
```

where appropriate.

The Material 3 system should provide the common visual foundation for Remmi.

---

## 5.1 Material 3 Theme

Remmi should use:

```kotlin
MaterialTheme
```

as the Compose theme foundation.

The theme should define:

* `colorScheme`
* `typography`
* `shapes`

Support:

* light theme
* dark theme
* Android dynamic color where appropriate

Do not create multiple competing theme systems.

If a theme already exists, modify the existing implementation rather than creating a duplicate theme.

---

## 5.2 Material 3 Components

Prefer Material 3 components where an appropriate component already exists.

Examples include:

```text
Scaffold
Surface
Card
Button
IconButton
FloatingActionButton
TopAppBar
NavigationBar
NavigationRail
TabRow
ScrollableTabRow
TextField
AlertDialog
AssistChip
FilterChip
SuggestionChip
CircularProgressIndicator
LinearProgressIndicator
```

Do not replace a working custom component simply because Material 3 provides an alternative.

Use the smallest appropriate change.

---

## 5.3 Material 3 Typography

Use:

```kotlin
MaterialTheme.typography
```

for common text styles.

Examples:

```text
headlineLarge
headlineMedium
titleLarge
titleMedium
bodyLarge
bodyMedium
labelLarge
```

Avoid unnecessary hard-coded typography when an appropriate Material 3 style already exists.

---

## 5.4 Material 3 Colors

Use:

```kotlin
MaterialTheme.colorScheme
```

instead of scattering hard-coded colors throughout the UI.

Examples:

```text
primary
onPrimary
primaryContainer
onPrimaryContainer
secondary
secondaryContainer
surface
surfaceVariant
background
error
```

Existing intentional custom colors may remain where they are necessary for a specific UI element.

Do not remove meaningful existing branding or plugin-specific visual identity without explicit instruction.

---

## 5.5 Material 3 Shapes and Surfaces

Prefer:

```kotlin
MaterialTheme.shapes
```

and Material 3 `Surface` / `Card` components.

Avoid unnecessary:

* custom shadows
* gradients
* arbitrary corner radii
* excessive borders
* decorative effects

unless explicitly requested.

The goal is a clean, modern Material 3 Android UI.

---

# 6. UI Navigation and Future Assistant Interface

Remmi's UI is evolving toward an **Assistant → Plugin** navigation model.

This is a UI concept only and does not create a new architectural layer.

The intended future interaction is:

```text
Assistant
    ↓
Plugin
    ↓
Plugin UI
```

### Assistant Navigation

The future assistant selector is intended to be a horizontally scrollable row containing assistants.

Example:

```text
👑 Master   🧠 Personal   ❤️ Health   🍳 Nutrition   💼 Work
```

The assistant selector should be implemented inside the existing `ui/` structure when explicitly requested.

### Plugin Navigation

After selecting an assistant, the selected assistant's plugins should be accessible through a horizontal plugin navigation system.

The intended interaction is:

```text
Select Assistant
        ↓
Show its plugins
        ↓
Select plugin tab
        ↓
Swipe left/right between plugins
```

A Compose `HorizontalPager` may be used when this feature is explicitly requested.

A Material 3 `TabRow` or `ScrollableTabRow` may be used for plugin tabs when appropriate.

### Important

This section defines the intended UI direction.

It does **not** authorize implementing it automatically.

Only implement this navigation when explicitly requested.

---

# 7. `plugins/` — Plugins

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

# 8. Generic Service Principle

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

# 9. Database Load / Bootstrap Script

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

# 10. Existing Implementations Must Be Preserved

Do not delete or replace working implementations simply because another approach is considered more modern, standard, cleaner, or more idiomatic.

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

# 11. No Unrequested Architecture

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

Material 3 is permitted because it is the established UI design system for Remmi.

However, Material 3 must remain a **UI/design-system dependency** and must not be used as justification for architectural changes.

---

# 12. Change Procedure

For every task:

1. Read this entire contract before modifying code.
2. Identify whether the change belongs to `core`, `ui`, or `plugins`.
3. Find the existing implementation.
4. Reuse it where possible.
5. Modify the minimum number of files.
6. Preserve existing implementations.
7. Keep the established folder structure.
8. Update the database load/bootstrap script if persisted data changes.
9. Build/test the project.
10. Do not modify unrelated systems.

### For UI tasks

Additionally:

1. Check whether the requested UI can be implemented using existing components.
2. Prefer Material 3 components where appropriate.
3. Do not introduce a new UI architecture.
4. Do not move plugin UI ownership.
5. Do not redesign unrelated screens.
6. Do not change application behavior unless requested.
7. Keep UI state in the existing state-management approach.
8. Use `MaterialTheme` rather than introducing a second theme system.

---

# 13. Scope Control

The AI must distinguish between:

### Explicitly requested

May be changed.

### Required supporting changes

May be changed when directly necessary to implement the requested task.

### Unrelated improvements

Must **not** be changed.

For example, if asked:

> "Convert this screen to Material 3."

The AI may:

* update Material 3 imports
* update Material 3 components
* update theme usage
* update typography
* update colors
* fix compilation issues caused directly by the migration

The AI must not:

* redesign navigation
* reorganize the project
* introduce Hilt
* rewrite repositories
* modify the database
* rewrite EventBus communication
* move plugin files
* redesign unrelated screens
* create new managers

---

# 14. Final Ownership Rules

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

Inside `ui/`:

```text
screens/    → Application screens
popups/     → Application dialogs/popups
components/ → Reusable UI components
```

---

# 15. Final Principles

**Plugin-specific knowledge belongs in the plugin.**

**Generic infrastructure belongs in core.**

**UI belongs in ui.**

**Communication between independent systems goes through the EventBus.**

**The database service performs generic operations based on commands and their supplied data; it does not contain plugin-specific business logic.**

**Material 3 provides Remmi's common UI/design-system foundation.**

**Material 3 does not authorize architectural changes or unrelated redesigns.**

**Never create a new architectural location when an existing location already owns the responsibility.**

**Always make the smallest change necessary to satisfy the requested task.**

**When a requested change conflicts with this contract, follow the contract unless the user explicitly authorizes changing the contract itself.**
