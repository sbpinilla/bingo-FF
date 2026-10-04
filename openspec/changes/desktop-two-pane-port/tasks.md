# Tasks: Desktop Two-Pane Port of BingoFF

Path aliases (package `com.sergiodev.bingo`): `C` = `src/commonMain/kotlin/com/sergiodev/bingo`, `CT` = `src/commonTest/kotlin/com/sergiodev/bingo`, `D` = `src/desktopMain/kotlin/com/sergiodev/bingo`, `DT` = `src/desktopTest/kotlin/com/sergiodev/bingo`, `R` = `src/desktopMain/composeResources`. Android reference `/Users/sergio/AndroidStudioProjects/BingoFF` (read-only) is only read, never edited. Strict TDD from unit 2: each logic task is RED (failing test) then GREEN (minimal code), then REFACTOR if needed. One conventional commit per unit, straight to `main`, no AI attribution, no Co-Authored-By.

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | 6000-9000 total; per unit 300-1500 (scaffold, board-management, game-play, app-navigation largest) |
| 400-line budget risk | High (informational only) |
| Chained PRs recommended | No |
| Suggested split | 10 sequential commits on main, no PRs |
| Delivery strategy | exception-ok |
| Chain strategy | size-exception |

Decision needed before apply: No
Chained PRs recommended: No
Chain strategy: size-exception
400-line budget risk: High

### Suggested Work Units

| Unit | Goal | Likely PR | Focused test command | Runtime harness | Rollback boundary |
|------|------|-----------|----------------------|-----------------|-------------------|
| 1 | Scaffold + CI + shell | commit 1 | `./gradlew test` | `./gradlew run` shows 2-pane window; `packageDistributionForCurrentOS` | revert leaves empty project |
| 2 | Domain port | commit 2 | `./gradlew desktopTest --tests '*domain*'` | N/A, pure logic | `C/domain`, `CT/domain` |
| 3 | board-management | commit 3 | `./gradlew desktopTest --tests '*Board*'` | `./gradlew run`, create/delete/restart | data, Boards UI, DI |
| 4 | board-export-import | commit 4 | `./gradlew desktopTest --tests '*Json*' --tests '*Import*'` | run: export file, import in app | codec, dialogs, holder |
| 5 | game-session | commit 5 | `./gradlew desktopTest --tests '*Session*'` | run: start, quit, relaunch | session holder, file store |
| 6 | game-play | commit 6 | `./gradlew desktopTest --tests '*GamePlay*'` | run: call numbers, live marks | play holder, PlayPane |
| 7 | win-prediction | commit 7 | `./gradlew desktopTest --tests '*Prediction*'` | run: badges and list | prediction wiring |
| 8 | theme-preference | commit 8 | `./gradlew desktopTest --tests '*Theme*'` | run: switch, restart | theme files |
| 9 | ui-localization | commit 9 | `./gradlew desktopTest --tests '*Strings*'` | run with `-Duser.language=en` | resources |
| 10 | app-navigation | commit 10 | `./gradlew test` | run at 1920x1080 and 1200x700 | shell polish |

## Unit 1: scaffold (no strict TDD yet)

- [x] 1.1 Create `settings.gradle.kts`, `build.gradle.kts` (KMP `jvm("desktop")`, JVM toolchain 17), `gradle.properties`, Gradle wrapper 9.x.
- [x] 1.2 SPIKE (toolchain): create `gradle/libs.versions.toml`; verify each pin against Maven Central and the Gradle plugin portal at apply time (Kotlin 2.4.20, CMP 1.12.1, Room 2.8.5, sqlite-bundled, KSP, coroutines core/test/swing, serialization-json); fix failing pins only in the catalog.
- [x] 1.3 Add test deps `kotlin-test`, `kotlinx-coroutines-test` and `CT/SmokeTest.kt` (`smoke_test_runs`); confirm `./gradlew test` passes.
- [x] 1.4 Configure `compose.desktop.application` in `build.gradle.kts`: mainClass, nativeDistributions Dmg/Msi/Deb, icons under `src/desktopMain/resources`.
- [x] 1.5 Create `.github/workflows/build.yml`: matrix macos/windows/ubuntu, Temurin 21, `./gradlew test packageDistributionForCurrentOS`.
- [x] 1.6 Create `D/Main.kt` and `D/ui/AppWindow.kt`: Row with weight 2f and 1f placeholder panes, default 1440x860, `window.minimumSize` 1200x700.
- [x] 1.7 Set `strict_tdd: true` in `openspec/config.yaml` (and update `testing.runner`); add `.gitignore`.
- [x] 1.8 Commit: `chore: scaffold KMP desktop project with CI and two-pane shell`

## Unit 2: domain port (strict TDD)

- [x] 2.1 RED: port `CT/domain/model/BingoLetterTest.kt`, `BoardCardTest.kt`, `GameModeTest.kt` from the Android tests (read-only reference).
- [x] 2.2 GREEN: `C/domain/model/{BingoLetter,BoardCard,GameMode,GridPosition,WinPattern}.kt` ported verbatim (no `java.*`).
- [x] 2.3 RED: `CT/domain/game/BingoWinCheckerTest.kt`, `GameSessionTest.kt` (patterns COLUMN_B..O, O, L, I, FULL_CARD).
- [x] 2.4 GREEN: `C/domain/game/{BingoWinChecker,WinAnnouncement,AnnouncedWin,GameSession}.kt`.
- [x] 2.5 RED: `CT/domain/game/WinPredictionTest.kt` (thresholds 2/3/3/3/10, no call ceiling, ordering, exclusions).
- [x] 2.6 GREEN: `C/domain/game/WinPrediction.kt` (`predictPossibleWinners`); RED then GREEN `CT/domain/game/ReplayTest.kt` for pure `replay(mode, called, boards)`.
- [x] 2.7 Create repository ports `C/domain/repository/{BoardRepository,ActiveGameRepository,ThemeRepository,ImportResult}.kt` (interfaces only).
- [x] 2.8 Commit: `feat(domain): port bingo domain model, win checker and prediction`

## Unit 3: board-management

- [x] 3.1 SPIKE (Room KMP, early risk): add Room 2.8.5 + KSP + `BundledSQLiteDriver`; RED `DT/data/local/RoomSpikeTest.kt` (`room_opens_and_roundtrips_on_desktop`); check native lib in `./gradlew packageDistributionForCurrentOS` image. If it fails: switch boards to `JsonFileStore` fallback per proposal, record in design Open Questions.
- [x] 3.2 RED: `DT/data/local/RoomBoardRepositoryTest.kt` (`addBoard_duplicate_identifier_fails`, `ids_ascending_after_restart`, `delete_removes`, `import_skips_dups`).
- [x] 3.3 GREEN: `D/data/local/{BingoDatabase,BoardDao,BoardEntity,RoomBoardRepository}.kt`, `D/data/file/AppDirs.kt`; schema export to `schemas/`.
- [x] 3.4 RED: `CT/presentation/CreateBoardTest.kt` (`computeFieldErrors` out_of_range, duplicate, blank-on-submit, BlankIdentifier, DuplicateIdentifier) and `BoardListTest.kt` (delete confirm/cancel), using `FakeBoardRepository`.
- [x] 3.5 GREEN: `C/presentation/{BoardsState,CreateBoardHolder}.kt`; `D/di/AppContainer.kt`.
- [x] 3.6 UI: `D/ui/{BoardsPane,CreateBoardDialog,DeleteConfirmDialog}.kt` with adaptive grid and 5x5 cards; wire into `AppWindow.kt`.
- [x] 3.7 Commit: `feat(boards): add board create, delete and Room persistence`

## Unit 4: board-export-import

- [x] 4.1 RED: `CT/data/json/BoardJsonCodecTest.kt` (bare array, round trip, missing `numbers` rejects all, non-array root, Android sample fixture in `src/commonTest/resources`).
- [x] 4.2 GREEN: `C/data/json/BoardJsonCodec.kt` (`BoardDto`, `ignoreUnknownKeys = true`).
- [x] 4.3 RED: `CT/presentation/ImportExportHolderTest.kt` (BlankInput, InvalidJson, mixed payload imported=1 skipped=3, summary clearable, cancelled dialog writes nothing) with `FakeFileDialogs`.
- [x] 4.4 GREEN: `C/presentation/ImportExportHolder.kt`, `D/platform/{FileDialogs,Clipboard}.kt` (AWT, Main dispatcher, add `kotlinx-coroutines-swing`).
- [x] 4.5 UI: `D/ui/{ImportDialog,AppMenu}.kt` import/export entries; manual smoke of AWT dialog (Linux open question).
- [x] 4.6 Commit: `feat(boards): add JSON export and import with Android-compatible format`

## Unit 5: game-session

- [ ] 5.1 RED: `DT/data/file/FileStoresTest.kt` (save/load, corrupt falls back, atomic overwrite, `version: 1`); `GameSessionHolderTest.kt` (start, end clears, restart restores same repo, start disabled with no boards).
- [ ] 5.2 GREEN: `D/data/file/{JsonFileStore,FileActiveGameRepository}.kt`, `C/presentation/GameSessionHolder.kt`, `ShellState.kt` (`RightPaneDestination` derived from `session.active`).
- [ ] 5.3 RED then GREEN: `CT/presentation/ShellStateTest.kt` (restored opens Play, end returns Setup).
- [ ] 5.4 UI: `D/ui/{SetupPane,EndGameDialog}.kt` mode chips (default COLUMNA), start button, empty hint.
- [ ] 5.5 Commit: `feat(session): add game setup and persisted single active game`

## Unit 6: game-play

- [ ] 6.1 RED: `CT/presentation/GamePlayHolderTest.kt` (derived letter 47 to G, InvalidNumber 80, LetterMismatch, DuplicateCall(47) clears input, accept appends and persists, grouping 47,3,52, announce once, restart replay, dismissed letter still wins, toggle twice).
- [ ] 6.2 GREEN: `C/presentation/GamePlayHolder.kt` (ported from Android `GamePlayViewModel`, read-only), `toggleDismiss` in `GameSessionHolder`.
- [ ] 6.3 RED then GREEN: port `BingoNumberFieldTest` / `CreateBoardFocusOrderTest` if Compose-free, else drop and note.
- [ ] 6.4 RED then GREEN: `CT/presentation/BoardsPaneStateTest.kt` (marks = called ∩ numbers, winner pattern ids, no game means no marks); `C/presentation/BoardsPaneState.kt`.
- [ ] 6.5 UI: `D/ui/PlayPane.kt` (input, per-letter grid, announcements, end game), marked cells and winner highlight in `BoardsPane.kt`.
- [ ] 6.6 Commit: `feat(play): add number calling, live board marks and win announcements`

## Unit 7: win-prediction

- [ ] 7.1 RED: extend `BoardsPaneStateTest.kt` (badge missing=2, announced excluded, dismissed G filtered) and `CT/presentation/PlayPaneStateTest.kt` (ordering, live update on call and restore).
- [ ] 7.2 GREEN: `BoardCardState.missing` in `BoardsPaneState.kt`; `C/presentation/PlayPaneState.kt` possible winners.
- [ ] 7.3 UI: missing-count pill in `BoardsPane.kt`; possible-winners list with per-row toggle and right-click dismiss in `PlayPane.kt`.
- [ ] 7.4 Commit: `feat(prediction): show possible winners and near-win badges`

## Unit 8: theme-preference

- [ ] 8.1 RED: `DT/data/file/FileThemeRepositoryTest.kt` (default SYSTEM, corrupt falls back, persists) and `CT/presentation/ThemeHolderTest.kt` (startup, set).
- [ ] 8.2 GREEN: `D/data/file/FileThemeRepository.kt`, `C/presentation/ThemeHolder.kt`, `ThemeMode`.
- [ ] 8.3 UI: `D/ui/theme/BingoTheme.kt` light/dark palettes; Light/Dark/System in `AppMenu.kt`.
- [ ] 8.4 Commit: `feat(theme): add persisted light, dark and system theme`

## Unit 9: ui-localization

- [ ] 9.1 RED: `DT/i18n/StringsParityTest.kt` (keys of `values` equal `values-en`) and `LocaleSelectionTest.kt` (en gives English, fr gives Spanish).
- [ ] 9.2 GREEN: `R/values/strings.xml` (es), `R/values-en/strings.xml`; `D/ui/Strings.kt` maps reason types (BlankIdentifier, DuplicateIdentifier, InvalidJson, InvalidNumber, LetterMismatch, DuplicateCall(n), import counts) to text.
- [ ] 9.3 RED then GREEN: `ReasonMappingTest.kt` (DuplicateCall(47) message contains 47); replace all hard-coded UI strings.
- [ ] 9.4 Commit: `feat(i18n): add Spanish and English localization`

## Unit 10: app-navigation

- [ ] 10.1 RED then GREEN: `CT/presentation/ShellLayoutTest.kt` for column math (pane weights 2:1, about 5 columns at 1280dp, 3 at 800dp).
- [ ] 10.2 Polish `D/ui/AppWindow.kt`, `BoardsPane.kt` (`GridCells.Adaptive(220.dp)`, keys, empty state with create action), right-pane `when` on `RightPaneDestination`, menu.
- [ ] 10.3 Manual verify at 1920x1080 (about 15 cards) and 1200x700; run `./gradlew test packageDistributionForCurrentOS`.
- [ ] 10.4 Commit: `feat(shell): polish two-pane layout, adaptive grid and menu`
