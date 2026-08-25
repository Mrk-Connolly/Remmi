# Implementation Plan - Map Plugin (MapLibre)

This plan outlines the integration of a new **Map Plugin** into the Remmi ecosystem, replacing existing Google Maps dependencies with **MapLibre**.

## User Review Required

> [!IMPORTANT]
> The Map plugin will introduce a new top-level table `saved_locations` in the database.
> Other plugins will now use `PickLocationCommand` to request location selection, rather than calling UI dialogs directly.

## Proposed Changes

### Core Models & Infrastructure

#### [MODIFY] [SavedLocation.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/model/maps/SavedLocation.kt)
New model for user-saved locations using the existing `Location` component.

#### [MODIFY] [ContactItem.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/contacts/ContactItem.kt)
Add optional `address: String?` field to support contact locations on the map.

#### [MODIFY] [StandardCommands.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/events/commands/StandardCommands.kt)
Add `PickLocationCommand` and `ShowMapCommand`.

#### [MODIFY] [StandardEvents.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/events/events/StandardEvents.kt)
Add `LocationPickedEvent`.

#### [MODIFY] [UIStateManager.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/ui/state/UIStateManager.kt)
Add state flags for the global location picker overlay.

---

### Map Plugin Component (`plugins/maps`)

#### [NEW] [MapPlugin.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/maps/MapPlugin.kt)
Plugin entry point. Registers listeners for map-related commands.

#### [NEW] [MapRepository.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/maps/MapRepository.kt)
Manages persistence of `SavedLocation` objects.

#### [NEW] [MapActions.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/maps/MapActions.kt)
Logic for geocoding, fetching data from other plugins (Calendar/Contacts), and processing commands.

#### [NEW] [MapScreen.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/maps/ui/screens/MapScreen.kt)
Full-screen map view using MapLibre.

#### [NEW] [LocationPickerPopup.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/maps/ui/popups/LocationPickerPopup.kt)
Reusable location selection dialog owned by the Map plugin.

---

### Integration & Dependencies

#### [MODIFY] [build.gradle.kts](file:///home/mark/StudioProjects/Remmi/app/build.gradle.kts)
- Add `org.maplibre.compose:maplibre-compose-android:0.14.0`.
- Mark Google Maps dependencies for removal in a future cleanup.

#### [MODIFY] [startup.sql](file:///home/mark/StudioProjects/Remmi/db-scripts/src/main/resources/startup.sql)
Add the `saved_locations` table and sample data.

#### [MODIFY] [AppMenu.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/screens/AppMenu.kt)
Integrate the global `LocationPickerPopup` overlay.

#### [MODIFY] [CalendarScreenEditor.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/calendar/ui/screens/CalendarScreenEditor.kt)
Replace direct `LocationDialog` call with `PickLocationCommand`.

## Communication Flow

1. **Request**: `CalendarPlugin` publishes `PickLocationCommand`.
2. **Handle**: `MapPlugin` receives command, sets `UIStateManager.showLocationPicker = true`.
3. **UI**: `AppNavigation` renders `LocationPickerPopup`.
4. **Result**: User selects point. `MapPlugin` publishes `LocationPickedEvent`.
5. **Update**: `CalendarPlugin` updates its local state with the result.

## Verification Plan

### Automated Tests
- Verify `MapRepository` CRUD operations.
- Verify `MapActions` correctly filters events/contacts with locations.

### Manual Verification
- Deploy app and navigate to "Map" from the island menu.
- Open Calendar, add an event, and use the new MapLibre-based location picker.
- Verify saved locations appear as markers on the main map.
