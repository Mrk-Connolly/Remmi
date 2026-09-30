# REMMI AI DEVELOPMENT SYSTEM — CORE DIRECTIVE

## System Identity & Context
Remmi is a long-running Android application evolving into a launcher/home experience with integrated plugins and Android system capabilities.
The primary structure consists of three distinct areas:
- `core/`: Fundamental infrastructure (automation, database, android capability integrations, eventBus, host, controller, models).
- `ui/`: Presentation layer (`screens/`, `popups/`, `components/`, `home/`).
- `plugins/`: Feature applications owning their domain models, repositories, actions, and UI.

## Core Rules

1. **Smallest Correct Change Principle**
   - Implement the smallest correct change that preserves existing architecture, performance, reliability, and working behavior.
   - A feature request never authorizes an architectural redesign or speculative abstraction.

2. **Inspect Before Changing**
   - Always search the codebase and inspect existing implementations, tests, and documentation before editing or creating code.

3. **Reuse Before Creating**
   - Reuse existing utilities, models, components, services, and test fixtures before introducing new ones.

4. **Ownership & Architectural Boundaries**
   - Plugins MUST NOT access Room database instances, DAOs, or entities directly.
   - Plugins MUST NOT access internal implementations of other plugins.
   - `ui/` MUST NOT directly implement Android system infrastructure.
   - `core/` MUST NOT depend on plugin implementations or `ui/`.

5. **Main Thread Discipline**
   - Keep the main thread free from database operations, file I/O, network calls, or heavy computation. Use Kotlin coroutines and appropriate dispatchers (`Dispatchers.IO`, `Dispatchers.Default`).

6. **Failure Isolation**
   - Subsystem failures (e.g. plugin or service errors) MUST NOT crash the Home launcher experience. Gracefully degrade functionality.

7. **Prohibited Technologies & Frameworks**
   - DO NOT introduce: Hilt, Dagger, Koin, new EventBus, new state-management framework, new database layer, generic "manager" layers, new Gradle modules, or duplicate infrastructure.

8. **STOP Conditions**
   - Stop and re-assess if:
     - Application architecture would need to change to support a task.
     - A requested path or requirement is ambiguous.
     - Existing working systems would be rewritten rather than extended.

9. **Verification Requirements**
   - Never claim tests or builds passed without running them.
   - Run `./gradlew :app:testDebugUnitTest` to verify unit tests and architectural constraints.
