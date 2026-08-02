# TMDB

An Android movie browser built on the [TMDB API](https://developer.themoviedb.org/docs) —
browse by category, search as you type, on a layout that adapts from phone to tablet.

## Tech stack

- **UI** — Jetpack Compose, Material 3 (adaptive navigation suite)
- **Navigation** — Navigation 3
- **Pagination** — Paging 3
- **Dependency injection** — Hilt
- **Networking** — Retrofit, OkHttp, kotlinx.serialization
- **Images** — Coil
- **Async** — Coroutines and Flow

## Architecture

A multi-module Gradle build. Dependencies point one way only, and `:app` is a pure sink —
nothing depends on it.

```
:app                       single Activity, app shell, theme, E2E tests
 ├─ :feature:<name>:api     the @Serializable NavKey — the route identity, nothing else
 ├─ :feature:<name>:impl    screen, ViewModel, and one entry() extension
 ├─ :core:ui                shared Compose components (grid, poster, error, search field)
 ├─ :core:navigation        NavigationState, per-tab back stacks, Navigator
 ├─ :core:network           Retrofit service, DTOs, auth interceptor, mock implementation
 ├─ :core:data              MovieRepository, PagingSource, DTO → model mapping
 ├─ :core:common            safeCall, NetworkError, error strings, dispatcher qualifiers
 └─ :core:model             pure-JVM domain model (Movie, MovieCategory)

:domain                    use cases; the only way features reach a repository
:core:testing              unit-test doubles and MainDispatcherRule
:core:data-test            instrumentation doubles and the Hilt test runner
build-logic                convention plugins shared by every module
```

Each feature splits into an `api` module holding only its `NavKey` and an `impl` module holding
the screen. `impl` exposes one public function — its Navigation 3 entry point — and keeps its
composables `internal`, so there are no cross-feature dependencies. Features reach data through
`:domain`, never `:core:data`, so DTOs stay out of the UI layer.

Shared build configuration lives in [`build-logic`](build-logic) as convention plugins.

## Setup

Three secrets are required. **The build fails with a message naming the missing key** rather
than producing an app that breaks at runtime. Add them to `local.properties` in the project
root (already git-ignored):

```properties
BASE_URL=https://api.themoviedb.org/3/
IMAGE_URL=https://image.tmdb.org/t/p/
API_TOKEN=<your TMDB v4 read access token>
```

Get `API_TOKEN` from the [TMDB API settings page](https://www.themoviedb.org/settings/api) —
the **API Read Access Token**

Environment variables of the same name also work and are more convenient for CI;
`local.properties` takes precedence, and a key present but empty counts as unset.

## Build variants

| Flavor | Data source |
|---|---|
| `mock` | Bundled JSON fixtures in `core/network/src/main/assets/` — no network required |
| `prod` | The live TMDB API |

Development and all instrumentation tests run against `mock`, so the suite works offline. The
two variants have different application IDs and can be installed side by side. The build-time
secret check applies to both.

```bash
./gradlew :app:installMockDebug
```

## Testing

```bash
./gradlew testMockDebugUnitTest        # unit tests
./gradlew spotlessCheck                # formatting and unused imports
```

Instrumentation tests need a connected device or emulator:

```bash
./gradlew :app:connectedMockDebugAndroidTest
./gradlew :feature:home:impl:connectedMockDebugAndroidTest
./gradlew :feature:search:impl:connectedMockDebugAndroidTest
```