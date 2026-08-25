# Implementation Plan - Fix PostgrestRestException: Table 'saved_locations' not found

The application is experiencing a fatal crash because the `MapPlugin` attempts to sync with a non-existent `saved_locations` table in Supabase during initialization. This plan aims to prevent the crash and ensure the database is correctly updated.

## User Review Required

> [!IMPORTANT]
> I will be running the database update script to create the missing `saved_locations` table. If you have already manually modified your Supabase schema, please let me know.
> I will also be adding error handling to the plugin initialization to prevent future schema mismatches from crashing the app.

## Proposed Changes

### Core Infrastructure

#### [MODIFY] [CloudRepository.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/plugin/repository/CloudRepository.kt)
- Add a `try-catch` block in the `refresh()` method to handle `PostgrestRestException` and other network/database errors gracefully.
- Log errors instead of allowing them to propagate and crash the app.

### Plugins

#### [MODIFY] [MapPlugin.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/maps/MapPlugin.kt)
- Wrap the initial `refresh()` call in `onLoad()` with error handling.

### Database

#### [RUN] Database Update Script
- Execute the `:db-scripts:run` task to apply `startup.sql` which includes the `saved_locations` table definition.
- *Note: To bypass environment-specific Gradle failures in the `:app` module, I may temporarily isolate `:db-scripts` in `settings.gradle.kts` during execution.*

## Verification Plan

### Automated Tests
- Run `:db-scripts:run` and verify it reports "Successfully executed SQL script".

### Manual Verification
- Deploy the app and verify it no longer crashes on startup.
- Check logcat for "[MapPlugin] - Failed to sync locations" if the table is still missing, or verify markers appear if it was successfully created.
