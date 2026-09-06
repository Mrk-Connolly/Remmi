# Fix Call Recorder Plugin Loading and Visibility Plan

Resolve a critical initialization bug that prevents the Call Recorder plugin from loading at startup, which in turn makes it invisible in the main menu.

## User Review Required

> [!IMPORTANT]
> **Initialization Bug**: The Call Recorder was failing to load because it tried to access Android system services before the application was fully ready. This caused a "crash" during plugin loading that silently removed the Recorder from the menu.
>
> **Solution**: I will delay the initialization of the recording engine until the first time the UI is actually shown. This ensures the plugin loads successfully at startup and appears correctly in your menu.

## Proposed Changes

### [Call Recorder Plugin]

#### [MODIFY] [CallRecorderActions.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/callrecorder/CallRecorderActions.kt)
- Convert `isRecordingActive` and `currentRecording` from eager properties to computed properties (`get()`).
- This prevents the "Recording Manager" from being created prematurely during the plugin's boot phase.

#### [MODIFY] [CallRecorderPlugin.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/callrecorder/CallRecorderPlugin.kt)
- Add a safety check to `onLoad` to prevent repository operations if the context is not yet available (though it should be by that point).

---

### [Navigation & UI]

#### [MODIFY] [AppNavigation.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/ui/components/AppNavigation.kt)
- Double-check the `QuickAccessButton` for the Recorder to ensure it uses the correct icon and ID.

## Verification Plan

### Manual Verification
1.  **Restart App**: Fully close and reopen the app.
2.  **Check Menu**: Open the central FAB menu. Verify the **"Recorder"** button is now visible at the top.
3.  **Launch Plugin**: Tap the button and verify it opens the recording screen.
4.  **Check Productivity Group**: Scroll down and verify "Call Recorder" also appears in the Productivity section.
