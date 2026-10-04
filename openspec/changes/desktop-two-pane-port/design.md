# Design: Desktop Two-Pane Port of BingoFF

## Technical Approach

Single-module Gradle KMP project, one JVM target named `desktop` (`jvm("desktop")`, source sets `commonMain/commonTest/desktopMain/desktopTest`). Domain, repository ports and state holders live in `commonMain` (no `java.*`/`android.*`). Platform code (Room driver, file stores, AWT dialogs, app-data paths, Compose UI) lives in `desktopMain`. State holders are plain classes taking a `CoroutineScope` (no AndroidX ViewModel), wired by a manual composition root. The domain is ported verbatim from Android (`BingoWinChecker`, `predictPossibleWinners`, `GameSession`, `BoardCard`, `GameMode`, `BingoLetter`); only `GamePlayViewModel` logic is rewritten into a persisted `GameSessionHolder`.

## Toolchain Pins (verification status)

context7 and web tools were NOT available to this executor. Versions below are from the explore phase (web-verified Oct 2026, obs #620) or marked as expected. Task 1 (scaffold) MUST confirm resolution against Maven/Gradle plugin portal before committing; any failing pin is fixed in `gradle/libs.versions.toml` only.

| Item | Pin | Status |
|------|-----|--------|
| Kotlin | 2.4.20 | explore-verified, re-check at scaffold |
| Compose Multiplatform plugin | 1.12.1 | explore-verified |
| Room KMP + compiler | 2.8.5 (`androidx.room`) | explore-verified (Room 3.0 alpha rejected) |
| `androidx.sqlite:sqlite-bundled` | 2.6.x, must match Room 2.8.5 transitive | UNVERIFIED, take version Room resolves |
| KSP | version line compatible with Kotlin 2.4.20 (KSP2, independent versioning) | UNVERIFIED |
| kotlinx-coroutines (core, test, swing) | 1.10.x or newer matching Kotlin | UNVERIFIED |
| kotlinx-serialization-json | 1.9.x or newer matching Kotlin | UNVERIFIED |
| Gradle wrapper | 9.x (must support Kotlin 2.4 + CMP 1.12) | UNVERIFIED |
| JDK | toolchain 17; CI runs Temurin 21 (jpackage needs 17+) | decided |

## Architecture Decisions

| Decision | Choice | Rejected (why) |
|----------|--------|----------------|
| Modules | One module, `desktop` JVM target | `:domain` + `:app` (build overhead, no second consumer) |
| DI | Manual `AppContainer` in `desktopMain/di` | Koin/Hilt (about 6 holders; extra dependency) |
| Boards DB | Room KMP + `BundledSQLiteDriver` + KSP, `board` table (id autoGenerate, unique `identifier`, `numbers` as comma string via TypeConverter) | SQLDelight (new schema dialect), JSON-only (kept only as spike fallback per proposal) |
| Duplicate identifier | DAO `@Transaction insertIfAbsent` (SELECT existence then INSERT) returns `Result.failure(DuplicateIdentifierException)`; backstop catches `androidx.sqlite.SQLiteException` (KMP type) | `android.database.SQLiteConstraintException` (not on JVM) |
| Active game storage | JSON file `active-game.json` (`{mode, calledNumbers, dismissedLetters}`), atomic write (temp + `Files.move ATOMIC_MOVE`) | Room table: needs migrations/entity for a single 3-field doc, couples game restore to the Room spike; a file keeps game working even if the DB is the fallback case. Winners are never stored, rebuilt by replay as on Android |
| Theme storage | `theme.json` `{"mode":"SYSTEM"}` via same `JsonFileStore<T>` | DataStore (Android-centric), java.util.Properties (untyped) |
| Export codec | kotlinx.serialization, `@Serializable data class BoardDto(id, identifier, numbers)`, top-level `List<BoardDto>` = bare array; `ignoreUnknownKeys = true` | Gson (JVM-only reflection), wrapped object (breaks Android compat) |
| File dialogs | `java.awt.FileDialog` behind `FileDialogs` interface (commonMain) | JFileChooser (non-native look on macOS), FileKit (extra dependency; AWT is sufficient for two simple open/save flows). Caveat: FilenameFilter is ignored on Windows, so default file name `boards.json` plus `setFile("*.json")`, and content validated on import |
| Dialog threading | Invoked on the Swing/Main dispatcher (`Dispatchers.Main` from Compose Desktop; add `kotlinx-coroutines-swing`), blocking call isolated in the interface impl | Dispatchers.IO (AWT dialogs need EDT) |
| COLUMNA dismiss | Per-letter toggle and right-click on the possible-winner row; persisted in the active-game file | Session-only (lost on restart, contradicts persistence scope) |
| Left-pane marking | Pure derivation in `BoardsPaneState` (below) | Per-cell stateful widgets |
| Shell layout | `Row` with `weight(2f)` boards pane and `weight(1f)` right pane (2/3 : 1/3). Minimum window 1200x700 (set via `window.minimumSize`), default 1440x860 | Keeping 900x600: left would be 600dp (2 columns) and right 300dp, too cramped for the number input plus per-letter grid |
| Boards grid | `LazyVerticalGrid(GridCells.Adaptive(220.dp), key = { board.id }, contentType = { "board" })`, 8dp gaps, vertical scroll | Fixed column count from width (needs measuring code; Adaptive already derives it), `FlowRow` (not lazy) |
| Right-pane navigation | `sealed interface RightPaneDestination { Setup; Play }` in `ShellState` (commonMain holder, `StateFlow`); a `when` in the right pane | NavHost/Navigation Compose (overkill for two destinations) |
| Resources | Compose MP Resources `composeResources/values` (es default) + `values-en`; key-parity test reads both XML files | Hand-rolled bundle |
| Packaging | `compose.desktop.application` nativeDistributions Dmg/Msi/Deb, icons `icon.icns/.ico/.png`, no signing; CI matrix macos/windows/ubuntu runs `./gradlew test packageDistributionForCurrentOS` | Cross-compilation (unsupported by jpackage) |

App-data paths (`AppDirs`, desktopMain, injectable root for tests): macOS `~/Library/Application Support/BingoFF`, Windows `%APPDATA%\BingoFF`, Linux `$XDG_DATA_HOME/bingoff` or `~/.local/share/bingoff`. Files: `bingoff.db`, `active-game.json`, `theme.json`.

## Package Layout (`com.sergiodev.bingo`)

```
commonMain/
  domain/model   BingoLetter, BoardCard, GameMode, GridPosition, WinPattern
  domain/game    BingoWinChecker, WinPrediction, WinAnnouncement, AnnouncedWin, GameSession
  domain/repository BoardRepository, ActiveGameRepository, ThemeRepository, ImportResult
  data/json      BoardJsonCodec (kotlinx.serialization)
  presentation   BoardsState(holder), GameSessionHolder, GamePlayHolder, BoardsPaneState, ThemeHolder, ImportExportHolder
desktopMain/
  data/local     BingoDatabase, BoardDao, BoardEntity, RoomBoardRepository
  data/file      JsonFileStore, FileActiveGameRepository, FileThemeRepository, AppDirs
  platform       FileDialogs(AWT), Clipboard
  di             AppContainer
  ui             AppWindow (two-pane shell, menu), BoardsPane, SetupPane, PlayPane, dialogs, theme
  Main.kt
  composeResources/values{,-en}/strings.xml
```
(Proposal wrote `jvmMain`; the source set is `desktopMain` everywhere. Spec/tasks should use `desktopMain`.) `presentation` also holds `ShellState` (below).

## Layout and Card Sizing

Right-pane navigation: `ShellState(session: GameSessionHolder)` exposes `StateFlow<RightPaneDestination>`. On startup, after the active-game load, destination is `Play` if a game was restored, else `Setup`. `session.start(mode)` moves to `Play`; `session.end()` (after confirmation) returns to `Setup`. Destination is derived from `session.active != null`, so there is no separate state to desync.

Card sizing (1920x1080): left pane about 1280dp minus 16dp padding gives 5 adaptive columns at min 220dp (about 250dp each); available height about 1000dp gives about 3 visible rows of about 300dp, so 15 boards visible, more via scroll.
- Card = header row (board number, identifier, badge) plus a 5x5 cell grid with `aspectRatio(1f)` cells (about 40dp), cell text 14sp (never below 12sp), so numbers stay legible at the 220dp minimum width.
- Called marks: filled cell with contrasting text (colour plus bold, not colour alone). Near-win badge: compact pill in the header showing missing count. Winner: thicker primary border plus tinted card background and a "winner" label.
- Min window 1200x700: left about 800dp gives 3 columns, right about 400dp.
- Cheap recomposition: `@Immutable data class BoardCardState(id, identifier, cells: List<CellState>, missing: Int?, winningPatternIds)` precomputed in `BoardsPaneState` (a `List<BoardCardState>` keyed by id), so a new call only changes cards whose marks changed (structural equality skips the rest). Cells are drawn in one `Canvas`/`Row`-`Column` composable per card without per-cell state objects; hoist nothing into per-cell `remember`.

## Key Interfaces

```kotlin
interface BoardRepository { // unchanged from Android
  fun observeBoards(): Flow<List<BoardCard>>
  suspend fun addBoard(identifier: String, numbers: List<Int>): Result<Unit>
  suspend fun importBoards(boards: List<BoardCard>): ImportResult
  suspend fun deleteBoard(id: Long)
}
data class ActiveGame(val mode: GameMode, val calledNumbers: List<Int>, val dismissedLetters: Set<BingoLetter>)
interface ActiveGameRepository { suspend fun load(): ActiveGame?; suspend fun save(game: ActiveGame); suspend fun clear() }
interface ThemeRepository { val themeMode: Flow<ThemeMode>; suspend fun set(mode: ThemeMode) }
interface FileDialogs { suspend fun pickOpen(): String?; suspend fun pickSave(suggested: String): java.nio.file.Path? }
```
`GameSessionHolder(boards, repo, scope)` exposes `StateFlow<ActiveGame?>`; `start(mode)`, `end()` (clears file), `call(n)`, `toggleDismiss(letter)`; each mutation saves. Initial load in `init` (restore). Input validation (letter derivation, mismatch, duplicate) is ported from `GamePlayViewModel` into `GamePlayHolder`.

## Data Flow

```
BoardRepository.observeBoards ─┐
ActiveGameRepository (file) ─> GameSessionHolder.active ─┤
                                                         ├─ combine ─> BoardsPaneState ─> BoardsPane (left)
                                                         └─ combine ─> PlayPaneState   ─> PlayPane   (right)
```
`BoardsPaneState` per board: `called ∩ board.numbers` (cell marks), winning pattern ids from replay (`rebuildSession` moved to a pure domain function `replay(mode, called, boards)`), and `PredictionCandidate` for that board with `missing` (badge, after dismiss filter). Replay is recomputed per emission; at most 75 calls so cost is trivial. Right-pane actions mutate only the holder; the left pane reacts through the flow.

## Testing Strategy

Strict TDD from task 2 (flip `strict_tdd: true` in `openspec/config.yaml` after scaffold: `./gradlew test` passes with one smoke test). kotlin.test + JUnit5/4 runner, `kotlinx-coroutines-test` (`runTest`, `StandardTestDispatcher`; replaces `MainDispatcherRule`). Fakes: `FakeBoardRepository`, `InMemoryActiveGameRepository`, `FakeFileDialogs`.

| Layer | Approach |
|-------|----------|
| Domain (commonTest) | Direct ports: BingoLetter, BoardCard, GameMode, GameSession, BingoWinChecker, WinPrediction |
| Codec | BoardJsonCodecTest ported to kotlinx.serialization; add Android-sample fixture round-trip |
| Room (desktopTest) | RoomBoardRepositoryTest on temp-dir DB (or in-memory builder): duplicate identifier, import skips, autoincrement |
| File stores (desktopTest) | temp dir: save/load, corrupt file falls back to default, atomic overwrite |
| Holders | Rewrite SavedStateHandle tests (GamePlayViewModelTest, GameSetupViewModelTest) against `GameSessionHolder` + in-memory repo, including restart (new holder, same repo) restores replay |
| UI logic | BingoNumberFieldTest, CreateBoardFocusOrderTest port if the logic is Compose-free; else drop |
| i18n | `StringsParityTest` compares keys in `values` vs `values-en` |

Android test files found: 19 (18 tests + `MainDispatcherRule`), not 20. Mapping: BoardList/CreateBoard/ImportBoards VM tests -> holder tests; ThemeViewModel + DataStoreThemeRepository -> ThemeHolder + FileThemeRepository; MainActivityViewModelTest -> ThemeHolder startup; MainDispatcherRule dropped.

## Threat Matrix

N/A — no routing, shell, subprocess, VCS/PR automation, executable-file classification, or process-integration boundary. (Import reads user-chosen files; validated by codec and size/shape checks, covered by codec tests.)

## Migration / Rollout

No migration. Room schema v1 exported to `schemas/`; `active-game.json` carries `"version": 1`. Ten commits on main in the settled order; CI workflow added in commit 1.

## Open Questions

- [ ] All toolchain pins marked UNVERIFIED must be confirmed at scaffold.
- [x] Room KMP desktop spike: PASSED (unit 3). KSP codegen, on-disk round trip and the native lib inside the packaged image all work; boards stay on Room, no JSON fallback. The native lib ships inside sqlite-bundled-jvm-2.6.2.jar (natives/<os>_<arch>) and is extracted at runtime.
- [ ] Linux AWT FileDialog look and `FilenameFilter` support need manual smoke check.
- [ ] Whether `xdg` var empty-string handling needs a test (treated as unset).
