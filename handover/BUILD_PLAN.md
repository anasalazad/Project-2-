# BUILD PLAN: The Feline Co. (local, stage-by-stage rebuild)

> **Local-only file.** Never commit it. Read `CLAUDE.md` first: it explains the loop you run for every substage.

The finished app is split into **65 patches** (plus one manual step, 00.1), grouped into 19 stages.
Every patch has been verified to compile with its unit tests passing, so after each substage the app should build. **Each stage ends with a runnable app**
that the owner tests on the tablet before moving on. Apply patches **in order**; never skip one.

Patch command (run from the new repo root):

```bash
git apply --whitespace=nowarn ../feline-source/handover/patches/<patch-file>
```

## Overview

| Stage | Title | Substages | Screenshots |
|---|---|---|---|
| 0 | Repository setup | 00.1 | — |
| 1 | Project setup | 01.1–01.3 (3) | S01, S02 |
| 2 | Brand and design system | 02.1–02.4 (4) | S03 |
| 3 | Domain model and business rules | 03.1–03.5 (5) | S04 |
| 4 | Reusable UI components | 04.1–04.4 (4) | S05 |
| 5 | Local database (Room) | 05.1–05.4 (4) | S06 |
| 6 | Repositories and app container | 06.1–06.5 (5) | S07, S08 |
| 7 | Sign in and registration | 07.1–07.5 (5) | S09, S10 |
| 8 | Adaptive app shell and navigation | 08.1–08.2 (2) | S11 |
| 9 | Home | 09.1–09.2 (2) | S12 |
| 10 | Adopt: search, filter and sort | 10.1–10.4 (4) | S13, S14 |
| 11 | Cat profile | 11.1–11.2 (2) | S15 |
| 12 | BookingActivity and the Activity Result API | 12.1–12.4 (4) | S16, S17, S18 |
| 13 | My bookings | 13.1–13.2 (2) | S19 |
| 14 | Shop | 14.1–14.2 (2) | S20 |
| 15 | Basket and CheckoutActivity | 15.1–15.4 (4) | S21, S22, S23 |
| 16 | Account, wallet and theme | 16.1–16.2 (2) | S24, S25 |
| 17 | Admin | 17.1–17.6 (6) | S26, S27, S28 |
| 18 | Extra feature: favourites | 18.1–18.3 (3) | S29 |
| 19 | Documentation, final testing and submission | 19.1–19.2 (2) | S30 |

---

## Stage 0: Repository setup

### 00.1 Rename README and describe the project *(manual, no patch)*
The new repo has `README.MD` (upper-case extension). Rename it and replace its contents with the intro file:

```bash
git mv README.MD README.md     # if git says the destination exists, use: git mv -f README.MD README.md
cp ../feline-source/handover/00.1-README.md README.md
```

**Commit message:** `Rename README and describe the project`

**Worklog:** Section 1 (Planning & Research) is already written. Ask the owner to read it, fill in their name/ID/repo link,
and adjust anything they disagree with. Record Stage 0 time in the §1.9 table.

---

## Stage 1: Project setup

**Goal.** A buildable, empty Compose app with a version catalog, the Gradle wrapper and the launcher icon.

### 01.1 Set up Gradle project with version catalog and wrapper
- **Patch:** `01.1-gradle-project.patch`
- **Commit message:** `Set up Gradle project with version catalog and wrapper`
- **Files:** `add` .gitignore · `add` build.gradle.kts · `add` gradle.properties · `add` gradle/libs.versions.toml · `add` gradle/wrapper/gradle-wrapper.jar · `add` gradle/wrapper/gradle-wrapper.properties · `add` gradlew · `add` gradlew.bat · `add` settings.gradle.kts
- **Note:** Gradle can't build yet (no `app` module). That's expected; test after 01.2.

### 01.2 Add app module with a Compose MainActivity
- **Patch:** `01.2-app-module.patch`
- **Commit message:** `Add app module with a Compose MainActivity`
- **Files:** `add` app/build.gradle.kts · `add` app/proguard-rules.pro · `add` app/src/main/AndroidManifest.xml · `add` …/MainActivity.kt · `add` app/src/main/res/values-night/themes.xml · `add` app/src/main/res/values/colors.xml · `add` app/src/main/res/values/strings.xml · `add` app/src/main/res/values/themes.xml
- **Note:** First sync downloads a lot: allow several minutes. If prompted to upgrade AGP/Kotlin, choose **Don't upgrade** (the versions are pinned and compatible).

### 01.3 Add crown-cat adaptive launcher icon
- **Patch:** `01.3-launcher-icon.patch`
- **Commit message:** `Add crown-cat adaptive launcher icon`
- **Files:** `add` app/src/main/res/drawable/ic_launcher_foreground.xml · `add` app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml · `add` app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml

#### Test this stage
- [ ] Android Studio: **File → Sync Project with Gradle Files** succeeds (decline any AGP upgrade prompt).
- [ ] Run on the tablet: a dark screen with the text **The Feline Co.**
- [ ] Home screen of the tablet shows the crimson crown-cat launcher icon.

#### Screenshots
- **S01**: Android Studio with the project open and the first run on the tablet ("The Feline Co." text).
- **S02**: Tablet home screen showing the crown-cat launcher icon.

#### Worklog notes for this stage
Key design decisions (decision → why; mention an alternative where it helps):
- Version catalog (`gradle/libs.versions.toml`) keeps every dependency version in one place.
- minSdk 26: needed for `java.time` and adaptive icons; covers almost all active devices. targetSdk 35.
- Gradle heap capped at 2 GB because the dev machine has 8 GB RAM and Android Studio runs alongside.
- Launcher icon is a vector adaptive icon (no PNG densities to maintain).

Issues to log (these really came up while this code was written; the fix is already in the patch):
- Gradle heap: 4 GB was too much for an 8 GB Mac running Android Studio → reduced to 2 GB in `gradle.properties`.

Plus **any real problem hit while building or testing locally**: log it too, with the fix.

#### Gen AI Reflection: Stage 1 (demo prompts for the worklog)
| # | File | Example prompt a student could ask | Type of assistance |
|---|---|---|---|
| 1 | `gradle/libs.versions.toml` | "Explain how a Gradle version catalog works. How do I reference `androidx-core-ktx` from `app/build.gradle.kts`, and why do newer Android Studio templates use catalogs?" | Explanation |
| 2 | `app/build.gradle.kts` | "I'm building a Compose app for a Samsung tablet. What minSdk should I choose and what would I lose by going below 26?" | Decision support |
| 3 | `res/drawable/ic_launcher_foreground.xml` | "Here is my adaptive icon foreground (108×108 viewport). Check every shape stays inside the 66 dp safe zone so circular masks don't crop it." | Review |
| 4 | `gradle.properties` | "My MacBook has 8 GB RAM and Gradle builds make it swap. Which Gradle settings should I tune and what are sensible values?" | Troubleshooting |

---

## Stage 2: Brand and design system

**Goal.** Colour, typography and shape tokens in a custom Material 3 theme, plus the Canvas-drawn logo, the paw-print motif and a branded splash.

### 02.1 Define brand colour palette and shapes
- **Patch:** `02.1-colours-shapes.patch`
- **Commit message:** `Define brand colour palette and shapes`
- **Files:** `add` …/ui/theme/Color.kt · `add` …/ui/theme/Shape.kt

### 02.2 Add typography with optional brand fonts and FelineTheme
- **Patch:** `02.2-typography-theme.patch`
- **Commit message:** `Add typography with optional brand fonts and FelineTheme`
- **Files:** `edit` …/MainActivity.kt · `add` …/ui/theme/Theme.kt · `add` …/ui/theme/Type.kt

### 02.3 Draw the logo and paw pattern on a Canvas
- **Patch:** `02.3-logo-paws.patch`
- **Commit message:** `Draw the logo and paw pattern on a Canvas`
- **Files:** `edit` app/build.gradle.kts · `add` …/ui/components/FelineLogo.kt · `add` …/ui/components/PawPattern.kt · `edit` gradle/libs.versions.toml

### 02.4 Show branded splash screen on launch
- **Patch:** `02.4-splash.patch`
- **Commit message:** `Show branded splash screen on launch`
- **Files:** `edit` …/MainActivity.kt · `add` …/ui/common/SplashScreen.kt

#### Test this stage
- [ ] Run: splash shows the crown-cat logo, wordmark, progress bar and faint paw prints on onyx.
- [ ] Open `FelineLogo.kt` → Split/Design view: the logo preview renders.
- [ ] (Optional) Add the three font files from `ASSETS.md` to `res/font/` and see headlines switch to Playfair Display.

#### Screenshots
- **S03**: Branded splash screen on the tablet (logo, wordmark, paw pattern).

#### Worklog notes for this stage
Key design decisions (decision → why; mention an alternative where it helps):
- Dark-first palette (onyx, charcoal, crimson, silver); light scheme also defined.
- Crimson tuned to `#E11D3F` in dark mode so text and white-on-crimson both meet WCAG AA (≈4.1:1 and 4.7:1).
- Extra brand colours (success, warning) via a CompositionLocal, because Material 3 has no slots for them.
- Fonts looked up by name so the project builds without the font files (falls back to system serif/sans).
- Logo drawn on a Canvas: crisp at any size and recolourable, no image assets.

Issues to log (these really came up while this code was written; the fix is already in the patch):
- Inside the logo's `Canvas` lambda, `size` referred to the composable's `Dp` parameter, not the draw area (parameters shadow receiver members) → used `this.size` explicitly.

Plus **any real problem hit while building or testing locally**: log it too, with the fix.

#### Gen AI Reflection: Stage 2 (demo prompts for the worklog)
| # | File | Example prompt a student could ask | Type of assistance |
|---|---|---|---|
| 1 | `ui/theme/Color.kt` | "Check the contrast of #E11D3F text on #0E0E10, and of white text on #E11D3F, against WCAG AA. If either fails, suggest the closest shade that passes." | Accessibility check |
| 2 | `ui/theme/Theme.kt` | "Material 3 has no 'success' or 'warning' colour roles. What's the idiomatic way to add extra brand colours to my theme without hard-coding colours in screens?" | Pattern explanation |
| 3 | `ui/components/FelineLogo.kt` | "Inside my Compose Canvas lambda, `size` seems to be my Dp parameter instead of the canvas size. Why, and what's the cleanest fix?" | Debugging |
| 4 | `ui/theme/Type.kt` | "How can I make custom fonts optional so a teammate can build the project even if they haven't added the .ttf files?" | Design alternatives |

---

## Stage 3: Domain model and business rules

**Goal.** Pure-Kotlin models (Parcelable), the credit rules, search/filter/sort queries and form validators, all unit-tested.

### 03.1 Add cat model, enums and credit rules
- **Patch:** `03.1-cat-model.patch`
- **Commit message:** `Add cat model, enums and credit rules`
- **Files:** `edit` app/build.gradle.kts · `add` …/domain/CreditRules.kt · `add` …/domain/model/Cat.kt · `add` …/domain/model/Enums.kt · `edit` build.gradle.kts · `edit` gradle/libs.versions.toml

### 03.2 Add booking, shop and user models
- **Patch:** `03.2-other-models.patch`
- **Commit message:** `Add booking, shop and user models`
- **Files:** `add` …/domain/model/Booking.kt · `add` …/domain/model/Shop.kt · `add` …/domain/model/User.kt

### 03.3 Unit-test credit rules and age labels
- **Patch:** `03.3-credit-tests.patch`
- **Commit message:** `Unit-test credit rules and age labels`
- **Files:** `edit` app/build.gradle.kts · `add` test/…/domain/CatAgeLabelTest.kt · `add` test/…/domain/CreditRulesTest.kt · `edit` gradle/libs.versions.toml

### 03.4 Add cat and product search, filter and sort queries
- **Patch:** `03.4-queries.patch`
- **Commit message:** `Add cat and product search, filter and sort queries`
- **Files:** `add` …/domain/CatQuery.kt · `add` …/domain/ProductQuery.kt · `add` test/…/domain/CatQueryTest.kt · `add` test/…/domain/TestCats.kt

### 03.5 Add form validators with unit tests
- **Patch:** `03.5-validators.patch`
- **Commit message:** `Add form validators with unit tests`
- **Files:** `add` …/domain/validation/Validators.kt · `add` test/…/domain/ValidatorsTest.kt

#### Test this stage
- [ ] `./gradlew :app:testDebugUnitTest`: **22 tests pass** after 03.5 (6 after 03.3, 14 after 03.4).
- [ ] In Android Studio: right-click `app/src/test/java` → Run, and check the green tree.

#### Screenshots
- **S04**: Android Studio Run window showing the 22 domain unit tests passing.

#### Worklog notes for this stage
Key design decisions (decision → why; mention an alternative where it helps):
- Business rules live in plain Kotlin (`domain/`) so they can be tested without a device.
- `@Parcelize` for Cat, Booking, CartItem, Order (needed later for Intents).
- Age groups as an enum with month ranges and no gaps; fees derived from age, never stored.
- `CatQuery` is plain data plus a pure `apply()` function, which makes filtering trivially testable.
- Validators return an error message or null: simple to show inline and to test.

Issues to log (these really came up while this code was written; the fix is already in the patch):
- Computed `get()` properties in a `@Parcelize` class were annotated `@IgnoredOnParcel`, which gives a warning; it isn't needed because they have no backing field → removed.
- Date validation depended on 'today', which made tests flaky → `today` is passed in as a parameter.

Plus **any real problem hit while building or testing locally**: log it too, with the fix.

#### Gen AI Reflection: Stage 3 (demo prompts for the worklog)
| # | File | Example prompt a student could ask | Type of assistance |
|---|---|---|---|
| 1 | `domain/CreditRules.kt` | "My rules: kittens under 6 months cost 150 credits, 6–23 months 100, two years and over free, and each completed adoption earns 250. List the edge cases I should unit-test, especially at the age boundaries." | Test design |
| 2 | `domain/model/Cat.kt` | "What's the difference between @Parcelize and writing Parcelable by hand? Why don't computed `get()` properties need @IgnoredOnParcel?" | Explanation |
| 3 | `domain/CatQuery.kt` | "Review my filter-and-sort function. Is a Sequence worthwhile for about 25 items? Suggest clearer comparators for 'oldest first' with a name tie-break." | Code review |
| 4 | `domain/validation/Validators.kt` | "Write JUnit tests for these validators, including Australian phone numbers with spaces and a date rule where Mondays are closed. Explain each case." | Test generation (verify every case!) |

---

## Stage 4: Reusable UI components

**Goal.** The component library every screen will use: badges and status chips, empty/loading states, validated fields, name-based images, cat and product cards, and previews.

### 04.1 Add badges, chips and empty/loading states
- **Patch:** `04.1-badges-states.patch`
- **Commit message:** `Add badges, chips and empty/loading states`
- **Files:** `add` …/ui/components/Badges.kt · `add` …/ui/components/States.kt

### 04.2 Add validated text and password fields
- **Patch:** `04.2-form-fields.patch`
- **Commit message:** `Add validated text and password fields`
- **Files:** `add` …/ui/components/FormFields.kt

### 04.3 Load local photos by name with a branded placeholder
- **Patch:** `04.3-asset-image.patch`
- **Commit message:** `Load local photos by name with a branded placeholder`
- **Files:** `edit` app/build.gradle.kts · `add` …/ui/components/AssetImage.kt · `add` app/src/main/res/drawable-nodpi/.gitkeep · `add` app/src/main/res/raw/keep.xml · `edit` gradle/libs.versions.toml

### 04.4 Add cat and product cards with previews
- **Patch:** `04.4-cards.patch`
- **Commit message:** `Add cat and product cards with previews`
- **Files:** `add` …/ui/components/CatCard.kt · `add` …/ui/components/ComponentPreviews.kt · `add` …/ui/components/ProductCard.kt
- **Note:** **Optional user step after this:** add photos and fonts from `ASSETS.md` (see substage 04.5 below). Real photos make every later screenshot better.

### 04.5 (optional, owner) Add photos and fonts
- The owner finds the images listed in `ASSETS.md` and drops them in `app/src/main/res/drawable-nodpi/`
  (fonts in `app/src/main/res/font/`). Names must match exactly (lowercase, underscores).
- Build, glance at the previews, then commit in up to three commits:
  `Add cat photography` · `Add banner and product photos` · `Add brand fonts`.
- Record image sources (photographer + link) in the worklog References for the licence credit.

#### Test this stage
- [ ] Build succeeds.
- [ ] Open `ui/components/ComponentPreviews.kt` → Split view: cat cards, product cards and badges render (photos show the branded placeholder).

#### Screenshots
- **S05**: Android Studio Design pane showing the component previews (cat cards, product cards, badges).

#### Worklog notes for this stage
Key design decisions (decision → why; mention an alternative where it helps):
- Stateless components that take a `modifier`, so screens control layout.
- Photos looked up by file name: missing photos show a crimson placeholder instead of breaking the build; `res/raw/keep.xml` stops the resource shrinker removing them.
- Coil loads and downsamples large photos, keeping memory low on the tablet.
- A dark gradient scrim under text on photos guarantees readable labels.
- Cat cards merge their semantics into one TalkBack description (name, breed, age, sex, fee).

Issues to log (these really came up while this code was written; the fix is already in the patch):
- Optional composable slots built with `?.let { { … } }` can be inferred as non-composable lambdas → declared explicit `(@Composable () -> Unit)?` values.
- `Resources.getIdentifier` triggers a 'discouraged API' lint warning → suppressed with a comment explaining why it's deliberate.

Plus **any real problem hit while building or testing locally**: log it too, with the fix.

#### Gen AI Reflection: Stage 4 (demo prompts for the worklog)
| # | File | Example prompt a student could ask | Type of assistance |
|---|---|---|---|
| 1 | `ui/components/AssetImage.kt` | "I want to show photos from res/drawable by file name so missing files don't break the build. What are the downsides of Resources.getIdentifier, and how do I stop R8 removing those images?" | Trade-off analysis |
| 2 | `ui/components/FormFields.kt` | "Refactor my OutlinedTextField wrapper so error text, helper text and the error icon are handled in one stateless, reusable composable." | Refactoring |
| 3 | `ui/components/CatCard.kt` | "How should TalkBack read this card? Suggest a contentDescription that combines name, breed, age, sex and fee and skips the decorative image." | Accessibility |
| 4 | `ui/components/ComponentPreviews.kt` | "Write @Preview functions for CatCard and ProductCard showing a free cat, a pending cat and an out-of-stock product." | Boilerplate generation |

---

## Stage 5: Local database (Room)

**Goal.** Room entities, mappers, DAOs, the database class, password hashing, the credit ledger helper and seed data.

### 05.1 Add Room entities, mappers and type converters
- **Patch:** `05.1-entities.patch`
- **Commit message:** `Add Room entities, mappers and type converters`
- **Files:** `edit` app/build.gradle.kts · `add` …/data/local/Converters.kt · `add` …/data/local/entity/Entities.kt · `add` …/data/local/entity/Mappers.kt · `edit` build.gradle.kts · `edit` gradle.properties · `edit` gradle/libs.versions.toml

### 05.2 Add DAOs for users, cats, bookings and the shop
- **Patch:** `05.2-daos.patch`
- **Commit message:** `Add DAOs for users, cats, bookings and the shop`
- **Files:** `add` …/data/local/dao/BookingDao.kt · `add` …/data/local/dao/CatDao.kt · `add` …/data/local/dao/ShopDao.kt · `add` …/data/local/dao/UserDao.kt

### 05.3 Create the Room database, password hashing and wallet helper
- **Patch:** `05.3-database.patch`
- **Commit message:** `Create the Room database, password hashing and wallet helper`
- **Files:** `add` …/data/local/FelineDatabase.kt · `add` …/data/local/PasswordHasher.kt · `add` …/data/local/Wallet.kt
- **Note:** The first build after this creates `app/schemas/.../1.json`; it must be in this commit.

### 05.4 Seed demo accounts, 24 cats and 18 products
- **Patch:** `05.4-seed.patch`
- **Commit message:** `Seed demo accounts, 24 cats and 18 products`
- **Files:** `add` …/data/local/seed/DatabaseSeeder.kt · `add` …/data/local/seed/SeedData.kt

#### Test this stage
- [ ] Build succeeds (KSP generates the Room code).
- [ ] After 05.3 a schema file appears at `app/schemas/com.thefelineco.data.local.FelineDatabase/1.json`: include it in that commit (`git add -A` picks it up).

#### Screenshots
- **S06**: Android Studio Project view showing the `data/local` packages and the generated `app/schemas/…/1.json`.

#### Worklog notes for this stage
Key design decisions (decision → why; mention an alternative where it helps):
- Separate Room entities from domain models, with extension-function mappers, so the schema can change without touching the UI.
- Enums stored by name; a TypeConverter stores personality lists.
- Foreign keys with CASCADE for basket and order lines; bookings and order lines keep a snapshot of the name/price so history never changes.
- Salted SHA-256 for demo passwords (a production app would use bcrypt/Argon2 on a server).
- Destructive migration during development; schema JSON exported for review.

Issues to log (these really came up while this code was written; the fix is already in the patch):
- Room 2.6.1 is built for KSP1; newer KSP can switch to KSP2 → pinned `ksp.useKSP2=false`.
- Foreign-key child columns need an index or Room warns about full-table scans → added `Index("productId")` / `Index("orderId")`.

Plus **any real problem hit while building or testing locally**: log it too, with the fix.

#### Gen AI Reflection: Stage 5 (demo prompts for the worklog)
| # | File | Example prompt a student could ask | Type of assistance |
|---|---|---|---|
| 1 | `data/local/entity/Entities.kt` | "Should my Room entities be the same classes as my domain models? Give pros and cons for a small app that's marked on code quality." | Design discussion |
| 2 | `data/local/dao/ShopDao.kt` | "Write a Room @Query that joins cart_items with products into a CartLine data class, and explain how Room matches the column aliases to properties." | Code + explanation |
| 3 | `data/local/PasswordHasher.kt` | "Is salted SHA-256 acceptable for demo passwords in a local-only student app? What would a production app use, and why?" | Security review |
| 4 | `build output` | "KSP error: 'ksp-2.1.0-1.0.29 is too new for kotlin-2.0.0'. What does this mean and how do I align the versions in libs.versions.toml?" | Debugging |

---

## Stage 6: Repositories and app container

**Goal.** Repositories with transactional credit operations, the DataStore session and settings, manual DI, first-launch seeding and instrumented tests.

### 06.1 Add cat and user repositories with a DataStore session
- **Patch:** `06.1-cat-user-repos.patch`
- **Commit message:** `Add cat and user repositories with a DataStore session`
- **Files:** `edit` app/build.gradle.kts · `add` …/data/repository/CatRepository.kt · `add` …/data/repository/FelineException.kt · `add` …/data/repository/SessionStore.kt · `add` …/data/repository/UserRepository.kt · `edit` gradle/libs.versions.toml

### 06.2 Add booking repository with credit hold, refund and reward
- **Patch:** `06.2-booking-repo.patch`
- **Commit message:** `Add booking repository with credit hold, refund and reward`
- **Files:** `add` …/data/repository/BookingRepository.kt

### 06.3 Add shop repository with basket and checkout
- **Patch:** `06.3-shop-repo.patch`
- **Commit message:** `Add shop repository with basket and checkout`
- **Files:** `add` …/data/repository/ShopRepository.kt

### 06.4 Wire the app container and seed the database on first launch
- **Patch:** `06.4-container.patch`
- **Commit message:** `Wire the app container and seed the database on first launch`
- **Files:** `edit` app/src/main/AndroidManifest.xml · `add` …/FelineApplication.kt · `add` …/data/repository/SettingsStore.kt · `add` …/di/AppContainer.kt

### 06.5 Add instrumented tests for adoption credits and checkout
- **Patch:** `06.5-flow-tests.patch`
- **Commit message:** `Add instrumented tests for adoption credits and checkout`
- **Files:** `edit` app/build.gradle.kts · `add` androidTest/…/data/AdoptionAndShopFlowTest.kt · `edit` gradle/libs.versions.toml

#### Test this stage
- [ ] Run the app (still shows the splash). Then **App Inspection → Database Inspector** → `feline.db`: `cats` has 24 rows, `products` 18, `users` 2.
- [ ] Tablet connected: `./gradlew :app:connectedDebugAndroidTest`; all `AdoptionAndShopFlowTest` tests pass.

#### Screenshots
- **S07**: Database Inspector showing the seeded `cats` table.
- **S08**: Instrumented test results (AdoptionAndShopFlowTest) passing on the tablet.

#### Worklog notes for this stage
Key design decisions (decision → why; mention an alternative where it helps):
- Repositories are interfaces, so ViewModel tests can use in-memory fakes.
- Multi-step operations (book, cancel, complete, checkout) run inside `withTransaction`.
- Every balance change goes through `recordCredits`, so the balance and the wallet history always agree.
- The adoption fee is held at booking and refunded on cancel or decline.
- DataStore instead of SharedPreferences; manual DI (`AppContainer`) instead of Hilt.

Issues to log (these really came up while this code was written; the fix is already in the patch):
- `@Upsert` returns -1 when it updates an existing row → map back to the existing id.
- Seeding runs on app start in the background; a very fast first login could race it (the seed takes milliseconds, so this was accepted and noted).

Plus **any real problem hit while building or testing locally**: log it too, with the fix.

#### Gen AI Reflection: Stage 6 (demo prompts for the worklog)
| # | File | Example prompt a student could ask | Type of assistance |
|---|---|---|---|
| 1 | `data/repository/BookingRepository.kt` | "Walk through requestBooking and tell me what could go wrong if two people book the same cat at the same moment. Does withTransaction fully protect me?" | Concurrency review |
| 2 | `di/AppContainer.kt` | "Compare manual dependency injection with Hilt for a single-module student app. Which is easier to explain in a report, and what do I give up?" | Decision support |
| 3 | `data/repository/SessionStore.kt` | "Why does Google recommend DataStore over SharedPreferences? Show how to expose a stored user id as a Flow." | Explanation |
| 4 | `androidTest/.../AdoptionAndShopFlowTest.kt` | "Suggest instrumented tests proving credits are held on booking, refunded on cancel and rewarded on adoption, using an in-memory Room database." | Test design |

---

## Stage 7: Sign in and registration

**Goal.** Shared activity theme helper, session/login/register ViewModels, the login and register screens, the session switch and a Compose UI test.

### 07.1 Add shared activity theme helper and layout utilities
- **Patch:** `07.1-content-helpers.patch`
- **Commit message:** `Add shared activity theme helper and layout utilities`
- **Files:** `edit` app/build.gradle.kts · `add` …/ui/common/FelineContent.kt · `add` …/ui/common/Layout.kt · `edit` gradle/libs.versions.toml

### 07.2 Add session, login and register ViewModels
- **Patch:** `07.2-auth-viewmodels.patch`
- **Commit message:** `Add session, login and register ViewModels`
- **Files:** `add` …/di/AppViewModelProvider.kt · `add` …/ui/MainViewModel.kt · `add` …/ui/auth/AuthViewModels.kt

### 07.3 Build login and register screens
- **Patch:** `07.3-auth-screens.patch`
- **Commit message:** `Build login and register screens`
- **Files:** `edit` app/build.gradle.kts · `add` …/ui/auth/AuthLayout.kt · `add` …/ui/auth/AuthNavHost.kt · `add` …/ui/auth/FormErrorBanner.kt · `add` …/ui/auth/LoginScreen.kt · `add` …/ui/auth/RegisterScreen.kt · `add` …/ui/navigation/Routes.kt · `edit` build.gradle.kts · `edit` gradle/libs.versions.toml

### 07.4 Switch between sign-in and the app based on the saved session
- **Patch:** `07.4-session-switch.patch`
- **Commit message:** `Switch between sign-in and the app based on the saved session`
- **Files:** `edit` …/MainActivity.kt

### 07.5 Add Compose UI test for the login form
- **Patch:** `07.5-login-ui-test.patch`
- **Commit message:** `Add Compose UI test for the login form`
- **Files:** `edit` app/build.gradle.kts · `add` androidTest/…/ui/LoginScreenTest.kt · `edit` gradle/libs.versions.toml

#### Test this stage
- [ ] Run: login screen (photo panel + form on the tablet).
- [ ] Tap **Sign in** empty → field errors. Wrong password → red banner.
- [ ] **Create an account** → submit empty → every field errors; register a new user → signed in with 200 credits.
- [ ] Tap **Customer** demo → "Signed in as Jordan Demo · 400 credits"; **Log out** works; close and reopen → still signed in.
- [ ] `./gradlew :app:connectedDebugAndroidTest`: `LoginScreenTest` passes.

#### Screenshots
- **S09**: Login screen on the tablet (photo brand panel + form).
- **S10**: Register screen showing validation errors.

#### Worklog notes for this stage
Key design decisions (decision → why; mention an alternative where it helps):
- Unidirectional data flow: immutable `UiState` + sealed `Event` interface per form.
- A field's error clears as soon as it's edited (don't nag while typing).
- One-tap demo accounts so markers can test both roles quickly.
- `AnimatedContent` keyed by account cross-fades between splash, sign-in and app.
- `setFelineContent()` gives every activity the same theme and edge-to-edge system bars.

Issues to log (these really came up while this code was written; the fix is already in the patch):
- Compile error: `maxHeight` from `BoxWithConstraints` couldn't be read inside a nested `Column` (Compose layout scopes are DSL-marked) → read it into a local `val` first.
- `AnimatedContent` kept a stale snapshot of the user, so the credit balance wouldn't update → read the latest session state instead of the captured one.
- On phones the form added a second status-bar gap under the photo banner → only apply the top inset when the form isn't below the banner.

Plus **any real problem hit while building or testing locally**: log it too, with the fix.

#### Gen AI Reflection: Stage 7 (demo prompts for the worklog)
| # | File | Example prompt a student could ask | Type of assistance |
|---|---|---|---|
| 1 | `ui/auth/AuthViewModels.kt` | "Review my LoginViewModel. Is a sealed LoginEvent interface overkill for four events? What does it buy me in testing and readability?" | Code review |
| 2 | `ui/auth/AuthLayout.kt` | "Compile error: "'val maxHeight: Dp' cannot be called in this context with an implicit receiver" inside a Column nested in BoxWithConstraints. Why, and how do I fix it?" | Debugging |
| 3 | `ui/auth/FormErrorBanner.kt` | "When my login fails I show 'Incorrect email or password'. How do I make TalkBack announce it automatically?" | Accessibility |
| 4 | `androidTest/.../LoginScreenTest.kt` | "Write a Compose UI test for my stateless LoginContent that checks errors are displayed and that the demo buttons send the right events." | Test generation |

---

## Stage 8: Adaptive app shell and navigation

**Goal.** Top-level destinations, the adaptive shell (navigation rail on tablets, bottom bar on phones), the NavHost with placeholder screens, and the final MainActivity.

### 08.1 Add top-level destinations and a placeholder screen
- **Patch:** `08.1-destinations.patch`
- **Commit message:** `Add top-level destinations and a placeholder screen`
- **Files:** `add` …/ui/common/ComingSoonScreen.kt · `add` …/ui/navigation/TopLevelDestination.kt

### 08.2 Add adaptive navigation shell (rail on tablets, bar on phones)
- **Patch:** `08.2-app-shell.patch`
- **Commit message:** `Add adaptive navigation shell (rail on tablets, bar on phones)`
- **Files:** `edit` app/build.gradle.kts · `edit` …/MainActivity.kt · `add` …/ui/navigation/FelineAppShell.kt · `add` …/ui/navigation/FelineNavHost.kt · `edit` gradle/libs.versions.toml
- **Note:** MainActivity becomes final here; the temporary signed-in placeholder from 07.4 is gone.

#### Test this stage
- [ ] Customer demo: a rail on the left with Home, Adopt, Shop, Basket, Bookings, Account; tapping each shows its placeholder; the selected item is highlighted.
- [ ] Account placeholder → Log out works.
- [ ] Admin demo: the rail shows Dashboard, Cats, Appointments, Products, Account.
- [ ] Rotate to portrait / try a phone emulator: a bottom bar appears instead of the rail.

#### Screenshots
- **S11**: App shell with the navigation rail on the tablet (customer).

#### Worklog notes for this stage
Key design decisions (decision → why; mention an alternative where it helps):
- Type-safe `@Serializable` routes instead of string routes: arguments are compile-time checked.
- `NavigationSuiteScaffold` picks a rail or a bar by width (≥600 dp → rail).
- Top-level navigation uses `popUpTo(start){saveState}` + `restoreState` so each tab keeps its scroll position.
- Customers and admins get different destination sets in the same app.
- A temporary `ComingSoonScreen` keeps every destination runnable until it's built.

Issues to log (these really came up while this code was written; the fix is already in the patch):
- The bottom bar already pads for the system navigation bar but the rail doesn't, so content was hidden behind the gesture bar on tablets → add navigation-bar padding only in rail mode.

Plus **any real problem hit while building or testing locally**: log it too, with the fix.

#### Gen AI Reflection: Stage 8 (demo prompts for the worklog)
| # | File | Example prompt a student could ask | Type of assistance |
|---|---|---|---|
| 1 | `ui/navigation/Routes.kt` | "Explain type-safe Navigation Compose routes with @Serializable. How do I pass a catId and read it in a ViewModel?" | Explanation |
| 2 | `ui/navigation/FelineAppShell.kt` | "My bottom bar pads for the system navigation bar but the navigation rail doesn't. How should I handle WindowInsets so content isn't hidden in either layout?" | Layout debugging |
| 3 | `ui/navigation/FelineAppShell.kt` | "What do saveState and restoreState do when navigating between bottom-navigation tabs, and what bug do they prevent?" | Explanation |
| 4 | `ui/navigation/TopLevelDestination.kt` | "Which top-level destinations should a customer and an admin see? Base it on Material 3's guidance for navigation rails." | UX advice |

---

## Stage 9: Home

**Goal.** The Home dashboard: hero, live stats, new-arrivals carousel, the free-adoption banner, how-it-works steps and shop highlights.

### 09.1 Add Home ViewModel combining cats, products and the user
- **Patch:** `09.1-home-viewmodel.patch`
- **Commit message:** `Add Home ViewModel combining cats, products and the user`
- **Files:** `edit` …/di/AppViewModelProvider.kt · `add` …/ui/home/HomeViewModel.kt

### 09.2 Build Home screen with hero, stats and carousels
- **Patch:** `09.2-home-screen.patch`
- **Commit message:** `Build Home screen with hero, stats and carousels`
- **Files:** `add` …/ui/home/HomeScreen.kt · `edit` …/ui/navigation/FelineNavHost.kt

#### Test this stage
- [ ] Home shows the hero, "24 cats looking for a home", new arrivals (newest first), the senior banner, three steps and shop picks.
- [ ] Credit chip shows 400 for the demo customer.
- [ ] Scroll is smooth; on a phone the stat cards hide their icons.

#### Screenshots
- **S12**: Home screen on the tablet.

#### Worklog notes for this stage
Key design decisions (decision → why; mention an alternative where it helps):
- One `HomeUiState` built by `combine()`-ing the user, cats and products flows (`stateIn(WhileSubscribed(5000))`).
- The hero stays dark in both themes so its white text is always readable.
- Content width is capped at 1280 dp so lines stay readable on large tablets.

Issues to log (these really came up while this code was written; the fix is already in the patch):
- In the light theme the hero faded into a pale background right under white text → hero kept dark with rounded bottom corners.
- Stat cards were cramped on narrow phones → icons hidden below 600 dp.

Plus **any real problem hit while building or testing locally**: log it too, with the fix.

#### Gen AI Reflection: Stage 9 (demo prompts for the worklog)
| # | File | Example prompt a student could ask | Type of assistance |
|---|---|---|---|
| 1 | `ui/home/HomeViewModel.kt` | "I combine three Flows and call stateIn(viewModelScope, WhileSubscribed(5000), …). What does the 5000 ms do and when do the upstream flows stop?" | Explanation |
| 2 | `ui/home/HomeScreen.kt` | "Critique my Home layout for an 11-inch tablet in landscape against Material 3 large-screen guidance. What would you change first?" | Design critique |
| 3 | `ui/home/HomeScreen.kt` | "White text over a photo hero is hard to read in my light theme. Give three ways to guarantee contrast and their trade-offs." | Problem solving |

---

## Stage 10: Adopt: search, filter and sort

**Goal.** Search field and sort menu, the Adopt ViewModel (with fakes and unit tests), the filter panel and the Adopt screen.

### 10.1 Add search field, sort menu and formatting helpers
- **Patch:** `10.1-search-sort.patch`
- **Commit message:** `Add search field, sort menu and formatting helpers`
- **Files:** `add` …/ui/common/Formatters.kt · `add` …/ui/components/SearchAndSort.kt

### 10.2 Add Adopt ViewModel with unit tests and fake repositories
- **Patch:** `10.2-adopt-viewmodel.patch`
- **Commit message:** `Add Adopt ViewModel with unit tests and fake repositories`
- **Files:** `edit` app/build.gradle.kts · `edit` …/di/AppViewModelProvider.kt · `add` …/ui/adopt/AdoptViewModel.kt · `add` test/…/testing/Fakes.kt · `add` test/…/testing/MainDispatcherRule.kt · `add` test/…/ui/AdoptViewModelTest.kt

### 10.3 Add the filter panel and brand filter chips
- **Patch:** `10.3-filter-panel.patch`
- **Commit message:** `Add the filter panel and brand filter chips`
- **Files:** `add` …/ui/adopt/CatFilterPanel.kt

### 10.4 Build Adopt screen with responsive grid and filter sheet
- **Patch:** `10.4-adopt-screen.patch`
- **Commit message:** `Build Adopt screen with responsive grid and filter sheet`
- **Files:** `add` …/ui/adopt/AdoptScreen.kt · `edit` …/ui/navigation/FelineNavHost.kt

#### Test this stage
- [ ] Adopt shows a grid of 24 cats with the filter side panel (tablet).
- [ ] Search `maine` → Willow and Archie. **Free to adopt** → only 2+ cats. Sort **Oldest first** → Olive first.
- [ ] Over-filter → "No cats match" + **Clear filters**. Home → **Browse free-to-adopt cats** opens Adopt with Free selected.
- [ ] Rotate: filters are kept. `./gradlew :app:testDebugUnitTest` → **26 tests pass**.

#### Screenshots
- **S13**: Adopt screen with the filter side panel.
- **S14**: Adopt results filtered and sorted (e.g. Free to adopt + Oldest first).

#### Worklog notes for this stage
Key design decisions (decision → why; mention an alternative where it helps):
- All filter state lives in one `CatQuery` inside the ViewModel; results recompute when the query or the data changes.
- Quick chips for common filters + a full panel: permanent side panel ≥840 dp, bottom sheet below.
- `Modifier.animateItem()` animates cards as results change (dynamic UI).
- The `freeOnly` route argument is read through `SavedStateHandle.toRoute()`.
- ViewModel tests use in-memory fake repositories and a `MainDispatcherRule`.

Issues to log (these really came up while this code was written; the fix is already in the patch):
- Unit test read `uiState.value` but it never updated: `stateIn(WhileSubscribed)` only runs while collected → tests start a collector in `backgroundScope`.
- Quick-chip labels were built by adding "s" ("Youngs") → proper plural labels per age group.

Plus **any real problem hit while building or testing locally**: log it too, with the fix.

#### Gen AI Reflection: Stage 10 (demo prompts for the worklog)
| # | File | Example prompt a student could ask | Type of assistance |
|---|---|---|---|
| 1 | `ui/adopt/AdoptViewModel.kt` | "My filters live in one CatQuery data class inside a MutableStateFlow. Is that better than a separate StateFlow per filter? Consider testing and recomposition." | Design review |
| 2 | `test/.../AdoptViewModelTest.kt` | "My test reads uiState.value but it never changes. The ViewModel uses stateIn(WhileSubscribed). Why, and how do I fix it with runTest?" | Debugging |
| 3 | `ui/adopt/AdoptScreen.kt` | "When should filters be a permanent side panel versus a bottom sheet? Base the answer on window size classes." | UX research |
| 4 | `ui/adopt/CatFilterPanel.kt` | "Write concise KDoc for these filter composables that explains the why, not just the what." | Documentation |

---

## Stage 11: Cat profile

**Goal.** The cat profile ViewModel and screen: photo, story, personality, special care, facts, health, compatibility, fee vs. balance, similar cats.

### 11.1 Add cat profile ViewModel
- **Patch:** `11.1-detail-viewmodel.patch`
- **Commit message:** `Add cat profile ViewModel`
- **Files:** `edit` …/di/AppViewModelProvider.kt · `add` …/ui/catdetail/CatDetailViewModel.kt

### 11.2 Build the cat profile screen
- **Patch:** `11.2-detail-screen.patch`
- **Commit message:** `Build the cat profile screen`
- **Files:** `add` …/ui/catdetail/CatDetailScreen.kt · `edit` …/ui/navigation/FelineNavHost.kt

#### Test this stage
- [ ] Open Mochi: two panes on the tablet (photo left, details right). Fee 150, balance 400.
- [ ] Open a senior: fee **Free**. Similar cats row opens other profiles. Back returns to Adopt.
- [ ] **Book a meet & greet** shows a 'coming soon' snackbar (booking arrives in stage 12).

#### Screenshots
- **S15**: Cat profile in the two-pane tablet layout.

#### Worklog notes for this stage
Key design decisions (decision → why; mention an alternative where it helps):
- `canBook` / `canAfford` / `creditsShort` are computed in the UiState, not the composable, so they're testable.
- When booking isn't possible the card explains why (already reserved, adopted, or short of credits): error prevention.
- 'You might also love' suggests other available cats in the same life stage.

Plus **any real problem hit while building or testing locally**: log it too, with the fix.

#### Gen AI Reflection: Stage 11 (demo prompts for the worklog)
| # | File | Example prompt a student could ask | Type of assistance |
|---|---|---|---|
| 1 | `ui/catdetail/CatDetailViewModel.kt` | "Where should the rule 'this user can book this cat' live: the composable, the ViewModel or the domain layer? Justify it for testability." | Architecture |
| 2 | `ui/catdetail/CatDetailScreen.kt` | "Write the message shown when the booking button is disabled because the user is short of credits, following Nielsen's 'help users recognise, diagnose and recover from errors'." | UX writing |
| 3 | `ui/catdetail/CatDetailScreen.kt` | "Can I put a LazyRow inside a Column with verticalScroll? When would that crash, and why?" | Explanation |

---

## Stage 12: BookingActivity and the Activity Result API

**Goal.** Dialogs and a date strip, the booking ViewModel (with tests), `BookingActivity` with its Parcelable input/output contract, and launching it from the profile.

### 12.1 Add confirm/success dialogs and a date strip
- **Patch:** `12.1-dialogs-datestrip.patch`
- **Commit message:** `Add confirm/success dialogs and a date strip`
- **Files:** `add` …/ui/components/DateStrip.kt · `add` …/ui/components/Dialogs.kt

### 12.2 Add booking ViewModel with validation and unit tests
- **Patch:** `12.2-booking-viewmodel.patch`
- **Commit message:** `Add booking ViewModel with validation and unit tests`
- **Files:** `add` …/ui/booking/BookingViewModel.kt · `add` test/…/ui/BookingViewModelTest.kt

### 12.3 Add BookingActivity with the meet & greet form
- **Patch:** `12.3-booking-activity.patch`
- **Commit message:** `Add BookingActivity with the meet & greet form`
- **Files:** `edit` app/src/main/AndroidManifest.xml · `add` …/ui/booking/BookMeetAndGreet.kt · `add` …/ui/booking/BookingActivity.kt · `add` …/ui/booking/BookingScreen.kt

### 12.4 Launch booking from the cat profile and confirm the result
- **Patch:** `12.4-launch-booking.patch`
- **Commit message:** `Launch booking from the cat profile and confirm the result`
- **Files:** `edit` …/ui/navigation/FelineNavHost.kt

#### Test this stage
- [ ] From Mochi → **Book a meet & greet** → BookingActivity opens showing Mochi (Cat passed as Parcelable).
- [ ] **Confirm booking** empty → errors for day, time, phone, home and terms + banner. Mondays show *Closed*.
- [ ] Toggle **I have other pets** → a warning appears (Mochi prefers to be the only pet).
- [ ] Complete and confirm → activity closes → **"You're booked in!"** dialog (Booking returned). Credits now 250.
- [ ] Book another cat for the same day/time → that slot shows *Booked*. Back out of the form → nothing changes.
- [ ] `./gradlew :app:testDebugUnitTest` → **34 tests pass**.

#### Screenshots
- **S16**: BookingActivity: the meet & greet form on the tablet.
- **S17**: Booking form showing validation errors.
- **S18**: "You're booked in!" confirmation dialog back in MainActivity.

#### Worklog notes for this stage
Key design decisions (decision → why; mention an alternative where it helps):
- A separate Activity as the brief requires, with a custom `ActivityResultContract<Cat, Booking?>` for typed input and output.
- `IntentCompat.getParcelableExtra` instead of the deprecated `getParcelableExtra(String)`.
- The ViewModel receives the Cat through a small factory in the activity.
- A horizontal date strip instead of a calendar dialog: every option is visible and one tap away on a tablet.
- Errors are keyed by a `BookingField` enum; the repository re-checks the rules (defence in depth).
- The confirmation dialog state is `rememberSaveable` (Parcelable), so it survives rotation.

Issues to log (these really came up while this code was written; the fix is already in the patch):
- `Intent.getParcelableExtra(String)` is deprecated from API 33 → `IntentCompat` keeps one code path for API 26–35.
- A slot could be taken between picking it and confirming → the repository rejects it and the form refreshes the taken slots.

Plus **any real problem hit while building or testing locally**: log it too, with the fix.

#### Gen AI Reflection: Stage 12 (demo prompts for the worklog)
| # | File | Example prompt a student could ask | Type of assistance |
|---|---|---|---|
| 1 | `ui/booking/BookMeetAndGreet.kt` | "Show me how to write a custom ActivityResultContract that takes a Parcelable Cat and returns a Booking or null. Why is this better than startActivityForResult?" | Explanation + code |
| 2 | `ui/booking/BookingActivity.kt` | "getParcelableExtra(String) is deprecated. What's the backward-compatible replacement when minSdk is 26?" | API migration |
| 3 | `ui/booking/BookingViewModel.kt` | "Review validate(). Does keying errors by an enum make sense, and how do I clear only the edited field's error?" | Code review |
| 4 | `test/.../BookingViewModelTest.kt` | "List the test cases for a booking form with date, time slot, phone, home type and terms, including a slot taken by someone else." | Test design |

---

## Stage 13: My bookings

**Goal.** A reusable booking card and the My Bookings screen with cancel and refund.

### 13.1 Add booking card component
- **Patch:** `13.1-booking-card.patch`
- **Commit message:** `Add booking card component`
- **Files:** `add` …/ui/components/BookingCard.kt

### 13.2 Build My Bookings with cancel and refund
- **Patch:** `13.2-my-bookings.patch`
- **Commit message:** `Build My Bookings with cancel and refund`
- **Files:** `edit` …/di/AppViewModelProvider.kt · `add` …/ui/bookings/BookingsScreen.kt · `add` …/ui/bookings/BookingsViewModel.kt · `edit` …/ui/navigation/FelineNavHost.kt

#### Test this stage
- [ ] Bookings tab: the new booking is under **Upcoming** with a *Requested* chip.
- [ ] **Cancel booking** → confirmation dialog → snackbar "…credits refunded"; it moves to **Past**; the cat is available again.
- [ ] With no bookings: empty state with **Find a cat**.

#### Screenshots
- **S19**: My Bookings with upcoming and past sections.

#### Worklog notes for this stage
Key design decisions (decision → why; mention an alternative where it helps):
- One-off messages (snackbars) go through a `Channel`, not the UiState, so they aren't replayed on rotation.
- Destructive actions ask for confirmation and state the consequence (refund, cat released).

Issues to log (these really came up while this code was written; the fix is already in the patch):
- `animateItem()` was unresolved inside a helper's card lambda because the lambda lacked the grid item scope → gave the lambda `LazyGridItemScope` as its receiver.

Plus **any real problem hit while building or testing locally**: log it too, with the fix.

#### Gen AI Reflection: Stage 13 (demo prompts for the worklog)
| # | File | Example prompt a student could ask | Type of assistance |
|---|---|---|---|
| 1 | `ui/bookings/BookingsViewModel.kt` | "Should snackbar messages be part of UiState or a separate Channel? What happens to each on rotation?" | Design trade-off |
| 2 | `ui/bookings/BookingsScreen.kt` | "Error: 'Unresolved reference: animateItem' inside a lambda I pass to my own LazyGridScope extension. Why, and how do I give the lambda the right scope?" | Debugging |
| 3 | `ui/components/Dialogs.kt` | "When should an app ask for confirmation before an action? Give HCI guidance and an example of good confirmation wording." | UX guidance |

---

## Stage 14: Shop

**Goal.** The shop ViewModel, a quantity stepper and the shop screen with categories, sorting and a product sheet.

### 14.1 Add shop ViewModel and quantity stepper
- **Patch:** `14.1-shop-viewmodel.patch`
- **Commit message:** `Add shop ViewModel and quantity stepper`
- **Files:** `edit` …/di/AppViewModelProvider.kt · `add` …/ui/components/QuantityStepper.kt · `add` …/ui/shop/ShopViewModel.kt

### 14.2 Build the shop with categories and a product sheet
- **Patch:** `14.2-shop-screen.patch`
- **Commit message:** `Build the shop with categories and a product sheet`
- **Files:** `edit` …/ui/navigation/FelineNavHost.kt · `add` …/ui/shop/ShopScreen.kt

#### Test this stage
- [ ] Shop: category chips, search, **Price: high to low**.
- [ ] Open a product → sheet with description and stock; quantity can't exceed stock minus what's in the basket.
- [ ] **Add to basket** → snackbar; the **Basket** rail item shows a badge. The out-of-stock slicker brush can't be added.

#### Screenshots
- **S20**: Shop with the product sheet open.

#### Worklog notes for this stage
Key design decisions (decision → why; mention an alternative where it helps):
- Prices are in credits only, tying the shop to the adoption economy.
- The sheet remembers the product *id*, not a copy, so stock shown is always live.
- The basket badge comes from `MainViewModel`, visible on every screen.

Plus **any real problem hit while building or testing locally**: log it too, with the fix.

#### Gen AI Reflection: Stage 14 (demo prompts for the worklog)
| # | File | Example prompt a student could ask | Type of assistance |
|---|---|---|---|
| 1 | `ui/shop/ShopViewModel.kt` | "I combine four flows (products, query, user, basket). Is there a limit to combine(), and how do I keep this readable?" | Explanation |
| 2 | `ui/shop/ShopScreen.kt` | "My bottom sheet keeps a Product copy and shows stale stock after an update. How do I keep it live?" | Debugging |
| 3 | `ui/components/QuantityStepper.kt` | "Make my − / + stepper accessible: what content descriptions and touch-target sizes should it have?" | Accessibility |

---

## Stage 15: Basket and CheckoutActivity

**Goal.** The basket with live totals, the checkout ViewModel (with tests), `CheckoutActivity` with its Parcelable list contract, and launching it from the basket.

### 15.1 Build the basket with live totals
- **Patch:** `15.1-basket.patch`
- **Commit message:** `Build the basket with live totals`
- **Files:** `edit` …/di/AppViewModelProvider.kt · `add` …/ui/basket/BasketScreen.kt · `add` …/ui/basket/BasketViewModel.kt · `edit` …/ui/navigation/FelineNavHost.kt

### 15.2 Add checkout ViewModel with unit tests
- **Patch:** `15.2-checkout-viewmodel.patch`
- **Commit message:** `Add checkout ViewModel with unit tests`
- **Files:** `add` …/ui/checkout/CheckoutViewModel.kt · `add` test/…/ui/CheckoutViewModelTest.kt

### 15.3 Add CheckoutActivity with the delivery form
- **Patch:** `15.3-checkout-activity.patch`
- **Commit message:** `Add CheckoutActivity with the delivery form`
- **Files:** `edit` app/src/main/AndroidManifest.xml · `add` …/ui/checkout/CheckoutActivity.kt · `add` …/ui/checkout/CheckoutContract.kt · `add` …/ui/checkout/CheckoutScreen.kt

### 15.4 Launch checkout from the basket and confirm the order
- **Patch:** `15.4-launch-checkout.patch`
- **Commit message:** `Launch checkout from the basket and confirm the order`
- **Files:** `edit` …/ui/navigation/FelineNavHost.kt

#### Test this stage
- [ ] Basket: change quantities and remove items; totals, balance-after and the shortfall warning update instantly.
- [ ] **Checkout** → CheckoutActivity opens with the items (ArrayList of Parcelables).
- [ ] Standard delivery with no address → address, suburb and postcode errors; letters can't be typed into postcode.
- [ ] **Click & collect** → address fields slide away. **Place order** → "Order placed!" dialog; basket empty; credits reduced; stock reduced.
- [ ] `./gradlew :app:testDebugUnitTest` → **40 tests pass**.

#### Screenshots
- **S21**: Basket with order summary.
- **S22**: CheckoutActivity with the delivery form.
- **S23**: "Order placed!" dialog back in MainActivity.

#### Worklog notes for this stage
Key design decisions (decision → why; mention an alternative where it helps):
- The basket goes to the activity as `ArrayList<CartItem>`; the placed `Order` comes back as a Parcelable result.
- Checkout re-reads prices and stock from the database rather than trusting the basket passed in.
- Conditional validation: address fields only for delivery methods that need them.
- Input filtering: postcode keeps digits only (max 4).

Issues to log (these really came up while this code was written; the fix is already in the patch):
- Stock can drop after an item was added to the basket → over-stock warning on the line and a guard at checkout.

Plus **any real problem hit while building or testing locally**: log it too, with the fix.

#### Gen AI Reflection: Stage 15 (demo prompts for the worklog)
| # | File | Example prompt a student could ask | Type of assistance |
|---|---|---|---|
| 1 | `ui/checkout/CheckoutContract.kt` | "How do I pass a List of Parcelables through an Intent and read it back safely on API 26–35?" | Explanation |
| 2 | `ui/checkout/CheckoutViewModel.kt` | "Address fields are only required for delivery, not click & collect. What's a clean way to express conditional validation?" | Design |
| 3 | `data/repository/ShopRepository.kt` | "Why should checkout re-read prices and stock from the database instead of trusting the basket the screen passes in?" | Robustness review |
| 4 | `test/.../CheckoutViewModelTest.kt` | "Write tests showing express delivery adds 10 credits and that the order is blocked when the balance is too low." | Test generation |

---

## Stage 16: Account, wallet and theme

**Goal.** The account ViewModel and screen: profile card, credit wallet history, orders and the dark/light theme setting.

### 16.1 Add account ViewModel with wallet, orders and theme setting
- **Patch:** `16.1-profile-viewmodel.patch`
- **Commit message:** `Add account ViewModel with wallet, orders and theme setting`
- **Files:** `edit` …/di/AppViewModelProvider.kt · `add` …/ui/profile/ProfileViewModel.kt

### 16.2 Build the account screen with wallet history and theme toggle
- **Patch:** `16.2-profile-screen.patch`
- **Commit message:** `Build the account screen with wallet history and theme toggle`
- **Files:** `edit` …/ui/navigation/FelineNavHost.kt · `add` …/ui/profile/ProfileScreen.kt

#### Test this stage
- [ ] Account: balance, earned/spent, wallet history (welcome bonus, demo bonus, fee held, refund, purchase) and the Orders tab.
- [ ] Switch **Dark theme** off → the whole app, including the Booking and Checkout activities, turns light. Restart → the setting is remembered.
- [ ] Admin account: profile and settings only (no wallet).

#### Screenshots
- **S24**: Account screen with the wallet history (dark theme).
- **S25**: The app in the light theme.

#### Worklog notes for this stage
Key design decisions (decision → why; mention an alternative where it helps):
- Theme choice stored in DataStore and applied by the shared `setFelineContent()` in all three activities.
- The wallet is a ledger of `CreditTransaction`s, so the balance is always explainable.

Issues to log (these really came up while this code was written; the fix is already in the patch):
- On tablets the history list overflowed under the tabs (a `Column` gives each child the full height) → `Modifier.weight(1f)` on the list.

Plus **any real problem hit while building or testing locally**: log it too, with the fix.

#### Gen AI Reflection: Stage 16 (demo prompts for the worklog)
| # | File | Example prompt a student could ask | Type of assistance |
|---|---|---|---|
| 1 | `ui/common/FelineContent.kt` | "My three activities each call setContent. How can they share one theme and follow a dark/light setting stored in DataStore?" | Refactoring |
| 2 | `ui/profile/ProfileScreen.kt` | "My LazyColumn under a TabRow cuts off the last items on a tablet. Why does the Column give it the full height?" | Debugging |
| 3 | `ui/profile/ProfileViewModel.kt` | "Is it fine to compute totals like 'earned' and 'spent' as computed properties on the UiState?" | Review |

---

## Stage 17: Admin

**Goal.** Admin components, dashboard, listing management, the cat form, appointments management, and product management (the placeholder screen is removed at the end).

### 17.1 Add shared admin components
- **Patch:** `17.1-admin-components.patch`
- **Commit message:** `Add shared admin components`
- **Files:** `add` …/ui/admin/AdminComponents.kt

### 17.2 Build the admin dashboard
- **Patch:** `17.2-admin-dashboard.patch`
- **Commit message:** `Build the admin dashboard`
- **Files:** `edit` …/di/AppViewModelProvider.kt · `add` …/ui/admin/AdminDashboard.kt · `edit` …/ui/navigation/FelineNavHost.kt

### 17.3 Add listing management with search and status filter
- **Patch:** `17.3-admin-cats.patch`
- **Commit message:** `Add listing management with search and status filter`
- **Files:** `edit` …/di/AppViewModelProvider.kt · `add` …/ui/admin/AdminCatsScreen.kt · `edit` …/ui/navigation/FelineNavHost.kt

### 17.4 Add the cat add/edit form with validation
- **Patch:** `17.4-cat-form.patch`
- **Commit message:** `Add the cat add/edit form with validation`
- **Files:** `edit` …/di/AppViewModelProvider.kt · `add` …/ui/admin/CatFormScreen.kt · `add` …/ui/admin/CatFormViewModel.kt · `edit` …/ui/navigation/FelineNavHost.kt

### 17.5 Add appointments management for admins
- **Patch:** `17.5-admin-bookings.patch`
- **Commit message:** `Add appointments management for admins`
- **Files:** `edit` …/di/AppViewModelProvider.kt · `add` …/ui/admin/AdminBookingsScreen.kt · `edit` …/ui/navigation/FelineNavHost.kt

### 17.6 Add product management and remove the placeholder screen
- **Patch:** `17.6-admin-products.patch`
- **Commit message:** `Add product management and remove the placeholder screen`
- **Files:** `edit` …/di/AppViewModelProvider.kt · `add` …/ui/admin/AdminProductsScreen.kt · `add` …/ui/admin/ProductFormScreen.kt · `delete` …/ui/common/ComingSoonScreen.kt · `edit` …/ui/navigation/FelineNavHost.kt
- **Note:** After this, no placeholder screens remain.

#### Test this stage
- [ ] Admin demo → Dashboard: stats, new requests (Confirm/Decline), low stock.
- [ ] Cats: search, status chips with counts. **Add cat** → Save empty → errors; image name `Cat-Luna` → resource-name error; age 0 y 3 m → fee preview 150.
- [ ] Save → snackbar, back on the list; the new cat appears for customers too. Delete a cat with an active booking → blocked with a message.
- [ ] Appointments: Requests / Confirmed / Adopted / Closed tabs; **Complete adoption** → confirmation → customer gets +250 (check by signing in as the customer).
- [ ] Products: stock stepper; out-of-stock items become available in the shop; product form validation.

#### Screenshots
- **S26**: Admin dashboard.
- **S27**: Cat form showing validation errors and the live fee preview.
- **S28**: Appointments with the 'Complete adoption' confirmation.

#### Worklog notes for this stage
Key design decisions (decision → why; mention an alternative where it helps):
- Same app, role-based destinations; *Pending* is only ever set by a booking, so the form offers just Available/Adopted.
- Number fields are kept as text while typing and parsed on save.
- Image names are validated against Android resource rules, with a live preview.
- Credit-changing admin actions (decline, complete) ask for confirmation.

Issues to log (these really came up while this code was written; the fix is already in the patch):
- A snackbar launched from a form that's being popped never showed (its coroutine scope was cancelled) → the message is shown from a NavHost-level scope.
- Deleting a cat with an active booking would orphan held credits → the repository blocks it with a clear message.

Plus **any real problem hit while building or testing locally**: log it too, with the fix.

#### Gen AI Reflection: Stage 17 (demo prompts for the worklog)
| # | File | Example prompt a student could ask | Type of assistance |
|---|---|---|---|
| 1 | `ui/admin/CatFormViewModel.kt` | "Should numeric form fields be stored as String or Int in the UiState? What happens while the user is mid-typing?" | Design |
| 2 | `ui/navigation/FelineNavHost.kt` | "My snackbar doesn't show after popBackStack() from a form screen. Why is the coroutine cancelled, and where should it be launched?" | Debugging |
| 3 | `ui/admin/AdminBookingsScreen.kt` | "Suggest a status-filter design for an admin appointments list (requests, confirmed, adopted, closed) and how to show counts." | UX |
| 4 | `ui/admin/AdminComponents.kt` | "Write a regex for valid Android resource file names and explain each part." | Code + explanation |

---

## Stage 18: Extra feature: favourites

**Goal.** Per-user favourites stored in Room (database version 2), a favourites filter, an animated heart, and favourites on Adopt, profiles and Home.

### 18.1 Store favourites in Room (database version 2)
- **Patch:** `18.1-favourites-data.patch`
- **Commit message:** `Store favourites in Room (database version 2)`
- **Files:** `edit` androidTest/…/data/AdoptionAndShopFlowTest.kt · `edit` …/data/local/FelineDatabase.kt · `edit` …/data/local/dao/CatDao.kt · `edit` …/data/local/entity/Entities.kt · `edit` …/data/repository/CatRepository.kt · `edit` test/…/testing/Fakes.kt
- **Note:** Database version 1 → 2: on next launch the old data is wiped and re-seeded automatically (sign in again). A new `app/schemas/.../2.json` appears; it belongs in this commit.

### 18.2 Add favourites filter and animated heart button
- **Patch:** `18.2-favourites-ui-parts.patch`
- **Commit message:** `Add favourites filter and animated heart button`
- **Files:** `edit` …/domain/CatQuery.kt · `add` …/ui/common/Favourites.kt · `edit` …/ui/components/CatCard.kt · `edit` test/…/domain/CatQueryTest.kt

### 18.3 Show favourites on Adopt, cat profiles and Home
- **Patch:** `18.3-favourites-screens.patch`
- **Commit message:** `Show favourites on Adopt, cat profiles and Home`
- **Files:** `edit` …/di/AppViewModelProvider.kt · `edit` …/ui/adopt/AdoptScreen.kt · `edit` …/ui/adopt/AdoptViewModel.kt · `edit` …/ui/adopt/CatFilterPanel.kt · `edit` …/ui/catdetail/CatDetailScreen.kt · `edit` …/ui/catdetail/CatDetailViewModel.kt · `edit` …/ui/home/HomeScreen.kt · `edit` …/ui/home/HomeViewModel.kt · `edit` test/…/ui/AdoptViewModelTest.kt

#### Test this stage
- [ ] Heart two cats → hearts pop red; the **♥ Favourites (2)** chip filters to them; Home shows **Your favourites**.
- [ ] Un-heart → it disappears immediately. Restart → favourites persist. A different account has its own favourites.
- [ ] Unit tests → **42 pass**; instrumented `favouritesToggleAndAreRemovedWithTheCat` passes.

#### Screenshots
- **S29**: Adopt with hearted cats and the Favourites filter active.

#### Worklog notes for this stage
Key design decisions (decision → why; mention an alternative where it helps):
- `favourites` table with a composite primary key (userId, catId) and a CASCADE foreign key to cats.
- Cats are saved with `@Upsert` (insert-or-update) rather than REPLACE, so editing a cat never cascades away favourites.
- Shared helpers (`favouriteIds`, `toggleFavourite`) avoid repeating the same flow logic in three ViewModels.

Issues to log (these really came up while this code was written; the fix is already in the patch):
- `REPLACE` conflict strategy deletes and re-inserts the row, which would trigger the cascade and wipe favourites → verified that `@Upsert` updates in place (instrumented test).
- Bumping the database version wiped existing data (destructive migration) → acceptable in development; a real release would need a `Migration`.

Plus **any real problem hit while building or testing locally**: log it too, with the fix.

#### Gen AI Reflection: Stage 18 (demo prompts for the worklog)
| # | File | Example prompt a student could ask | Type of assistance |
|---|---|---|---|
| 1 | `data/local/entity/Entities.kt` | "Design a Room table for per-user favourite cats with cascade delete. What should the primary key be?" | Schema design |
| 2 | `data/local/dao/CatDao.kt` | "Will OnConflictStrategy.REPLACE on my cats table trigger ON DELETE CASCADE on a favourites table? How can I prove it with a test?" | Investigation |
| 3 | `ui/components/CatCard.kt` | "Add a subtle 'pop' animation to a heart icon when it's toggled, using animateFloatAsState with a spring." | Code generation |
| 4 | `data/local/FelineDatabase.kt` | "I bumped my Room version and my test data disappeared. Explain fallbackToDestructiveMigration and when I'd need a real Migration." | Explanation |

---

## Stage 19: Documentation, final testing and submission

**Goal.** README and the testing guide; a full manual test pass on tablet and phone; final worklog sections.

### 19.1 Write README with features, architecture and demo accounts
- **Patch:** `19.1-readme.patch`
- **Commit message:** `Write README with features, architecture and demo accounts`
- **Files:** `edit` README.md

### 19.2 Add testing guide with the manual test script
- **Patch:** `19.2-testing-guide.patch`
- **Commit message:** `Add testing guide with the manual test script`
- **Files:** `add` TESTING.md

#### Test this stage
- [ ] Run the whole `TESTING.md` script on the tablet; rotate on key screens; try a phone emulator; quick TalkBack pass on a cat card.
- [ ] `./gradlew :app:testDebugUnitTest` (42 pass) and `./gradlew :app:connectedDebugAndroidTest` (all pass).
- [ ] Fill in §5 Testing Summary, §6 Reflection, the time totals, the references (image credits) and the screenshot index.

#### Screenshots
- **S30**: The app on a phone-sized screen (bottom navigation bar).

#### Worklog notes for this stage
Key design decisions (decision → why; mention an alternative where it helps):
- The README is written for a marker with five minutes: features, architecture, how to run, demo accounts, tests.
- The manual test script is mapped to the brief's topics.

Plus **any real problem hit while building or testing locally**: log it too, with the fix.

#### Gen AI Reflection: Stage 19 (demo prompts for the worklog)
| # | File | Example prompt a student could ask | Type of assistance |
|---|---|---|---|
| 1 | `README.md` | "Review my README as if you were a marker with five minutes. What's missing or unclear?" | Review |
| 2 | `TESTING.md` | "Turn my feature list into a manual test script with expected results, mapped to the assignment's required topics." | Structuring |
| 3 | `WORKLOG.md` | "Read my reflection and point out any claims that aren't backed by evidence such as tests, commits or screenshots." | Critique |

---

## After stage 19
- Worklog: complete §5 Testing Summary, §6 Reflection, the time totals in §1.9, the References (image credits) and the §9 screenshot index.
- Tell the owner the plan is complete. Further changes follow the same loop: small change → build → test → worklog → commit commands.
