# Implementation Plan - Redo Maps Plugin

Redo the `maps` plugin from scratch to fix a `java.lang.IllegalStateException` and simplify its functionality to only "opening the map". The new implementation will follow the Remmi Architecture Contract's canonical plugin structure.

## User Review Required

> [!IMPORTANT]
> This plan will remove existing functionality such as "Saved Locations", geocoding, and the "Location Picker" popup, as requested ("the only function it should have is to open the map, nothing else").

## Proposed Changes

### [Maps Plugin]

The plugin will be restructured to follow the canonical structure in `app/src/main/kotlin/com/remmi/app/plugins/maps/`.

#### [NEW] [MapsPlugin.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/maps/MapsPlugin.kt)
New plugin entry point.

#### [NEW] [MapsActions.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/maps/MapsActions.kt)
Minimal actions for the map plugin.

#### [NEW] [MapsWidgets.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/maps/MapsWidgets.kt)
Minimal widget (placeholder).

#### [NEW] [MapsRepository.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/maps/MapsRepository.kt)
Minimal repository (placeholder).

#### [MODIFY] [MapsScreen.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/maps/ui/screens/MapsScreen.kt)
Redesigned screen that only displays the map and fixes the `IllegalStateException` by correctly scoping MapLibre components.

#### [DELETE] `app/src/main/kotlin/com/remmi/app/plugins/maps/MapPlugin.kt`
#### [DELETE] `app/src/main/kotlin/com/remmi/app/plugins/maps/MapActions.kt`
#### [DELETE] `app/src/main/kotlin/com/remmi/app/plugins/maps/repository/MapRepository.kt`
#### [DELETE] `app/src/main/kotlin/com/remmi/app/plugins/maps/screens/MapScreen.kt`
#### [DELETE] `app/src/main/kotlin/com/remmi/app/plugins/maps/popups/LocationPickerPopup.kt`
#### [DELETE] `app/src/main/kotlin/com/remmi/app/plugins/maps/models/SavedLocation.kt`

### [Core]

#### [MODIFY] [PluginManager.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/plugin/PluginManager.kt)
Update reference from `MapPlugin` to `MapsPlugin`.

#### [MODIFY] [AppMenu.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/screens/AppMenu.kt)
Update reference from `MapPlugin` to `MapsPlugin`.

## Verification Plan

### Automated Tests
- Build the project to ensure no compilation errors: `./gradlew :app:assembleDebug`

### Manual Verification
- Deploy the app to a device/emulator.
- Open the "Maps" plugin from the menu.
- Verify the map renders without crashing.
- Verify that only the map is shown (no other UI elements like markers or FABs from the previous implementation).
