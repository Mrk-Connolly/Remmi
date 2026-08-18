# Walkthrough - Fixing Supabase Client Initialization Crash

I have fixed the `ExceptionInInitializerError` that was occurring when the Supabase client was being initialized. The crash was primarily due to a missing Ktor HTTP engine and missing configurations for Java 8+ features required by the Supabase SDK on older Android versions.

## Changes Made

### 1. Added Internet Permission
I updated the [AndroidManifest.xml](file:///home/mark/StudioProjects/Remmi/app/src/main/AndroidManifest.xml) to include the `INTERNET` permission, which is required for Supabase to communicate with its backend services.

### 2. Added Ktor HTTP Engine
I added the `ktor-client-android` dependency to [app/build.gradle.kts](file:///home/mark/StudioProjects/Remmi/app/build.gradle.kts). Supabase-kt uses Ktor internally but requires an explicit engine implementation (like `Android`, `OkHttp`, or `CIO`) to be present on the classpath.

### 3. Enabled Core Library Desugaring
Since the project's `minSdk` is 24 and Supabase-kt v3.x uses modern Java features (like `java.time`), I enabled `isCoreLibraryDesugaringEnabled` and added the `desugar_jdk_libs` dependency. This ensures the app remains compatible with devices running older versions of Android.

## Verification Results

### Build Success
The project was successfully synchronized with Gradle and built using `./gradlew :app:assembleDebug`.

### Runtime Verification
I deployed the app to an emulator and monitored the logs:
- **Client Initialization:** The logs show `SupabaseClient created!`, confirming the `ExceptionInInitializerError` is resolved.
- **Network Connectivity:** The app successfully reached the Supabase backend. Although a `PostgrestRestException` (permission denied) was logged, this confirms that the client is functional and the network request was made successfully.

> [!TIP]
> To resolve the `permission denied for table test` error, ensure that Row Level Security (RLS) is configured correctly in your Supabase dashboard or grant access to the `anon` role:
> `GRANT SELECT ON public.test TO anon;`
