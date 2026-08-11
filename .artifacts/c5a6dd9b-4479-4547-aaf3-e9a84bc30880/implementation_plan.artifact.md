# Apply Settings with Confirmation Button

Modify the settings screen so that changes are not applied immediately. A button will appear only when changes are detected. Clicking this button will apply all changes, reload the plugins, and navigate the user back to the Home screen.

## User Review Required

> [!IMPORTANT]
> The "Apply Changes" button will refresh all plugins. This might cause a brief flicker or reset the state of active plugin screens as they are re-instantiated.

## Proposed Changes

### Core Logic

#### [MODIFY] [PluginManager.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/plugins/PluginManager.kt)
- Add `updateAllPluginSettings(newList: List<PluginMetadata>)` for batch updates.
- Ensure `loadPlugins` can be safely re-called to refresh active plugins.

### UI Changes

#### [MODIFY] [SettingsScreen.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/screens/SettingsScreen.kt)
- Update signature to accept `PluginContext` instead of just `PluginManager`.
- Implement local state tracking using `pendingMetadata`.
- Show an "Apply Changes" button (Floating Action Button) only when `pendingMetadata` differs from the current configuration.
- Update `PluginSettingItem` callbacks to modify `pendingMetadata` instead of calling the manager directly.

#### [MODIFY] [AppNavigation.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/navigation/AppNavigation.kt)
- Update `SettingsScreen` call to pass the `context`.

## Verification Plan

### Automated Tests
- I will verify if the build succeeds after these changes.
- I will check if `PluginMetadata` equality works as expected for change detection.

### Manual Verification
1. Open Settings.
2. Toggle a plugin setting (e.g., Enable/Disable).
3. Verify that the "Apply Changes" button appears.
4. Verify that the changes are NOT applied to the app immediately (e.g., the navigation bar doesn't change yet).
5. Press "Apply Changes".
6. Verify that the user is navigated to the Home screen.
7. Verify that the changes are now applied (e.g., plugin appeared/disappeared from navigation).
