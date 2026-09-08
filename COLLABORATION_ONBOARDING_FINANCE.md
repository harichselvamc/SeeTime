# SeeTime Onboarding Flows & Offline Finance Features Research

## Objective
To research recent popular apps' onboarding flows (user attractiveness, engagement) and explore finance-related offline features (expense trackers, due reminders, budgeting) that would be suitable for the SeeTime app, given its lack of a backend. This document serves as a collaborative workspace for Jim, Dwight, and Kelly.

## Roles & Focus Areas:
- **Dwight (Primary Scribe, UI/UX Lead):** Focus on the UI/UX aspects of onboarding and how to best integrate proposed finance features visually and interactively.
- **Jim (Technical Feasibility):** Assess the technical feasibility of proposed onboarding strategies and offline finance features, considering SeeTime's existing architecture and offline-first principles.
- **Kelly (Market Research):** Research existing popular apps' onboarding flows and explore the market relevance and user demand for various offline finance-related features.

## Research on Onboarding Flows:

### Key Learnings from Popular Apps (General Best Practices):
*   **Show, Don't Tell:** Users learn by doing. Interactive tutorials or quick walkthroughs are more effective than long text explanations.
*   **Highlight Value Proposition Early:** Clearly communicate the core benefits of SeeTime upfront to encourage engagement.
*   **Progressive Disclosure:** Introduce complex features gradually, allowing users to get comfortable with basic functionality first.
*   **Personalization (Initial Setup):** Allow users to customize initial settings (e.g., primary timezone, preferred alarm sounds) to make the app feel tailored from the start.
*   **Clear Call to Action:** Guide users clearly on what to do next at each step of the onboarding process.
*   **Skippable Onboarding:** Provide an option to skip the onboarding process for experienced users or those eager to dive in.
*   **Feedback & Confirmation:** Provide visual or haptic feedback for user actions and confirm successful setup.

### Proposed Onboarding Strategies for SeeTime:
1.  **Welcome & Value Proposition Tour:** A short, visually appealing swipe-through introduction (2-3 screens) highlighting SeeTime's unique selling points: time zone overlap, camera overlay, smart alarms, and its offline/privacy focus.
2.  **Interactive Setup for First Time Pair:** Guide the user through setting up their first local and remote time pair, including a brief explanation of the camera overlay feature when applicable.
3.  **Permissions Granting:** Clearly explain *why* certain permissions (e.g., camera for overlay, notifications for alarms) are needed before prompting for them.
4.  **Optional Advanced Feature Introduction:** After basic setup, offer a brief, optional tutorial or hints for more advanced features like smart alarms or customizing the home screen.

### UI/UX Considerations for Onboarding:
*   **Minimalist & Clean Design:** Leverage Material 3 guidelines to ensure a clean, intuitive, and visually appealing onboarding experience.
*   **Illustrations/Animations:** Use subtle animations or clear illustrations to demonstrate features rather than just describing them.
*   **Progress Indicators:** A visual indicator (e.g., dots at the bottom of a swipe-through) to show progress through the onboarding flow.
*   **Consistent Branding:** Maintain SeeTime's visual identity throughout the onboarding process.

## Offline Finance Features Exploration:

Given SeeTime's offline and no-backend constraints, finance features must rely on local data storage and user input. The focus is on personal utility and privacy rather than complex financial integrations.

### Identified Finance Features & Justification:
1.  **Basic Expense Tracker (Local Data):**
    *   **Description:** Allows users to manually input expenses with categories (e.g., Food, Transport, Utilities), amount, and date. All data is stored locally on the device.
    *   **Justification:** Provides a fundamental tool for personal financial awareness without needing bank integration or cloud syncing. It helps users understand where their money goes, aligning with a self-management approach.
    *   **User Benefits:** Increased financial transparency, better control over spending habits, and enhanced privacy as no financial data leaves the device.

2.  **Simple Due Date Reminders (Local & Event-Driven):**
    *   **Description:** Users can add recurring or one-time due dates for bills or payments, with customizable local notifications. These would be independent of external calendars or financial services.
    *   **Justification:** Essential for managing personal finances effectively by preventing late payments and associated fees. It's a direct application of SeeTime's reminder capabilities.
    *   **User Benefits:** Reduced stress from forgotten bills, improved financial discipline, and proactive management of financial obligations.

3.  **Basic Budgeting Tool (Category-Based, Local):**
    *   **Description:** Users can set monthly budgets for different expense categories. The app would track spending against these budgets based on the manually entered expenses, providing visual progress (e.g., a progress bar for each category).
    *   **Justification:** Empowers users to gain control over their spending and work towards financial goals, leveraging the data from the local expense tracker. This aligns with the self-improvement aspect of time management.
    *   **User Benefits:** Helps in identifying overspending, encourages saving, and provides a clear, private overview of financial health.

### UI/UX Integration Proposals for Finance Features:
*   **Dedicated "Finance" Section:** A new primary navigation tab or a prominent section within a main menu for these features.
*   **Clear Data Input Forms:** Simple, intuitive forms for adding expenses and setting budgets/reminders.
*   **Visual Progress Bars/Charts:** Easy-to-understand visual representations of spending against budgets and upcoming due dates.
*   **Search & Filter:** Ability to search and filter expenses by category, date, or amount for quick review.

### Technical Feasibility & Data Storage Considerations:
*   **SQLite/Local Storage:** All data must be stored locally using efficient and secure on-device databases (e.g., SQLite).
*   **No Backend Dependencies:** Features must be entirely self-contained within the app, with no reliance on external APIs, cloud services, or real-time synchronization.
*   **Export/Backup Options:** Provide clear options for users to export their financial data (e.g., CSV, encrypted file) to their local device for personal backup and control.

### Implementation Strategy for Onboarding & Finance:
- **Onboarding:** Jetpack Compose `HorizontalPager` with custom animated page indicators, highlighting SeeTime's core pillars:
  1. Multi-Timezone Glance & Overlap Visualizer.
  2. Solar Azimuth & Camera Overlay.
  3. Privacy-First Local Activity Tracking.
- **Finance Hub (`FinanceScreen.kt`):**
  - Offline Room tables: `ExpenseEntity`, `BudgetCategoryEntity`, `BillReminderEntity`.
  - Monthly budget gauge widgets with Material 3 dynamic color indicators (Green -> Amber -> Red).
  - Exact `AlarmManager` reminders for payment due dates without cloud sync.

---

**Contributors:**
- Dwight (Primary Scribe, UI/UX Lead)
- Jim (Technical Feasibility)
- Kelly (Market Research & Product UX)
- Hari (Engineering Lead)
- Toby (QA & Specifications)
