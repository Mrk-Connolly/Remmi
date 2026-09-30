---
name: remmi-architecture
description: Enforces Remmi's architecture contract, layer ownership (core/ui/plugins), Android capability boundaries, database rules, EventBus usage, StateFlow rules, Controller boundaries, plugin isolation, main-thread rule, startup behavior, and failure isolation. Activate when making structural changes, modifying core/ui/plugins, or adding system capabilities.
---

# Remmi Architecture Skill

## Core Directive
Follow the procedure:
**INSPECT → IDENTIFY OWNER → REUSE → IMPLEMENT SMALLEST CHANGE → VERIFY**

## Primary Area Responsibilities

### 1. Core (`core/`)
Provides fundamental infrastructure for Remmi:
* `core/automation/`: Automation scheduling and background rules engine.
* `core/database/`: Room database, entities, DAOs, converters, migrations, and database services.
* `core/android/`: Android system bridges (launcher, notifications, alarms, speech, location, services).
* `core/eventBus/`: Single application EventBus for cross-system commands and events.
* `core/host/`: System host interfaces.
* `core/controller/`: System coordination (`RemmiController`, `RemmiContainer`).
* `core/models/`: Shared models and interfaces required across system boundaries.

*Core must never depend on plugins or UI implementations.*

### 2. UI (`ui/`)
Contains presentation components (`screens/`, `popups/`, `components/`, `home/`).
* Home communicates with owning systems via StateFlow and EventBus.
* UI must NOT directly implement Android infrastructure (e.g. PackageManager, NotificationListenerService).
* UI must NOT directly access Room, DAOs, or database instances.

### 3. Plugins (`plugins/`)
Remmi's domain feature modules (e.g. `alarm`, `calendar`, `contacts`, `ingredients`, `tasks`).
* Plugins own: domain models, actions, repositories, UI, and business rules.
* Plugins MUST NOT access Room directly or use DAOs. They communicate persistence via `core/database/` services or repositories.
* Plugins MUST NOT access internal implementations of other plugins.

## EventBus & State
* Use `EventBus` for asynchronous cross-system commands and events between independent components.
* Use `StateFlow` / `Flow` for observable state owned by a specific component.
* Do not convert EventBus into a state-management engine.

## Controller Boundaries
* `RemmiController` coordinates core startup and lifecycle.
* `RemmiController` MUST NOT contain plugin business logic, raw SQL/Room queries, UI logic, or raw Android capability implementations.

## Main Thread & Startup
* Main thread is strictly for UI rendering and short framework callbacks.
* Never perform I/O, database queries, or heavy computation on `Dispatchers.Main`.
* Prioritize Home rendering on startup. Defer non-critical plugin/service initialization lazily.

## Failure Isolation
* Subsystem failures must degrade functionality gracefully without crashing the Home launcher.

## STOP Conditions
Stop and request clarification before editing if:
* The proposed change requires introducing new frameworks (Hilt, Dagger, Koin, new state frameworks).
* The change breaks ownership boundaries between `core/`, `ui/`, and `plugins/`.
* Existing working implementation would be rewritten rather than extended.
