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
