---
name: remmi-ui
description: Provides guidelines for implementing and reviewing UI in Remmi using Jetpack Compose, Material 3/Expressive, adaptive layouts, accessibility, semantics, touch targets, typography, spacing, motion, state ownership, and recomposition awareness. Activate when creating or modifying UI components, composables, screens, or Home layout.
---

# Remmi UI Skill

## Design & Implementation Principles

### 1. Jetpack Compose & Material 3
* Use `MaterialTheme.colorScheme`, `MaterialTheme.typography`, and `MaterialTheme.shapes`.
* Use Material 3 Expressive elements when they enhance emphasis, hierarchy, or interaction.
* Reuse existing Remmi components in `ui/components/` and plugin UI before creating custom ones.

### 2. Layout & Adaptive Design
* Support responsive layouts across window size classes (compact, medium, expanded).
* Respect system insets (`WindowInsets`, status bar, navigation bar, keyboard IME).
* Enforce minimum touch target sizes (48dp x 48dp) for all interactive elements.

### 3. State Ownership & Recomposition Awareness
* Composables must be stateless where possible; hoist state to state holders or ViewModels.
* Use `remember` and key parameters appropriately to avoid unnecessary recomposition.
* Use Immutable/Stable data structures for composable parameters.

### 4. Accessibility & Semantics
* Provide explicit `contentDescription` for non-decorative icons and images.
* Support dynamic font scaling without layout clipping.
* Ensure clear visual focus and proper semantic grouping for screen readers.

### 5. Home Launcher Responsiveness
* Home presentation (`ui/home/`) must remain smooth, fast, and responsive.
* Avoid heavy computations or side effects inside composable functions.

## Prohibited UI Practices
* DO NOT access Room, DAOs, or database instances directly from composables or ViewModels.
* DO NOT implement raw Android system infrastructure (e.g. `NotificationManager`, `PackageManager`) inside UI composables.
* DO NOT embed business logic inside composables.
* DO NOT hardcode colors or dimensions; use theme tokens and spacing constants.
