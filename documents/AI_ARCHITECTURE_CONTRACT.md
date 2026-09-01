# REMMI ARCHITECTURE CONTRACT

## MANDATORY ARCHITECTURE AND CODE-CHANGE RULES

This document is the architectural source of truth for the Remmi Android application.

These rules are mandatory for every AI-assisted code change.

The primary objective is:

> **Preserve the existing application while making the smallest architectural and code change required to complete the task.**

The AI must not redesign working systems merely because another architecture appears preferable.

However, when an approved architecture restructuring task is explicitly requested, the AI must reorganize the affected code toward the architecture defined in this document.

---

# 1. CORE PRINCIPLES

The following principles always apply.

### 1.1 Preserve existing functionality

Do not:

* Remove working functionality without justification.
* Rewrite unrelated code.
* Change public APIs unnecessarily.
* Replace working systems with alternatives merely because they are preferred.
* Reformat unrelated files.
* Rename classes unnecessarily.
* Move unrelated files.
* Introduce new abstractions without an architectural reason.

---

### 1.2 Ownership before implementation

Before creating, moving, or modifying a class, determine:

> **Who owns this responsibility?**

Possible owners are:

* `core`
* `ui`
* `plugins/<plugin>`
* EventBus
* Android service
* Database service
* File service
* Automation feature

The class MUST be placed inside the directory owned by that responsibility.

Do not place a class according to convenience.

Do not put unrelated classes together simply because they are used by the same feature.

---

### 1.3 One responsibility per class

Every class must have a clear responsibility.

Avoid:

* God classes.
* Manager classes containing business logic.
* Services containing unrelated responsibilities.
* Plugins containing core functionality.
* UI classes containing infrastructure logic.
* Repositories acting as hidden service locators.
* Controllers becoming feature implementations.

If a class is performing multiple unrelated responsibilities, inspect the existing architecture before deciding whether the responsibilities should be separated.

Do not automatically split classes unless required.

---

# 2. TOP-LEVEL PROJECT STRUCTURE

Remmi is an Android application.

The principal architectural source directories are:

```text
core/
ui/
plugins/
```

Other project-level directories may exist when required by the existing project, such as:

```text
db-scripts/
```

Do not create additional top-level architectural directories unless explicitly approved.

The three primary application ownership boundaries are:

```text
core/
    Internal application infrastructure

ui/
    Shared application UI

plugins/
    Feature/plugin ownership
```

---

# 3. ARCHITECTURAL OWNERSHIP

The architecture is divided into three major areas.

## 3.1 `core/`

`core/` owns internal application infrastructure.

Examples:

* Application lifecycle.
* Host.
* Controller.
* Managers.
* EventBus.
* Android infrastructure.
* Database infrastructure.
* File infrastructure.
* Automation infrastructure.
* Generic plugin infrastructure.
* Shared core contracts/interfaces.

Core must NOT contain plugin-specific business functionality.

---

## 3.2 `ui/`

`ui/` owns shared application UI.

This includes:

* Core application screens.
* Reusable components.
* Generic popups.
* Shared UI infrastructure.

`ui/` must NOT contain plugin-specific screens or plugin-specific popups.

---

## 3.3 `plugins/`

`plugins/` owns application features.

Each plugin must be as self-contained as reasonably possible.

A plugin owns:

* Plugin models.
* Plugin actions.
* Plugin UI.
* Plugin screens.
* Plugin-specific popups.
* Plugin widgets.
* Plugin repository/domain persistence configuration.
* Plugin-specific business logic.

A plugin must not directly access core implementations.

The EventBus is the communication boundary between plugins and core.

---

# 4. CANONICAL DIRECTORY STRUCTURE

The expected high-level structure is:

```text
core/
    ...

ui/
    components/
        ...

    popups/
        ...

    screens/
        ...

plugins/
    <plugin-name>/
        <PluginName>Plugin.kt
        <PluginName>Actions.kt
        <PluginName>Repository.kt
        <PluginName>Widgets.kt

        models/
            ...

        ui/
            screens/
                ...

            popups/
                ...

            widgets/
                ...
```

The exact existing project structure takes precedence where this contract does not explicitly require a change.

Do not create directories simply to make the structure look cleaner.

---

# 5. CLASS PLACEMENT LAW

## THIS RULE IS MANDATORY

Before adding or moving a class, determine its responsibility and place it in the correct directory.

The AI must not use a generic folder as a dumping ground.

Examples:

| Responsibility            | Location                                                           |
| ------------------------- | ------------------------------------------------------------------ |
| Core application screen   | `ui/screens/`                                                      |
| Shared reusable component | `ui/components/`                                                   |
| Generic popup             | `ui/popups/`                                                       |
| Plugin screen             | `plugins/<plugin>/ui/screens/`                                     |
| Plugin popup              | `plugins/<plugin>/ui/popups/`                                      |
| Plugin widget             | `plugins/<plugin>/ui/widgets/` or canonical plugin widget location |
| Plugin model              | `plugins/<plugin>/models/`                                         |
| Plugin actions            | `plugins/<plugin>/<Plugin>Actions.kt`                              |
| Plugin repository         | `plugins/<plugin>/<Plugin>Repository.kt`                           |
| Plugin root/lifecycle     | `plugins/<plugin>/<Plugin>Plugin.kt`                               |
| Database implementation   | `core/.../database/`                                               |
| File implementation       | `core/.../files/`                                                  |
| Android implementation    | `core/.../android/`                                                |
| Automation implementation | `core/.../automation/`                                             |
| EventBus implementation   | `core/eventBus/`                                                   |

Never place a screen in a generic component directory.

Never place a component in a screen directory.

Never place a popup in a screen directory.

Never place plugin UI in `ui/screens/`.

Never place core infrastructure in a plugin.

---

# 6. UI ARCHITECTURE

The top-level `ui/` directory contains UI shared by the application and reusable UI components.

It does NOT own plugin-specific functionality.

The structure is:

```text
ui/
    components/
    popups/
    screens/
```

---

## 6.1 SHARED COMPONENTS

Reusable UI components belong in:

```text
ui/components/
```

A component belongs here when it is generic and reusable by multiple screens or features.

Examples:

* Navigation menu.
* Add button.
* Shared editor scaffold.
* Shared field.
* Shared picker.
* Reusable visual component.
* Shared design element.

A component should not contain feature-specific business logic.

Do not place a plugin-specific component in `ui/components/` merely because another screen happens to use it.

If the component belongs to a plugin's functionality, it belongs to that plugin.

---

## 6.2 SHARED POPUPS

Generic application-wide popups belong in:

```text
ui/popups/
```

Examples:

* Generic color selector.
* Generic confirmation popup.
* Generic reusable selection popup.

A popup belongs in `ui/popups/` only when it is genuinely generic.

---

## 6.3 CORE SCREENS

Core application screens belong in:

```text
ui/screens/
```

Examples:

* Home.
* Settings.
* Application shell.
* Main navigation.
* Other application-wide screens.

Plugin screens MUST NOT be placed here.

---

# 7. PLUGIN ARCHITECTURE

Each plugin is an independent feature boundary.

Example:

```text
plugins/
    tasks/
    calendar/
    alarms/
    recipes/
```

A plugin should contain everything specific to its functionality.

---

## 7.1 CANONICAL PLUGIN STRUCTURE

The preferred structure is:

```text
plugins/<plugin>/
    <Plugin>Plugin.kt
    <Plugin>Actions.kt
    <Plugin>Repository.kt
    <Plugin>Widgets.kt

    models/

    ui/
        screens/
        popups/
        widgets/
```

Existing plugins that already have a valid established structure must not be reorganized during unrelated tasks.

---

## 7.2 PLUGIN ROOT FILES

These files belong directly in the plugin root:

```text
<Plugin>Plugin.kt
<Plugin>Actions.kt
<Plugin>Repository.kt
<Plugin>Widgets.kt
```

Do not create:

```text
repository/
actions/
plugin/
widgets/
```

for these files unless the architecture is explicitly changed.

---

# 8. PLUGIN MODELS

Plugin-specific models belong in:

```text
plugins/<plugin>/models/
```

Examples:

```text
plugins/tasks/models/Task.kt
plugins/tasks/models/TaskSettings.kt
```

Do not place plugin-specific models in:

```text
core/
ui/
another plugin/
```

Shared contracts should only be moved outside the plugin when they genuinely belong to a shared subsystem.

Do not create generic model dumping folders.

---

# 9. PLUGIN UI

ALL plugin-specific UI belongs inside:

```text
plugins/<plugin>/ui/
```

The preferred structure is:

```text
plugins/<plugin>/ui/
    screens/
    popups/
    widgets/
```

---

## 9.1 PLUGIN SCREENS

Plugin screens MUST be inside:

```text
plugins/<plugin>/ui/screens/
```

Never:

```text
plugins/<plugin>/screens/
plugins/<plugin>/ui/<Screen>.kt
ui/screens/
core/screens/
```

unless the screen is genuinely core/shared UI.

---

## 9.2 PLUGIN POPUPS

Plugin-specific popups MUST be inside:

```text
plugins/<plugin>/ui/popups/
```

These popups remain owned by the plugin.

Other plugins may request or use functionality owned by that plugin through the appropriate architectural mechanism.

Do not copy plugin-specific UI into another plugin.

Example:

If the Alarm plugin owns alarm configuration:

```text
plugins/alarm/ui/popups/AlarmConfigurationPopup.kt
```

Calendar must not create:

```text
plugins/calendar/ui/popups/AlarmPopup.kt
```

to duplicate alarm functionality.

---

## 9.3 PLUGIN WIDGETS

Plugin-specific widgets belong to the plugin.

The primary plugin widget definition remains:

```text
plugins/<plugin>/<Plugin>Widgets.kt
```

Additional widget UI implementation may be placed under:

```text
plugins/<plugin>/ui/widgets/
```

when genuinely necessary.

Do not create widget infrastructure in `core/` for plugin-specific functionality.

Generic Android widget infrastructure remains in the Android boundary.

---

# 10. NO GENERIC PLUGIN DIRECTORIES

Do not create these by default:

```text
repository/
logic/
services/
external/
components/
dialogs/
managers/
controllers/
helpers/
utils/
```

A directory must represent a real architectural responsibility.

Do not create a directory merely because a file could technically be placed there.

Especially:

### Do not create:

```text
plugins/<plugin>/repository/
```

for one repository.

Use:

```text
plugins/<plugin>/<Plugin>Repository.kt
```

### Do not create:

```text
plugins/<plugin>/logic/
```

as a generic business-logic dumping folder.

---

# 11. PLUGIN ACTIONS

`<Plugin>Actions.kt` defines actions that users can perform through the plugin.

Examples:

```text
CreateTask
UpdateTask
DeleteTask
CompleteTask
```

Actions represent requests to perform operations.

The action must connect to the EventBus communication system.

The conceptual flow is:

```text
UI
 ↓
Plugin Action
 ↓
EventBus Command
 ↓
Interested Service / Component
 ↓
Operation
 ↓
EventBus Event
 ↓
Interested listeners
```

The action itself must not directly call:

* DatabaseManager.
* DatabaseService.
* AndroidServiceManager.
* FileService.
* AutomationEngine.
* Another plugin implementation.

Use the EventBus boundary.

---

# 12. PLUGIN CLASS

`<Plugin>Plugin.kt` is the head/lifecycle entry point of the plugin.

It is responsible for connecting the plugin's internal components with the application lifecycle.

It may:

* Initialize plugin-specific functionality.
* Create plugin-owned components where appropriate.
* Register/unregister the plugin's EventBus listeners.
* Start plugin functionality.
* Stop plugin functionality.
* Release plugin resources.

It must NOT become a general-purpose application manager.

It must NOT directly access core implementations.

The plugin communicates with the rest of the application through the EventBus.

---

# 13. PLUGIN REPOSITORY

`<Plugin>Repository.kt` represents the plugin's repository/domain persistence configuration.

It may contain:

* Plugin-specific data mapping.
* Local state/cache management.
* Persistence configuration.
* Domain-to-persistence mapping.
* Query/result handling where appropriate.

However:

> **A repository must never become a hidden EventBus bypass.**

Plugins must not use a repository to directly access database infrastructure if the architecture requires database communication through EventBus.

Do not assume that a repository means direct database access.

Inspect its existing role before modifying it.

---

# 14. COMMUNICATION LAW

## EVENTBUS IS THE APPLICATION COMMUNICATION BOUNDARY

Communication between architectural components MUST use the EventBus.

This includes communication between:

* Plugins.
* Database services.
* File services.
* Android services.
* AutomationEngine.
* Other core services/components.

Conceptually:

```text
Plugin
   ↓
EventBus
   ↓
Service
```

and:

```text
Service
   ↓
EventBus
   ↓
Interested listener
```

and:

```text
Plugin
   ↓
EventBus
   ↓
Another plugin
```

Direct cross-boundary calls are prohibited unless they are explicitly part of the same internal subsystem.

---

# 15. MANAGER LAW

## CRITICAL RULE

> **Managers do not perform EventBus communication or business operations.**

Managers are lifecycle/factory/orchestration-at-startup classes.

Their primary responsibility is:

1. Create their dedicated class/service.
2. Provide the dependencies required by that class.
3. Give the EventBus to the created component when that component requires EventBus communication.
4. Start/stop or register/unregister the created component where required.

After creation, the created service/component owns its functionality.

---

## 15.1 DATABASE MANAGER

The DatabaseManager is responsible for creating and initializing the database service.

Conceptually:

```text
DatabaseManager
      ↓
creates
      ↓
DatabaseService
      ↓
receives EventBus
```

DatabaseManager MUST NOT:

* Execute database operations.
* Subscribe to database commands.
* Publish database events.
* Contain Supabase logic.
* Contain SQL.
* Become a database service.
* Route database communication.

The DatabaseService owns database functionality.

---

## 15.2 DATABASE SERVICE

The DatabaseService is responsible for database functionality.

For the current architecture, this includes the Supabase/database integration.

It:

* Receives EventBus communication.
* Listens for database-related commands.
* Executes database operations.
* Publishes resulting events.
* Owns database-specific implementation.
* Manages its database connection/client where appropriate.

Conceptually:

```text
EventBus
   ↓
DatabaseService
   ↓
Supabase / Database
   ↓
DatabaseService
   ↓
EventBus
```

The DatabaseService is self-contained within its responsibility.

---

# 16. ANDROID MANAGER LAW

AndroidManager is responsible for creating Android-specific services.

Examples:

* AlarmService.
* NotificationService.
* CalendarService.
* ContactService.
* LocationService.
* WeatherService.
* System settings services.
* Android widget services.
* Other Android integrations.

The manager:

* Creates the service.
* Supplies required dependencies.
* Supplies the EventBus where required.
* Starts/stops the service when appropriate.

The manager does NOT implement the Android functionality.

---

# 17. ANDROID SERVICE LAW

Each Android service must be self-contained within its responsibility.

Conceptually:

```text
AndroidManager
      ↓
creates
      ↓
AndroidService
      ↓
EventBus
```

The service:

* Receives commands through EventBus.
* Performs the Android operation.
* Publishes resulting events through EventBus.

The service may call other internal classes/folders belonging to its own Android subsystem where appropriate.

It must not expose its implementation directly to plugins.

Plugins must communicate through EventBus.

---

# 18. FILE SERVICE LAW

File functionality belongs to the Android/core service boundary.

The architecture must not create unnecessary layers such as:

```text
FileManager
FileServiceManager
PluginFileManager
```

when an existing FileService already owns the responsibility.

The manager's role is only to create/configure the FileService.

The FileService owns:

* File operations.
* File-system implementation.
* File loading.
* File creation.
* File modification.
* File deletion.
* File existence checks.
* Relevant file-related behavior.

Communication follows:

```text
Plugin
 ↓
EventBus
 ↓
FileService
 ↓
File System
```

and:

```text
FileService
 ↓
EventBus
 ↓
Interested listeners
```

---

# 19. PLUGIN MANAGER LAW

PluginManager is responsible for plugin lifecycle.

It may:

* Discover plugins.
* Create plugins.
* Register plugins.
* Start plugins.
* Stop plugins.
* Unload plugins.
* Provide generic plugin infrastructure.

PluginManager MUST NOT:

* Execute plugin business logic.
* Execute database operations.
* Execute file operations.
* Execute Android operations.
* Act as EventBus router.
* Become a feature manager.

The PluginManager creates/configures the plugin and gives it the dependencies required for initialization.

The plugin itself owns its EventBus registration and plugin functionality where appropriate.

---

# 20. AUTOMATION ENGINE

AutomationEngine owns automatic application behavior.

Examples:

* Automatic refreshes.
* Briefings.
* Scheduled automated operations.
* Automatic responses.
* Background automation.

AutomationEngine communicates through EventBus.

Conceptually:

```text
EventBus
   ↓
AutomationEngine
   ↓
Automation feature
   ↓
EventBus
```

AutomationEngine must not directly call plugins or services when EventBus communication is required.

Automation feature-specific code belongs under:

```text
automation/features/<feature-name>/
```

Do not place feature-specific logic directly into AutomationEngine unless it is genuinely engine-level logic.

---

# 21. EVENTBUS ARCHITECTURE

There must be exactly one application EventBus.

Current location:

```text
core/eventBus/
```

It may contain:

```text
EventBus.kt

commands/
    CommandListener.kt
    CommandOperations.kt
    RemmiCommand.kt
    StandardCommands.kt

events/
    EventListener.kt
    EventOperations.kt
    RemmiEvent.kt
    StandardEvents.kt
```

Additional existing EventBus types may include:

```text
EventType.kt
MessageContext.kt
PluginEvent.kt
RemmiMessage.kt
```

Preserve the existing structure unless the current task explicitly changes it.

---

# 22. COMMANDS VS EVENTS

Commands and events are different concepts.

## COMMAND

A command is a request.

Examples:

```text
CreateTaskCommand
UpdateAlarmCommand
DeleteCalendarEventCommand
```

Conceptually:

```text
"Please perform this operation."
```

---

## EVENT

An event is a fact that already happened.

Examples:

```text
TaskCreatedEvent
AlarmUpdatedEvent
CalendarEventDeletedEvent
```

Conceptually:

```text
"This operation has happened."
```

Never use an event as a command.

Never use a command to announce a completed operation.

---

# 23. EVENTBUS EXTENSIBILITY

Future communication types may include:

```text
queries/
responses/
```

Do not implement them unless requested.

Each communication type should own its:

* Base type.
* Listener.
* Operations.
* Publish behavior.
* Subscribe behavior.
* Unsubscribe behavior.
* Standard implementations.

`EventBus.kt` should coordinate communication rather than become a giant implementation class.

Do not place all communication logic into `EventBus.kt`.

---

# 24. DATABASE ACCESS LAW

Plugins must NEVER directly access:

* `DatabaseManager`.
* `DatabaseService`.
* `SupabaseService`.
* Supabase clients.
* SQL.
* Database tables.
* Database SDKs.

The expected architecture is:

```text
Plugin
 ↓
Action / Command
 ↓
EventBus
 ↓
DatabaseService
 ↓
Database
```

Results return through EventBus events.

---

# 25. ANDROID ACCESS LAW

Plugins must not directly access Android infrastructure when the architecture requires an Android service boundary.

Plugins must not directly receive unrestricted Android `Context`.

Plugins must not directly access:

* Android services.
* Android file APIs.
* Contacts APIs.
* Calendar APIs.
* Location APIs.
* Notification APIs.
* Alarm APIs.
* System settings.
* Other Android infrastructure.

The expected architecture is:

```text
Plugin
 ↓
EventBus
 ↓
Android Service
 ↓
Android API
```

Do not expose an entire Android manager or `Context` to a plugin.

Expose functionality through commands/events.

---

# 26. NO MEGA CONTEXT

Never create:

```text
PluginContext
AppContext
CoreContext
ManagerContext
ServiceLocator
```

or equivalent classes that expose the entire application.

Dependencies must remain explicit.

A class should receive only what it actually needs.

---

# 27. UI OWNERSHIP LAW

UI ownership must follow functionality ownership.

### Core UI

```text
ui/screens/
ui/components/
ui/popups/
```

### Plugin UI

```text
plugins/<plugin>/ui/screens/
plugins/<plugin>/ui/popups/
plugins/<plugin>/ui/widgets/
```

A plugin-specific screen belongs to its plugin.

A plugin-specific popup belongs to its plugin.

A generic reusable component belongs to shared UI.

Do not move ownership merely because another component uses the UI.

---

# 28. CROSS-PLUGIN UI

Do not duplicate plugin-specific UI in another plugin.

Example:

If Alarm owns:

```text
plugins/alarm/ui/popups/AlarmConfigurationPopup.kt
```

Calendar must not create an alarm-specific popup.

The Alarm plugin remains the owner.

Cross-plugin interaction must occur through the established plugin/EventBus architecture.

---

# 29. DATABASE SCHEMA OWNERSHIP

Database implementation belongs to core database services.

Database startup schema belongs to:

```text
db-scripts/src/main/resources/startup.sql
```

Plugin ownership of a model does NOT give the plugin direct database access.

Responsibilities remain:

```text
Plugin
    owns domain/model structure

DatabaseService
    owns database implementation

startup.sql
    owns complete bootstrap schema
```

---

# 30. STARTUP.SQL RULE

`startup.sql` is the complete authoritative database bootstrap/rebuild definition.

It is NOT a migration-only file.

It MUST contain the complete current schema required to recreate the Remmi database from an empty state.

When modifying one table, do not reduce the file to that table.

For example:

```text
users
tasks
alarms
calendar_events
```

If `tasks` changes, the final script must still contain:

```text
users
tasks
alarms
calendar_events
```

with complete definitions.

---

# 31. STARTUP.SQL COMPLETENESS

Whenever `startup.sql` is modified, preserve:

* Every existing table.
* Every required column.
* Primary keys.
* Foreign keys.
* Relationships.
* Constraints.
* Defaults.
* Indexes.
* Policies.
* Triggers.
* Functions.
* Other required database objects.
* Destruction/reset instructions.
* Correct dependency/creation order.

If an existing object is unrelated to the task, preserve it.

If uncertain:

> **KEEP IT.**

Never replace a complete `startup.sql` with a partial script.

---

# 32. DATABASE SYNCHRONIZATION

Whenever a persisted plugin model/item changes:

1. Inspect the plugin model.
2. Read the complete `startup.sql`.
3. Find the corresponding database definition.
4. Determine whether the schema must change.
5. Apply the minimum required schema modification.
6. Preserve all unrelated schema.
7. Verify the resulting schema.
8. Build the affected application code.

A persisted model change is incomplete until its database representation has been checked.

---

# 33. NON-PERSISTED CHANGES

Do NOT modify `startup.sql` for:

* UI changes.
* Screen changes.
* Popup changes.
* Widget appearance.
* Non-persisted state.
* Pure business logic changes.
* Other changes that do not alter persisted database structure.

---

# 34. MINIMAL CHANGE LAW

The smallest possible change is always preferred.

Before creating a class:

1. Search for existing equivalent functionality.
2. Determine whether it can be reused.
3. Determine ownership.
4. Only create a new class if responsibility is genuinely new.

Do not duplicate:

* Managers.
* Services.
* Repositories.
* Commands.
* Events.
* Models.
* Components.
* UI.

---

# 35. FILE MODIFICATION SAFETY

Before deleting a file:

1. Search all references.
2. Verify ownership.
3. Verify replacement functionality exists.
4. Update references.
5. Build.
6. Delete only when safe.

Do not:

* Rewrite complete files for small changes.
* Reformat unrelated code.
* Rename public APIs unnecessarily.
* Move unrelated files.
* Touch unrelated plugins.

---

# 36. ARCHITECTURAL RESTRUCTURING

When explicitly asked to redesign/restructure existing code, the AI may reorganize the affected architecture.

However, it must still proceed incrementally.

Before making a large restructuring:

1. Inspect the affected structure.
2. Identify ownership violations.
3. Identify misplaced classes.
4. Identify duplicated responsibilities.
5. Identify direct communication that violates EventBus rules.
6. Identify manager classes containing service logic.
7. Identify UI classes in incorrect directories.
8. Identify plugin-specific code outside the plugin.
9. Identify unnecessary layers.

Then define:

* Current structure.
* Target structure.
* Files to move.
* Files to modify.
* Files to delete.
* Dependencies affected.
* Risks.

Then implement incrementally.

---

# 37. CLASS DISTRIBUTION REQUIREMENT

## CRITICAL AI RULE

Every class must be placed according to what the class **is responsible for**, not where it happens to be used.

Before creating or moving a class, answer internally:

```text
What does this class do?
Who owns that responsibility?
Is it UI?
Is it a screen?
Is it a popup?
Is it a reusable component?
Is it a model?
Is it a plugin?
Is it a service?
Is it a manager?
Is it EventBus infrastructure?
Is it database infrastructure?
Is it Android infrastructure?
Is it automation infrastructure?
```

Then place it accordingly.

### Examples

A screen:

```text
ui/screens/
```

or:

```text
plugins/<plugin>/ui/screens/
```

A popup:

```text
ui/popups/
```

or:

```text
plugins/<plugin>/ui/popups/
```

A reusable component:

```text
ui/components/
```

A plugin model:

```text
plugins/<plugin>/models/
```

A database service:

```text
core/.../database/
```

An Android service:

```text
core/.../android/
```

An EventBus command:

```text
core/eventBus/commands/
```

An EventBus event:

```text
core/eventBus/events/
```

Never place all classes from one feature into a single directory regardless of responsibility.

---

# 38. MANAGER VS SERVICE RESPONSIBILITY

This distinction is mandatory.

## Manager

A manager creates/configures/starts/stops its dedicated class.

```text
Manager
    ↓
create/configure
    ↓
Service
```

The manager does NOT implement the service's functionality.

---

## Service

The service contains the actual functionality.

```text
Service
    ↓
receives EventBus messages
    ↓
performs operation
    ↓
publishes EventBus messages
```

Managers must not become service proxies.

Do not implement:

```text
DatabaseManager.createTask()
DatabaseManager.deleteTask()
FileManager.readFile()
AndroidManager.showNotification()
PluginManager.executePluginAction()
```

when those operations belong to the corresponding service/plugin.

---

# 39. MANAGERS AND EVENTBUS

Managers may receive or be given the EventBus as a dependency when necessary to initialize their services.

However:

> **The manager itself must not become an EventBus communication endpoint.**

The manager should not:

* Subscribe to application commands.
* Publish application events.
* Route messages.
* Execute service commands.
* Contain event handlers.

Instead:

```text
Manager
   ↓
creates Service(EventBus)
   ↓
Service subscribes/publishes
```

This rule applies to:

* DatabaseManager.
* AndroidManager.
* FileServiceManager.
* PluginManager.
* Other lifecycle managers.

---

# 40. SELF-CONTAINED SERVICES

A service should be self-contained within its responsibility.

For example:

```text
DatabaseService
```

owns database behavior.

```text
FileService
```

owns file behavior.

```text
NotificationService
```

owns notification behavior.

```text
LocationService
```

owns location behavior.

Each service may use internal implementation classes belonging to its subsystem.

It must not require another manager to perform its ordinary operation.

---

# 41. FORBIDDEN ARCHITECTURAL ACTIONS

NEVER:

* Create a second EventBus.
* Create plugin-specific EventBuses.
* Bypass EventBus across architectural boundaries.
* Give plugins direct database access.
* Give plugins direct Android infrastructure access.
* Give plugins unrestricted Context.
* Give plugins direct AutomationEngine access.
* Give plugins direct manager access.
* Turn managers into service implementations.
* Put service functionality inside managers.
* Put plugin business logic into core.
* Put plugin screens in `ui/screens/`.
* Put plugin popups in `ui/popups/`.
* Put shared UI inside plugins without ownership justification.
* Create `PluginContext`.
* Create a service locator.
* Create unnecessary managers.
* Create unnecessary service wrappers.
* Create unnecessary repositories.
* Create generic `logic/` folders.
* Create repository folders for a single repository.
* Duplicate plugin functionality.
* Duplicate plugin-specific UI.
* Move unrelated code.
* Refactor unrelated plugins during unrelated tasks.
* Rewrite the entire project unnecessarily.
* Delete files without checking references.
* Replace `startup.sql` with a partial schema.
* Remove unrelated tables from `startup.sql`.
* Remove unrelated database objects.
* Convert `startup.sql` into a migration-only file.
* Modify `startup.sql` for non-persisted changes.
* Change unrelated database definitions.
* Introduce a new architecture simply because it is cleaner.

---

# 42. REQUIRED WORKFLOW FOR EVERY TASK

Every coding task MUST follow this process.

## STEP 1 — READ

Read:

```text
documents/AI_ARCHITECTURE_CONTRACT.md
```

Treat it as mandatory.

---

## STEP 2 — DEFINE SCOPE

Identify the minimum relevant:

* Files.
* Directories.
* Classes.
* Dependencies.
* Database definitions if applicable.

Do not scan or modify the entire project unnecessarily.

---

## STEP 3 — LIST RELEVANT FILES

Before modifying code, identify the files directly related to the task.

For restructuring tasks, include:

* Current files.
* Target files.
* Files that may need moving.
* Files that may need deletion.

---

## STEP 4 — INSPECT

Inspect the existing implementation before changing it.

For plugin changes:

* Inspect the plugin.
* Inspect relevant models.
* Inspect relevant UI.
* Inspect relevant actions.
* Inspect repository usage.
* Inspect EventBus contracts when communication changes.

For database changes:

* Read the COMPLETE `startup.sql`.

For restructuring:

* Inspect the affected architectural boundaries.

---

## STEP 5 — PLAN

Briefly report:

```text
Current structure:
...

Target structure:
...

Files to change:
...

Files to move:
...

Files to delete:
...

Reason:
...
```

For database changes also state:

```text
startup.sql change required: YES/NO

If YES:
All existing tables and required database objects will remain represented.
```

---

## STEP 6 — IMPLEMENT

Make the smallest change that satisfies the task.

When restructuring is explicitly requested:

* Move misplaced classes.
* Correct ownership.
* Correct package declarations.
* Correct imports.
* Correct EventBus boundaries.
* Separate manager responsibilities from service responsibilities.
* Preserve behavior.
* Remove obsolete layers only when safe.

Do not perform unrelated cleanup.

---

## STEP 7 — VERIFY STRUCTURE

After implementation, verify:

### Class ownership

* Every class is in the correct directory.
* Screens are in screen directories.
* Popups are in popup directories.
* Components are in component directories.
* Models are in model directories.
* Plugin-specific code remains in the plugin.
* Core functionality remains in core.
* UI functionality remains in UI.

### Communication

* Cross-boundary communication uses EventBus.
* Managers are not communication endpoints.
* Services own their functionality.
* Plugins do not directly access managers/services.
* No second EventBus exists.

### Database

If applicable:

* Plugin persisted structure matches startup.sql.
* startup.sql remains complete.
* All tables remain present.
* All required objects remain present.
* Reset/destruction instructions remain present.

---

## STEP 8 — BUILD AND TEST

Run the relevant build and tests.

At minimum, verify compilation of the affected code.

If the project provides relevant tests, run them.

Do not claim success without actually verifying it.

---

# 43. FINAL REPORT FORMAT

After completing a task, report only:

```text
Files inspected:
- ...

Files changed:
- ...

Files moved:
- ...

Files deleted:
- ...

Summary:
- ...

Build/test result:
- ...

Remaining issues:
- ...
```

For database changes, explicitly include:

```text
startup.sql:
- Complete bootstrap schema preserved: YES/NO
- All existing tables preserved: YES/NO
- Requested schema change applied: YES/NO
- Unrelated database definitions removed: YES/NO
```

Do not provide unnecessary explanations.

---

# 44. WHEN UNCERTAIN

When uncertain:

> **DO NOT GUESS.**

Inspect the existing implementation.

If the responsibility is unclear:

> Preserve the existing implementation until ownership can be established.

If uncertain whether something belongs in `startup.sql`:

> **KEEP IT.**

If uncertain whether an existing table should remain:

> **KEEP IT.**

If uncertain whether a manager should perform an operation:

> The manager should only create/configure the dedicated class. The dedicated service/component should perform the operation.

If uncertain where a class belongs:

Determine what the class actually does and place it according to responsibility.

Never use an arbitrary directory simply because it already exists.

---

# 45. ARCHITECTURAL PRIORITY ORDER

When rules appear to conflict, apply them in this order:

1. Preserve working functionality.
2. Preserve the ownership boundaries defined by this contract.
3. Preserve EventBus communication boundaries.
4. Preserve service/manager separation.
5. Place classes according to responsibility.
6. Make the smallest required change.
7. Avoid unrelated refactoring.
8. Preserve existing implementation details where they do not conflict with the architecture.

The fundamental rule is:

> **Correct ownership + clear responsibility + EventBus communication + self-contained services + minimal change.**

---

# 46. FINAL ARCHITECTURE SUMMARY

The intended architecture is:

```text
                         RemmiHost
                             │
                             ▼
                      RemmiController
                             │
        ┌────────────────────┼─────────────────────┐
        │                    │                     │
        ▼                    ▼                     ▼
 PluginManager         DatabaseManager        AndroidManager
        │                    │                     │
        ▼                    ▼                     ▼
    Plugins          DatabaseService       Android Services
        │                    │                     │
        └────────────────────┼─────────────────────┘
                             │
                             ▼
                          EventBus
                             ▲
                             │
                    AutomationEngine
```

The managers create and configure their dedicated classes.

The dedicated classes perform the actual functionality.

Communication between architectural boundaries occurs through EventBus.

The UI is separated according to responsibility:

```text
ui/
    components/
    popups/
    screens/
```

Plugins are self-contained:

```text
plugins/
    <plugin>/
        <Plugin>Plugin.kt
        <Plugin>Actions.kt
        <Plugin>Repository.kt
        <Plugin>Widgets.kt

        models/

        ui/
            screens/
            popups/
            widgets/
```

The fundamental communication model is:

```text
                 ┌───────────────┐
                 │   EventBus    │
                 └───────┬───────┘
                         │
          ┌──────────────┼──────────────┐
          │              │              │
          ▼              ▼              ▼
       Plugins       Core Services   Automation
                         │
               ┌─────────┼─────────┐
               ▼         ▼         ▼
           Database     File     Android
           Service    Service    Services
```

Managers do not perform these operations.

They create and configure the classes that do.

The final architectural rule is:

> **Managers create. Services operate. Plugins own features. UI owns presentation. EventBus connects boundaries. Classes live in the directory that owns their responsibility.**
