# Fix ExceptionInInitializerError in Supabase Client Initialization

The application crashes when initializing the Supabase client because it cannot find a Ktor HTTP engine. Additionally, the project lacks necessary configurations for network access and backward compatibility for modern Java features used by the Supabase SDK.

## User Review Required

> [!IMPORTANT]
> This fix adds new dependencies for Ktor and Java 8+ desugaring. It also updates the `AndroidManifest.xml` to include Internet permissions.

## Proposed Changes

### [Android Configuration]

#### [MODIFY] [AndroidManifest.xml](file:///home/mark/StudioProjects/Remmi/app/src/main/AndroidManifest.xml)
- Add `<uses-permission android:name="android.permission.INTERNET" />` to allow the app to make network requests.

#### [MODIFY] [app/build.gradle.kts](file:///home/mark/StudioProjects/Remmi/app/build.gradle.kts)
- Add `ktor-client-android` dependency to provide an HTTP engine for the Supabase client.
- Enable `isCoreLibraryDesugaringEnabled` in `compileOptions`.
- Add `desugar_jdk_libs` dependency to support modern Java features on devices with API < 26 (current `minSdk` is 24).

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure the project builds with the new dependencies.

### Manual Verification
- Deploy the app to a device or emulator.
- Monitor Logcat for "Supabase" tags to verify that `testSupabase()` executes successfully without crashing.
- Verify that data is logged from the Supabase "test" table.
