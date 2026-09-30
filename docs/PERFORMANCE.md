# Remmi Performance Guidelines & Critical Journeys

## Critical User Journeys & Target Metrics

1. **App Startup**:
   - Time to first frame (TTFF) < 500ms.
   - Perceived Home usability must occur before non-critical plugin initialization.

2. **Home Rendering & Interaction**:
   - Frame rendering < 16ms (target 60fps / 120fps smooth scrolling).
   - Zero main-thread blocking calls during gestures or launcher navigation.

3. **App Launch & Return to Home**:
   - Instant response on app launch tap.
   - Smooth transition back to Home launcher screen.

4. **App Search & Drawer**:
   - Real-time search query filtering over installed apps < 50ms response time.

5. **Plugin Switching**:
   - Fast state restoration without full re-instantiation of background services.

## Performance Requirements & Optimization Rules

* **Main Thread**: Strictly reserve `Dispatchers.Main` for Compose UI rendering. All Room, I/O, and CPU-intensive parsing must run on `Dispatchers.IO` or `Dispatchers.Default`.
* **Recomposition**: Ensure composables use `@Stable` / `@Immutable` data parameters to avoid unnecessary recomposition passes.
* **Allocations**: Avoid allocating object instances inside composable bodies or tight loops.
* **Database Queries**: Batch database queries and index frequently queried Room columns.

## Measurement Tools
* Baseline Profiles (`app/src/androidTest/`)
* Macrobenchmark & Microbenchmark tests
* Perfetto system tracing
