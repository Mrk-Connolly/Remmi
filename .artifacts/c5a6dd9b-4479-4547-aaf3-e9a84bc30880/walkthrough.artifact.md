# Settings Confirmation Walkthrough

I have implemented the "Apply Changes" confirmation flow for the Settings screen. Changes to plugin settings (Enabled status, Navigation visibility, and Widget visibility) are now held in a pending state and only applied once you explicitly confirm them.

## Changes Overview

### Batch Settings Application
- Added `updateAllPluginSettings` to [PluginManager.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/plugins/PluginManager.kt) to allow updating all plugins in a single transaction.
- Enhanced `loadPlugins` to support reloading by properly unloading existing plugins and clearing the widget registry.

### UI Confirmation Flow
- Modified [SettingsScreen.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/screens/SettingsScreen.kt) to track changes locally.
- A new **Extended Floating Action Button** ("Apply Changes") appears at the bottom right only when there are unsaved changes.
- Pressing "Apply Changes" will:
    1. Persist the new configuration.
    2. Reload all plugins to ensure the UI (Navigation Bar, Widgets) updates immediately.
    3. Navigate you back to the Home screen.

### Dependency Injection Update
- Updated [AppNavigation.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/navigation/AppNavigation.kt) to provide the full `PluginContext` to the settings screen, enabling it to trigger plugin reloads.

## Verification
- Checked that toggling a switch makes the button appear.
- Verified that toggling it back makes the button disappear (no changes).
- Confirmed that clicking "Apply Changes" correctly saves the state and returns to Home.

> [!TIP]
> This new flow prevents accidental changes from immediately affecting your navigation layout, giving you a chance to review your plugin configuration before it goes live.
