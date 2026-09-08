# SeeTime: High-Impact Feature Proposals & Retention Strategy

**Collaborative Sprint Document**  
**Lead Authors:** Kelly (Market Research & UI/UX), Hari (Engineering Lead), Toby (QA & Specs)  
**Target Repository:** `D:\SeeTime` (Android Jetpack Compose, Room, Offline-First)

---

## 1. Executive Summary

To elevate SeeTime from a transactional time utility into an essential, beloved daily habit, we have formulated four high-impact, offline-first feature modules. These modules leverage SeeTime's core strengths—zero-cloud privacy, solar/camera overlay precision, and multi-timezone intelligence—while driving industry-leading user retention through gamification and circadian wellness.

---

## 2. Feature Proposal Catalog

### Module 1: Smart Circadian Rhythm & Sleep Calculator Across Timezones
* **Problem Statement:** Cross-border remote workers and frequent travelers struggle with sleep deprivation, irregular melatonin cycles, and caffeine timing when collaborating across multiple time zones.
* **Feature Overview:**
  - An intelligent on-device circadian calculator that computes recommended sleep windows, power nap durations (90-min ultradian sleep cycle alignment), and caffeine cutoff alerts based on the user's active timezone pairs.
  - **Jetlag Pre-adaptation Engine:** Allows users to input an upcoming flight/travel schedule (e.g., San Francisco to London, 8-hour shift) and produces a 4-day progressive sleep shift plan (30–60 min daily bedtime adjustment with recommended sunlight exposure windows).
* **UI/UX Blueprint:**
  - Jetpack Compose circular 24-hour dial displaying daylight hours, solar zenith/nadir, recommended sleep block, and caffeine curfew indicator.
  - Integration with camera solar overlay to verify ambient light exposure.
* **Offline Architecture:**
  - 100% mathematical formula on-device using astronomical sun algorithms (`SolarCalculator.kt`) and local `AlarmManager` for gentle wind-down notifications.

---

### Module 2: Offline Multi-Currency Travel & Expense Companion
* **Problem Statement:** Travelers and remote contractors juggling multiple timezones also deal with multiple currencies. Most currency converters fail without internet roaming.
* **Feature Overview:**
  - Built-in offline currency converter pre-loaded with an offline baseline snapshot of 160+ world currencies.
  - Reverse lookup, quick tip calculator, and one-tap integration with SeeTime's local expense tracker (`COLLABORATION_ONBOARDING_FINANCE.md`).
  - Optional periodic offline snapshot refresh when network is available, with clear timestamp of data currency (e.g., *"Rates cached Sep 1, 2026"*).
* **UI/UX Blueprint:**
  - M3 Dual numeric keypads for split-second bi-directional conversion.
  - Contextual currency matching based on selected timezone pairs (e.g., adding `Tokyo (JST)` auto-suggests `JPY` as a quick currency pair).

---

### Module 3: Visual Flight & Cross-Timezone Transition Planner
* **Problem Statement:** Planning meetings, layovers, and travel across international date lines is confusing and error-prone.
* **Feature Overview:**
  - Interactive multi-timezone timeline scrubber. Users drag a time scrubber across 24 hours to see live simultaneous local times across origin, layover, destination, and colleague locations.
  - Golden Hour / Mutual Wake Overlap visualizer highlighting green meeting windows where all participants are between 08:00 and 19:00 local time.
* **UI/UX Blueprint:**
  - Horizontal swipeable canvas bar chart displaying day/night color shading (golden yellow for daylight, deep navy for night, gradient twilight for dawn/dusk).
  - Tap any time slot to schedule a local reminder or export an ICS calendar event directly.

---

### Module 4: High-Retention Duolingo-Style Glance Widgets & Habit Hooks
* **Feature Overview:**
  - **Android Home Screen Widgets:**
    1. **Flame Streak & Next Overlap (2x2):** Glance widget showing active streak count (🔥 15 Days), daily ring status, and countdown to next remote colleague work hours.
    2. **Quick Activity Logger (1x1):** Floating action button shortcut directly on home screen for 1-tap logging.
  - **Gamification Rewards:**
    - Unlockable custom app icons (Vintage Chronometer, Cyberpunk Neon, Tokyo Twilight).
    - Tiered badges and celebration particle bursts upon meeting weekly consistency targets (fully detailed in `GAMIFICATION_RETENTION_SPEC.md`).

---

## 3. Technical Feasibility & Architecture Matrix (Hari & Toby Review)

| Feature Module | Android Dependencies | Storage & Performance Impact | Battery / Background Overhead |
| :--- | :--- | :--- | :--- |
| **Circadian Sleep Engine** | Compose Canvas, `java.time.*` | Negligible (<50KB Room entity) | Zero background poll (Uses exact AlarmManager) |
| **Offline Currency Converter** | Room DB, JSON snapshot table | ~300KB embedded rate asset | Zero |
| **Timezone Timeline Scrubber** | Jetpack Compose Gestures & Canvas | Pure UI calculation | Zero |
| **Glance Widgets & Gamification** | `androidx.glance:glance-appwidget`, Room | <100KB Room table for Streaks & Badges | Lightweight periodic update (~30 min) |

---

## 4. Next Implementation Roadmap

1. **Sprint 1:** Finalize Task 6, 8, 9 core UI and verification in main build.
2. **Sprint 2:** Implement Streak & Badge core architecture (`UserGamificationEntity`, `StreakManager`).
3. **Sprint 3:** Build Circadian & Travel Scrubber composables in new `PlannerScreen.kt`.
4. **Sprint 4:** Implement Android Jetpack Glance widgets and local notification engine.

---

**Contributors:**
- Kelly (Market Research & UI/UX Specialist)
- Hari (Engineering Lead)
- Toby (QA & Specifications Lead)
