# The Feline Co.

A premium, cats-only adoption and pet-supply app for Android tablets, built with Kotlin and Jetpack Compose.
Browse cats, book a meet & greet, adopt with credits (cats aged 2+ are free), earn credits, and spend them
on Royal Feline food and toys. Admins manage listings, appointments and products.

- **Plan, architecture and progress:** [`CONTEXT.md`](CONTEXT.md)
- **Photos and fonts to add:** [`ASSETS.md`](ASSETS.md)
- **Course brief:** [`Specifications and requirements.md`](Specifications%20and%20requirements.md)

## Run it

1. Open this folder in **Android Studio** (File → Open → select the `Project-2-` folder).
2. Let Gradle sync. If Android Studio offers to upgrade AGP or Kotlin, you can skip it; the pinned versions work.
3. Pick your Samsung tablet (USB debugging on) or a tablet emulator, then press **Run ▶**.

## Demo accounts

| Role | Email | Password |
|---|---|---|
| Customer (400 credits) | `demo@thefelineco.com` | `Demo123!` |
| Admin | `admin@thefelineco.com` | `Admin123!` |

The login screen also has one-tap **Customer** / **Admin** demo buttons.

## Tests

- Unit tests (rules, filters, validators): right-click `app/src/test` → **Run tests**, or `./gradlew test`.
