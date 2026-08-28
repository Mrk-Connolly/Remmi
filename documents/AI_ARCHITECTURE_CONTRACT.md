# REMMI ARCHITECTURE CONTRACT

# MANDATORY RULES FOR ALL CODE CHANGES

You are modifying an existing Android application called Remmi.

This document is the architectural source of truth.

Your highest priority is:

**PRESERVE THE EXISTING APPLICATION WHILE MAKING THE SMALLEST POSSIBLE CHANGE REQUIRED BY THE TASK.**

Do not redesign the application.

Do not invent a new architecture.

Do not move, rename, or reorganize unrelated code.

Do not replace existing systems with alternatives simply because you prefer another design.

When uncertain:

**PRESERVE THE CURRENT STRUCTURE.**

============================================================

1. CURRENT PROJECT STRUCTURE
   ============================================================

Remmi is a single Android application module.

Gradle modules:

:app
:db-scripts

Do not create additional Gradle modules unless explicitly requested.

The important architecture roots are:

core/
plugins/

Main application files include:

MainActivity.kt
RemmiApplication.kt

============================================================
2. TOP-LEVEL OWNERSHIP
   ======================

The hierarchy is:

RemmiHost
?
creates and starts
RemmiController
?
creates and owns
??? PluginManager
??? DatabaseServiceManager / database services
??? AndroidServiceManager
??? EventBus
??? AutomationEngine

RemmiController owns application lifecycle coordination.

RemmiController is allowed to:

* Create core managers/services.
* Hold references to core managers/services.
* Start the application.
* Stop the application.
* Coordinate application lifecycle.

RemmiController must NOT become a feature implementation class.

Do not add:

* Plugin-specific logic.
* Database implementation.
* Android API implementation.
* File implementation.
* Automation feature logic.
* UI feature logic.

to RemmiController.

============================================================
3. COMMUNICATION LAW
   ====================

ALL communication between:

* Plugins.
* Core managers.
* Services.
* AutomationEngine.

must go through EventBus.

Examples:

Plugin
?
EventBus
?
Database service

Database service
?
EventBus
?
interested listeners

Plugin
?
EventBus
?
Android service

Plugin
?
EventBus
?
AutomationEngine

Plugin
?
EventBus
?
another interested plugin

Plugin
?
EventBus
?
File service

Direct calls between architectural components are prohibited unless they are explicitly part of the same internal subsystem.

============================================================
4. PLUGIN ISOLATION LAW
   =======================

A plugin owns all plugin-specific functionality.

Plugin-specific functionality must exist inside:

plugins/<plugin-name>/

Do not create plugin-specific functionality inside:

core/
another plugin/

Examples of plugin-specific code:

* Actions.
* Plugin implementation.
* Models.
* Repository contracts/implementations owned by the plugin.
* Business logic.
* Screens.
* Popups.
* External plugin interactions.
* Widgets.
* Feature-specific services.

============================================================
5. REQUIRED PLUGIN STRUCTURE
   ============================

Every plugin MUST use the following canonical structure:

plugins/ <pluginName>/ <PluginName>Plugin.kt <PluginName>Actions.kt <PluginName>Widgets.kt <PluginName>Repository.kt

```
    models/
        ...

    ui/
        screens/
            ...

        popups/
            ...
```

The canonical structure is:

<PluginName>Plugin.kt <PluginName>Actions.kt <PluginName>Widgets.kt <PluginName>Repository.kt
models/
ui/
screens/
popups/

This structure is the default structure for every plugin.

---

## 5.1 PLUGIN ROOT FILES

The following plugin files MUST be directly inside the plugin directory:

<PluginName>Plugin.kt <PluginName>Actions.kt <PluginName>Widgets.kt <PluginName>Repository.kt

Example:

plugins/example/
ExamplePlugin.kt
ExampleActions.kt
ExampleWidgets.kt
ExampleRepository.kt

Do NOT put these files inside additional directories.

For example, this is WRONG:

plugins/example/repository/ExampleRepository.kt

The correct structure is:

plugins/example/ExampleRepository.kt

---

## 5.2 MODELS

Plugin-specific models MUST be inside:

plugins/<pluginName>/models/

Example:

plugins/example/models/
ExampleModel.kt
ExampleSettings.kt

Do not place plugin-specific models in core/.

---

## 5.3 UI

ALL plugin UI MUST be inside:

plugins/<pluginName>/ui/

The ui directory MUST contain:

screens/
popups/

Screens MUST be inside:

plugins/<pluginName>/ui/screens/

Popups MUST be inside:

plugins/<pluginName>/ui/popups/

Correct:

plugins/example/
ExamplePlugin.kt
ExampleActions.kt
ExampleWidgets.kt
ExampleRepository.kt

```
models/
    ExampleModel.kt

ui/
    screens/
        ExampleMainScreen.kt
        ExampleSettingsScreen.kt

    popups/
        ExampleEditPopup.kt
        ExampleDeletePopup.kt
```

The following structures are WRONG:

plugins/example/screens/
plugins/example/popups/

plugins/example/ui/ExampleMainScreen.kt

plugins/example/ui/ExampleEditPopup.kt

Plugin screens and popups MUST NOT exist outside:

ui/screens/
ui/popups/

---

## 5.4 REPOSITORY DIRECTORY RESTRICTION

Do NOT create:

plugins/<pluginName>/repository/

when the plugin has only one repository file.

A single repository file MUST be:

plugins/<pluginName>/<PluginName>Repository.kt

Do not create a directory simply to contain one file.

If a plugin genuinely requires multiple repository files, a repository directory MAY be introduced only when explicitly justified by the implementation.

Do not create empty directories.

Do not create additional architectural layers merely to make the project appear more structured.

---

## 5.5 LOGIC DIRECTORY RESTRICTION

Do NOT create:

plugins/<pluginName>/logic/

by default.

Do not create generic logic directories unless the task explicitly requires a separate logical subsystem and there is a real architectural reason for it.

Plugin logic should remain in the appropriate existing plugin files unless a separate file is genuinely necessary.

---

## 5.6 NO ADDITIONAL DEFAULT PLUGIN DIRECTORIES

The canonical plugin structure contains only:

<PluginName>Plugin.kt <PluginName>Actions.kt <PluginName>Widgets.kt <PluginName>Repository.kt
models/
ui/
screens/
popups/

Do NOT add additional plugin directories such as:

repository/
logic/
external/
services/
components/
dialogs/

unless explicitly required by the task and justified by a real architectural responsibility.

Do not invent additional layers.

---

## 5.7 EXISTING PLUGINS

If an existing plugin already follows the canonical structure, do not reorganize it.

If an existing plugin does NOT follow the canonical structure, do not automatically migrate it during an unrelated task.

Only migrate an existing plugin structure when:

* Explicitly requested.
* Directly required by the current task.
* Performing an approved architecture cleanup.

Do not reorganize every plugin during unrelated work.

============================================================
6. INTERNAL VS EXTERNAL PLUGIN UI
   =================================

Plugin UI is owned by the plugin that implements the functionality.

Internal plugin UI belongs in:

plugins/<plugin>/ui/

Screens belong in:

plugins/<plugin>/ui/screens/

Popups belong in:

plugins/<plugin>/ui/popups/

Do NOT create a separate:

ui/external/

directory as part of the default plugin structure.

If another plugin needs to request UI interaction from a plugin, the owning plugin remains responsible for that UI.

Example:

Alarm plugin owns its alarm configuration UI.

If Calendar needs the user to configure an alarm:

Calendar must NOT contain:

AlarmDialog
AlarmConfigurationDialog
Alarm-specific configuration UI

Calendar should request the Alarm plugin interaction through EventBus/plugin action contracts.

The Alarm plugin owns the alarm UI.

The alarm UI remains inside:

plugins/alarm/ui/screens/
plugins/alarm/ui/popups/

Do not move UI ownership to the requesting plugin.

This rule applies to:

* Alarms.
* Tasks.
* Contacts.
* Maps.
* Locations.
* Ingredients.
* Recipes.
* Any future plugin.

============================================================
7. SPECIFIC EXISTING VIOLATION RULE
   ===================================

Do not create new cross-plugin UI ownership.

Existing examples that should eventually be corrected include:

Calendar containing alarm-related dialogs.

Calendar containing task-related dialogs.

Future changes must move toward plugin ownership.

However:

DO NOT automatically refactor unrelated cross-plugin UI during a small task.

Only fix it when:

* Explicitly requested.
* Directly required by the task.
* Performing an approved architecture cleanup.

============================================================
8. EVENT BUS LAW
   ================

There must be exactly one application EventBus system.

Do not create:

* A second EventBus.
* Plugin-specific EventBus.
* Manager-specific EventBus.
* Alternative communication system that bypasses EventBus.

Current EventBus location:

core/eventBus/

Current major components include:

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

Additional EventBus components include:

EventType.kt
MessageContext.kt
PluginEvent.kt
RemmiMessage.kt

============================================================
9. COMMAND AND EVENT LAW
   ========================

Commands and events are different.

COMMAND:

A request to perform an action.

Examples:

CreateTaskCommand
UpdateAlarmCommand
DeleteCalendarEventCommand

EVENT:

A fact that occurred.

Examples:

TaskCreatedEvent
AlarmUpdatedEvent
CalendarEventDeletedEvent

Never use an event as a command.

Never use a command to announce something that already happened.

Each EventBus communication type owns its own:

* Base type.
* Listener.
* Publish behavior.
* Subscribe behavior.
* Unsubscribe behavior.
* Standard implementations.

Event-specific operations belong inside:

core/eventBus/events/

Command-specific operations belong inside:

core/eventBus/commands/

EventBus.kt should coordinate/delegate communication.

Do not move all command/event implementation back into EventBus.kt.

============================================================
10. EXTENSIBILITY LAW
    =====================

The EventBus communication system must allow future communication types.

For example, future additions may include:

queries/
responses/

Do not implement these now unless requested.

Do not overengineer.

When adding a new communication type:

Create its own directory and contracts.

Do not pollute EventBus.kt with unrelated type-specific behavior.

============================================================
11. PLUGIN DATABASE ACCESS LAW
    ==============================

Plugins must NEVER directly access:

* SupabaseService.
* DatabaseService.
* DatabaseManager.
* Database clients.
* SQL.
* Supabase SDK.
* Database tables.

Plugins communicate database requests through EventBus.

Example:

TasksPlugin
?
CreateTaskCommand
?
EventBus
?
DatabaseManager
?
Database

The Database service publishes resulting events.

Example:

TaskCreatedEvent
TaskUpdatedEvent
TaskDeletedEvent

Plugins may listen to those events.

============================================================
12. PLUGIN REPOSITORY RULE
    ==========================

Plugin repositories may exist.

However, a plugin repository must not become a hidden bypass around EventBus.

Before modifying a repository:

Inspect its role.

Determine whether it is:

A. Local plugin data/cache/state.
B. Domain mapping.
C. Query/result caching.
D. A direct database access layer.

Direct persistence requests from plugins must not bypass EventBus.

Do not delete repositories automatically.

Do not replace repositories without inspecting current usage.

A single plugin repository belongs at the plugin root:

plugins/<pluginName>/<PluginName>Repository.kt

Do not create a repository directory for a single repository file.

============================================================
13. ANDROID CONTEXT LAW
    =======================

Android Context access is restricted.

Plugins must NOT directly receive or access Android Context.

Plugins must NOT access Android APIs directly.

Business logic must not receive unrestricted Context.

Android-specific operations belong to the Android service boundary.

Relevant existing structure includes:

core/android/
alarms/
notifications/
services/
system/
files/

AndroidServiceManager coordinates Android services, including file-related services.

If a plugin needs Android functionality:

Plugin
?
EventBus request
?
Android service listener
?
Specific Android operation

Examples:

* Schedule alarm.
* Show notification.
* Request permission.
* Access contacts.
* Access calendar.
* Access location.
* Read weather through Android integration.
* Access widgets.
* Access system settings.
* Read/write application files.
* Create/delete application files.
* Check file existence.
* Perform other Android file-system operations.

Do not pass the entire Context to a plugin.

Do not expose AndroidServiceManager internals to plugins.

Expose specific operations/contracts.

============================================================
14. ANDROID SERVICE OWNERSHIP
    =============================

Android-specific functionality belongs inside the Android service boundary.

Existing Android service categories include:

alarms/
notifications/
services/
system/
files/

Examples include:

AlarmService
SystemAlarmService
NotificationService
SystemNotificationService
AndroidServiceManager
AndroidWidgetService
SystemSettingsService
CalendarService
ContactService
LocationService
WeatherService
FileService

AndroidServiceManager owns and coordinates Android services, including FileService.

There must NOT be a separate FileServiceManager at the application architecture level.

File functionality is part of the Android service boundary.

Keep Android implementation inside the Android boundary.

Do not move plugin business logic into these services.

Do not use Android services as a shortcut to bypass EventBus.

Do not expose Android service implementations directly to plugins.

Plugins interact with file functionality through EventBus and the appropriate file-service command/event contracts.

============================================================
15. AUTOMATION OWNERSHIP
    ========================

AutomationEngine owns automation execution.

AutomationEngine receives communication through EventBus.

Plugins must not directly call AutomationEngine.

Automation features belong inside:

automation/features/

Current examples include:

dailybriefing/

All future automation features should be added under:

automation/features/<feature-name>/

AutomationEngine coordinates features.

Feature-specific logic must not be placed directly inside AutomationEngine unless it is genuinely engine-level logic.

============================================================
16. DATABASE OWNERSHIP
    ======================

Database functionality belongs under:

core/service/database/

Existing classes include:

DatabaseManager
DatabaseService
SupabaseService

Database implementation belongs here.

Do not move database code into:

plugins/
automation/
screens/
popups/

============================================================
17. DATABASE SCHEMA SYNCHRONIZATION LAW
    =======================================

The database schema and startup definitions MUST remain synchronized with plugin-owned persisted item structures.

When a plugin changes the structure of an item that is persisted in the database, the corresponding database definition MUST be checked and updated in:

db-scripts/src/main/resources/startup.sql

This applies whenever a plugin:

* Adds a new persisted item.
* Adds a new persisted field/column.
* Removes a persisted item.
* Removes a persisted field/column.
* Renames a persisted item.
* Renames a persisted field/column.
* Changes the type of a persisted field.
* Changes constraints.
* Changes relationships.
* Changes indexes.
* Changes defaults.
* Changes other database structure required by the changed item.

A plugin item structure change is NOT complete until startup.sql has been checked.

---

## 17.1 REQUIRED SYNCHRONIZATION WORKFLOW

When modifying a plugin's persisted item structure:

1. Inspect the plugin's existing model/item structure.
2. Inspect the corresponding database definition in:

db-scripts/src/main/resources/startup.sql

3. Determine whether the database schema must change.
4. If the schema must change, update startup.sql in the same task.
5. Keep the database definition consistent with the plugin's current persisted structure.
6. Verify that the resulting schema matches the plugin's expected fields, types, relationships, and constraints.

Example:

If a plugin changes:

ExampleItem
name
description

to:

ExampleItem
name
description
priority

then the corresponding database definition in:

db-scripts/src/main/resources/startup.sql

MUST also be updated to include the new persisted field.

---

## 17.2 DELETED OR RENAMED STRUCTURES

If a plugin removes or renames a persisted item or field, inspect startup.sql and update the corresponding database definition when required.

Do not leave obsolete schema definitions behind when the task explicitly changes the persisted structure.

Do not delete database structures blindly.

Before removing a database item or field:

1. Search for references.
2. Verify that the structure is owned by the affected plugin.
3. Verify that it is safe to remove.
4. Update startup.sql.
5. Build and verify affected code.

---

## 17.3 DATABASE OWNERSHIP REMAINS UNCHANGED

This rule does NOT give plugins direct database access.

Plugins must still follow the Plugin Database Access Law.

Plugins communicate database operations through:

Plugin
?
EventBus
?
Database service
?
Database

The plugin owns its domain/item structure.

The database service owns database implementation.

startup.sql defines the database startup schema.

These responsibilities must remain separate.

---

## 17.4 MINIMAL SCHEMA CHANGES

Only modify the relevant database definitions in:

db-scripts/src/main/resources/startup.sql

Do not redesign the database schema.

Do not modify unrelated tables, fields, relationships, indexes, policies, or other database definitions.

Do not change existing database structure unless it is required by the current plugin change or explicitly requested.

IMPORTANT:

The requirement to preserve the complete startup.sql file does NOT mean that unrelated schema definitions should be changed.

Unrelated definitions must remain unchanged and must continue to be present.

---

## 17.5 UI-ONLY AND NON-PERSISTED CHANGES

Do NOT modify startup.sql for changes that do not affect persisted database structure.

Examples:

* UI-only changes.
* Screen layout changes.
* Popup changes.
* Widget appearance changes.
* Non-persisted state changes.
* Local UI behavior changes.
* Business logic changes that do not alter persisted structure.

Only synchronize startup.sql when the plugin's persisted item/database structure requires it.

---

## 17.6 STARTUP.SQL MUST ALWAYS BE A COMPLETE DATABASE DEFINITION

`db-scripts/src/main/resources/startup.sql` is the COMPLETE and AUTHORITATIVE database bootstrap definition for the Remmi application.

`startup.sql` MUST always contain the complete database structure required to initialize the Remmi database from an empty state.

`startup.sql` MUST contain the definitions and instructions for ALL Remmi database tables.

It must NOT contain only:

* The table currently being edited.
* The table currently being created.
* The latest database change.
* The latest plugin's database structure.
* The SQL required for the current task.

When any database table is modified, `startup.sql` MUST still contain the complete definitions for every other existing database table.

For example, if the database contains:

Table A
Table B
Table C
Table D

and Table B is modified, the final `startup.sql` MUST still contain:

* The required destruction/reset instructions.
* Complete definition for Table A.
* Complete definition for Table B, including the requested change.
* Complete definition for Table C.
* Complete definition for Table D.
* All required relationships.
* All required constraints.
* All required indexes.
* All required defaults.
* All required policies.
* All required triggers/functions or other database objects.

Do NOT remove unchanged tables simply because they are unrelated to the current task.

---

## 17.7 STARTUP.SQL IS A FULL DATABASE REBUILD SCRIPT

`startup.sql` MUST be treated as a complete database reconstruction/bootstrap script.

The script must contain everything required to recreate the current Remmi database schema from a clean/empty state according to the project's existing database setup.

Where required by the existing implementation, the startup sequence must support:

1. Destroying/removing existing Remmi database structures required for a clean rebuild.
2. Recreating all required tables.
3. Recreating all required columns and data types.
4. Recreating all required primary keys.
5. Recreating all required foreign keys and relationships.
6. Recreating all required constraints.
7. Recreating all required indexes.
8. Recreating all required defaults.
9. Recreating all required policies.
10. Recreating all required triggers/functions.
11. Recreating all other required database objects.
12. Leaving the database in the complete schema expected by the current application.

Do not redesign the database as part of this requirement.

Preserve the existing database architecture and structure unless the current task explicitly requires a change.

---

## 17.8 STARTUP.SQL MUST CONTAIN ALL TABLES

Whenever `startup.sql` is created or modified, it MUST contain every current Remmi database table.

This applies even when the current task only affects one table.

If the database contains:

users
tasks
alarms
calendar_events

and the `tasks` table changes, the final `startup.sql` MUST still define:

users
tasks
alarms
calendar_events

The `tasks` definition must contain the requested change.

The other tables must remain fully defined.

The agent MUST NOT replace the existing startup.sql with:

CREATE TABLE tasks (...)

or:

DROP TABLE tasks;
CREATE TABLE tasks (...);

while removing the other tables from the file.

The complete schema must remain.

---

## 17.9 STARTUP.SQL MUST CONTAIN COMPLETE TABLE DEFINITIONS

It is not sufficient for startup.sql to merely mention every table.

Every table must have its complete current definition.

For every table, preserve the applicable:

* Columns.
* Data types.
* Primary keys.
* Foreign keys.
* Relationships.
* Constraints.
* Defaults.
* Indexes.
* Policies.
* Triggers.
* Other required table/database configuration.

If a table is unchanged by the current task, its complete existing definition must remain in startup.sql.

---

## 17.10 STARTUP.SQL MUST CONTAIN DESTRUCTION/RESET INSTRUCTIONS

`startup.sql` must preserve the project's established destruction/reset instructions required to rebuild the complete database.

When a table is added or modified, do NOT remove destruction/reset instructions for other existing tables simply because those tables were not changed.

The startup process must be able to move from an existing database state to the complete schema represented by the script according to the project's established reset strategy.

When modifying destruction order:

* Respect foreign-key dependencies.
* Preserve the existing project's approach where possible.
* Do not introduce destructive behavior beyond the established startup/rebuild strategy.
* Do not remove existing reset instructions without verifying their purpose.

If the existing script uses DROP TABLE statements, preserve the complete set required for the full schema.

If the existing script uses another reset mechanism, preserve that mechanism.

Do not invent a new reset architecture.

---

## 17.11 STARTUP.SQL IS NOT A MIGRATION-ONLY FILE

`startup.sql` is NOT a migration-only file.

Do not convert it into a file containing only:

* ALTER TABLE statements for the latest change.
* The newly added table.
* The newly added column.
* The latest database modification.
* A partial schema.
* Only structures touched by the current task.

If a separate migration system exists, follow it only when explicitly required.

Do not invent a separate migration system merely to avoid maintaining the complete startup.sql.

`startup.sql` must remain a complete bootstrap/rebuild definition.

---

## 17.12 PRESERVE ALL EXISTING TABLE DEFINITIONS

When editing startup.sql, existing table definitions MUST be preserved unless the current task explicitly requires them to change.

Do not accidentally remove tables because they were not part of the current task.

Do not omit tables because they are owned by another plugin.

Do not omit tables because they were not recently modified.

Do not omit tables because the current task only concerns one plugin.

Do not rewrite unchanged table definitions unnecessarily.

If an existing table is part of the Remmi database schema, its complete definition must remain represented in startup.sql.

---

## 17.13 FULL SCHEMA REBUILD AFTER A SINGLE-TABLE CHANGE

If a task changes one table, the expected behavior is:

1. Read the existing complete startup.sql.
2. Preserve all existing tables.
3. Preserve all existing destruction/reset instructions.
4. Modify only the required table definition.
5. Preserve all other database definitions.
6. Verify that startup.sql still represents the complete schema.

Example:

Before:

users
tasks
alarms
calendar_events

Task:

Add `priority` to tasks.

After:

users
tasks with priority
alarms
calendar_events

NOT:

tasks with priority

The final startup.sql must still contain the complete schema.

---

## 17.14 NEW TABLE REQUIREMENT

When a new persisted plugin item requires a new database table:

1. Add the new table definition to startup.sql.
2. Preserve all existing table definitions.
3. Add the required destruction/reset instruction for the new table where appropriate.
4. Respect foreign-key and dependency ordering.
5. Preserve all existing relationships and database objects.
6. Verify that the startup script can recreate the complete database.

Adding one table must NEVER result in removing or omitting existing tables.

---

## 17.15 TABLE REMOVAL REQUIREMENT

When a persisted database table is intentionally removed:

1. Search for all references to the table.
2. Verify ownership and dependencies.
3. Verify that removal is explicitly required.
4. Remove the table definition from startup.sql.
5. Remove or update its destruction/reset instruction as appropriate.
6. Update dependent relationships, indexes, policies, triggers, functions, or other objects when required.
7. Verify that every remaining table is still represented.
8. Verify that startup.sql remains a complete rebuildable schema.

Removing one table does NOT mean rebuilding startup.sql around the edited table.

The resulting script must still contain every remaining database table.

---

## 17.16 STARTUP.SQL COMPLETENESS CHECK

Whenever startup.sql is modified, the agent MUST verify that it still contains the complete database schema.

At minimum, verify:

1. Every existing Remmi database table is represented.
2. Every table has a complete current definition.
3. The affected table contains the requested change.
4. Required relationships are still represented.
5. Required constraints are still represented.
6. Required indexes are still represented.
7. Required defaults are still represented.
8. Required policies are still represented.
9. Required triggers/functions are still represented when applicable.
10. Required destruction/reset instructions remain present.
11. Creation order is compatible with dependencies.
12. No unrelated table was accidentally removed.
13. No unrelated database object was accidentally removed.
14. startup.sql remains capable of recreating the complete current database schema.

If the project contains another authoritative source listing database tables, compare startup.sql against that source.

Do not assume that the table being modified is the only table that needs to appear in startup.sql.

---

## 17.17 STARTUP.SQL IS THE DATABASE BOOTSTRAP SOURCE OF TRUTH

For database bootstrap purposes:

# startup.sql

complete current Remmi database schema.

The agent MUST NOT interpret startup.sql as:

"SQL needed for the current task."

The agent MUST interpret startup.sql as:

"Complete SQL needed to recreate the current Remmi database."

Therefore:

Every modification to startup.sql must preserve the completeness of the file.

A successful database schema change is:

"The complete startup script still represents the entire current database schema, including the requested change."

It is NOT merely:

"The requested table or column exists."

---

## 17.18 CRITICAL AI AGENT RULE

NEVER replace the existing complete startup.sql with a partial SQL script.

Before modifying startup.sql, assume:

**THIS FILE MUST ALWAYS BE COMPLETE.**

If the current task modifies one table:

The final file must still contain ALL existing tables.

If the current task modifies one column:

The final file must still contain ALL existing tables and the complete definition of every table.

If the current task adds one table:

The final file must still contain ALL existing tables plus the new table.

If the current task removes one table:

The final file must still contain ALL remaining tables.

If the current task changes one relationship:

The final file must still contain ALL tables and ALL other required database objects.

The agent must preserve the complete schema unless the task explicitly changes the schema itself.

============================================================
17.19 DATABASE OWNERSHIP REMAINS UNCHANGED
==========================================

The complete startup.sql requirement does NOT give plugins direct database access.

Plugins must still follow the Plugin Database Access Law.

Plugins communicate database operations through:

Plugin
?
EventBus
?
Database service
?
Database

The plugin owns its domain/item structure.

The database service owns database implementation.

startup.sql owns the complete database bootstrap definition.

These responsibilities remain separate.

============================================================
18. FILE SERVICE OWNERSHIP
    ==========================

File functionality belongs to the Android service boundary.

File service implementation must remain under the Android-related service structure.

Plugins must NEVER directly access:

* FileService.
* File service implementations.
* Android file APIs.
* Android Context for file access.
* File-system implementation details.

Plugins communicate file requests through EventBus.

Example:

Plugin
?
File command
?
EventBus
?
AndroidServiceManager / FileService
?
File operation

The FileService may publish resulting events through EventBus when other components need to know that a file operation occurred.

Examples:

FileCreatedEvent
FileUpdatedEvent
FileDeletedEvent

Use commands for requests and events for facts that already occurred.

Do not create a separate FileManager or FileServiceManager merely to wrap FileService.

Do not move file implementation into plugins.

Do not move file implementation into Database services.

Do not expose Android Context to plugins for file operations.

============================================================
19. PLUGIN MANAGER LAW
    ======================

PluginManager owns generic plugin lifecycle and generic plugin operations.

PluginManager may:

* Load plugins.
* Unload plugins.
* Register plugins.
* Discover plugins.
* Manage plugin lifecycle.
* Provide generic plugin infrastructure.

PluginManager must NOT become:

* Database manager.
* File manager.
* Android service manager.
* Event router.
* Plugin business logic container.
* UI manager.
* Feature manager.

Plugin-specific behavior remains in the plugin.

============================================================
20. AUTOMATION AND PLUGIN ACCESS
    ================================

Plugins do not directly access:

* AutomationEngine.
* DatabaseManager.
* AndroidServiceManager.
* FileService.
* File service implementations.

Managers and plugins communicate through EventBus.

Do not inject all managers into plugins.

Do not create a PluginContext replacement.

============================================================
21. NO MEGA CONTEXT OR SERVICE LOCATOR
    ======================================

Do not create:

PluginContext
AppContext
CoreContext
ManagerContext
ServiceLocator

that exposes everything.

Classes receive only the specific dependency or contract they need.

Do not replace one mega-context with another mega-context.

============================================================
22. UI OWNERSHIP
    ================

Shared application UI belongs under:

core/screens/

Existing shared components include:

components/
NutritionRadarGraph
RemmiEditorScaffold
RemmiFields
RemmiPickers

Application-wide screens may remain in core/screens.

Examples:

HomeScreen
SettingsScreen
RemmiApp
RemmiScreen
AppMenu

Plugin-specific UI belongs inside its plugin.

The canonical plugin UI structure is:

plugins/<pluginName>/ui/
screens/
popups/

Do not create:

core/ui/

Do not create global plugin UI directories outside the plugin.

Do not place plugin-specific screens or popups directly under the plugin root.

============================================================
23. GLOBAL UI STATE RULE
    ========================

Avoid global UI state managers.

UI state must have a clear owner.

Examples:

Screen state
?
Screen/ViewModel/state holder.

Plugin state
?
Plugin.

Popup state
?
Popup owner.

Navigation state
?
Navigation system.

Do not introduce new global UI state systems.

Before removing an existing global UI state class:

Find all usages.

Move each state responsibility to its proper owner.

Do not blindly delete it.

============================================================
24. WIDGET OWNERSHIP
    ====================

Plugin-specific widgets belong to the plugin that owns their functionality.

Examples:

TasksWidget
AlarmWidget
CalendarWidget

Generic Android widget infrastructure belongs under the Android boundary.

The primary plugin widget file belongs at the plugin root:

plugins/<pluginName>/<PluginName>Widgets.kt

Do not create a widgets directory for the primary plugin widget file unless explicitly required.

Do not move plugin-specific widget business logic into core.

Dashboard/widget infrastructure should be inspected carefully before moving.

Do not restructure it during unrelated work.

============================================================
25. MODEL OWNERSHIP
    ===================

Plugin-specific models belong inside their plugin.

Examples:

plugins/tasks/models/
plugins/alarm/models/
plugins/calendar/models/

Shared contracts belong in the subsystem that owns them.

Do not create generic dumping folders.

Do not place plugin-specific models in core.

============================================================
26. FEATURE OWNERSHIP
    =====================

Before adding a class, determine:

Who owns this responsibility?

Possible owners:

* Plugin.
* EventBus.
* Automation feature.
* Database service.
* File service.
* Android service.
* Shared core screen/component.

Place the class with its owner.

Never place a feature inside another unrelated feature.

============================================================
27. MINIMAL CHANGE LAW
    ======================

Before creating a new class:

1. Search for existing equivalent functionality.
2. Reuse or extend existing functionality if appropriate.
3. Create a new class only when ownership is genuinely new.

Do not duplicate:

* Services.
* Managers.
* Repositories.
* Models.
* Event types.
* Commands.
* UI components.

If file functionality already exists inside the Android service boundary, reuse it.

Do not create a new FileManager or FileServiceManager to duplicate or wrap the existing file service.

When changing a persisted plugin item structure, update startup.sql as required.

IMPORTANT:

For database changes, "minimal change" refers to the amount of schema that is actually changed.

It does NOT mean that startup.sql should contain only the changed table.

startup.sql MUST remain complete.

The correct approach is:

**Minimal schema modification + complete startup.sql preservation.**

============================================================
28. FILE MODIFICATION SAFETY
    ============================

Do not:

* Delete files without checking usages.
* Rename public classes unnecessarily.
* Move files unnecessarily.
* Rewrite entire files for small changes.
* Reformat unrelated code.
* Change package names unnecessarily.
* Touch unrelated plugins.

Before deleting:

1. Search references.
2. Verify ownership.
3. Remove references.
4. Build.
5. Then delete.

When migrating file functionality into the Android service boundary:

* Inspect existing FileService/FileManager usage first.
* Identify all references to the old file-service architecture.
* Preserve existing behavior.
* Remove obsolete manager layers only after references are migrated.
* Do not perform unrelated Android-service refactoring.

When changing persisted plugin data structures:

* Inspect startup.sql before making the change.
* Search for references to the affected database structure.
* Update the required schema definition.
* Preserve ALL other database definitions.
* Preserve ALL existing tables.
* Preserve required destruction/reset instructions.
* Verify that startup.sql remains a complete database bootstrap script.
* Do not modify unrelated database definitions unnecessarily.

============================================================
29. REQUIRED WORKFLOW
    =====================

For every task:

STEP 1 — READ RULES

Read this architecture contract.

STEP 2 — DEFINE SCOPE

Identify the minimum relevant:

* Files.
* Directories.
* Dependencies.

STEP 3 — INSPECT

Inspect only those files.

Do not scan the entire project unless required.

For plugin data/model changes, inspect the corresponding startup.sql definition.

For database-related tasks, inspect the COMPLETE startup.sql, not only the affected table.

STEP 4 — PLAN

Briefly state:

* Existing structure being used.
* Files that will change.
* Why each file changes.
* Whether startup.sql must change.
* If startup.sql changes, confirm that ALL existing tables and required database objects will remain represented.

STEP 5 — IMPLEMENT

Make the smallest possible change.

For database changes:

* Preserve the complete startup.sql.
* Modify only the required schema definitions.
* Do not remove unchanged tables.
* Do not create a partial startup.sql.

STEP 6 — VERIFY

Check:

* Imports.
* References.
* Dependencies.
* Compilation.
* Relevant tests.
* Database schema consistency when persisted plugin structures changed.
* startup.sql completeness.
* All existing tables are still represented.
* All existing table definitions remain complete.
* Required destruction/reset instructions remain present.
* Database creation order remains valid.
* No unrelated database definitions were accidentally removed.

STEP 7 — REPORT

Return only:

* Files inspected.
* Files changed.
* Summary.
* Build/test result.
* Remaining issues.

For database changes, explicitly confirm:

* `startup.sql` remains a complete database bootstrap script.
* All existing tables remain represented.
* The requested schema change was applied.
* No unrelated database definitions were removed.

============================================================
30. STRUCTURAL CHANGE SAFETY
    ============================

If a task requires architectural restructuring:

DO NOT immediately perform a large rewrite.

First provide:

1. Current affected structure.
2. Proposed affected structure.
3. Files to move.
4. Files to modify.
5. Files to delete.
6. Dependencies affected.
7. Risks.

Then refactor incrementally.

After each major step:

* Fix imports.
* Build affected code.
* Fix errors.
* Continue.

---

## 30.1 FILE-SERVICE MIGRATION

For file-service migration specifically:

Before removing any old file manager/service boundary:

1. Identify all current file-service implementations.
2. Identify all callers.
3. Identify all EventBus commands/events related to file operations.
4. Determine which functionality belongs inside the Android service boundary.
5. Migrate callers to the existing Android service/EventBus boundary.
6. Remove obsolete layers only after all references are migrated.
7. Build and verify affected functionality.

---

## 30.2 PLUGIN STRUCTURE MIGRATION

For plugin structure changes:

1. Inspect the existing plugin structure.
2. Do not automatically reorganize existing plugins.
3. If the task explicitly requires structural migration, compare the current structure with the canonical structure in Section 5.
4. Move only files that violate the required structure.
5. Update package declarations/imports/references as necessary.
6. Do not introduce repository/, logic/, external/, or other directories unless explicitly required by the task.
7. Build and verify after the migration.

The target structure is:

plugins/<pluginName>/ <PluginName>Plugin.kt <PluginName>Actions.kt <PluginName>Widgets.kt <PluginName>Repository.kt

```
models/

ui/
    screens/
    popups/
```

Do not combine plugin structure cleanup with unrelated feature work.

---

## 30.3 DATABASE STRUCTURE MIGRATION

When a task changes a persisted plugin item structure:

1. Identify the affected plugin item/model.

2. Read the COMPLETE:

   db-scripts/src/main/resources/startup.sql

3. Identify ALL existing database tables.

4. Identify ALL existing database objects required for startup.

5. Locate the corresponding database definition.

6. Compare the plugin structure with the database definition.

7. Determine the minimum required schema change.

8. Apply the change to the affected definition.

9. Preserve EVERY unrelated table definition.

10. Preserve EVERY unrelated database object definition.

11. Preserve required destruction/reset instructions.

12. Verify dependency and creation order.

13. Verify that the entire startup.sql remains a complete database bootstrap script.

14. Build and verify the affected code.

15. Confirm that the plugin structure and complete startup.sql remain synchronized.

The final startup.sql MUST represent the entire current database schema, not merely the database changes introduced by the current task.

============================================================
30.4 DATABASE STARTUP.SQL CHANGE SAFETY
=======================================

Whenever startup.sql is modified:

1. NEVER start by replacing the file with a new partial script.
2. Read the existing startup.sql first.
3. Identify every existing table.
4. Identify every required database object.
5. Identify the existing destruction/reset strategy.
6. Preserve all existing tables.
7. Preserve all existing database objects.
8. Modify only the required definitions.
9. Add new definitions only when required.
10. Remove definitions only when explicitly required and verified safe.
11. Verify the complete script after modification.
12. Ensure the file can still represent a full database rebuild.

If the agent cannot determine whether an existing table/object should remain:

**KEEP IT.**

Do not remove database definitions merely because they are not related to the current task.

============================================================
31. FORBIDDEN ACTIONS
    =====================

NEVER:

* Rewrite the entire project.
* Invent a new architecture.
* Add new managers without permission.
* Add a new EventBus.
* Add a second database access system.
* Give plugins direct database access.
* Give plugins direct file-service access.
* Give plugins Android Context.
* Give plugins direct AutomationEngine access.
* Give plugins direct AndroidServiceManager access.
* Give plugins direct Android API access.
* Add PluginContext or equivalent mega-context.
* Put plugin-specific UI in core.
* Put plugin screens outside ui/screens/.
* Put plugin popups outside ui/popups/.
* Create ui/external/ by default.
* Create repository/ for a single repository file.
* Create logic/ by default.
* Put one plugin's popup inside another plugin.
* Move unrelated code.
* Refactor unrelated code.
* Delete working functionality.
* Change database structure without explicit permission.
* Change public APIs without necessity.
* Create unnecessary boilerplate.
* Create unnecessary abstractions.
* Create a separate FileManager/FileServiceManager when the responsibility already belongs to AndroidServiceManager.
* Bypass EventBus for plugin-to-file-service communication.
* Reorganize existing plugins during unrelated tasks.
* Change a persisted plugin item structure without checking startup.sql.
* Leave startup.sql inconsistent with a plugin's persisted item structure.
* Modify unrelated database definitions unnecessarily while synchronizing a plugin's persisted structure.
* Modify startup.sql for UI-only or non-persisted changes.
* Replace startup.sql with a partial schema.
* Keep only the table currently being edited in startup.sql.
* Remove unchanged tables from startup.sql.
* Remove unchanged database objects from startup.sql.
* Convert startup.sql into a migration-only script.
* Remove destruction/reset instructions for unchanged tables merely because they were not modified.
* Assume another plugin's table does not belong in startup.sql.
* Recreate only the affected table when modifying database structure.
* Treat "minimal change" as permission to omit the rest of the database schema.
* Leave startup.sql unable to recreate the complete current database schema.
* Accidentally delete existing table definitions while adding or modifying another table.
* Create a new startup.sql containing only the latest database change.
* Remove complete table definitions merely because they were not involved in the current task.

============================================================
32. WHEN UNCERTAIN
    ==================

When unsure:

**DO NOT GUESS.**

Inspect the relevant code.

If still unclear:

**PRESERVE THE EXISTING IMPLEMENTATION.**

When unsure whether a table belongs in startup.sql:

**KEEP IT.**

When unsure whether an unchanged table should remain in startup.sql:

**KEEP IT.**

When unsure whether a destruction/reset instruction should remain:

**KEEP IT** unless there is clear evidence that it is obsolete and its removal is explicitly required.

When unsure whether a database definition is unrelated:

Do not remove it.

When unsure whether a change requires startup.sql modification:

Inspect the persisted structure and existing startup.sql before deciding.

The safe database default is:

**Preserve the complete existing startup schema**
+
**apply the smallest required database change**
+
**preserve all other tables and database objects**
+
**preserve required destruction/reset instructions**
+
**verify the complete schema remains rebuildable.**

The preferred solution is always:

**The smallest change**
that **preserves the architecture**
and **does not break working code**
while keeping **startup.sql as a complete, authoritative, rebuildable representation of the entire Remmi database schema.**
