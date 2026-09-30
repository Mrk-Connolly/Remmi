---
name: remmi-performance
description: Performance and reliability guidance for Remmi covering startup speed, Home responsiveness, main-thread discipline, recomposition efficiency, allocation control, database/system query optimization, coroutine lifecycle, memory leaks, and failure isolation. Activate when optimizing or analyzing performance.
---

# Remmi Performance & Reliability Skill

## Performance Requirements

### 1. Main-Thread & Rendering
* Main-thread work must complete in under 16ms per frame to avoid jank.
* Strictly offload I/O, database queries, and heavy computations to `Dispatchers.IO` or `Dispatchers.Default`.
* Avoid heavy object allocation during recomposition or inside tight loops.

### 2. Startup Optimization
* Minimize `Application.onCreate()` and `MainActivity.onCreate()` blocking work.
* Initialize core launcher capabilities first; defer non-critical plugin initialization lazily.

### 3. Database & System Queries
* Batch Room queries and state updates where possible.
* Avoid repeated query calls inside UI rendering cycles or loops.
* Unsubscribe from listeners, receivers, and Flow collectors when components are disposed or destroyed.

### 4. Recomposition Efficiency
* Mark stable parameters with `@Stable` or `@Immutable`.
* Use derived state (`derivedStateOf`) when observing state changes that occur more frequently than UI updates.

### 5. Stack & Profiling Tools
Use Remmi's existing stack for performance management:
* Kotlin Coroutines & Flow
* Room persistence
* R8 minification and keep rules (`app/proguard-rules.pro`)
* Baseline Profiles & Macrobenchmark
* Perfetto / Android System Tracing

Measure before optimizing. Do not introduce third-party performance frameworks.
