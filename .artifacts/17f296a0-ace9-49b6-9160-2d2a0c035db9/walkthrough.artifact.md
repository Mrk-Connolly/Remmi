# Calendar Overhaul Walkthrough

I have significantly enhanced the Calendar plugin with improved navigation, a more powerful editor, and better system integration.

## Key Enhancements

### 1. Smart Navigation & Highlighting
- **Active Date Highlighting**: As you scroll through upcoming events, the date at the top of the list is now highlighted (larger and bold).
- **Contextual Event Creation**: Tapping the "Add Event" button (now conveniently located lower on the screen) will automatically pre-select the currently highlighted date.
- **Empty State "Today"**: The list now always shows a "Today" section, even if you have no events, providing a clear reference point.

### 2. Redesigned Calendar Editor
The editor has been completely overhauled for a faster, more intuitive workflow:
- **Date/Time Grid**: Starting and ending dates/times are now paired in rows.
- **Range Selection**: Tapping the calendar icon opens a new range picker. Tap the start date, then the end date, and the range is automatically highlighted and saved.
- **Greyed-out Defaults**: The "End Date" is greyed out by default (matching the start date) until you explicitly change it.
- **Quick Toggles**: "Add Task" and "Add Alarm" are now simple toggles. If enabled, the app will automatically create and link a task or alarm when you save the event.
- **Direct Selection**: Tapping a time field opens a dedicated clock popup for easy selection.

### 3. Advanced Feature Dialogs
- **Location with Maps**: The "Add Location" dialog now features an integrated Google Map. Tap anywhere on the map to drop a marker, and the app will automatically capture the address.
- **Improved Participant Selection**: Search for contacts and filter them by group to quickly build your meeting list.

## Technical Summary
- **Dependencies**: Added `maps-compose` and `play-services-location`.
- **UI Components**: Created `RemmiDateRangePickerDialog` and updated `LocationDialog` with Map support.
- **Permissions**: Added necessary Location permissions to `AndroidManifest.xml`.

## Verification Path
1. **Calendar Screen**: Scroll the list and verify the date headers "grow" when they reach the top.
2. **FAB**: Click the "Add Event" button and verify the date matches the highlighted one.
3. **Editor**:
    - Toggle "Add Task" and "Add Alarm".
    - Use the calendar icon to select a multi-day range.
    - Click "Location" and verify the map opens (requires API key for full functionality).
