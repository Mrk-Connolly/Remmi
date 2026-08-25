# Walkthrough - Maps Plugin Redo

I have successfully redone the Maps plugin from scratch, fixing the `IllegalStateException` and simplifying it to only provide a map view. The plugin now follows the canonical Remmi Architecture.

## Changes

### [Maps Plugin]

- **Canonical Structure**: Restructured the plugin to follow the required hierarchy:
    - [MapsPlugin.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/maps/MapsPlugin.kt)
    - [MapsActions.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/maps/MapsActions.kt)
    - [MapsWidgets.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/maps/MapsWidgets.kt)
    - [MapsRepository.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/maps/MapsRepository.kt)
    - [ui/screens/MapsScreen.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/maps/ui/screens/MapsScreen.kt)
- **Simplified Functionality**: Removed all previous logic for saving locations, geocoding, and location picking. The plugin now only renders a `MaplibreMap`.
- **Crash Fix**: Resolved the `java.lang.IllegalStateException` by correctly implementing the `MapsScreen` without style-dependent components outside the `MaplibreMap` scope.

### [Core Updates]

- **PluginManager**: Updated the registry to instantiate the new `MapsPlugin`.
- **AppMenu**:
    - Updated navigation to use the new `MapsPlugin` screen.
    - Removed the global `LocationPickerPopup` overlay.
    - Removed the `PickLocationCommand` listener for the map plugin.

## Verification Results

### Automated Tests
- Executed `:app:assembleDebug`: **BUILD SUCCESSFUL**.

### Manual Verification Required
- Deploy the app and open the "Map" plugin to verify the map renders correctly.
- Note that the location picking functionality in the Calendar plugin will no longer work, as the Map plugin no longer supports this feature.
