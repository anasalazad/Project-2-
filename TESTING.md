# The Feline Co. — Testing Guide

## 1. Automated tests

| Suite | Where | How to run | What it covers |
|---|---|---|---|
| Unit tests (42) | `app/src/test` | Right-click `app/src/test/java` → **Run 'Tests in…'**, or `./gradlew test` | Credit rules, age groups, search/filter/sort, all form validators, Adopt/Booking/Checkout ViewModels (with fake repositories) |
| Database flows | `app/src/androidTest/.../data` | Tablet connected → right-click `app/src/androidTest/java` → **Run** | Seed data, fee held on booking, refund on cancel, reward on adoption, no double booking, checkout charges credits and reduces stock, stock limits |
| Compose UI | `app/src/androidTest/.../ui` | Same as above | Login form shows field and form errors; demo buttons send the right events |

## 2. Manual test script (on the tablet)

Tick each step. The **Brief** column shows which requirement the step demonstrates.

### A. Sign in and style
| # | Steps | Expected | Brief |
|---|---|---|---|
| A1 | Launch the app | Splash with crown-cat logo, then Login (photo panel left, form right) | Styling |
| A2 | Tap **Sign in** with empty fields | "Email is required" / "Password is required" under the fields | Form validation |
| A3 | Enter `demo@thefelineco.com` / `wrong` → Sign in | Red banner "Incorrect email or password" | Error handling |
| A4 | Tap **Create an account** → submit empty | Every field shows an error, plus the terms error | Form validation |
| A5 | Go back → tap **Customer** demo | Home screen with the navigation rail on the left and a 400-credit chip | Navigation Compose |

### B. Adopt (search, filter, sort)
| # | Steps | Expected | Brief |
|---|---|---|---|
| B1 | Rail → **Adopt** | Grid of 24 cats and the filter side panel | Reusable composables |
| B2 | Type `maine` in search | Only Willow and Archie | Search |
| B3 | Clear search → tap **Free to adopt** | Only cats aged 2+, each with a red **FREE** badge | Filtering |
| B4 | Add **Good with: Dogs** and **Coat: Long** | Results narrow; the count text updates ("Showing X of 24") | Dynamic UI |
| B5 | Sort → **Oldest first** | Olive (15 yrs) first | Sorting |
| B6 | Filter to nothing (e.g. Kitten + Senior + Hairless) | "No cats match" and **Clear filters** | Empty state |
| B7 | Home → **Browse free-to-adopt cats** banner | Adopt opens with Free already selected | Navigation args |
| B8 | Tap the ♥ on two cats → tap the **♥ Favourites** chip | Hearts pop red; only those two cats show; the chip count is 2 | Extra feature |
| B9 | Go **Home** | A "Your favourites" row appears; un-hearting removes the cat instantly | Dynamic UI |

### C. Book a meet & greet (second activity)
| # | Steps | Expected | Brief |
|---|---|---|---|
| C1 | Open **Mochi** (kitten) | Profile: fee 150 credits, balance 400, health/compatibility tiles | Navigation Compose |
| C2 | Tap **Book a meet & greet** | **BookingActivity** opens showing Mochi's photo, name and fee (Cat sent as Parcelable) | Multiple activities, Parcelable |
| C3 | Tap **Confirm booking** with nothing filled | Errors for day, time, phone, home and terms, plus a banner | Form validation |
| C4 | Pick a Monday | Mondays show "Closed" and can't be selected | Validation |
| C5 | Toggle **I have other pets** | Warning: Mochi would prefer to be your only pet (updates live) | Dynamic UI |
| C6 | Complete the form → **Confirm booking** | Activity closes; **"You're booked in!"** dialog on the profile (Booking returned) | Activity Result API |
| C7 | **View my bookings** | Booking under Upcoming; header chip now shows 250 credits | State management |
| C8 | Book another cat for the same day and time | That slot shows "Booked" and is disabled | Error prevention |
| C9 | **Cancel booking** → confirm | Snackbar "150 credits refunded"; moves to Past; Mochi available again | Dynamic UI |

### D. Shop, basket and checkout (third activity)
| # | Steps | Expected | Brief |
|---|---|---|---|
| D1 | Rail → **Shop** → category **Toys**, sort **Price: high to low** | Filtered and sorted grid | Filter/sort |
| D2 | Tap a product → set quantity 3 → **Add to basket** | Snackbar; basket badge on the rail shows 3 | Dynamic UI |
| D3 | Try the **Self-Cleaning Slicker Brush** | "Out of stock" badge; add button disabled | Error handling |
| D4 | **Basket** → adjust quantities / remove | Totals and balance-after update instantly | State management |
| D5 | **Checkout** | **CheckoutActivity** opens with the items (ArrayList of Parcelables) | Multiple activities, Parcelable |
| D6 | Submit with Standard delivery and no address | Address, suburb and postcode errors | Form validation |
| D7 | Type letters in postcode | Only digits accepted, max 4 | Input validation |
| D8 | Choose **Click & collect** | Address fields slide away | Dynamic UI |
| D9 | **Place order** | Activity closes; "Order placed!" dialog; basket empty (Order returned) | Activity Result API |
| D10 | **View my orders** | Account → Orders tab lists the order; Wallet shows the purchase | State management |

### E. Account and theme
| # | Steps | Expected | Brief |
|---|---|---|---|
| E1 | Account → switch **Dark theme** off | Whole app (and Booking/Checkout screens) switch to the light theme | Styling |
| E2 | Close and reopen the app | Theme choice and sign-in are remembered | Persistence |

### F. Admin
| # | Steps | Expected | Brief |
|---|---|---|---|
| F1 | Log out → tap **Admin** demo | Dashboard: stats, new requests, low stock | Navigation |
| F2 | Confirm a request from the dashboard | Snackbar; request disappears from the list | Dynamic UI |
| F3 | **Appointments** → Confirmed → **Complete adoption** → confirm | Cat becomes Adopted; the customer gets +250 credits (check by logging in as the customer) | State management |
| F4 | **Cats** → **Add cat** → Save empty | Field errors on the form | Form validation |
| F5 | Type image name `Cat-Luna` | "Use lowercase letters, numbers and _ only" | Validation |
| F6 | Type age 0 years 3 months | Live fee preview shows **150 credits** | Dynamic UI |
| F7 | Fill in and save | Back on the list with a snackbar; the new cat appears for customers too | CRUD |
| F8 | Edit a cat with an active booking → Delete | Blocked: "This cat has an active booking…" | Error handling |
| F9 | **Products** → use the stock stepper | Stock updates; out-of-stock items become available in the shop | Dynamic UI |

### G. Device behaviour
| # | Steps | Expected |
|---|---|---|
| G1 | Rotate the tablet on each screen | Layout adapts; form input and filters are kept |
| G2 | Run on a phone emulator | Bottom navigation bar; filter bottom sheet; stacked layouts |
| G3 | Turn on TalkBack and swipe through a cat card | Reads name, breed, age, sex and fee |

## 3. Reporting issues
When something fails, note the step number (e.g. **C6**), what happened, and any red error text from
Android Studio's **Build** or **Logcat** window.
