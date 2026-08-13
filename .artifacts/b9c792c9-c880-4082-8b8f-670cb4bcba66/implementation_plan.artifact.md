# Implementation Plan - Fix Exploded File System

The Kotlin source code was accidentally moved into the `app/src/main/res/images` directory. I will restore the project structure by moving the files back to their intended locations and cleaning up the resource directory.

## User Review Required

> [!IMPORTANT]
> I will move all Kotlin files from `app/src/main/res/images/app` back to `app/src/main/kotlin/com/remmi/app`. I will also remove the `app/src/main/res/images` directory as it is not a standard Android resource folder and currently contains misplaced source code.

## Proposed Changes

### [Component] Project Structure Restoration

#### [MOVE] Restore Kotlin Source Files
- Move all content from `app/src/main/res/images/app/` to `app/src/main/kotlin/com/remmi/app/`.
- This includes:
    - `MainActivity.kt`
    - `RemmiApp.kt`
    - `core/` directory
    - `plugins/` directory

#### [CLEANUP] Remove Misplaced Directories
- Remove `app/src/main/res/images` after the move is complete.

## Verification Plan

### Automated Tests
- Run `gradlew assembleDebug` to verify that the build system can locate the source files again.

### Manual Verification
- Verify that the `com.remmi.app` package structure is visible in the project view.
- Ensure no source files are left in the `res` directory.
