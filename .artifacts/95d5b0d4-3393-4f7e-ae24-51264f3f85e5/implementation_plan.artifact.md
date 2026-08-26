# Implementation Plan - Calendar Event Creation and Plugin Integration (Fixes)

This plan addresses the error in calendar event creation and implements the requested linked item creation flow between the Calendar, Alarm, Tasks, Maps, and Contacts plugins. It also fixes compilation errors from the previous attempt.

## User Review Required

> [!IMPORTANT]
> The database schema for the `calendar` table will be updated to include several new flags (`create_alarm`, `create_task`, `create_location`, `create_contact`, `is_repeatable`, `repeatable_type`).
> The `saved_locations` table will also be updated to include a `linked_calendar_event` column to support deletion cleanup.

> [!WARNING]
> Toggling the linked item buttons (Alarm, Task, Location) in the Calendar editor will now immediately trigger a configuration popup provided by the respective plugin.

## Proposed Changes

### [Database]
#### [MODIFY] [startup.sql](file:///home/mark/StudioProjects/Remmi/db-scripts/src/main/resources/startup.sql)
- Sync `calendar` and `saved_locations` tables with the new linked fields.

### [Core / Common]
#### [MODIFY] [StandardCommands.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/eventBus/commands/StandardCommands.kt)
- Fix `CreateCalendarEventCommand` to include all requested fields.
#### [MODIFY] [StandardEvents.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/eventBus/events/StandardEvents.kt)
- Fix `LinkedCreationRequest` to include location and contact flags.
#### [MODIFY] [GlobalUIState.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/controller/GlobalUIState.kt)
- Standardize state for linked item popups.

### [Calendar Plugin]
#### [MODIFY] [CalendarItem.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/calendar/models/CalendarItem.kt)
- Align with required structure.
#### [MODIFY] [CalendarActions.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/calendar/CalendarActions.kt)
- Update logic for creating events with linked items.
#### [MODIFY] [CalendarScreenEditor.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/calendar/ui/screens/CalendarScreenEditor.kt)
- Fix compilation errors (imports, variable scoping).
- Implement the row of 4 buttons (Alarm, Task, Location, Contact) using equal weight.
#### [MODIFY] [CalendarScreen.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/calendar/ui/screens/CalendarScreen.kt)
- Fix package and imports.

### [Alarm & Tasks Plugins]
#### [MODIFY] [AlarmConfigurationDialog.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/alarm/popups/AlarmConfigurationDialog.kt)
- Implement day/month/year fields with easy selection icons as requested.
#### [MODIFY] [TaskConfigurationDialog.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/tasks/popups/TaskConfigurationDialog.kt)
- Implement task list with "add more" functionality.

### [Maps Plugin]
#### [MODIFY] [MapsActions.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/maps/MapsActions.kt)
- Fix `Clock.System` import.
#### [MODIFY] [MapsPlugin.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/maps/MapsPlugin.kt)
- Fix `launch` import and coroutine usage.
#### [NEW] [LocationPickerPopup.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/maps/ui/popups/LocationPickerPopup.kt)
- Implement map-based picker with search bar.

## Verification Plan

### Automated Tests
- `./gradlew :app:assembleDebug`

### Manual Verification
- Create a calendar event, toggle each of the 4 link buttons, verify their respective popups appear and function.
- Save the event and check logs/database for linked item creation.
- Delete the event and verify cascading deletion.
