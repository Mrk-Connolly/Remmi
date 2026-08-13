# Implementation Plan - Plugin Database Reformatting and SQL Reorganization

This plan addresses the requirement to add a "Reformat Database" feature for each plugin in the Settings page and to reorganize the SQL creation scripts to be self-contained within each plugin's directory.

## User Review Required

> [!IMPORTANT]
> **Reformatting Limitation:** In a Supabase environment, "reformatting" will be implemented as **clearing all data** from the plugin's tables. Dropping and recreating tables (the "structure") from a mobile client is generally not supported by Postgrest/Supabase for security reasons. The SQL scripts moved to plugin folders will serve as documentation and local reference.

> [!WARNING]
> **Data Loss:** Reformatting a plugin's database is irreversible. A confirmation dialog will be added to prevent accidental data loss.

## Proposed Changes

### SQL Script Reorganization

I will copy the existing SQL scripts from `documents/sql-scripts/plugins/` to their respective plugin folders under a new `sql-scripts/create/` directory.

- `alarm`: [alarms.sql](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/alarm/sql-scripts/create/alarms.sql)
- `calendar`: [calendar.sql](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/calendar/sql-scripts/create/calendar.sql)
- `contacts`: [contacts.sql](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/contacts/sql-scripts/create/contacts.sql)
- `tasks`: [tasks.sql](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/tasks/sql-scripts/create/tasks.sql)
- `gift`: [gift_ideas.sql](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/gift/sql-scripts/create/gift_ideas.sql)

### Core Framework

#### [MODIFY] [DatabaseService.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/service/DatabaseService.kt)
- Add `suspend fun clearTable(tableName: String)` to the interface.

#### [MODIFY] [SupabaseService.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/service/SupabaseService.kt)
- Implement `clearTable` using a `delete` operation with a broad filter (e.g., `neq("id", "")`).

#### [MODIFY] [CloudRepository.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/repository/CloudRepository.kt)
- Add `suspend fun clearAll()` which calls `databaseService.clearTable(tableName)` and then clears the local `items` map.

#### [MODIFY] [RemmiPlugin.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/plugins/RemmiPlugin.kt)
- Add `suspend fun reformat()` to the interface.

### Plugin Implementations

I will update each plugin to implement the `reformat()` method, typically by delegating to its repository.

- `AlarmPlugin`, `CalendarPlugin`, `ContactPlugin`, `GiftPlugin`, `TasksPlugin`.

### UI Changes

#### [MODIFY] [SettingsScreen.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/screens/SettingsScreen.kt)
- Update the `Plugin Information` dialog (triggered by long-press on a setting item) to include a "Reformat Database" button.
- Implement a confirmation dialog when this button is clicked.
- Call `plugin.reformat()` upon confirmation.

## Verification Plan

### Automated Tests
- Build the project to ensure all interface changes are correctly implemented.

### Manual Verification
1. **File Move:** Verify that SQL scripts are correctly copied to the new locations.
2. **Reformat UI:**
   - Open Settings.
   - Long-press a plugin.
   - Verify the "Reformat Database" button exists.
   - Click it and verify the confirmation dialog appears.
3. **Reformat Execution:**
   - Add some data to a plugin (e.g., an Alarm).
   - Reformat that plugin's database.
   - Verify that the data is gone from the UI and from the cloud (if possible to check).
   - Verify other plugins' data remains untouched.
