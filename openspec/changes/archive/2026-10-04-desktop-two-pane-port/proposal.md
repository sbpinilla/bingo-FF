# Proposal: Desktop Two-Pane Port of BingoFF

## Intent

Port the Android BingoFF app (read-only reference `/Users/sergio/AndroidStudioProjects/BingoFF`) to a Kotlin Multiplatform + Compose Desktop (JVM) app for macOS, Windows and Linux. Replace multi-screen navigation with one two-pane window so board status and game play are visible together.

## Scope

### In Scope
- Gradle KMP JVM desktop scaffold, `./gradlew test`, dmg/msi/deb packaging, GitHub Actions OS matrix, minimal shell.
- Domain port to commonMain (boards, win checker, announcements, prediction).
- Boards: create (dialog) and delete (confirmation) only.
- Left pane: all boards as 5x5 grids, called numbers marked live, likely winners badged with missing count, winners highlighted.
- Right pane: setup (mode chips, start), play (number input, per-letter called grid, announcements, possible winners, end game with confirmation).
- Active game (mode, called numbers, dismissed COLUMNA letters) persisted across restarts; one active game at a time.
- Export/import: native save/open dialog (.json) plus paste-JSON import; clipboard export. Format is the bare Android array `{id, identifier, numbers}`, round-trip compatible.
- Room KMP 2.8.x + BundledSQLiteDriver, DB in per-OS app-data dir.
- Theme (Light/Dark/System) in a file-backed preference; es (default) + en via Compose Multiplatform Resources.
- Shell menu (import/export, theme); minimum window ~1200x700; COLUMNA dismiss via per-row toggle/right-click.

### Out of Scope
- Board editing, multiple simultaneous games, narrow/stacked layout.
- macOS signing/notarization.
- Any change to the Android repo.

## Capabilities

### New Capabilities
- `board-management`: create/delete boards, unique identifier, persistence.
- `board-export-import`: JSON file dialog, paste and clipboard, round-trip format.
- `game-session`: mode selection, start, persisted single active game, end with confirmation.
- `game-play`: number input, called grid, announcements, restart restore by replay.
- `win-prediction`: possible winners, missing-count badges, COLUMNA<=2, O/L/I<=3, FULL<=10, no call ceiling (follows code, not the stale Android MAX_PREDICTION_CALLS spec text).
- `theme-preference`: Light/Dark/System persisted to file.
- `ui-localization`: es/en resources.
- `app-navigation`: redefined as the two-pane shell and menu polish.

### Modified Capabilities
- None (the desktop project has no existing specs).

## Approach

Ten commits on main in order: scaffold, domain port, board-management, board-export-import, game-session, game-play, win-prediction, theme-preference, ui-localization, app-navigation. Domain stays platform-agnostic in commonMain. Manual DI via a composition root (no Hilt/Koin). kotlinx.serialization replaces Gson. Strict TDD enabled after the scaffold. Pin Kotlin 2.4.20, CMP 1.12.1, Room 2.8.5 in design after re-verification.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `build.gradle.kts`, `settings.gradle.kts`, `gradle/` | New | KMP desktop build, packaging |
| `.github/workflows/` | New | OS packaging matrix |
| `src/commonMain` (domain) | New | Ported logic |
| `src/desktopMain` (data, ui, di) | New | Room, file stores, Compose UI |
| `openspec/config.yaml` | Modified | Flip `strict_tdd` after scaffold |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Room KMP desktop quirks | Med | Spike in board-management; JSON store as fallback |
| Version drift | Med | Re-verify and pin in design |
| Android specs are deltas/stale | Med | Specs follow code |
| CI packaging per OS | Med | Matrix, no cross-compilation |

## Rollback Plan

Each capability is one commit; `git revert` the commit. Scaffold revert leaves an empty project.

## Dependencies

- JDK 17+; GitHub Actions runners for macOS, Windows and Linux.

## Success Criteria

- [ ] `./gradlew test` passes with domain parity against Android behavior.
- [ ] Installers build on all three OSes.
- [ ] Export from Android imports on desktop and back.
- [ ] Game survives app restart.
