# Implementation Plan - Fix Unresolved Reference 'logic' in CallRecorderService

The `CallRecorderService` is failing to compile because it depends on a `logic` package and several missing classes (`CallRecordingManager`, `CallStateMonitor`) within the `callrecorder` plugin. Additionally, it expects certain static accessors in `CallRecorderPlugin`.

## Proposed Changes

### [Component] Call Recorder Plugin Logic

#### [NEW] [CallRecordingManager.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/callrecorder/logic/CallRecordingManager.kt)
- Create this class to encapsulate `MediaRecorder` operations.
- Implement `startRecording(phoneNumber: String?)` to begin audio capture.
- Implement `stopRecording(): CallRecording?` to finalize the recording and return a model object.

#### [NEW] [CallStateMonitor.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/callrecorder/logic/CallStateMonitor.kt)
- Create this `BroadcastReceiver` to listen for telephony state changes.
- Provide callbacks for `onCallStarted` (with phone number) and `onCallEnded`.

### [Component] Call Recorder Plugin Integration

#### [MODIFY] [CallRecorderPlugin.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/callrecorder/CallRecorderPlugin.kt)
- Add a `companion object` to provide static access to the plugin's `CallRecordingManager` and `CallRecorderActions`.
- Initialize a private `_manager` instance of `CallRecordingManager`.
- Set the static instance reference in `init`.

#### [MODIFY] [CallRecorderActions.kt](file:///home/mark/StudioProjects/Remmi/app/src/main/kotlin/com/remmi/app/plugins/callrecorder/CallRecorderActions.kt)
- Add `saveRecording(recording: CallRecording)` to persist recordings captured by the service.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify that all unresolved references are fixed.

### Manual Verification
- N/A (Build fix only)
