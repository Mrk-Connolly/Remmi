# Final Calendar & Navigation Polishing Plan

Addressing the "Double Menu" navigation issue, fixing group visibility in the editor, and implementing advanced scroll highlighting for upcoming events.

## User Review Required

> [!IMPORTANT]
> - I will eliminate the **redundant fixed navigation bar** that appears when the menu is expanded. The navigation icons will only exist in the bottom "stripe" of the sheet.
> - The **Upcoming Events** list will now have **Active Date Highlighting**: as you scroll, the date header at the top will be circled, and all associated events will be encased in a "highlight box". The same date will be "shaded" in the top monthly calendar for visual synchronization.

## Proposed Changes

### Navigation & Menu (`AppMenu.kt`)

#### [MODIFY] [AppMenu.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/screens/AppMenu.kt)
- **Remove Redundant Bottom Bar**: Delete the second `Surface` (the fixed navigation stripe) inside `UnifiedDockMenu`.
- **Align Icons**: Ensure the single navigation bar is always at the bottom of the sheet and is perfectly centered in the 90dp peek state.

### Calendar Plugin (`CalendarScreen.kt`)

#### [MODIFY] [CalendarScreen.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/calendar/ui/screens/CalendarScreen.kt)
- **Active Date Highlighting**:
    - Use `LazyListState` to track the `firstVisibleItemIndex`.
    - Map this index back to the date header currently at the top of the view.
    - **Visuals**:
        - Circle the `DateHeader` when it's the "active" view date.
        - Shade the corresponding day in the `SelectableCalendar` at the top.
        - Add a "highlight square" (border/background) around all events belonging to the active date.
        - Apply a slight `scale` (e.g., 0.95f) to unhighlighted events for better focus.
- **Group Filter Fix**: Ensure `groups` (fetched from `actions.getCalendarGroups()`) are properly passed to the `CalendarHeader` dropdown.
- **FAB Position**: Position the button at `bottom = 16.dp` to sit cleanly next to the dock.

### Calendar Editor & Popups

#### [MODIFY] [CalendarScreenEditor.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/calendar/ui/screens/CalendarScreenEditor.kt)
- **Add Description**: Place the `description` field directly under the `title` field.
- **Fix Group Sync**:
    - Ensure `groups` state is updated immediately after a new group is saved.
    - Correct the `DropdownMenu` logic to ensure items are visible and selectable.

## Verification Plan

### Manual Verification
- Verify that only one stable bottom bar exists across all states.
- Confirm that swiping up pulls the menu from the absolute bottom.
- Test "Active Date Highlighting": scroll the upcoming list and verify headers are circled, events are boxed, and the top calendar shades the matching day.
- Verify group selection and description field in the editor.
- Confirm FAB doesn't obscure the dock.
