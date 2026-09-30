# Remmi Architectural Decisions (ADR)

## ADR-001: Core / UI / Plugins Ownership Boundaries
* **Status**: Accepted
* **Context**: Remmi requires clear boundaries to remain maintainable and resilient as features grow.
* **Decision**: Enforce a strict three-tier architecture: `core/` (infrastructure), `ui/` (presentation), and `plugins/` (feature modules).
* **Consequences**: Features are isolated inside plugins; core infrastructure is shared via generic interfaces and EventBus.

## ADR-002: Room Database Ownership in Core
* **Status**: Accepted
* **Context**: Direct database access across features leads to schema coupling and migration failures.
* **Decision**: Database configuration, Room entities, DAOs, and migrations belong exclusively to `core/database/`. Plugins interact with persistence via domain repositories and core database services.
* **Consequences**: Prevents schema leaks into feature plugins and ensures single-point migration safety.

## ADR-003: Asynchronous Cross-System Communication via EventBus
* **Status**: Accepted
* **Context**: Independent plugins and core systems need to communicate without direct compile-time coupling.
* **Decision**: Use a single core `EventBus` for cross-system commands and events.
* **Consequences**: Decouples plugins from each other and from core components. Local synchronous calls should use direct interfaces instead of EventBus.

## ADR-004: Home Launcher Separation
* **Status**: Accepted
* **Context**: Remmi serves as an Android home launcher experience.
* **Decision**: Separate Android platform launcher integration (`core/android/launcher/`) from Home UI presentation (`ui/home/`).
* **Consequences**: Ensures platform home integration can be tested independently of UI rendering.

## ADR-005: Strict Prohibition of Unnecessary Frameworks
* **Status**: Accepted
* **Context**: Adding heavy dependency injection or state frameworks increases build complexity and memory overhead.
* **Decision**: Prohibit Hilt, Dagger, Koin, or secondary state management frameworks. Use manual container injection (`RemmiContainer`, `RemmiController`) and native Kotlin `StateFlow`.
* **Consequences**: Keeps the application lightweight, fast, and simple to debug.
