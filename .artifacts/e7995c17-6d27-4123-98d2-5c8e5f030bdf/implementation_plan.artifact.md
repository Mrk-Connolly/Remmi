# Implementation Plan - Fix Suspend Function Call in CallRecordingService

Fix the compilation error: `Suspend function 'suspend fun publishEvent(event: RemmiEvent): Unit' can only be called from a coroutine or another suspend function.` in `CallRecordingService.kt`.

## User Review Required

> [!IMPORTANT]
> I will initialize the instance variable `eventBus` from `CallRecordingService.eventBus` in `onCreate`. This ensures the service can publish events if the static `eventBus` was provided.

## Proposed Changes

### app

#### [MODIFY] [CallRecordingService.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/core/android/services/CallRecordingService.kt)

- **Initialize `eventBus`**: In `onCreate`, assign `CallRecordingService.eventBus` to the instance property `eventBus`.
- **Refactor `stopRecordingSync`**:
    - Change signature to `private fun stopRecordingSync(callInfo: CallDetectedEvent?): CallRecordingFinishedEvent?`.
    - Remove the `eventBus?.publishEvent(...)` call.
    - Return the `CallRecordingFinishedEvent` instead of publishing it.
- **Update `stopRecording`**:
    - Change signature to `private suspend fun stopRecording(callInfo: CallDetectedEvent? = currentCallInfo)`.
    - Call `stopRecordingSync(callInfo)` and then `publishEvent(event)` if an event is returned.
- **Update `handleCallEnded`**:
    - Capture `currentCallInfo` before setting it to `null`.
    - Pass the captured `callInfo` to `stopRecording(callInfo)`.
- **Update `onDestroy`**:
    - Capture `currentCallInfo` and call `stopRecordingSync(callInfo)`.
    - If an event is returned, launch a coroutine to publish it: `scope.launch { eventBus?.publishEvent(event) }`.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify the fix.
- Run any relevant unit tests.

### Manual Verification
- N/A (Compilation fix)
