# Remmi AI UI Design Skill

## Purpose

This is Remmi's UI/UX decision guide.

Use it when designing, reviewing, or implementing UI in the existing `ui/` layer.

It does **not** define application architecture.

---

## 1. Design Philosophy

Remmi should be:

* clear
* modern
* calm
* information-efficient
* expressive
* accessible
* responsive
* easy to understand
* fast to operate

Design for the user's task, not for the component.

**Choose the UX first; choose the Material component second.**

---

## 2. Material 3

Use Jetpack Compose + Material 3.

Prefer:

```kotlin
MaterialTheme.colorScheme
MaterialTheme.typography
MaterialTheme.shapes
```

Use Material 3 Expressive when it improves hierarchy, emphasis, interaction, or personality.

Do not add expressive effects merely for decoration.

---

## 3. Layout

* Establish clear information hierarchy.
* Group related content.
* Use consistent spacing.
* Avoid unnecessary containers.
* Keep primary actions easy to reach.
* Respect system insets.
* Avoid excessive whitespace or excessive density.
* Adapt layouts to available window size.

---

## 4. Typography

Use the Material typography scale consistently.

Typography should communicate:

```text
Page → Section → Content → Metadata
```

Avoid excessive font sizes, weights, and all-caps text.

Ensure text remains usable with larger system font sizes.

---

## 5. Color

Use `MaterialTheme.colorScheme`.

Color should communicate:

* hierarchy
* selection
* emphasis
* state
* semantic meaning

Support dynamic color where appropriate.

Do not rely on color alone to communicate meaning.

---

## 6. Shapes & Surfaces

Use Material 3 shapes and tonal surfaces consistently.

Use cards/containers only when they communicate grouping or interaction.

Avoid:

* unnecessary shadows
* arbitrary corner radii
* excessive borders
* decorative containers
* visual clutter

---

## 7. Components

Reuse existing Remmi components first.

Prefer Material 3 components when they fit the task.

Create custom components only when they provide a meaningful UX benefit.

Do not mechanically use a Material component just because one exists.

---

## 8. Navigation

Navigation should be predictable and reflect the user's mental model.

Future Remmi direction:

```text
Assistant
    ↓
Plugin
    ↓
Plugin Content
```

Assistant selection:

* horizontally scrollable
* obvious selected state
* accessible without relying only on color

Plugin navigation:

* clearly identifies the active plugin
* supports horizontal exploration
* may support swipe navigation
* must not overwhelm plugin content

Do not implement this navigation unless explicitly requested.

---

## 9. Motion

Use motion to communicate:

* cause and effect
* state changes
* hierarchy
* continuity
* navigation

Motion should be fast and purposeful.

Avoid animation that delays, distracts, or makes routine interactions cumbersome.

---

## 10. Accessibility

Consider accessibility from the beginning.

Check:

* TalkBack semantics
* content descriptions
* semantic roles
* touch targets
* text scaling
* contrast
* non-color indicators
* focus behavior
* screen-reader order
* reduced-motion considerations

Custom components must provide appropriate semantics.

---

## 11. Responsive Design

Design mobile-first, then adapt.

Consider:

* compact phones
* large phones
* tablets
* foldables
* landscape
* multi-window

Adapt layouts based on available space rather than device names.

Larger screens should use additional space intelligently rather than simply enlarging everything.

---

## 12. UI States

Every data-driven screen should consider:

### Empty

Explain what is empty and what the user can do.

### Loading

Show appropriate progress without unnecessarily blocking the whole screen.

### Error

Explain the problem clearly and provide recovery where possible.

### Success

Provide confirmation when it improves user confidence.

---

## 13. Interaction

Prefer direct manipulation and immediate feedback.

Examples:

```text
Complete task → immediately show completion
Add item      → immediately show item
Select plugin → immediately show selection/content
Save          → provide appropriate confirmation
```

For destructive actions, use confirmation or undo when appropriate.

Minimize unnecessary input.

---

## 14. Information Hierarchy

Every screen should answer:

1. What is this?
2. What matters now?
3. What can I do?
4. What happens next?

Use:

* size
* typography
* spacing
* position
* color
* shape
* motion

Do not emphasize everything.

---

## 15. Consistency

Maintain consistent:

* spacing
* typography
* colors
* shapes
* components
* selection states
* terminology
* navigation
* loading/error behavior
* interaction patterns

Reuse established Remmi patterns before creating new ones.

---

## 16. Information Density

Remmi is a productivity application.

Prefer useful information density without sacrificing readability.

Show the most important information first.

Use progressive disclosure for secondary information.

Avoid ornamental UI that competes with useful content.

---

## 17. UX Decision Process

For meaningful UI changes:

1. Identify the user's primary task.
2. Define the information hierarchy.
3. Select the appropriate interaction pattern.
4. Apply Material 3 / Expressive principles.
5. Check accessibility.
6. Check responsive behavior.
7. Check empty/loading/error states.
8. Reuse existing components.
9. Make the smallest coherent UI change.
10. Verify that no application architecture was changed.

---

## 18. Architecture Boundary

This skill does NOT authorize changes to:

* `core/`
* database infrastructure
* EventBus infrastructure
* plugin business logic
* repositories
* services
* managers
* dependency injection
* navigation architecture
* state-management architecture
* Gradle modules

**A UI requirement is not a reason to introduce new architecture.**

If the existing architecture prevents a clean implementation, identify the constraint instead of silently redesigning the architecture.
