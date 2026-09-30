# Tasks - Fix Suspend Function Call in CallRecordingService

- [x] Initialize `eventBus` in `onCreate`
- [x] Refactor `stopRecordingSync` to return the event
- [x] Update `stopRecording` to publish the returned event
- [x] Update `handleCallEnded` to capture call info before nulling
- [x] Update `onDestroy` to publish the final event if it exists
- [x] Verify build with `./gradlew :app:compileDebugKotlin`
