# The Feline Co. — Project Context

> Working document for building the app. It holds the agreed plan, the architecture and
> the conventions, and gets updated as each phase lands. Read this before touching the code.

## 1. What we are building

**The Feline Co.** is a premium, cats-only adoption and pet-supply app for Android tablets
(tested on a Samsung tablet, also works on phones). It is loosely inspired by
[The Lost Dogs' Home](https://dogshome.com/) but is its own product.

Customers can:
- browse cats up for adoption, with search, filters and sorting
- view a cat's full profile and **book a meet & greet appointment**
- adopt with **credits**: kittens cost credits, cats aged 2 and over are **free**
- **earn credits** by completing an adoption, and spend them in the shop on
  "Royal Feline" food, toys, beds and accessories
- manage a basket, check out, and see their bookings, orders and credit history

Admins (same app, admin login) can:
- add, edit and delete cat listings and change their status (Available / Pending / Adopted)
- confirm, decline or complete adoption appointments
- manage shop products (price, stock)

**Brand:** premium, and red / gray / black. Dark-first. Playful cat touches (paw prints, a
cat logo with a crown) on top of a clean, professional base. It should look like a real product.

The university report is **out of scope**. Only the app is built here.

## 2. Brief requirements → where they live

| Requirement | Where it is met |
|---|---|
| Jetpack Compose | Entire UI (`ui/**`) |
| Navigation Compose | `ui/navigation/FelineNavHost.kt`: type-safe `@Serializable` routes for every screen inside `MainActivity` |
| Multiple Activities | `MainActivity`, `BookingActivity`, `CheckoutActivity` |
| Parcelable data transfer | `Cat` → `BookingActivity`; `ArrayList<CartItem>` → `CheckoutActivity`; `Booking` / `Order` come back as results. All `@Parcelize` |
| Activity Result APIs | `rememberLauncherForActivityResult` with the custom contracts `BookMeetAndGreet` and `CheckoutContract` |
| ViewModel & State Management | One ViewModel per screen, `StateFlow<UiState>` + `collectAsStateWithLifecycle`, unidirectional data flow |
| Search, Filtering & Sorting | Adopt screen (text search, age / sex / coat / compatibility / free-only filters, 5 sort orders); Shop (search, category, price sort); Admin lists |
| Form Validation & Error Handling | Login/Register, BookingActivity, CheckoutActivity, admin Cat/Product forms. Pure validators in `domain/validation`, inline field errors, snackbars, repository `Result` errors |
| Dynamic UI Updates | Room `Flow`s → live lists, basket badge, credit balance, status chips; loading / empty / error states; animated visibility |
| Reusable Composables | `ui/components/*` (CatCard, ProductCard, FeeBadge, StatusChip, ValidatedTextField, EmptyState, SectionHeader, FelineLogo, PawPattern, AssetImage, …) |
| Styling | `ui/theme/*`: custom Material 3 colour scheme, typography, shapes, dark + light |

### Required navigation flow (given by the course)

```
MainActivity ──Navigation Compose──▶ Resource / Basket screen
                                     (Cat Detail  or  Basket)
                                            │ Intent + Parcelable
                                            ▼
                             BookingActivity  /  CheckoutActivity
                                            │ Activity Result API
                                            ▼
                                       MainActivity  (snackbar / dialog, navigate to My Bookings or Orders)
```

- **Cat Detail → BookingActivity** (adoption meet & greet). It receives a `Cat` and returns a `Booking`.
- **Basket → CheckoutActivity** (buying food, toys and so on). It receives `ArrayList<CartItem>` and returns an `Order`.

Both secondary activities validate and persist through the shared repositories (`AppContainer`),
then `setResult(RESULT_OK, intent.putExtra(..., parcelable))` and `finish()`.

## 3. Screens

### Auth (MainActivity, no session)
- **Login**: email + password, "use demo account" shortcuts, validation, error on bad credentials.
- **Register**: name, email, password, confirm. Validation. New users get the **200 credit welcome bonus**.

### Customer (MainActivity → NavigationSuiteScaffold: rail on tablet, bar on phone)
| Screen | Contents |
|---|---|
| **Home** | Hero image + logo + tagline, credit balance chip, "Free to adopt: cats 2+" banner, featured cats row, shop highlights, how adoption works (3 steps), paw decorations |
| **Adopt** | Search bar, filter chips (age group, sex, coat, good with kids / cats / dogs, free only), sort menu, adaptive grid of `CatCard`s, result count, empty state |
| **Cat Detail** | Large photo, name, breed, age, sex, fee badge, status, personality chips, health checklist, compatibility, story. "Book a meet & greet" → **BookingActivity** |
| **Shop** | Search, category tabs, sort by price/name, product grid, add to basket with a quantity badge |
| **Basket** | Line items with a quantity stepper, subtotal vs balance, warning if short. "Checkout" → **CheckoutActivity** |
| **My Bookings** | Upcoming and past appointments, status chips, cancel (refund) |
| **Profile / Wallet** | Name, email, credit balance, transaction history, order history, log out |

### Secondary activities
- **BookingActivity**: summary of the cat, date picker (next 30 days, closed Mondays), time-slot chips
  (taken slots disabled), full name, email, phone, home type, other pets, children, notes, terms.
  Checks: required fields, email and phone format, a future date, a free slot, the cat still
  available, enough credits for the fee.
- **CheckoutActivity**: order summary, delivery name, address, suburb, postcode (4 digits),
  delivery method (standard / express +10 credits / click & collect). Checks: required fields,
  enough credits, enough stock.

### Admin (MainActivity with the admin role; different navigation items)
| Screen | Contents |
|---|---|
| **Dashboard** | Stat cards (available / pending / adopted cats, requested appointments, low stock), quick links |
| **Manage Cats** | Search + status filter + sort, list with status dropdown, FAB → Cat form |
| **Cat Form** | Add/edit every cat field with validation, delete with confirmation |
| **Appointments** | Filter by status; Confirm / Decline / Complete adoption actions |
| **Manage Products** | List, edit price/stock inline, product form |

## 4. Credit rules (`domain/CreditRules.kt`)

| Rule | Credits |
|---|---|
| Welcome bonus on register | **+200** |
| Kitten (< 6 months) adoption fee | **150** |
| Young cat (6–23 months) adoption fee | **100** |
| Cat 2 years or older | **FREE** |
| Reward when an adoption is completed | **+250** |
| Shop items | ~12–120 each |
| Express delivery | +10 |

Lifecycle:
1. **Book**: needs balance ≥ fee. The fee is **charged (held) at booking**, and the cat becomes **Pending**.
2. **Cancel (customer) / Decline (admin)**: the fee is **refunded**, and the cat goes back to **Available**.
3. **Confirm (admin)**: the booking is confirmed and the cat stays Pending.
4. **Complete adoption (admin)**: booking **Completed**, cat **Adopted**, customer gets **+250**.

Every change to the balance writes a `CreditTransaction` row, which the Wallet shows.

## 5. Data model (Room, `data/local`)

| Entity | Key fields |
|---|---|
| `UserEntity` | id, fullName, email (unique), passwordHash, salt, role (CUSTOMER/ADMIN), credits, createdAt |
| `CatEntity` | id, name, breed, ageMonths, sex, coat, colour, weightKg, personality (List<String>), description, imageName, goodWithKids/Cats/Dogs, indoorOnly, vaccinated, desexed, microchipped, specialNeeds?, status, listedAt |
| `BookingEntity` | id, userId, catId, catName, catImageName, dateEpochDay, timeSlot, fullName, email, phone, homeType, hasOtherPets, hasChildren, notes, feeCredits, status, createdAt |
| `ProductEntity` | id, name, brand, category, priceCredits, description, imageName, stock |
| `CartItemEntity` | (userId, productId) PK, quantity |
| `OrderEntity` + `OrderItemEntity` | order header (userId, totals, delivery details, status, createdAt) + lines |
| `CreditTransactionEntity` | id, userId, amount (±), type, description, createdAt |

Seed data (`data/local/seed/`) runs on first launch: 24 cats, 18 products, an admin and a demo customer.

**Demo accounts**
- Customer: `demo@thefelineco.com` / `Demo123!` (starts with 400 credits)
- Admin: `admin@thefelineco.com` / `Admin123!`

## 6. Architecture

- **MVVM + unidirectional data flow.** The UI sends events to the ViewModel, which exposes an immutable `UiState`.
- **Layers:** `ui` → `domain` (models, rules, validators, pure filter/sort logic) ← `data` (Room + repositories).
- Repositories are **interfaces** (easy to fake in unit tests), with `Offline*Repository` implementations.
  Multi-step operations (book, cancel, complete, checkout) run inside `database.withTransaction { }`.
- **Manual DI:** `FelineApplication` owns an `AppContainer`, and `AppViewModelProvider.Factory`
  builds every ViewModel. No Hilt, to keep the Gradle setup simple.
- **Session:** DataStore Preferences stores the logged-in user id.
- **Adaptive UI:** `NavigationSuiteScaffold` (rail on tablets, bottom bar on phones),
  `LazyVerticalGrid(GridCells.Adaptive)`, and max content widths on large screens.
- **Images** are local drawables looked up **by name** (`AssetImage`). A missing file shows a branded
  placeholder, so the app always builds and runs before the photos are added. See `ASSETS.md`.

### Package layout (`app/src/main/java/com/thefelineco/`)
```
FelineApplication.kt        MainActivity.kt
di/                         AppContainer, AppViewModelProvider
data/local/                 FelineDatabase, Converters, dao/, entity/, seed/
data/repository/            Cat/Booking/Shop/User/Session repositories
domain/model/               Cat, Booking, Product, CartItem, Order, User, CreditTransaction, enums
domain/                     CreditRules, CatQuery (filter+sort), ProductQuery
domain/validation/          Validators, FieldError
ui/theme/                   Color, Type, Shape, Theme
ui/components/              reusable composables
ui/navigation/              Routes, FelineNavHost, FelineAppShell (nav suite)
ui/auth/ ui/home/ ui/adopt/ ui/catdetail/ ui/shop/ ui/basket/ ui/bookings/ ui/profile/ ui/admin/
ui/booking/                 BookingActivity, BookingViewModel, BookMeetAndGreet contract
ui/checkout/                CheckoutActivity, CheckoutViewModel, CheckoutContract
```

### Tech stack (pinned for a stable first sync; Android Studio may offer upgrades, which are optional)
| | Version |
|---|---|
| Android Gradle Plugin | 8.7.3 (Gradle 8.11.1 wrapper) |
| Kotlin / Compose compiler plugin | 2.1.0 |
| KSP | 2.1.0-1.0.29 |
| Compose BOM | 2024.12.01 (Material 3, adaptive navigation suite, icons-extended) |
| Navigation Compose | 2.8.5 (type-safe routes + kotlinx-serialization 1.7.3) |
| Room | 2.6.1 |
| Lifecycle | 2.8.7 · Activity Compose 1.9.3 · DataStore 1.1.1 · Coil 2.7.0 |
| SDK | minSdk 26, target/compile 35 |

## 7. Visual design

- **Palette:** Onyx `#0E0E10` background · Charcoal `#1A1A1E` / `#232328` surfaces · Graphite `#3A3A42` outlines ·
  Crimson `#C8102E` primary · Deep ruby `#8E0B20` · Rose `#FF5A6E` highlight · Silver `#B8B8C0` / Mist `#E6E6EA` text.
  A light theme mirrors it (paper white surfaces, crimson primary, charcoal text).
- **Type:** serif display (Playfair Display if the font files are added, otherwise system serif) for headlines,
  rounded sans (Nunito if added, otherwise system sans) for body text.
- **Shape:** generous rounded corners (16–28dp), pill chips and buttons.
- **Motifs:** crown-cat logo (Canvas-drawn `FelineLogo`), scattered faint paw prints (`PawPattern`),
  crimson gradient scrims on photos, fee badges ("FREE" in crimson, "150 cr" in outline).

## 8. Conventions

- Kotlin official code style; KDoc on every public class and function, with short "why" comments.
- Composables: stateless where possible (`state` in, `onEvent` out), preview functions for components.
- Use Kotlin features where they read well: sealed interfaces for events and results, data classes,
  extension functions (entity ↔ domain mappers), `when` expressions, default arguments.
- No hard-coded user-facing strings in logic. UI strings live in composables/`strings.xml`.
- Tests: `app/src/test` for validators, credit rules, cat/product queries and ViewModels (fake repos);
  `app/src/androidTest` for key Compose flows.

## 9. Build phases

- [x] **Phase 0**: Plan, `CONTEXT.md`, `ASSETS.md`
- [x] **Phase 1**: Gradle project, manifest, theme, branding (logo, paws, launcher icon), Room data layer +
      seed data, repositories, DI, auth (login/register/session), app shell with navigation (other screens
      as branded placeholders), Home screen, domain unit tests (22 passing)
- [ ] **Phase 2**: Adopt (search/filter/sort) + Cat Detail
- [ ] **Phase 3**: BookingActivity + Activity Result + My Bookings + credit holds/refunds
- [ ] **Phase 4**: Shop + Basket + CheckoutActivity + Profile/Wallet (transactions, orders)
- [ ] **Phase 5**: Admin (dashboard, cat CRUD, appointments, products)
- [ ] **Phase 6**: Polish (animations, empty/error states), unit + UI tests, final review

After each phase the user syncs and runs the app in Android Studio on the tablet and reports issues.

## 10. Decision log
- Cats only; brand name **The Feline Co.**; red / gray / black, premium, with cute cat touches.
- Admin is part of the same app, unlocked by an admin login.
- BookingActivity = adoption appointment; CheckoutActivity = shop purchase.
- The adoption fee is charged at booking and refunded on cancel/decline (prevents double-spending credits).
- Photos are provided manually by the user as local drawables (list in `ASSETS.md`); the app falls back to
  branded placeholders.
- No network needed at runtime.
