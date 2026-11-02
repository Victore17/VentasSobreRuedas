# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

"Ventas sobre ruedas": single-module Android app (`:app`, package `victor.mexico.ventassobreruedas`) to register and review daily sales. Jetpack Compose + Material 3, Navigation 3, Firebase Auth (Google sign-in) and Cloud Firestore. UI text, string resources and code comments are in Spanish — keep it that way.

## Commands

```bash
./gradlew :app:assembleDebug                 # build
./gradlew :app:testDebugUnitTest             # unit tests (JVM)
./gradlew :app:testDebugUnitTest --tests "victor.mexico.ventassobreruedas.util.MoneyTest"   # single test class
./gradlew :app:lintDebug                     # Android lint
./gradlew :app:installDebug                  # install on connected device
./gradlew :app:signingReport                 # SHA-1 to register in Firebase
```

The build requires `app/google-services.json`, which is git-ignored; download it from the Firebase console (project `ventassobreruedas` → Project settings → Your apps). It must include the OAuth web client (`client_type 3`), otherwise `R.string.default_web_client_id` is not generated and compilation fails.

## Architecture

- **DI**: manual, in `VentasApplication`. Composables get dependencies through `salesRepository()` / `authRepository()` and pass them to `ViewModel` factories (`XViewModel.factory(...)` companion functions).
- **Auth gating**: `AppNavigation` observes `AuthRepository.observeUser()`; with no user it shows `LoginRoute`, otherwise a `NavDisplay` back stack (`HomeKey`, `SalesKey`, `SalesDaysKey`, `SummaryKey(epochDay)`). Nav keys are `@Serializable` `NavKey`s; dates travel as epoch days.
- **Data**: `SalesRepository` is per-user (`users/{uid}/sales/{saleId}` in Firestore; fields `productName`, `priceCents`, `createdAt` Timestamp). `VentasApplication.salesRepository` throws if there is no signed-in user, so only use it behind the auth gate.
- **Offline**: Firestore's local cache is the offline store. Writes (`addSale`/`updateSale`/`deleteSale`) are intentionally fire-and-forget — never `await()` them or the UI blocks without network. Reads are `snapshots()` flows with `.catch` to avoid crashing `stateIn` on permission errors.
- **Day boundaries**: "a day" is computed with the repository's `Clock` zone (`toTimestampRange`, grouping in `observeSalesDays`); keep both consistent.
- **Money** is always `Long` cents (`util/Money.kt`: `parsePriceToCents`, `formatCents`). Dates are formatted in Spanish regardless of device locale (`util/Dates.kt`).
- **Theme**: fixed turquoise palette in `ui/theme` (dynamic color deliberately disabled). Login header gradient uses `SeaShallow`/`SeaDeep`.

## Gotchas

- Kotlin is 2.3.x because current Firebase/googleid artifacts carry Kotlin 2.4 metadata; Kotlin 2.2 can't read them.
- Firestore security rules (set in the console) only allow `request.auth.uid == uid` on `users/{uid}/sales/**`.

## Lenguaje

- Todo siempre responde en español.

## Cambios en los archivos.

- Cada que modifiques un archivo .kt, corre la app en el dispositivo conectado. Si no hay ninguno físico conectado, entonces no lo hagas.

- No subas screenshots a github al menos que yo te lo pida explicitamente.