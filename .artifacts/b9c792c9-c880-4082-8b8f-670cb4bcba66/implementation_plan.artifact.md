# Implementation Plan - Enhanced Alarm Selection in Tasks Editor

I will modify the `TasksEditorScreen` to allow users to set a specific date and time for alarms when a task has a due date. The alarm date will default to the task's due date but remain fully customizable.

## Proposed Changes

### [Tasks Plugin]

#### [MODIFY] [TasksEditorScreen.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/tasks/TasksEditorScreen.kt)
- **State Updates**:
    - Add `alarmDate: LocalDate?` to track the specific date for the alarm.
    - Add `showAlarmDatePicker: Boolean` to control the visibility of the alarm date picker.
- **Logic Updates**:
    - When `addToAlarm` is enabled:
        - If `isDueDateEnabled` is true, initialize `alarmDate` with the current `startDate`.
        - Always trigger the `showAlarmTimePicker`.
    - In the "Save" handler:
        - Combine `alarmDate` (or `startDate` as fallback) and `alarmTime` into the `finalAlarmInstant` passed to `actions.createTask`.
- **UI Updates**:
    - In the "Quick Actions" section, if `addToAlarm` is active:
        - If `isDueDateEnabled` is true:
            - Display both the `alarmDate` and `alarmTime`.
            - Make the `alarmDate` clickable to open a `DatePickerDialog`.
            - Make the `alarmTime` clickable to open the `TimePicker`.
        - If `isDueDateEnabled` is false:
            - Continue showing only the `alarmTime` (assuming it refers to the current day or relative to task creation).
- **Dialogs**:
    - Add a new `DatePickerDialog` instance specifically for the alarm date.

## Verification Plan

### Automated Tests
- Run `gradlew assembleDebug` to ensure no build regressions.

### Manual Verification
- **Scenario 1: No Due Date**
    - Create a new task without a due date.
    - Enable "Create Alarm".
    - Verify only the time picker appears.
- **Scenario 2: With Due Date**
    - Create a new task with a due date set to next Friday.
    - Enable "Create Alarm".
    - Verify the alarm date defaults to next Friday.
    - Change the alarm date to next Thursday using the calendar.
    - Change the alarm time.
    - Save the task and verify (via logs or database) that the alarm is scheduled for the custom date and time.
