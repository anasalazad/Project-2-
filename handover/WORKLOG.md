# The Feline Co. — Development Worklog

**Project:** The Feline Co. (cats-only adoption & pet-supply app)
**Unit:** Mobile Application Development (Project 2)
**Author:** _[your name]_  **Student ID:** _[id]_
**Repository:** _[GitHub link]_
**Device used for testing:** Samsung Galaxy Tab (Android 14) · Development machine: MacBook Air M1, 8 GB

> This worklog records how the app was planned, built and tested, stage by stage. Each stage lists the
> time spent, the key design decisions, the problems I hit and how I solved them, the testing done and
> screenshots as evidence. Each stage ends with a Gen AI reflection. A full reflection is at the end.

---

## Contents
1. [Planning & Research](#1-planning--research)
2. [Development Log](#2-development-log) (one entry per stage)
3. [Key Design Decisions (summary)](#3-key-design-decisions-summary)
4. [Issues Log](#4-issues-log)
5. [Testing Summary](#5-testing-summary)
6. [Reflection](#6-reflection)
7. [References](#7-references)
8. [Disclaimer on the Gen AI sections](#8-disclaimer-on-the-gen-ai-sections)
9. [Appendix: Screenshot Index](#9-appendix-screenshot-index)

---

## 1. Planning & Research

### 1.1 The brief and my interpretation
The brief asks for a **Resource Booking & Management App** that demonstrates eleven Android topics:
Jetpack Compose, Navigation Compose, multiple activities, Parcelable data transfer, Activity Result APIs,
ViewModel and state management, search/filtering/sorting, form validation and error handling, dynamic UI
updates, reusable composables, and styling. We were also given a required navigation flow:

```
MainActivity ──Navigation Compose──▶ Resource / Basket screen
                                          │ Intent + Parcelable
                                          ▼
                                   BookingActivity
                                          │ Activity Result API
                                          ▼
                                   MainActivity
```

My "resource" is **a cat available for adoption**, and the "booking" is **a meet & greet appointment**.
To use the *Basket* half of the diagram properly, the app also has a **shop**: the basket is passed to a
second secondary activity, `CheckoutActivity`, using the same Intent + Parcelable + Activity Result pattern.

**The Feline Co.** is a premium, cats-only adoption service:

| For customers | For admins (same app, admin login) |
|---|---|
| Browse, search, filter and sort cats | Dashboard with listing and booking stats |
| View a full profile (story, personality, health, compatibility) | Add, edit and delete cat listings |
| Book a meet & greet (BookingActivity) | Confirm, decline and complete adoptions |
| Credits economy: kittens cost credits, cats 2+ are free, adoptions earn credits | Manage shop products and stock |
| Spend credits in the shop; basket and checkout (CheckoutActivity) | |
| Account with wallet history, orders and a dark/light theme | |

**Target device:** a Samsung tablet in landscape. The layout must also work on a phone, which keeps it
honest about responsive design.

### 1.2 Inspiration and competitor research
I looked at how real adoption services present animals, mainly **The Lost Dogs' Home** (Melbourne) and
the RSPCA adoption pages. Patterns worth borrowing:

- **Listing grid with a strong photo**, name, breed, age and sex. Users decide visually first.
- **Filters by age, sex and size/coat**, plus "good with kids / cats / dogs", which are the questions adopters actually ask.
- **A health checklist** (vaccinated, microchipped, desexed) on every profile builds trust.
- **Senior animals are hard to rehome**. Shelters run reduced-fee campaigns for older animals.

That last point shaped my **credit economy**: kittens cost credits, while **cats aged two and over are free**,
and **every completed adoption earns 250 credits** for the shop. Adopting an older cat therefore leaves the
user better off, which nudges behaviour the way a real shelter would want.

What I deliberately did *differently*: the reference sites are document-like websites. Mine is a
**tablet-first native app**, with a navigation rail, two-pane layouts and a persistent basket.

### 1.3 Requirements → feature mapping

| Brief topic | Where it's demonstrated |
|---|---|
| Jetpack Compose | Every screen; no XML layouts |
| Navigation Compose | Type-safe `@Serializable` routes in `FelineNavHost`; arguments (`catId`, `freeOnly`) read through `SavedStateHandle` |
| Multiple Activities | `MainActivity`, `BookingActivity`, `CheckoutActivity` |
| Parcelable data transfer | `@Parcelize` `Cat` → BookingActivity; `ArrayList<CartItem>` → CheckoutActivity; `Booking` / `Order` returned |
| Activity Result APIs | Custom `ActivityResultContract`s `BookMeetAndGreet` and `CheckoutContract` with `rememberLauncherForActivityResult` |
| ViewModel & State Management | One ViewModel per screen; immutable `UiState` in a `StateFlow`; sealed `Event` interfaces (unidirectional data flow) |
| Search, Filtering & Sorting | Adopt (text + 6 filter groups + 5 sorts), Shop (text, category, stock, price), admin lists |
| Form Validation & Error Handling | Login/Register, booking form, checkout form, admin cat/product forms; pure validators; inline errors, banners, snackbars; repository-level checks |
| Dynamic UI Updates | Room `Flow`s keep every screen live: basket badge, credit balance, availability, animated grid items, live warnings |
| Reusable Composables | `ui/components`: CatCard, ProductCard, FeeBadge, StatusChips, ValidatedTextField, SearchField, SortMenu, DateStrip, QuantityStepper, EmptyState, dialogs, logo |
| Styling | Custom Material 3 colour scheme (dark + light), typography, shapes, Canvas-drawn logo, paw-print motif |

### 1.4 Marking rubric → how I plan to meet it

| Criterion (weight) | What "Excellent" asks for | My plan |
|---|---|---|
| **UI/UX (10)** | Research-informed design; excellent mobile experience | Competitor research (§1.2), Material 3 large-screen guidance, Nielsen's heuristics (§1.6), accessibility checks (contrast, TalkBack, 48 dp targets), adaptive rail/bar and two-pane layouts, clear empty/error states |
| **Functionality (40)** | All required functionality, thoroughly tested and debugged; extra functionality well integrated | Every brief topic mapped (§1.3); unit tests (rules, validators, ViewModels) + instrumented tests (Room flows, Compose UI) + a manual test script; extras: credit economy, admin side, favourites, theme toggle; issues and fixes logged (§4) |
| **Code Quality (20)** | Clean, documented, idiomatic Kotlin; Android-standard structure | Layered packages (`ui` / `domain` / `data`), MVVM + UDF, KDoc on public APIs, sealed interfaces, data classes, extension-function mappers, version catalog, no business logic in composables |

_[If the assignment PDF lists extra criteria, add them as rows here.]_

### 1.5 Research topics
These are the things I had to understand *before* building, and what I took from each.

| Topic | Why it matters here | Key takeaway | Source |
|---|---|---|---|
| App architecture (UI / domain / data layers) | Marked on code quality; keeps logic testable | ViewModels expose immutable state; repositories own data; business rules in plain Kotlin | Android "Guide to app architecture" |
| Unidirectional data flow & `StateFlow` | State management topic | State flows down, events flow up; `collectAsStateWithLifecycle` stops collecting in the background | Android "State and Jetpack Compose" |
| Type-safe Navigation Compose | Navigation topic | `@Serializable` route classes replace string routes; arguments are checked at compile time | Android "Type safety in Kotlin DSL and Navigation Compose" |
| Activity Result API | Required flow | A custom `ActivityResultContract` gives typed input/output and survives configuration changes | Android "Get a result from an activity" |
| Parcelable & `@Parcelize` | Required data transfer | `kotlin-parcelize` generates the boilerplate; use `IntentCompat.getParcelableExtra` on newer APIs | Android "Parcelable implementation generator" |
| Room + transactions | Persistence and credit integrity | DAOs return `Flow` for live UI; `withTransaction` keeps multi-step credit updates atomic | Android "Save data in a local database using Room" |
| DataStore | Session and settings | Asynchronous, Flow-based replacement for SharedPreferences | Android "DataStore" |
| Adaptive layouts / window size classes | Tablet target | Navigation rail at ≥600 dp, bottom bar below; two-pane content at ≥840 dp | Android "Adaptive layouts" / Material 3 |
| Accessibility | UI/UX marks | WCAG AA contrast (4.5:1 text), 48 dp touch targets, content descriptions, live regions for errors | Android "Accessibility in Compose"; WCAG 2.1 |
| Usability heuristics | UI/UX justification | Visibility of system status, error prevention, recognition over recall | Nielsen Norman Group, "10 Usability Heuristics" |
| Testing in Android | Functionality marks | JVM unit tests for logic and ViewModels (with fakes), instrumented tests for Room and Compose UI | Android "Test apps on Android" |
| Image licensing | Ethical use of photos | Use Unsplash/Pexels images under their free licences and credit sources | Unsplash / Pexels licence pages |

### 1.6 UX research and early design decisions
- **Tablet-first, landscape:** a navigation rail (not a bottom bar) on wide screens; filters in a permanent side panel at ≥840 dp; profile and forms in two panes.
- **Dark-first premium brand:** onyx/charcoal backgrounds, crimson accent (`#E11D3F` passes 4.5:1 on onyx and with white text), silver text, a serif display face for headlines. A light theme is available as a setting.
- **Personality without clutter:** a Canvas-drawn crown-cat logo and a faint paw-print pattern used sparingly (splash, empty states, headers).
- **Heuristics applied:**
  - *Visibility of system status*: credit balance chip, basket badge, status chips (Available / Pending / Adopted).
  - *Error prevention*: Mondays and booked slots are disabled, quantity steppers stop at stock levels, postcode only accepts digits.
  - *Help users recover*: inline field errors plus a summary banner, plus snackbars with outcomes ("150 credits refunded").
  - *Recognition rather than recall*: quick filter chips, demo login buttons, one-tap date pills.
- **Feedback on destructive or credit-changing actions:** confirmation dialogs for cancel, decline and complete.

### 1.7 Architecture plan

```
ui/            Compose screens + ViewModels (one per screen), reusable components, theme, navigation
 └─ observes StateFlow<UiState>, sends Events
domain/        Models (Parcelable), CreditRules, CatQuery/ProductQuery, Validators   ← pure Kotlin, unit-tested
data/          Room entities/DAOs, repositories (interfaces + Offline* implementations), DataStore stores
di/            AppContainer (manual DI) + AppViewModelProvider (ViewModel factory)
```

**Activities:** `MainActivity` (all Compose screens via `NavHost`) → `BookingActivity` (meet & greet) and
`CheckoutActivity` (shop purchase), both launched with custom Activity Result contracts.

**Why manual DI instead of Hilt:** fewer Gradle plugins and annotation processors, which means faster
builds on an 8 GB laptop, and it's easier to explain in this report. The trade-off is a little boilerplate
in `AppContainer`.

### 1.8 Data model and credit economy

| Table | Purpose |
|---|---|
| `users` | Accounts (customer/admin), salted password hash, credit balance |
| `cats` | Listings with status Available / Pending / Adopted |
| `bookings` | Meet & greet requests (stores a snapshot of the cat's name and photo for history) |
| `products`, `cart_items`, `orders`, `order_items` | Shop, persistent basket, order history |
| `credit_transactions` | Ledger behind the wallet history |
| `favourites` *(extra, stage 18)* | Per-user hearted cats |

**Credit rules:** welcome bonus +200 · kitten (<6 months) 150 · young (6–23 months) 100 · 2 years+ free ·
completed adoption +250 · express delivery +10.
**Lifecycle:** the fee is *held* when a booking is made, *refunded* if it is cancelled or declined, and
the reward is paid when an admin completes the adoption. Holding the fee at booking time stops a user
spending the same credits twice (once on a booking, once in the shop).

### 1.9 Development plan and time log
Built in 19 stages. Each stage ends with a working, runnable app that is tested on the tablet before
committing. Stage entries in §2 record the actual time.

| Stage | Focus | Planned (h) | Actual (h) | Status |
|---|---|---|---|---|
| 0 | Planning, research, repo setup | 6 | | |
| 1 | Project setup (Gradle, app module, launcher icon) | 2 | | |
| 2 | Brand and design system (colours, type, logo, splash) | 3 | | |
| 3 | Domain model and business rules (+ unit tests) | 3 | | |
| 4 | Reusable UI components (+ previews) | 3 | | |
| 5 | Local database (Room) and seed data | 3 | | |
| 6 | Repositories, dependency container (+ instrumented tests) | 4 | | |
| 7 | Sign in and registration (+ UI test) | 4 | | |
| 8 | Adaptive app shell and navigation | 3 | | |
| 9 | Home | 3 | | |
| 10 | Adopt: search, filter and sort (+ ViewModel tests) | 4 | | |
| 11 | Cat profile | 2 | | |
| 12 | BookingActivity and Activity Result (+ tests) | 5 | | |
| 13 | My bookings (cancel and refund) | 2 | | |
| 14 | Shop | 3 | | |
| 15 | Basket and CheckoutActivity (+ tests) | 5 | | |
| 16 | Account, wallet and theme setting | 2 | | |
| 17 | Admin: dashboard, listings, appointments, products | 6 | | |
| 18 | Extra feature: favourites | 3 | | |
| 19 | Documentation, final testing, submission | 3 | | |
| | **Total** | **69** | | |

### 1.10 Testing strategy
- **Unit tests (JVM):** credit rules, age groups, search/filter/sort, every validator, and the Adopt, Booking and Checkout ViewModels using in-memory fake repositories.
- **Instrumented tests (device):** Room flows with an in-memory database (fee held, refunded, rewarded; no double booking; checkout charges credits and reduces stock; favourites cascade), plus a Compose UI test of the login form.
- **Manual testing:** after every stage on the tablet, plus a final scripted run (`TESTING.md`) on tablet and phone, including rotation and TalkBack checks.

### 1.11 Risks and mitigations

| Risk | Mitigation |
|---|---|
| Gradle/Kotlin/KSP version mismatches break the build | Version catalog with pinned, compatible versions; upgrade only deliberately |
| 8 GB RAM makes builds slow | Gradle heap capped at 2 GB; build from one place at a time; stop daemons when idle |
| Missing photos break the build | Photos are looked up by name with a branded placeholder fallback |
| Credits get out of sync with history | Every balance change goes through one helper inside a database transaction |
| Scope creep | Core brief features first (stages 1–16); admin and favourites are later stages |

### Gen AI Reflection: Planning & Research
| # | File / artefact | Example prompt a student could ask | Type of assistance |
|---|---|---|---|
| 1 | `Specifications and requirements.md` | "Here is my assignment brief. Turn the eleven required topics into a checklist and suggest one concrete feature in a cat-adoption app that demonstrates each." | Requirements analysis |
| 2 | Rubric table (this worklog §1.4) | "For each rubric criterion, what evidence would a marker expect to see in my repo and report? Be specific to an Android Compose app." | Assessment literacy |
| 3 | Credit economy (§1.8) | "Critique my credit system: kittens 150, young cats 100, 2+ free, +250 per adoption. Can a user exploit it? What edge cases need rules?" | Design critique |
| 4 | Architecture plan (§1.7) | "Explain the trade-offs between manual dependency injection and Hilt for a single-module student app, with build-time and learning-curve in mind." | Decision support |
| 5 | Research table (§1.5) | "Which official Android documentation pages should I read before using Room transactions and type-safe Navigation Compose? Summarise the key rules in five bullets each." | Research guidance |

**How I would verify AI output:** cross-check every API claim against developer.android.com, and every
design claim against Material 3 / NN/g sources; keep only what I can justify in my own words.
**What AI should *not* do here:** pick my topic or write my justification. Those choices must be mine.

---

## 2. Development Log
_Entries are added after each stage is built, tested on the tablet, and committed._

<!--
TEMPLATE FOR EACH STAGE (copy, fill in, keep it concise):

### Stage N: Title
**Dates:** … · **Time:** planned X h / actual Y h · **Commits:** `NN.1` – `NN.k`

**Goal.** One or two sentences.

**What I built.**
- Bullet per meaningful piece (class/file and what it does).

**Key design decisions.**
- Decision → why → alternative considered.

**Issues encountered.** See Issues Log #x, #y. (One line each here; detail in §4.)

**Testing.**
- Automated: which tests, how many passed.
- Manual (tablet): what was checked and the result.

**Evidence.**
> 📸 **S0x**: what the screenshot shows.

#### Gen AI Reflection: Stage N
| # | File | Example prompt a student could ask | Type of assistance |
|---|---|---|---|
| 1 | `path/File.kt` | "…" | Explanation / Review / Debugging / Test design / … |

**How I would verify it:** …  ·  **What to watch out for:** …
-->

---

## 3. Key Design Decisions (summary)
_Maintained as stages complete. One line per decision; detail lives in the stage entries._

| # | Stage | Decision | Reason | Alternative considered |
|---|---|---|---|---|
| D1 | 0 | Cats as the "resource", meet & greet as the "booking" | Fits the brief's flow and my interest | Generic room booking |
| D2 | 0 | Two secondary activities (Booking, Checkout) | Uses both halves of the required flow ("Resource / Basket") | One activity only |
| D3 | 0 | Fee held at booking, refunded on cancel | Prevents double-spending credits | Charge on completion |
| D4 | 0 | Manual DI over Hilt | Simpler build on 8 GB machine, easier to explain | Hilt |

---

## 4. Issues Log
_Every non-trivial problem, with the options I considered and what I chose._

| # | Stage | Issue | Cause | Solutions considered | Solution used |
|---|---|---|---|---|---|

---

## 5. Testing Summary
_Filled in during stage 19._

| Suite | Count | Result |
|---|---|---|
| Unit tests (JVM) | | |
| Instrumented tests (device) | | |
| Manual script (`TESTING.md`) | | |

---

## 6. Reflection
_Written at the end (stage 19)._

### What worked well
### What could be improved
### What I would do differently next time
### Gen AI Reflection: Overall
_How AI assistance fitted into the workflow overall, where it saved time, where it was wrong, and how I
verified it. See the disclaimer in §8._

---

## 7. References
- Android Developers. *Guide to app architecture.* https://developer.android.com/topic/architecture
- Android Developers. *State and Jetpack Compose.* https://developer.android.com/develop/ui/compose/state
- Android Developers. *Navigation with Compose.* https://developer.android.com/develop/ui/compose/navigation
- Android Developers. *Type safety in Kotlin DSL and Navigation Compose.* https://developer.android.com/guide/navigation/design/type-safety
- Android Developers. *Get a result from an activity.* https://developer.android.com/training/basics/intents/result
- Android Developers. *Parcelable implementation generator.* https://developer.android.com/kotlin/parcelize
- Android Developers. *Save data in a local database using Room.* https://developer.android.com/training/data-storage/room
- Android Developers. *DataStore.* https://developer.android.com/topic/libraries/architecture/datastore
- Android Developers. *Accessibility in Compose.* https://developer.android.com/develop/ui/compose/accessibility
- Android Developers. *Test apps on Android.* https://developer.android.com/training/testing
- Material Design 3. https://m3.material.io/
- Nielsen Norman Group. *10 Usability Heuristics for User Interface Design.* https://www.nngroup.com/articles/ten-usability-heuristics/
- W3C. *Web Content Accessibility Guidelines (WCAG) 2.1.* https://www.w3.org/TR/WCAG21/
- The Lost Dogs' Home. https://dogshome.com/
- _[Image sources: list each Unsplash/Pexels photo used, with photographer and link.]_

---

## 8. Disclaimer on the Gen AI sections
The **"Gen AI Reflection"** tables in this worklog are **illustrative examples written for teaching
purposes**. They show the kind of specific, file-focused questions a student *could* ask an AI assistant
while building this exact project, and the type of help each one would give (explanation, review,
debugging, test design, and so on). **They are not a transcript of the prompts actually used** to produce
this exemplar. This exemplar was prepared under tight time constraints alongside other teaching
commitments, so the real interaction was not recorded in this form. Use the examples as models of good
practice: give the AI context (the file and the goal), ask it to explain rather than just produce code,
and always verify the answer against official documentation and by running tests.

---

## 9. Appendix: Screenshot Index
_Screenshots live in the local `screenshots/` folder, named by ID (e.g. `S01.png`)._

| ID | Stage | Shows |
|---|---|---|
