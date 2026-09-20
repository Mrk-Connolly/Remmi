Before continuing with the Remmi UI redesign, check whether your available AI/agent skills include a **UX/UI Designer**, **UI Designer**, **UX Designer**, or equivalent design skill.

### If a UX/UI design skill already exists

Inspect it and determine whether it is appropriate for designing a modern Android application using Jetpack Compose and Material 3 / Material 3 Expressive.

If it is outdated or does not adequately cover modern Material 3 / Material 3 Expressive design principles, update the skill so that it provides guidance for:

* Material 3 / Material 3 Expressive
* Modern Android UI/UX
* Jetpack Compose
* Responsive/adaptive layouts
* Accessibility
* Typography hierarchy
* Color systems
* Material 3 color schemes
* Dynamic color
* Shapes and component hierarchy
* Spacing and layout consistency
* Motion and transitions
* Navigation patterns
* Touch targets
* Empty/loading/error states
* Information hierarchy
* User flows
* Interaction feedback
* Mobile-first UX

The skill should help the AI make actual UI/UX design decisions rather than simply applying Material 3 components mechanically.

### If no UX/UI design skill exists

Do NOT invent an architectural framework or modify the Remmi application architecture.

Instead, create a **project-level UX/UI design skill or design-guidelines document** that can be used by the AI specifically when working on Remmi's UI.

The skill should define how Remmi's UI should be designed using current Material 3 / Material 3 Expressive principles.

It should cover:

1. Design philosophy
2. Layout and spacing
3. Typography
4. Color
5. Shapes
6. Components
7. Navigation
8. Motion
9. Accessibility
10. Responsive/adaptive behavior
11. Empty/loading/error states
12. Interaction patterns
13. Visual hierarchy
14. Consistency rules
15. UX decision-making principles

### Important

This is a **design skill**, not a new application architecture.

Do NOT introduce:

* Hilt
* Dagger
* Koin
* new managers
* new services
* new EventBuses
* new repositories
* new state-management architecture
* new navigation architecture
* new Gradle modules

Do not modify `core/`, database infrastructure, EventBus infrastructure, plugin business logic, or other application architecture simply to create the design skill.

The design skill should guide work in the existing `ui/` layer.

### Remmi design direction

The eventual Remmi UI will use:

**Assistant → Plugin → Plugin content**

The assistant interface will include:

* a horizontally scrollable assistant selector
* a selected assistant
* plugin tabs belonging to that assistant
* swipeable plugin content
* Material 3 / Material 3 Expressive components
* modern Android interaction patterns

Do not implement this navigation yet unless explicitly requested.

For now, establish or update the UX/UI design skill so it can guide the upcoming redesign.

### Final requirement

After completing this task, report:

1. Whether a UX/UI skill already existed.
2. Where it is located.
3. Whether you updated it or created it.
4. What design principles it now follows.
5. Confirm that no Remmi application architecture was changed.
