# Remmi Technical Debt Registry

This document tracks known technical debt and planned refactorings without breaking the Architecture Contract.

---

### TD-001
* **Description**: Ensure all feature plugins exclusively use domain repositories rather than raw database service helpers.
* **Reason**: Historical incremental plugin development.
* **Impact**: Low. Potential leaks of query structure if plugins invoke low-level database services.
* **Removal Condition**: Audit all plugin repositories to confirm clean abstraction over persistence.

---

### TD-002
* **Description**: Complete migration of legacy Android service callbacks to structured Kotlin Coroutines dispatchers.
* **Reason**: Older Android platform API integration callbacks.
* **Impact**: Low. Minor risk of main-thread delay if platform callbacks process large payloads.
* **Removal Condition**: Verify all platform callback handlers dispatch to `Dispatchers.IO` or `Dispatchers.Default`.

---

### TD-003
* **Description**: Consolidate custom spacing and surface elevation tokens across plugin composables into `MaterialTheme` tokens.
* **Reason**: UI components created prior to Material 3 Expressive theme standardization.
* **Impact**: Visual consistency.
* **Removal Condition**: Migrate hardcoded DP values to unified design system tokens.
