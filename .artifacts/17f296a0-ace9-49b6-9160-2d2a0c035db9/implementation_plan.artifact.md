# Implementation Plan - Calendar Editor Overhaul

Complete redesign of the Calendar Editor to improve data entry flow, add range selection, and integrate advanced features like location picking and contact filtering.

## User Review Required

> [!IMPORTANT]
> - **Google Maps Integration**: To implement the "Add Location" feature with a map, I need to add `com.google.maps.android:maps-compose` and `com.google.android.gms:play-services-maps` to the project dependencies. This will also require a Google Maps API Key to be configured in the `AndroidManifest.xml` (or I will provide a placeholder for you to fill).
> - **Date Range Logic**: The calendar popup will use a 3-step cycle:
>   1. Tap 1: Set Start Date.
>   2. Tap 2 (Later Date): Set End Date and highlight the range.
>   3. Tap 3: Reset selection.

## Proposed Changes

### Build Configuration

#### [MODIFY] [build.gradle.kts](file:///home/mark/StudioProjects/Remmi/app/build.gradle.kts)
- Add Google Maps and Location dependencies.

#### [MODIFY] [AndroidManifest.xml](file:///home/mark/StudioProjects/Remmi/app/src/main/AndroidManifest.xml)
- Add `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` permissions.
- Add Google Maps API key metadata placeholder.

### Core Components

#### [MODIFY] [RemmiPickers.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/screens/components/RemmiPickers.kt)
- Add `RemmiDateRangePickerDialog` implementing the requested cycle logic.
- Ensure `RemmiTimePickerDialog` uses the Material 3 `TimePicker` state correctly.

#### [MODIFY] [LocationDialog.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/screens/popups/LocationDialog.kt)
- Integrate `GoogleMap` composable.
- Allow users to tap on the map to select a location.
- Use `Geocoder` (or placeholder logic) to retrieve Name and Address from coordinates.

### Calendar Plugin UI

#### [MODIFY] [CalendarScreen.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/calendar/ui/screens/CalendarScreen.kt)
- **Today Highlight**: Update the upcoming list to highlight the current date (larger text/bold).
- **Today Placeholder**: Ensure "Today" header appears even if there are no events.
- **FAB Position**: Move the "Add Event" button lower, near the bottom menu.

#### [MODIFY] [CalendarScreenEditor.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/calendar/ui/screens/CalendarScreenEditor.kt)
- **New Layout Structure**:
    - Title / Description fields.
    - **Date/Time Grid**:
        - Row 1: `Start Date (Readonly)` | `Start Time Button` | `Calendar Icon Button`
        - Row 2: `End Date (Readonly)` | `End Time Button`
    - **End Date Styling**: Grey out the End Date field if it matches the Start Date.
    - **Quick Add Toggles**: Horizontal row of `FilterChip`s for "Add Task" and "Add Alarm".
    - **Action Buttons**: "Add Contacts" and "Add Location" icons with clear labels.
- **Logic**:
    - Pre-select `activeDate` from the `CalendarScreen` when creating.
    - Handle enabled/disabled states for Task/Alarm creation.

## Verification Plan

### Automated Tests
- Build project and check for dependency conflicts.

### Manual Verification
- **Editor Layout**: Verify the new row-based date/time layout.
- **Date Range**: Open the calendar popup, tap two dates, and verify the range highlights and populates both fields.
- **Time Picker**: Verify tapping time buttons opens the clock popup.
- **Location**: Verify the map opens and coordinates/addresses are captured.
- **Contacts**: Verify the search and group filters in the participant list.
- **FAB**: Verify the "Add Event" button is repositioned.
