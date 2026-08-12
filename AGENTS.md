# SisaRasa

Android app (Kotlin + Jetpack Compose, Material 3) to reduce food waste: connect people/merchants sharing surplus food. Single module `:app`, package `com.mobile.sisarasa`.

## Status

Base app built on the Compose template: Firebase-shaped mock backend (in-memory local repos), Indonesian UI, bottom nav (Beranda/Riwayat/Profil), feed with Gratis/Diskon filters, post CRUD + claim/sold-out states, pickup location (embedded Maps if key set, else "Buka di Google Maps"). Optionally runs on **real Firebase** via `secrets.properties`. The real spec is `PreFP_Android_452024611059_MuhammadAdrianoSakharahman.pdf` — keep new work aligned to it.

## Commands (run from project root with `./gradlew`)

- Build debug: `./gradlew assembleDebug`
- Unit tests (`app/src/test`): `./gradlew testDebugUnitTest`
- Lint: `./gradlew lint`
- Instrumented tests (`app/src/androidTest`): `./gradlew connectedDebugAndroidTest` (needs a device/emulator)

## Build details

- compileSdk 37 (AGP 9 DSL `release(37)`), minSdk 24, targetSdk 36; Java 11 source/target; Gradle 9.5 with JVM toolchain 21; Kotlin 2.2.10; Compose BOM 2026.02.01. (compileSdk must stay ≥37: lifecycle 2.11 and navigation 2.9 require it.)
- `gradle.properties` sets `org.gradle.configuration-cache=true`.
- Dependency versions live in `gradle/libs.versions.toml` — add deps there, not inline in `app/build.gradle.kts`.
- `local.properties` (SDK path) is machine-specific and gitignored; never commit changes to it.

## Setup / secrets

App config comes from root `secrets.properties` (gitignored; copy `secrets.properties.example`). Both keys feed `BuildConfig`:

- `MAPS_API_KEY` — Google Maps SDK key. Empty → pickup location falls back to a "Buka di Google Maps" deep link; non-empty → embedded `MapView` in `PickupLocationCard`. Also injected as the manifest `com.google.android.geo.API_KEY` meta-data. **The Google public "Maps Demo Key" (`AIzaSyADsD6DFp7ovc_4RrYkdOfBob-i60yMhNU`) does NOT support Maps SDK for Android** (JS API + a few web services only), so it's treated as unset and the deep-link fallback is used. A real key needs Maps SDK for Android enabled + billing + package/SHA-1 restriction.
- `FIREBASE_ENABLED=true` — switch data layer to real Firebase. **Requires also dropping a Firebase `google-services.json` into `:app/`**; the `google-services` plugin auto-applies when that file exists (declared `apply false` in root `build.gradle.kts`). Without the json the app builds AND runs on the in-memory mock backend, so keep the file committed-less until ready.

Architecture: `di/AppContainer` picks `Local*` vs `Firebase*` repository impls by `BuildConfig.FIREBASE_ENABLED` + whether `FirebaseApp.initializeApp` succeeds. Same `data/repository` interfaces back both.

## Gotchas

- The git repo root is `/home/mcqeems` (the whole home directory), not this project. `git status`/`git diff` show unrelated home-dir files; scope diffs to `Documents/Project/SisaRasa`.
- Template naming is mostly still default: `rootProject.name` = "My Application", theme `MyApplicationTheme` (app label `@string/app_name` is already "SisaRasa"). Rename the rest when doing real work.
- No CI, no existing instruction files.

## Product spec (what to build; from `PreFP_..._Sakharahman.pdf`)

Reduce food waste: connect people sharing surplus food with recipients wanting free or cheap food. **UI text is Indonesian**.

**User roles** (picked at signup):
- `Personal` — regular users (e.g. students) donating food for free.
- `Mitra Bisnis` — hotels/restos/cafes selling surplus food at a discount.

**Core features** (bottom nav: `Beranda` / `Riwayat` / `Profil`):
1. **Auth** — Firebase Auth (email/password), role chosen at signup.
2. **Two post types**, full CRUD, sharing one post model:
   - *Donasi* — personal users give food away free.
   - *Flash Rescue* — merchants sell surplus at 50–70% off, near closing time.
3. **Feed filter tab** — show only `Gratis` (free) or `Diskon` (discounted) posts.
4. **Maps** — Google Maps pickup location per post (merchant or donor address).
5. **Real-time status** — post `status` (available / claimed / sold out) updates live on all devices via Realtime Database listeners. Post fields: photo, name, price, location, status.
6. **Riwayat** — history of claimed/picked-up posts.

**No in-app payment** — price/status is informational; money changes hands at pickup.

## Stack decisions (PDF vs template — locked)

The PDF predates this template and names Fragments/RecyclerView/Glide. **Build in Compose; do not follow the PDF's View-stack literally**:
- Screens → single-activity Compose; each screen a `@Composable`.
- RecyclerView + CardView → `LazyColumn` + Material 3 `Card`.
- Glide → Coil (Compose-native image loading); food photos via Firebase Storage.
- Backend → Firebase Realtime Database (posts) + Firebase Auth (accounts).
- Maps → Google Maps SDK; requires a Maps API key and `INTERNET` + `ACCESS_FINE_LOCATION` permissions.
- Adding Firebase needs the `google-services` plugin + a Firebase `google-services.json` in `:app` (not present yet).
