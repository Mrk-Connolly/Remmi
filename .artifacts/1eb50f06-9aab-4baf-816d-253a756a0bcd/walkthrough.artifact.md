# Map Plugin Integration (MapLibre) Walkthrough

I have successfully integrated a new **Map Plugin** based on **MapLibre**, replacing the previous Google Maps implementation. This new plugin fits into the clean architecture and provides a centralized way for all other plugins to handle locations.

## Key Implementation Details

### 1. MapLibre Integration
- Replaced Google Maps dependencies with `org.maplibre.compose`.
- Implemented `MaplibreMap` with `rememberCameraState` for modern Compose-based map rendering.
- Used `GeoJsonSource` and `SymbolLayer` to render markers, ensuring a high-performance and declarative UI.

### 2. Command-Driven Architecture
- **PickLocationCommand**: Other plugins (like Calendar) now request location selection by publishing this command to the EventBus.
- **LocationPickedEvent**: The Map plugin publishes this event once a location is chosen, allowing the requester to update its state.
- **ShowMapCommand**: Navigate to the map view with an optional focus point.

### 3. Centralized Location Management
- **Map Plugin (`plugins/maps`)**: Owns all map-specific logic, repositories, and UI.
- **Saved Locations**: A new database table `saved_locations` was added to persist user-saved places.
- **Cross-Plugin markers**: The `MapScreen` dynamically fetches and geocodes locations from Calendar events and Contacts (when available) to display them as markers.

### 4. UI Components
- **MapScreen**: A full-screen map view showing all relevant locations.
- **LocationPickerPopup**: A reusable, MapLibre-powered dialog for picking any point on the map or searching for addresses.

## Files Created/Modified

### Core
- `app/src/main/kotlin/com/remmi/app/core/model/maps/SavedLocation.kt` (New Model)
- `app/src/main/kotlin/com/remmi/app/core/events/commands/StandardCommands.kt` (New Commands)
- `app/src/main/kotlin/com/remmi/app/core/events/events/StandardEvents.kt` (New Events)
- `app/src/main/kotlin/com/remmi/app/core/ui/state/UIStateManager.kt` (Added Overlay States)

### Map Plugin
- `app/src/main/kotlin/com/remmi/app/plugins/maps/MapPlugin.kt`
- `app/src/main/kotlin/com/remmi/app/plugins/maps/MapActions.kt`
- `app/src/main/kotlin/com/remmi/app/plugins/maps/repository/MapRepository.kt`
- `app/src/main/kotlin/com/remmi/app/plugins/maps/ui/screens/MapScreen.kt`
- `app/src/main/kotlin/com/remmi/app/plugins/maps/ui/popups/LocationPickerPopup.kt`

### Integration
- `app/src/main/kotlin/com/remmi/app/core/screens/AppMenu.kt` (Global Overlay & Nav)
- `app/src/main/kotlin/com/remmi/app/plugins/calendar/ui/screens/CalendarScreenEditor.kt` (Using new picker)
- `db-scripts/src/main/resources/startup.sql` (New Table)

## Verification Results
- **Build Status**: ✅ Success
- **Plugin Registry**: ✅ Maps plugin added to `PluginManager` and `assets/plugins.json`.
- **Event Flow**: ✅ Verified Calendar -> Map command/event roundtrip.

> [!TIP]
> You can now try the new "Map" option in the island menu to see your saved locations! To add a location to a calendar event, use the "Location" icon in the event editor.
