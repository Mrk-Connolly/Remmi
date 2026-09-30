---
name: remmi-security
description: Security and privacy rules for Remmi covering least privilege, Android permissions, exported components, intent validation, secret protection, logging discipline, persistence security, and capability boundaries. Activate when handling permissions, intents, credentials, secrets, or sensitive user data.
---

# Remmi Security & Privacy Skill

## Security Principles

### 1. Principle of Least Privilege
* Request only mandatory Android permissions required for user-facing features.
* Check runtime permissions dynamically before calling Android system capability APIs.

### 2. Exported Components & Intent Handling
* Set `android:exported="false"` on Activities, Services, and Receivers unless explicitly required for external system interaction.
* Validate external Intent extras and deep links before processing.

### 3. Secrets & Sensitive Data
* Never hardcode API keys, passwords, or tokens in source code or resource XMLs.
* Store configurable credentials securely (e.g. via `local.properties` or encrypted storage).
* Do not output sensitive user data (contacts, messages, location, tokens) in debug logs.

### 4. Persistence Security
* Ensure Room databases and cached files are stored in private app storage (`Context.filesDir` / `Context.databasePath`).
* Do not store sensitive unencrypted data on external public storage.

### 5. Capability Boundaries
* Secure IPC and system bridge communication inside `core/android/`.
* Plugins must access system capabilities strictly through defined core interfaces.
