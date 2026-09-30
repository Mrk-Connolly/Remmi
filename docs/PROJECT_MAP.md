# Remmi Project Map

## Architectural Structure Overview

Remmi is organized into three distinct primary layers: `core/`, `ui/`, and `plugins/`.

```
com.remmi.app/
├── core/                  # Core application infrastructure
│   ├── android/           # Android platform capability services (launcher, notifications, alarms, speech, location)
│   ├── automation/        # Automation engine and background schedulers
│   ├── controller/        # System lifecycle coordination (RemmiController, RemmiContainer)
│   ├── database/          # Room database configuration, DAOs, entities, and database services
│   ├── eventBus/          # Central EventBus, commands, events, listeners
│   ├── host/              # System host contracts
│   ├── memory/            # Memory providers and migration services
│   ├── models/            # Shared cross-boundary models
│   └── plugin/            # Base plugin interfaces, registry, and manager
├── ui/                    # Presentation layer
│   ├── components/        # Reusable UI widgets and design system elements
│   ├── home/              # Home launcher UI screens, app drawer, and assistant UI
│   ├── popups/            # Global dialogs and popups
│   └── screens/           # Main application screens
└── plugins/               # Domain feature applications
    ├── alarm/             # Alarm plugin
    ├── books/             # Books plugin
    ├── calendar/          # Calendar plugin
    ├── callrecorder/      # Call recording plugin
    ├── contacts/          # Contacts plugin
    ├── cv/                # Computer vision plugin
    ├── dashboard/         # Widget dashboard plugin
    ├── gift/              # Gift management plugin
    ├── health/            # Health tracking plugin
    ├── ingredients/       # Ingredients & pantry management plugin
    ├── maps/              # Maps & location plugin
    ├── notes/             # Notes plugin
    ├── recipes/           # Recipes plugin
    ├── tasks/             # Tasks management plugin
    ├── transcriptions/    # Transcriptions plugin
    └── weather/           # Weather plugin
```

## Layer Ownership Rules

### Core (`core/`)
* **Responsibility**: Provides core primitives (EventBus, Database infrastructure, Android capability services, Automation engine).
* **Restrictions**: Must NOT contain plugin business logic, plugin UI, or depend on plugin implementations or UI composables.

### UI (`ui/`)
* **Responsibility**: Provides Remmi launcher presentation, Home screen layout, navigation, and reusable UI components.
* **Restrictions**: Must NOT directly access Room/DAOs or implement raw Android capability infrastructure.

### Plugins (`plugins/`)
* **Responsibility**: Owns feature-specific business logic, domain models, repositories, actions, and plugin UI.
* **Restrictions**: Must NOT directly access Room database instances or DAOs. Must NOT depend on other plugins' internal implementations.
