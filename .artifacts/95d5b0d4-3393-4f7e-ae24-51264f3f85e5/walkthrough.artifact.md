# Walkthrough - Calendar Event Creation and Plugin Integration

I have successfully resolved the calendar event creation error and implemented the requested linked creation flow between plugins.

## Changes

### 1. Database Schema Synchronization
- Updated `startup.sql` to include missing columns in the `calendar` table:
    - `is_repeatable`, `repeatable_type`
    - `create_alarm`, `create_task`, `create_location`, `create_contact`
- Added `linked_calendar_event` column to `saved_locations` table to support cleanup.

### 2. Calendar Plugin Enhancements
- **Model Update**: Aligned `CalendarItem.kt` with the new schema and mandatory/optional field structure.
- **Actions Update**: Updated `CalendarActions.kt` to handle the new fields and publish events with the correct flags.
- **UI Redesign**:
    - Moved `CalendarScreen.kt` and `CalendarScreenEditor.kt` to `ui/screens/` to follow canonical structure.
    - Implemented the requested **row of 4 icon buttons** (Alarm, Task, Location, Contact) with equal weight.
    - Updated logic: toggling these buttons now triggers a configuration popup from the respective plugin via `GlobalUIState`.
    - Implemented the `LocationPickedEvent` listener to update the event's location list.

### 3. Linked Item Configuration Popups
- **Alarm**: Updated `AlarmConfigurationDialog.kt` with day/month/year fields and easy selection icons (Calendar and Clock).
- **Tasks**: Redesigned `TaskConfigurationDialog.kt` to support a draft list of tasks with an "Add Another Task" option.
- **Maps**: Implemented `LocationPickerPopup.kt` with a `MapLibre` view and a search bar for cities and streets.

### 4. Cross-Plugin Integration & Cleanup
- Updated `AlarmPlugin.kt`, `TasksPlugin.kt`, and `MapsPlugin.kt` to listen for `CalendarEventDeletedEvent` and perform cascading deletes of linked items.
- Standardized `GlobalUIState.kt` to manage cross-plugin creation requests.
- Updated `AppMenu.kt` to host the new global `LocationPickerPopup`.

### 5. Bug Fixes
- Resolved `Unresolved reference 'System'` errors by migrating from `Clock.System` to `Instant.fromEpochMilliseconds(System.currentTimeMillis())` for better compatibility with the current environment.
- Fixed various import and package visibility issues caused by the restructuring.

## Verification Results

### Automated Tests
- Executed `./gradlew :app:assembleDebug`: **BUILD SUCCESSFUL**.

### Manual Verification Path
1. Open the Calendar plugin.
2. Click "+" to create a new event.
3. Observe the 4 icon buttons below the group selection.
4. Toggle each button and verify the specialized popups appear.
5. Save the event and verify (via logs or DB) that linked items are requested.
6. Delete an event and verify linked items are cleaned up.
