# Apply Progress: desktop-two-pane-port

Mode: Standard for unit 1; Strict TDD from unit 2 (flipped on at the end of unit 1). Store: hybrid. Delivery: exception-ok, size exception accepted.

## Unit 1: scaffold (DONE, tasks 1.1-1.8)

- [x] 1.1 settings/build/gradle.properties, Gradle wrapper 9.8.0 (sha256 pinned)
- [x] 1.2 `gradle/libs.versions.toml`, all pins verified
- [x] 1.3 kotlin-test, coroutines-test, `SmokeTest.smoke_test_runs`
- [x] 1.4 compose.desktop.application, Dmg/Msi/Deb (icons deferred, none shipped yet)
- [x] 1.5 `.github/workflows/build.yml` (macos/windows/ubuntu, Temurin 21)
- [x] 1.6 `Main.kt`, `ui/AppWindow.kt` (weights 2f/1f, 1440x860 default, 1200x700 min)
- [x] 1.7 `strict_tdd: true`, `testing.runner` updated, `.gitignore`
- [x] 1.8 Commit `chore: scaffold KMP desktop project with CI and two-pane shell`

### Verified toolchain pins

| Item | Pin | Verification |
|------|-----|--------------|
| Kotlin | 2.4.20 | Maven Central metadata lists 2.4.20 (stable) |
| Compose Multiplatform | 1.12.1 | Maven Central plugin metadata and marker dir exist |
| compose material3 | 1.9.0 | Maven Central; latest stable, plugin default; versioned separately from CMP |
| Room | 2.8.5 | Google Maven metadata lists 2.8.5 |
| sqlite-bundled | 2.6.2 | room-runtime 2.8.5 POM depends on androidx.sqlite 2.6.2 (2.7.x exists but is not Room's) |
| KSP | 2.3.12 | Maven Central, latest stable, plugin marker exists; independent versioning (KSP2) |
| coroutines core/test/swing | 1.11.0 | Maven Central metadata (swing too) |
| serialization-json | 1.11.0 | Maven Central metadata |
| Gradle | 9.8.0 | services.gradle.org current stable, zip sha256 matches published checksum |
| JDK | toolchain 17 | local Temurin 17.0.19 used; CI Temurin 21 |

Room, KSP, sqlite and serialization are catalog entries only (not applied until units 3/4).

### Work Unit Evidence

| Evidence | Value |
|---|---|
| Focused test command | `./gradlew test compileKotlinDesktop`: BUILD SUCCESSFUL; SmokeTest[desktop] tests=1 skipped=0 failures=0 |
| Runtime harness | `./gradlew packageDistributionForCurrentOS`: BUILD SUCCESSFUL, produced `BingoFF-1.0.0.dmg` (macOS). Window not launched (GUI would block). |
| Rollback boundary | Revert the unit 1 commit; leaves openspec planning docs only |

### Deviations / notes

- KMP has no lifecycle `test` task, so `build.gradle.kts` registers `test` as an alias of `desktopTest`.
- Icons (task 1.4) not added: no assets exist; jpackage uses its default icon. Add later with `iconFile`.
- material3 pinned explicitly because the `compose.material3` accessor is deprecated.

## Unit 2: domain port (DONE, tasks 2.1-2.8, Strict TDD)

- [x] 2.1 RED: `BingoLetterTest`, `BoardCardTest`, `GameModeTest` (commonTest/domain/model)
- [x] 2.2 GREEN: `BingoLetter`, `BoardCard`, `GameMode`, `GridPosition`, `WinPattern` ported verbatim
- [x] 2.3 RED: `BingoWinCheckerTest`, `GameSessionTest`
- [x] 2.4 GREEN: `BingoWinChecker`, `WinAnnouncement`, `AnnouncedWin`, `GameSession`
- [x] 2.5 RED: `WinPredictionTest` (13 ported + 1 tie-break triangulation test)
- [x] 2.6 GREEN: `WinPrediction.kt` (`predictPossibleWinners`, `PredictionCandidate`); RED+GREEN `ReplayTest` and `Replay.kt` (`replay(mode, called, boards)`)
- [x] 2.7 Ports: `BoardRepository`, `ActiveGameRepository` (+ `ActiveGame`), `ThemeRepository` (+ `ThemeMode`), `ImportResult`
- [x] 2.8 Commit `feat(domain): port bingo domain model, win checker and prediction`

### TDD Cycle Evidence (unit 2)

| Task | Test File | Layer | Safety Net | RED | GREEN | TRIANGULATE | REFACTOR |
|------|-----------|-------|------------|-----|-------|-------------|----------|
| 2.1/2.2 | `domain/model/{BingoLetter,BoardCard,GameMode}Test.kt` | Unit | N/A (new) | Written; `desktopTest` failed: Unresolved reference 'BingoLetter' | 14/14 passed | 12 letter boundaries, 4 cell lookups, 8 mode shapes | None needed (verbatim port) |
| 2.3/2.4 | `domain/game/{BingoWinChecker,GameSession}Test.kt` | Unit | 14/14 | Written; compile failed: Unresolved reference 'BingoWinChecker', 'AnnouncedWin' | 13/13 passed | win/non-win, FREE, announce-once, matchCount cases | Replaced inline FQN `GameMode` with import |
| 2.5 | `domain/game/WinPredictionTest.kt` | Unit | 27/27 | Written; compile failed: Unresolved reference 'predictPossibleWinners' | 14/14 passed | thresholds 2/3/3/3/10 +1 edge, no ceiling, exclusions, ordering incl. tie-break | None needed |
| 2.6 | `domain/game/ReplayTest.kt` | Unit | 41/41 | Written; compile failed: Unresolved reference 'replay' | 5/5 passed | empty, single win, announce-once, ordering across boards, determinism | `replay` extracted from Android `GamePlayViewModel.rebuildSession` as a pure function |
| 2.7 | n/a | Interfaces only | n/a | Triangulation skipped: interfaces/data classes, no logic | compiles; full suite 47/47 | n/a | n/a |

Test count: 1 before (SmokeTest), 47 after (46 new). `./gradlew test`: BUILD SUCCESSFUL.

### Work Unit Evidence (unit 2)

| Evidence | Value |
|---|---|
| Focused test command | `./gradlew test` (JAVA_HOME 17): BUILD SUCCESSFUL; 8 suites, tests=47 skipped=0 failures=0 |
| Runtime harness | N/A: pure logic, no runtime boundary |
| Rollback boundary | `src/commonMain/.../domain`, `src/commonTest/.../domain` (revert the unit 2 commit) |

### Deviations / notes (unit 2)

- Android test names matched tasks.md; `BingoLetterTest` was parameterized (JUnit4) and became a table-driven loop in kotlin.test. Message arguments moved last (kotlin.test order).
- Followed Android CODE over specs: no call ceiling, thresholds COLUMNA<=2, O/L/I<=3, FULL<=10, sorted by missing, boardId, letter ordinal.
- `ThemeRepository` uses the design signature (`val themeMode: Flow<ThemeMode>`, `set(mode)`), not Android's `observeThemeMode/setThemeMode`. `ThemeMode` lives in the same file; unit 8 reuses it.
- `ActiveGame` (design Key Interfaces) lives in `ActiveGameRepository.kt`; `ImportResult` split into its own file.
- Incident: an in-place `sd` call briefly modified 5 Android test imports; restored with `git checkout` on that directory (Android repo clean again, verified `git status`).

## Unit 3: board-management (DONE, tasks 3.1-3.7, Strict TDD)

- [x] 3.1 SPIKE Room KMP + BundledSQLiteDriver: PASSED, boards stay on Room (no JSON fallback)
- [x] 3.2 RED: `RoomBoardRepositoryTest` (8), `AppDirsTest` (5)
- [x] 3.3 GREEN: `BingoDatabase`, `BoardDao`, `BoardEntity`, `IntListConverter`, `RoomBoardRepository`, `AppDirs`, `DuplicateIdentifierException`; schema exported to `schemas/`
- [x] 3.4 RED: `CreateBoardTest` (16), `BoardListTest` (6), `FakeBoardRepository`
- [x] 3.5 GREEN: `BoardsState` (+`BoardsUiState`), `CreateBoardHolder` (+`CreateBoardState`, `CreateBoardErrorReason`, `computeFieldErrors`), `AppContainer`
- [x] 3.6 UI: `BoardsPane` (Adaptive(220dp) grid, 5x5 cards, empty state), `CreateBoardDialog`, `DeleteConfirmDialog`, wired in `AppWindow`/`Main`
- [x] 3.7 Commit `feat(boards): add board create, delete and Room persistence`

### Spike outcome (3.1): Room

| Check | Evidence |
|---|---|
| KSP codegen on desktop target | `add("kspDesktop", room-compiler)`; `BingoDatabase_Impl` generated; schema `schemas/com.sergiodev.bingo.data.local.BingoDatabase/1.json` exported |
| Real DB file round trip | `RoomSpikeTest.room_opens_and_roundtrips_on_desktop` on a temp-dir file, closed and reopened: 1/1 passed (RED first: `Unresolved reference 'sqlite'`) |
| Native lib in packaged app | `./gradlew createDistributable` OK; `BingoFF.app/Contents/app/sqlite-bundled-jvm-2.6.2-*.jar` contains `natives/{osx_arm64,osx_x64,linux_arm64,linux_x64,windows_x64}/libsqliteJni.*`; headless check (`BundledSQLiteDriver().open(":memory:")` on the exact packaged classpath) printed `NATIVE_LOAD_OK` |
| Real app run | `./gradlew run` for 40s: window process started and created `bingoff.db` (+wal/shm) under `~/Library/Application Support/BingoFF` (cleaned up afterwards); no exception in the log |

### TDD Cycle Evidence (unit 3)

| Task | Test File | Layer | Safety Net | RED | GREEN | TRIANGULATE | REFACTOR |
|------|-----------|-------|------------|-----|-------|-------------|----------|
| 3.1 | `data/local/RoomSpikeTest.kt` | Integration (real SQLite file) | 47/47 | Written; `compileTestKotlinDesktop` failed: Unresolved reference 'sqlite' | 1/1 passed | Single: one end-to-end round trip (reopen proves persistence) | None needed |
| 3.2/3.3 | `data/local/RoomBoardRepositoryTest.kt` | Integration (real SQLite file) | 48/48 | Written; compile failed: Unresolved reference 'AppDirs' / 'DuplicateIdentifierException' | 8/8 passed | add ok vs duplicate, restart ordering with a deleted id, delete known vs unknown, import dedup (existing id, existing identifier, in-batch id, in-batch identifier) and id preservation, DAO throws `androidx.sqlite.SQLiteException` (backstop type) | None needed |
| 3.3 | `data/file/AppDirsTest.kt` | Unit | 48/48 | Written; compile failed: Unresolved reference 'AppDirs' | 5/5 passed | mac, windows, linux xdg, linux unset and empty xdg, db file | None needed |
| 3.4/3.5 | `presentation/CreateBoardTest.kt` | Unit | 61/61 | Written; compile failed: Unresolved reference 'CreateBoardHolder' | 16/16 passed | 6 pure `computeFieldErrors` cases, valid/blank/duplicate/out-of-range/blank-field submits, edit-after-failed-submit flag rules, error clearing | Renamed a misleading test (letter ranges are disjoint, so duplicates sit in one column) |
| 3.4/3.5 | `presentation/BoardListTest.kt` | Unit | 61/61 | Written; compile failed: Unresolved reference 'BoardsState' | 6/6 passed | initial, flow follow, request, cancel, confirm, confirm without pending | None needed |
| 3.6 | n/a | UI (Compose) | n/a | Triangulation skipped: pure rendering, no logic; logic lives in tested holders | compiles; manual run | n/a | n/a |

Test count: 47 before, 83 after (36 new: 1 spike + 8 repository + 5 AppDirs + 16 create + 6 list). `./gradlew test`: BUILD SUCCESSFUL.

### Work Unit Evidence (unit 3)

| Evidence | Value |
|---|---|
| Focused test command | `./gradlew test` (JAVA_HOME 17): BUILD SUCCESSFUL; tests=83 skipped=0 failures=0 |
| Runtime harness | `./gradlew run` 40s: app started, Room DB file created, no exception; `createDistributable` image holds the native lib and a headless load on its classpath succeeded. Interactive create/delete/restart through the window not exercised (no GUI driver). |
| Rollback boundary | `data/local`, `data/file/AppDirs`, `presentation/{BoardsState,CreateBoardHolder}`, `di/`, `ui/{BoardsPane,CreateBoardDialog,DeleteConfirmDialog}`, `schemas/`, the Room/KSP block in `build.gradle.kts` (revert the unit 3 commit) |

### Deviations / notes (unit 3)

- Duplicate identifier: transactional `BoardDao.insertIfAbsent` returns null, repository maps to `Result.failure(DuplicateIdentifierException)`; `androidx.sqlite.SQLiteException` caught as backstop. New `DuplicateIdentifierException` in `domain/repository` (commonMain), so holders can stay platform free. The `BoardRepository` interface is unchanged.
- DAO also has `@Transaction importNew` (check-and-insert in one transaction) instead of Android's prefetch outside a transaction; behaviour identical, atomic.
- No `@ConstructedBy`/expect object: the JVM-only target uses Room's reflective `BingoDatabase_Impl` lookup, verified by the tests.
- `computeFieldErrors` is `internal` in commonMain; the Android "duplicate across columns" scenario cannot occur literally because letter ranges are disjoint (duplicates are same-column); tests cover the same-column case.
- UI strings are hard-coded Spanish (app default locale) pending unit 9. Delete action is a text button (no material-icons dependency).
- Risk: jlinked runtime in the package lists modules `java.base java.datatransfer java.xml java.prefs java.desktop java.logging jdk.crypto.ec`; the SQLite JNI only needs `java.base`, but a packaged-launcher GUI run was not done in this unit (verify in unit 10).
- The unit 3 tasks 3.2 and 3.4 each list scenario names; the five spec scenarios of `import_skips_dups` were split across two tests.

## Unit 4: board-export-import (DONE, tasks 4.1-4.6, Strict TDD)

- [x] 4.1 RED: `data/json/BoardJsonCodecTest` (10, commonTest), `AndroidExportFixtureTest` (2, desktopTest, reads `src/commonTest/resources/android-export-sample.json`)
- [x] 4.2 GREEN: `data/json/BoardJsonCodec` (+ internal `BoardDto`), `ignoreUnknownKeys = true`; `kotlin-serialization` plugin and `serialization-json` added to commonMain
- [x] 4.3 RED: `presentation/ImportExportHolderTest` (17) with `FakeFileDialogs`, `FakeTextFiles`, `FakeClipboard` (`FakePlatform.kt`); `FakeBoardRepository.importBoards` now implements the real dedup contract
- [x] 4.4 GREEN: `platform/{FileDialogs,TextFiles,Clipboard}` interfaces (commonMain), `presentation/ImportExportHolder` (+ `ImportExportState`, `ImportErrorReason`, `ExportOutcome`); desktopMain `platform/{AwtFileDialogs,JvmTextFiles,AwtClipboard}` (dialogs on `Dispatchers.Main`); `AppContainer.importExportHolder`, `dialogOwner` set from `Main`
- [x] 4.5 UI: `ui/AppMenu` (top bar overflow with 4 entries), `ui/ImportDialog` (paste dialog + summary/error/export notices), wired in `AppWindow`
- [x] 4.6 Commit `feat(boards): add JSON export and import with Android-compatible format`

### TDD Cycle Evidence (unit 4)

| Task | Test File | Layer | Safety Net | RED | GREEN | TRIANGULATE | REFACTOR |
|------|-----------|-------|------------|-----|-------|-------------|----------|
| 4.1/4.2 | `data/json/BoardJsonCodecTest.kt` | Unit | 83/83 (unit 3 baseline) | Written; `compileTestKotlinDesktop` failed: Unresolved reference 'BoardJsonCodec' | 10/10 passed | round trip (2 boards), empty, exact bare-array string, malformed, missing identifier, one entry missing numbers rejects all, non-array root, wrong-typed numbers/identifier, unknown keys, Gson-shaped sample with `\u003d`/`\u003c` escapes | None needed |
| 4.1 | `data/json/AndroidExportFixtureTest.kt` | Unit (resource file) | 93/93 | Written alongside; codec already existed, so passes on first run (fixture coverage, not new behavior) | 2/2 passed | file decode with escapes, re-encode round trip | None needed |
| 4.3/4.4 | `presentation/ImportExportHolderTest.kt` | Unit with fakes | 95/95 | Written; compile failed: Unresolved reference 'platform' / 'FileDialogs' / 'TextFiles' / 'Clipboard' | 17/17 passed | paste: blank, invalid, missing-numbers all-or-nothing, mixed payload imported=1 skipped=3, summary cleared, error cleared on edit; file: valid, cancelled (state unchanged, 0 import calls), unreadable, non-board content, blank file; export: path + default name, cancelled (0 writes), write failure, clipboard, outcome cleared, export-then-import round trip | None needed |
| 4.5 | n/a | UI (Compose) | n/a | Triangulation skipped: pure rendering, logic lives in the tested holder | compiles; `./gradlew run` started the window, created the DB, no exception | n/a | n/a |

Test count: 83 before, 112 after (29 new: 10 codec + 2 fixture + 17 holder). `./gradlew test`: BUILD SUCCESSFUL.

### Work Unit Evidence (unit 4)

| Evidence | Value |
|---|---|
| Focused test command | `./gradlew test` (JAVA_HOME 17): BUILD SUCCESSFUL; tests=112 skipped=0 failures=0 (codec 10, fixture 2, holder 17) |
| Runtime harness | `./gradlew run` ~50s then SIGTERM: app started and opened the Room DB with no exception. AWT open/save dialogs, clipboard and the paste dialog were NOT exercised (no GUI driver); manual check pending, esp. Linux GTK `FileDialog` and Windows `*.json` pattern |
| Rollback boundary | `data/json`, `platform/`, `presentation/ImportExportHolder`, `ui/{AppMenu,ImportDialog}`, `AppWindow`/`AppContainer`/`Main` edits, serialization lines in `build.gradle.kts`, `FakeBoardRepository.importBoards`, `commonTest/resources` (revert the unit 4 commit) |

### Deviations / notes (unit 4)

- Interfaces live in commonMain `platform/` (the holder needs them); only the AWT/NIO implementations are in desktopMain. Added a `TextFiles` port (not in design) so the holder does the file I/O without `java.*` in commonMain. `FileDialogs` returns `String` paths (not `java.nio.file.Path`) for the same reason.
- Import semantics follow the Android code: skip id OR identifier already stored, in-batch duplicates keep the first, malformed JSON rejects everything. Dedup is reused from the atomic `importBoards`/`importNew` of unit 3 (no new repository code).
- Strictness difference: kotlinx (non-lenient) rejects what Gson tolerates (quoted numbers, unquoted keys); anything Gson writes is valid strict JSON, so Android exports always import. Gson-escaped `\u003d` style characters decode correctly.
- Pasted text is cleared on every submit (Android behavior); import errors for file imports show in a notice dialog, for pasted imports inline in the paste dialog. Cancel is silent in both flows.
- Entry point is a top bar with an overflow "Menu" in `AppWindow` (DropdownMenu), not a native MenuBar, since `AppWindow` has no window scope; revisit in unit 10 polish.
- Export always writes (an empty list yields `[]`); no `.json` extension is forced on the chosen save name.
- The add-board dialog misalignment reported by the user is deferred; the import dialog reuses the same AlertDialog title/body/buttons layout.
- UI strings hard-coded Spanish pending unit 9.
- `~/Library/Application Support/BingoFF` was removed after the smoke run.

## Unit 5: game-session (DONE, tasks 5.1-5.5, Strict TDD)

- [x] 5.1 RED: `desktopTest/data/file/FileStoresTest` (15), `commonTest/presentation/GameSessionHolderTest` (17, includes the rewritten Android `GameSetupViewModelTest` cases and restart tests), `InMemoryActiveGameRepository` fake
- [x] 5.2 GREEN: `data/file/{JsonFileStore,FileActiveGameRepository}` (desktopMain), `presentation/GameSessionHolder` (+ `GameSetupUiState`), `presentation/ShellState` (+ `RightPaneDestination`), `AppDirs.activeGameFile`
- [x] 5.3 RED then GREEN: `commonTest/presentation/ShellStateTest` (3): clean launch Setup, restored Play, start then end
- [x] 5.4 UI: `ui/{SetupPane,EndGameDialog,PlayPane}`; `AppWindow` right pane switches on `RightPaneDestination` (blank until the stored game is read); `AppContainer` wires `gameSession` and `shellState`
- [x] 5.5 Commit `feat(session): add game setup and persisted single active game`

### TDD Cycle Evidence (unit 5)

| Task | Test File | Layer | Safety Net | RED | GREEN | TRIANGULATE | REFACTOR |
|------|-----------|-------|------------|-----|-------|-------------|----------|
| 5.1/5.2 | `data/file/FileStoresTest.kt` | Integration (real temp-dir files) | 112/112 (unit 4 baseline) | Written; compile failed: Unresolved reference 'JsonFileStore' / 'FileActiveGameRepository' / 'GameSessionHolder' | 15/15 passed (after one test-harness fix, see notes) | store: save/load, missing, corrupt, wrong shape, overwrite with no temp left, nested dirs, clear; repo: round trip with order and dismissed letters, `"version": 1` on disk, corrupt, unknown version, unknown mode, clear, overwrite; real-file restart through the holder | Return types of `save`/`clear` pinned to `Unit` |
| 5.1/5.2 | `presentation/GameSessionHolderTest.kt` | Unit with fakes | 112/112 | Written; compile failed: Unresolved reference 'GameSessionHolder' / 'setup' / 'selectMode' / 'loaded' | 17/17 passed | zero boards vs one board (blocked, allowed, blocked again after delete), 5 modes, COLUMNA default, start (L, CARTON_COMPLETO), start ignored with no boards and while active, end clears store and keeps boards, new game after end, restart restores (same store), calls order and dismissed letters restored, replay rebuilds winners and announced, no winners case, ended game not restored | None needed |
| 5.3 | `presentation/ShellStateTest.kt` | Unit with fakes | 112/112 | Written; compile failed: Unresolved reference 'ShellState' / 'RightPaneDestination' | 3/3 passed | clean launch Setup vs restored Play vs start-then-end transitions | None needed |
| 5.2 | `data/file/AppDirsTest.kt` (+1) | Unit | 147/147 | Written after the property was added (trivial accessor), so it passed on first run | 148/148 | Single | n/a |
| 5.4 | n/a | UI (Compose) | n/a | Triangulation skipped: pure rendering, logic lives in the tested holders | compiles; `./gradlew run` ~50s, no exception | n/a | n/a |

Test count: 112 before, 148 after (36 new: 15 stores + 17 holder + 3 shell + 1 AppDirs). `./gradlew test --rerun-tasks` ran twice, BUILD SUCCESSFUL both times (no flakiness).

### Work Unit Evidence (unit 5)

| Evidence | Value |
|---|---|
| Focused test command | `./gradlew test --rerun-tasks` (JAVA_HOME 17): BUILD SUCCESSFUL x2; tests=148 skipped=0 failures=0 |
| Runtime harness | `./gradlew run` ~50s then SIGTERM: window process started, Room DB created, no exception. Start, quit and relaunch through the window NOT exercised (no GUI driver); persistence is covered by the real-file restart test. The created `~/Library/Application Support/BingoFF` was removed afterwards (it did not exist before) |
| Rollback boundary | `data/file/{JsonFileStore,FileActiveGameRepository}`, `AppDirs.activeGameFile`, `presentation/{GameSessionHolder,ShellState}`, `ui/{SetupPane,EndGameDialog,PlayPane}`, the right-pane edits in `AppWindow`/`AppContainer` (revert the unit 5 commit) |

### Deviations / notes (unit 5)

- `GameSessionHolder` takes a `BoardRepository` (design says `boards`) and exposes `active`, `loaded`, `setup` (the Android `GameSetupUiState`, incl. `canStart`), `session` (replayed `GameSession` with winners and announced), `selectMode`, `start(mode)`, `end()`. `call`/`toggleDismiss` are deferred to unit 6 (6.2). `start` is a silent no-op when a game is active or no board exists.
- Added `loaded` (not in design) so the right pane stays blank until the stored game is read, avoiding a Setup flash before a restored game opens Play. `ShellState(session, scope)` derives the destination from `session.active`.
- Repository calls run through a `Mutex` in call order; storage exceptions are swallowed (the game keeps working in memory, persistence is lost) so a bad disk never crashes the app. A `start` issued before the initial load finishes wins over the stored game.
- `ActiveGameDto` stores `version`, `mode`, `calledNumbers`, `dismissedLetters` (sorted); `JsonFileStore` is generic and version-agnostic, the repository rejects `version != 1`. Unknown mode names fail decoding and therefore count as no game. `JsonFileStore` uses a sibling `.tmp` plus `ATOMIC_MOVE` (falls back to a plain replace move).
- The real-file restart test uses real dispatchers (the store does IO on `Dispatchers.IO`), found when the first version with `runCurrent` failed; the failure was in the test harness, not production code.
- `PlayPane` is a placeholder (mode, call count, confirmed End game) until unit 6; it makes the Setup to Play to Setup loop usable now. Mode chip labels match Android (Columna, O, L, I, Completo), selected chip uses Android's Success green `#2E7D32`. UI strings hard-coded Spanish pending unit 9.
- Manual check pending: click Jugar, quit, relaunch to land in Play; Terminar juego returns to Setup.

## Unit 6: game-play (DONE, tasks 6.1-6.6, Strict TDD)

- [x] 6.1 RED: `commonTest/presentation/GamePlayHolderTest` (26), `NumberInputTest` (4)
- [x] 6.2 GREEN: `presentation/GamePlayHolder` (+ `GamePlayUiState`, `GamePlayInputError`), `GameSessionHolder.call(n): Boolean` and `toggleDismiss(letter)`, `presentation/NumberInput.kt` (`sanitizeNumberInput`)
- [x] 6.3 RED then GREEN: ported `BingoNumberFieldTest` (as `NumberInputTest`, sanitizer moved to commonMain) and `CreateBoardFocusOrderTest` (2, `flatFieldIndex` moved to commonMain `presentation/FocusOrder.kt`); `CreateBoardDialog` now uses both
- [x] 6.4 RED then GREEN: `commonTest/presentation/BoardsPaneStateTest` (13), `presentation/BoardsPaneState` (+ `@Immutable BoardCardState`, `CellState`)
- [x] 6.5 UI: `ui/PlayPane` (real pane: input + letter chips + Enter submit, announcements, per-letter called grid with per-row toggle and right-click dismiss in COLUMNA, confirmed end game), `ui/PatternLabels`, marked cells and winner highlight in `ui/BoardsPane`, wiring in `AppContainer`/`AppWindow`
- [x] 6.6 Commit `feat(play): add number calling, live board marks and win announcements`

### TDD Cycle Evidence (unit 6)

| Task | Test File | Layer | Safety Net | RED | GREEN | TRIANGULATE | REFACTOR |
|------|-----------|-------|------------|-----|-------|-------------|----------|
| 6.1/6.2 | `presentation/GamePlayHolderTest.kt` | Unit with fakes | 148/148 | Written; compile failed: Unresolved reference 'GamePlayHolder' | 26/26 passed (full suite 178/178) | derived letter (47 G, re-derive 4/42/80, override kept), accept vs override-match, InvalidNumber (80, blank, 0, abc, 76), LetterMismatch, DuplicateCall(47) clears input, error cleared on edit and on letter select, no game ignored, end game resets entry, grouping 47,3,52 and five empty rows, announce once with sequentialNumber = board id, second board without re-announcing first, restart replay, toggle twice persisted, dismissed survives restart, dismissed still wins, toggle ignored outside COLUMNA and without a game, `call` true/false and ordering persisted | `start`, `call` and `toggleDismiss` in `GameSessionHolder` share one private `update()` (set state, then save) |
| 6.3 | `presentation/NumberInputTest.kt` | Unit | 148/148 | Written with 6.1; compile failed: Unresolved reference 'sanitizeNumberInput' | 4/4 passed | 8 Android table cases, paste truncation, idempotence, non-ASCII digits rejected | None needed |
| 6.3 | `presentation/CreateBoardFocusOrderTest.kt` | Unit | 178/178 | Written together with the extraction of `flatFieldIndex`; the function was moved from the dialog, so the RED was by construction (compile error before `FocusOrder.kt` was added was not separately executed) | 2/2 passed | all 24 fields + 3 boundaries | `CreateBoardDialog` uses the shared function |
| 6.4 | `presentation/BoardsPaneStateTest.kt` | Unit with fakes | 182/182 | Written; compile failed: Unresolved reference 'BoardsPaneState' | 13/13 passed | no game, row-major layout with FREE, marks = called and on board, FREE not a mark, call on no board, winner ids per board, winner persists, dismissed letter still wins, end clears, restore rebuilds, add/delete boards, mode L, unaffected cards equal | None needed |
| 6.5 | n/a | UI (Compose) | n/a | Triangulation skipped: pure rendering, logic lives in tested holders | compiles; `./gradlew run` 50s, no exception | n/a | n/a |

Test count: 148 before, 193 after (45 new: 26 + 4 + 2 + 13). `./gradlew test --rerun-tasks`: BUILD SUCCESSFUL.

### Work Unit Evidence (unit 6)

| Evidence | Value |
|---|---|
| Focused test command | `./gradlew test --rerun-tasks` (JAVA_HOME 17): BUILD SUCCESSFUL; tests=193 skipped=0 failures=0 |
| Runtime harness | `./gradlew run` 50s then killed: window process started, no exception in the log. Typing, right-click dismiss, live marks and winner highlight NOT exercised (no GUI driver). Created `~/Library/Application Support/BingoFF` removed (it did not exist before) |
| Rollback boundary | `presentation/{GamePlayHolder,BoardsPaneState,NumberInput,FocusOrder}`, `GameSessionHolder.call/toggleDismiss`, `ui/{PlayPane,PatternLabels,BoardsPane,CreateBoardDialog}`, `AppContainer`/`AppWindow` wiring, `compose.runtime` in commonMain deps (revert the unit 6 commit) |

### Deviations / notes (unit 6)

- `commonMain` now depends on `compose.runtime` solely for `@Immutable` (design asks for `@Immutable BoardCardState`).
- Error reasons are `GamePlayInputError` (InvalidNumber, LetterMismatch, DuplicateCall(n)), same shape as Android's `GamePlayInputErrorReason`.
- `GameSessionHolder.call` returns `Boolean` (false on duplicate/no game); range validation stays in `GamePlayHolder`. `toggleDismiss` is a no-op outside COLUMNA and without a game. Dismissed letters only live in the persisted `ActiveGame`; predictions that use them arrive in unit 7.
- `BoardsPaneState(boards, session, scope)` takes `BoardRepository` and `GameSessionHolder`, combines `observeBoards()` with `session.active` and replays itself (consistent snapshot); cells are row-major, FREE is `number = null, marked = false`. `winningPatternIds` accumulate for the whole game (a winner stays highlighted). No `missing` badge yet (unit 7).
- `GamePlayHolder` clears the half-typed entry when the game ends.
- Android "possible winners" and swipe-to-dismiss confirmation dialog are not ported here: dismissal is a per-row button plus right-click without confirmation (reversible), prediction list is unit 7.
- Replaced `Char::isDigit` in `CreateBoardDialog` by `sanitizeNumberInput` (ASCII only), matching Android's field.
- Win announcement list in the Play pane is newest first and shows `#id identifier · pattern`; labels are hard-coded Spanish pending unit 9.
- Manual check pending: type 47 and Enter, duplicate clears the field, right-click a row in COLUMNA, winner card highlight on the left, quit/relaunch keeps calls and dismissed rows.

## Unit 7: win-prediction (DONE, tasks 7.1-7.4, Strict TDD)

- [x] 7.1 RED: `BoardsPaneStateTest` +10 (badge), new `PlayPaneStateTest` (10)
- [x] 7.2 GREEN: `BoardCardState.missing: Int?` in `BoardsPaneState.kt`; `presentation/PossibleWinners.kt` (`visiblePossibleWinners`), `presentation/PlayPaneState.kt` (`possibleWinners` flow)
- [x] 7.3 UI: `NearWinPill` ("Faltan N") in the card header of `BoardsPane.kt`; `PossibleWinners` list with per-row "Cerrar" and right-click dismiss (COLUMNA rows) in `PlayPane.kt`; `AppContainer.playPaneState` wiring
- [x] 7.4 Commit `feat(prediction): show possible winners and near-win badges`

### TDD Cycle Evidence (unit 7)

| Task | Test File | Layer | Safety Net | RED | GREEN | TRIANGULATE | REFACTOR |
|------|-----------|-------|------------|-----|-------|-------------|----------|
| 7.1/7.2 | `presentation/BoardsPaneStateTest.kt` | Unit with fakes | 13/13 (suite 193/193) | Written; compile failed: Unresolved reference 'missing' on BoardCardState | 23/23 passed | no game, missing=2 after 3 calls (not at 2), closest column and shrinking to 1, stays after 19 unrelated calls (no ceiling), announced B excluded, winner keeps a badge for a different column, dismissed G filtered and reopened, dismissing G keeps B badge, mode L threshold 3, restore rebuilds | One shared pure `visiblePossibleWinners` used by both panes |
| 7.1/7.2 | `presentation/PlayPaneStateTest.kt` | Unit with fakes | N/A (new) | Written; compile failed: Unresolved reference 'PlayPaneState' | 10/10 passed | no game, ordering (board2 B 1; board1 B 2; board1 G 2), live update 2 then 1, announced dropped, dismiss filter and reopen, mode I letter null (missing 2), FULL threshold 10, restore, restored dismissed letter filtered, end clears | None needed |
| 7.3 | n/a | UI (Compose) | n/a | Triangulation skipped: pure rendering, logic lives in tested state | compiles | n/a | n/a |

Test count: 193 before, 213 after (20 new: 10 + 10). `./gradlew test --rerun-tasks`: BUILD SUCCESSFUL.

### Work Unit Evidence (unit 7)

| Evidence | Value |
|---|---|
| Focused test command | `./gradlew desktopTest --tests '*PlayPaneState*' --tests '*BoardsPaneState*'` BUILD SUCCESSFUL (RED: compile errors first); full `./gradlew test --rerun-tasks` tests=213 failures=0 |
| Runtime harness | Not run (no GUI driver; compile of the UI verified by the full build). Pill and list rendering NOT exercised visually |
| Rollback boundary | `presentation/{PossibleWinners,PlayPaneState,BoardsPaneState}`, `ui/{BoardsPane,PlayPane,AppWindow}`, `di/AppContainer` (revert the unit 7 commit) |

### Deviations / notes (unit 7)

- Domain `predictPossibleWinners` reused unchanged; dismiss filter lives in presentation (`visiblePossibleWinners`), re-evaluated on every emission.
- Badge shows the board's closest candidate (fewest missing). Announced wins are already excluded by the domain, so a winner is never badged for the same pattern, but it may be badged for another near-win pattern (kept; winner highlight stays the stronger signal).
- `BoardCardState.missing` has a default of null so earlier call sites compile.
- Pill text is "Faltan N" (hard-coded Spanish until unit 9); list rows read `#id identifier · LETTER · faltan N`.
- Manual check pending: pill appears at 2 missing and updates live, right-click or Cerrar on a prediction row removes that letter, restart restores badges and list.


## Unit 8: theme-preference (DONE, tasks 8.1-8.4, Strict TDD)

- [x] 8.1 RED: `desktopTest/data/file/FileThemeRepositoryTest` (6), `commonTest/presentation/ThemeHolderTest` (6), `InMemoryThemeRepository` fake, `AppDirsTest` +1
- [x] 8.2 GREEN: `data/file/FileThemeRepository` (`theme.json` via `JsonFileStore`, `ThemeDto` version 1), `presentation/ThemeHolder` (+ `ThemeMode.isDark(systemDark)`), `AppDirs.themeFile`; `ThemeMode`/`ThemeRepository` reused from unit 2
- [x] 8.3 UI: `ui/theme/BingoTheme` (Light/Dark schemes ported from Android `Theme.kt`, no dynamic colour), radio group Claro/Oscuro/Sistema in `AppMenu`, `AppWindow` wraps content in `BingoTheme`, `AppContainer.themeHolder`
- [x] 8.4 Commit `feat(theme): add persisted light, dark and system theme`

### TDD Cycle Evidence (unit 8)

| Task | Test File | Layer | Safety Net | RED | GREEN | TRIANGULATE | REFACTOR |
|------|-----------|-------|------------|-----|-------|-------------|----------|
| 8.1/8.2 | `data/file/FileThemeRepositoryTest.kt` | Integration (real temp-dir file) | 213/213 | Written; compile failed: Unresolved reference 'FileThemeRepository' | 6/6 passed | no file, corrupt, unrecognized value "SEPIA", set emits and persists, restart restores each of the 3 modes, set before first read wins | None needed |
| 8.1/8.2 | `presentation/ThemeHolderTest.kt` | Unit with fake | 213/213 | Written; compile failed: Unresolved reference 'ThemeHolder' / 'isDark' | 6/6 passed | startup default vs stored, set twice with ordered writes, new holder restores, explicit modes ignore OS, SYSTEM follows OS both ways | Test helper simplified to one scope expression |
| 8.2 | `data/file/AppDirsTest.kt` (+1) | Unit | 219/219 | Written after the accessor (trivial), passed on first run | 226/226 | Single | n/a |
| 8.3 | n/a | UI (Compose) | n/a | Triangulation skipped: pure rendering, logic in tested holder | compiles | n/a | n/a |

Test count: 213 before, 226 after (13 new: 6 + 6 + 1). `./gradlew test --rerun-tasks`: BUILD SUCCESSFUL, failures=0.

### Work Unit Evidence (unit 8)

| Evidence | Value |
|---|---|
| Focused test command | `./gradlew desktopTest --tests '*Theme*'` passed (RED: compile errors first); full `./gradlew test --rerun-tasks` tests=226 failures=0 |
| Runtime harness | Not run (no GUI driver); UI verified by compilation only. Visual legibility NOT inspected |
| Rollback boundary | `data/file/{FileThemeRepository,AppDirs}`, `presentation/ThemeHolder`, `ui/theme/BingoTheme`, `ui/{AppMenu,AppWindow}`, `di/AppContainer` (revert the unit 8 commit) |

### Deviations / notes (unit 8)

- `ThemeHolder.set` updates state immediately, then persists asynchronously; storage errors are not surfaced. `FileThemeRepository` reads lazily on first collection; a `set` that races the read wins.
- Palette: only primary/secondary/tertiary are overridden (as in Android); all other roles use Material defaults. Board cells, winner highlight, near-win pill and PlayPane already use `colorScheme` tokens, so they adapt. The Success green chip (`#2E7D32` with white label) is fixed in both themes (contrast ok on light and dark surfaces).
- The theme radio group sits in the existing overflow menu and keeps it open on selection. Labels are hard-coded Spanish pending unit 9.
- Manual check pending: switch Claro/Oscuro/Sistema and see the window re-theme at once; relaunch keeps the choice; with Sistema, toggle the OS appearance; inspect winner card, called cells, "Faltan N" pill and green chip in both themes.

## Unit 9: ui-localization (DONE, tasks 9.1-9.4, Strict TDD)

- [x] 9.1 RED: `desktopTest/i18n/StringsParityTest` (6, parses both real `strings.xml` files: key parity, placeholder parity, non-blank), `LocaleSelectionTest` (2, real CMP lookup with `Locale.setDefault`), `NoHardCodedStringsTest` (2, optional guard), `desktopTest/ui/ReasonMappingTest` (7)
- [x] 9.2 GREEN: `src/commonMain/composeResources/values/strings.xml` (es, default) and `values-en/strings.xml` (72 keys each); `ui/Strings.kt` (`UiText(res, args)`, `resolve()`, `uiText()` mappings for GamePlayInputError, CreateBoardErrorReason, ImportErrorReason, ImportResult, ExportOutcome, GameMode, ThemeMode, `patternUiText`); `compose.components.resources` in commonMain, `compose.resources { packageOfResClass = "com.sergiodev.bingo.resources" }` in `build.gradle.kts`
- [x] 9.3 RED then GREEN: reason mapping tests; every hard-coded literal in `AppMenu`, `BoardsPane`, `CreateBoardDialog`, `DeleteConfirmDialog`, `EndGameDialog`, `ImportDialog`, `PlayPane`, `SetupPane`, `PatternLabels` and the window title in `Main.kt` replaced by `stringResource`
- [x] 9.4 Commit `feat(i18n): add Spanish and English localization`

### TDD Cycle Evidence (unit 9)

| Task | Test File | Layer | Safety Net | RED | GREEN | TRIANGULATE | REFACTOR |
|------|-----------|-------|------------|-----|-------|-------------|----------|
| 9.1/9.2 | `i18n/StringsParityTest.kt` | Integration (real resource files) | 226/226 | Written; test source set failed to compile (Res/compose resources unresolved) | 6/6 passed | key sets, placeholder sets per key, size and `app_name` presence, extraction ordering, no blank values | None needed |
| 9.1/9.2 | `i18n/LocaleSelectionTest.kt` | Integration (real CMP lookup) | 226/226 | Written; compile failed: Unresolved reference 'resources' / 'Res' | 2/2 passed | en-US and en-GB English; fr-FR and es-AR Spanish | None needed |
| 9.3 | `ui/ReasonMappingTest.kt` | Unit over real resources | 226/226 | Written; compile failed: Unresolved reference 'uiText' / 'UiText' | 7/7 passed | DuplicateCall(47) en and DuplicateCall(12) es, three input errors distinct, create-board reasons en/es, import reasons and counts en/es, export outcomes (path included), patterns (column, full card, O, L, I, unknown id), modes and themes | None needed |
| 9.3 | `i18n/NoHardCodedStringsTest.kt` | Static scan | 226/226 | Written after the replacement; the same pattern matches 35 literal lines at HEAD (`git grep`), so it would have failed before. First run on the new code failed on a false positive (`"#${board.id}"`) | 2/2 passed after template expressions are stripped | detector flags 4 literal shapes, accepts resources, symbols, template-only text and comments | Template stripping added |
| 9.3 | n/a | UI (Compose) | n/a | Triangulation skipped: pure rendering, mapping logic covered by `ReasonMappingTest` | compiles | n/a | n/a |

Test count: 226 before, 243 after (17 new: 6 + 2 + 7 + 2). `./gradlew test --rerun-tasks`: BUILD SUCCESSFUL, failures=0.

### Work Unit Evidence (unit 9)

| Evidence | Value |
|---|---|
| Focused test command | `./gradlew desktopTest --tests '*Strings*' --tests '*Locale*' --tests '*ReasonMapping*'`: 15/15 (RED first: compile errors); full `./gradlew test --rerun-tasks` tests=243 failures=0 |
| Runtime harness | Resource lookup exercised for real by `LocaleSelectionTest` and `ReasonMappingTest` (classpath resources, `getSystemResourceEnvironment()` from the JVM default locale). Window with `-Duser.language=en` NOT launched (no GUI driver) |
| Rollback boundary | `src/commonMain/composeResources/`, `ui/Strings.kt`, the string edits in `ui/*` and `Main.kt`, `compose.components.resources` and `compose.resources` in `build.gradle.kts`, `desktopTest/{i18n,ui}` (revert the unit 9 commit) |

### Deviations / notes (unit 9)

- Resources live in `src/commonMain/composeResources` (as instructed), not `src/desktopMain/composeResources` as the alias `R` in tasks.md says. `ui/Strings.kt` holds `UiText` plus the pure reason-to-resource functions (testable without Compose).
- Locale selection is CMP's own: `getSystemResourceEnvironment()` reads `Locale.getDefault()`; a `values-en` qualifier matches any English region and everything else falls back to the default `values` (Spanish), which is exactly the spec rule. No custom code. Verified with en-US, en-GB (English) and fr-FR, es-AR (Spanish).
- Android keys reused where they apply (`game_play_*`, `import_boards_*`, `theme_*`, `create_board_*`, `board_list_delete_*`); where the shipped desktop copy differs, the desktop wording was kept under the Android key (e.g. `board_list_delete_dialog_message` takes id and identifier). Android-only keys (back, exit dialog, column dismiss dialog, settings screen) are not defined because no desktop screen uses them. Pattern ids O, L, I have their own keys; an unknown id renders as-is via `pattern_other`.
- English for "Faltan N" is "N to go". Key count: 72 in each language.
- No holder contains UI text (checked); reason types are mapped only in `ui/Strings.kt`. The window title now uses `app_name` ("Bingo FF", previously "BingoFF").
- Manual check pending: run with `-Duser.language=en` and `-Duser.language=fr`, walk every screen and dialog; check long English strings do not clip in the 1/3 pane and the pill; confirm the packaged app also resolves resources (`packageDistributionForCurrentOS`, unit 10).

## Unit 10: app-navigation (DONE, tasks 10.1-10.4, Strict TDD)

- [x] 10.1 RED then GREEN: `commonTest/presentation/ShellLayoutTest` (10) and `RightPaneDestinationTest` (2, in `ShellStateTest.kt`); `presentation/ShellLayout` (pane weights 2:1, adaptive column count, card size and visible-rows math) and `rightPaneDestination()`
- [x] 10.2 Polish: `BoardsPane` (shared constants, cells at aspect 1.3, compact delete link, empty state with create action), `AppWindow` (weights from `ShellLayout`, right pane derives destination from `active`), `AppMenu` is now a native `MenuBar` (`FrameWindowScope.AppMenuBar`), `Main.kt` (screen menu bar on macOS, window constants), `CreateBoardDialog` rebuilt as a true 5x5 grid; `build.gradle.kts` `modules("java.instrument", "java.sql", "jdk.unsupported")`
- [x] 10.3 Verified: `./gradlew test` and `packageDistributionForCurrentOS` (dmg built); packaged app launched with a temp `user.home`, screenshot taken
- [x] 10.4 Commit `feat(shell): polish two-pane layout, adaptive grid and menu`

### TDD Cycle Evidence (unit 10)

| Task | Test File | Layer | Safety Net | RED | GREEN | TRIANGULATE | REFACTOR |
|------|-----------|-------|------------|-----|-------|-------------|----------|
| 10.1 | `presentation/ShellLayoutTest.kt` | Unit | 243/243 | Written; compile failed: Unresolved reference 'ShellLayout' | 10/10 (one test expectation of mine was wrong: 1440dp gives 4 columns, not 3; corrected the test, code unchanged) | weights, 2/3 width, grid width, 5/3/1/0 columns, exact column boundary, 1920/1440/1200 windows, card width fills columns, card height, 15 cards at 1920x1080, one row at 1200x700 | None needed |
| 10.2 | `presentation/ShellStateTest.kt` (RightPaneDestinationTest) | Unit | 253/253 | Written; compile failed: Unresolved reference 'rightPaneDestination' | 2/2 | null and active game | ShellState reuses the function |
| 10.2 | n/a | UI (Compose) | n/a | n/a | compiles, packaged app screenshot | n/a | n/a |

Test count: 243 before, 255 after (12 new). `./gradlew test --rerun-tasks`: BUILD SUCCESSFUL, failures=0.

### Work Unit Evidence (unit 10)

| Evidence | Value |
|---|---|
| Focused test command | `./gradlew desktopTest --tests '*ShellLayout*' --tests '*RightPaneDestination*'`; full `./gradlew test --rerun-tasks` tests=255 failures=0 |
| Runtime harness | Packaged `BingoFF.app` launched with `JAVA_TOOL_OPTIONS=-Duser.home=<scratchpad>` (real app data untouched), 20 seeded boards plus a restored COLUMNA game; screenshot `scratchpad/shell-1440x860.png`. It opened directly in Play, 4 columns at 1440dp, marks, "Faltan N" pill and winner highlight legible |
| Rollback boundary | `presentation/{ShellLayout,ShellState}`, `ui/{AppMenu,AppWindow,BoardsPane,CreateBoardDialog}`, `Main.kt`, string `menu_label`, `build.gradle.kts` modules (revert the unit 10 commit) |

### Deviations / notes (unit 10)

- Sizing: cells are 1.3 wider than tall; modelled card height is about 247dp at 1920 (5 columns), so 3 rows need about 780dp of the roughly 960dp grid viewport (winner cards add one line). Not run at 1920x1080 for real (screen is smaller); the 1440x860 screenshot shows 4 columns and about 2.7 rows, consistent with the model.
- Menu: native `MenuBar` chosen over the overflow: frees the top row for the grid, import/export and the Light/Dark/System radio group are standard menu items, and macOS uses the system menu bar (`apple.laf.useScreenMenuBar`). The menu is not visible in the screenshot (system bar), so it was not clicked. `menu_label` now reads Cartones/Boards; the menu items now use the existing `menu_*` string keys (AppMenu previously had hard-coded Spanish).
- Right pane: `RightPane` derives the destination with `rightPaneDestination(active)` from the same flow `loaded` gates on, to rule out a Setup flash; `ShellState` still exists, uses the same function, and is covered by its tests.
- CreateBoardDialog: old layout was one row per letter (B row, I row...) with a left-aligned letter label above each, i.e. transposed from a card, with fields at 64dp. Now: letter headers centred over columns, 5 rows by 5 columns (FREE star in the centre, same 60x56dp box as the fields), 6dp gaps, identifier field as wide as the grid, content centred. Focus order unchanged (column-major, tested by CreateBoardFocusOrderTest). Visual check of the dialog NOT done (not opened).
- Packaged app: `suggestRuntimeModules` suggested java.instrument and jdk.unsupported; added with java.sql. The jlinked runtime lists java.base, datatransfer, xml, prefs, desktop, instrument, logging, transaction.xa, sql, crypto.ec, unsupported. The app jar holds `natives/osx_arm64/libsqliteJni.dylib` (and x64) and `composeResources/.../values/strings.commonMain.cvr` plus `values-en`. The packaged app started, created `bingoff.db` and rendered Spanish strings.
- Manual checks pending, all units: (1) create a board in the new 5x5 dialog (errors, Tab/auto-advance, FREE centre); (2) delete a board; (3) import/export via the menu bar (file, clipboard, paste); (4) theme radio group in the menu, persistence across relaunch, Sistema following the OS; (5) start/end game, restored game opens in Play; (6) call numbers, announcements, possible winners, win highlight in both themes; (7) `-Duser.language=en` and `fr`, long English strings in the 1/3 pane; (8) resize to 1200x700 and to 1920x1080 (about 15 cards); (9) open the dmg on a clean machine; (10) Windows/Linux packaging not tried.
