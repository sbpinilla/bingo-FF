# Archive Report: Desktop Two-Pane Port

**Change**: desktop-two-pane-port  
**Archived to**: `openspec/changes/archive/2026-10-04-desktop-two-pane-port/`  
**Archive Date**: 2026-10-04  
**Verdict**: COMPLETE (PASS WITH WARNINGS, 0 CRITICAL)  
**Cycle Status**: Closed  

## Executive Summary

The desktop-two-pane port change has been fully implemented, verified, and archived. All 10 work units (56 tasks) are committed. The application builds and passes 256 tests with 0 failures. Specifications from the change have been synced into the main spec repository. The change folder has been moved to the archive.

## Final State Authority

This report describes the state of the change AT CLOSE per the Final-State Authority hierarchy. Work completed after intermediate snapshots (apply-progress, verify-report) is reflected here:

1. Persisted tasks artifact: All 56 tasks marked complete in `openspec/changes/archive/2026-10-04-desktop-two-pane-port/tasks.md`
2. Explicit final-state facts from orchestrator launch prompt (override intermediate snapshots):
   - After verify, commit c008684 fixed WARNING 1 (BoardDao.importNew in-batch dedup, with regression test) and WARNING 2 (removed dead ShellState)
   - Card header compacted (✕ delete glyph with tooltip) to stop identifier truncation
   - Final test count: 256, with 0 failures
3. Intermediate snapshots (apply-progress.md, verify-report.md) record state at their time of writing

## Work Completion

### Tasks
- **Total tasks**: 56
- **Completed**: 56 (100%)
- **Pending**: 0

All 10 implementation units are fully committed on main:
1. 60a661c — docs planning
2. 8bc0585 — scaffold
3. 54e5db8 — domain
4. c7c9846 — boards
5. 7d370c5 — export/import
6. 7bc196c — session
7. 066454e — play
8. 400b78b — prediction
9. 4c201dc — theme
10. 067c929 — i18n
11. de958c2 — shell

Final commit c008684 on main fixed verification issues.

### Build & Test Verification

**Build Status**: ✓ Passed  
**Test Count**: 256 (final count, per orchestrator final-state facts)  
**Test Failures**: 0  
**Test Command**: `./gradlew test --rerun-tasks` (re-run by orchestrator)  

Per verify-report.md at time of verification (evidence_revision sha256:4f2183f9...):
- Build command: `./gradlew compileKotlinDesktop --rerun-tasks` → exit 0
- Initial test count: 255 passed, 0 failed, 0 skipped
- Final test count per orchestrator: 256 (updated by commit c008684)

**Verdict from verify-report.md**: PASS WITH WARNINGS
- 0 CRITICAL findings
- 4 WARNING findings (see below)
- 4 SUGGESTION findings

### Spec Compliance

**Requirements Coverage**: 38/38 (100%)  
**Scenario Coverage**: 63/63 covered (59 COMPLIANT by passing test, 4 PARTIAL for UI-only)  

All 8 capability specs have been synced to the main spec repository:
- `openspec/specs/app-navigation/spec.md` ✓
- `openspec/specs/board-export-import/spec.md` ✓
- `openspec/specs/board-management/spec.md` ✓
- `openspec/specs/game-play/spec.md` ✓
- `openspec/specs/game-session/spec.md` ✓
- `openspec/specs/theme-preference/spec.md` ✓
- `openspec/specs/ui-localization/spec.md` ✓
- `openspec/specs/win-prediction/spec.md` ✓

### Verification Findings

#### CRITICAL
None. Archive proceeds without blockers.

#### WARNING
1. **BoardDao.importNew in-batch dedup**: Uses `ids.add(id) && identifiers.add(identifier)`. When an entry has a new id but duplicate identifier, its id is recorded in the seen set although the entry is skipped. Later in-batch entries with that same id and fresh identifier are skipped. Android adds both sets only on success.
   - **Status**: Fixed in commit c008684 with covering test `import_rejected_entry_does_not_reserve_its_id` (regression test added).
   - **Evidence**: orchestrator final-state facts.

2. **Dead ShellState code**: `ShellState` is constructed in `AppContainer` but its `destination` flow is not consumed by UI. `RightPane` derives destination independently.
   - **Status**: Fixed in commit c008684 (removed `ShellState` dead code). `RightPaneDestination.kt` and `RightPaneDestinationTest.kt` remain and are tested.
   - **Evidence**: orchestrator final-state facts.

3. **UI-only scenarios unverified**: Four spec scenarios have no automated test:
   - Minimum window size (1200x700 is constant in Main.kt, `window.minimumSize` not asserted)
   - Menu entries (AppMenu.kt has MenuBar with export/import/theme items; no automated test)
   - Empty state with create action (composable in BoardsPane.kt; only `boardAddedOrDeleted_cardsFollow` at state level)
   - Cancel end game (EndGameDialog is composable-local state; no automated test)
   - CreateBoardDialog relayout not visually verified (logic and focus order tested)
   - **Status**: Known follow-ups per design. Pending manual checks recorded in apply-progress.md.
   - **Evidence**: verify-report.md, apply-progress.md.

4. **Resources in commonMain instead of desktopMain**: Spec design alias `R` said resources should live in `desktopMain`, but they are in `src/commonMain/composeResources` per CMP conventions.
   - **Status**: Documented deviation from tasks.md alias. Behavior correct, just namespace differs.
   - **Evidence**: verify-report.md "Known Deviations Assessment", apply-progress.md.

#### SUGGESTION
1. Near-win badge on winner cards for a different pattern may confuse users; consider hiding it.
2. Add a confirmation or undo for COLUMNA dismiss, matching Android.
3. Add a coverage tool (Kover) and Compose UI test setup for the four UI-only scenarios.
4. `SmokeTest` is scaffold-only and can be removed.

## Pending Manual Checks (Known Follow-ups)

Per apply-progress.md and verify-report.md:
- CreateBoardDialog 5x5 relayout and new card header on screen
- 1920x1080 (~15 cards) and 1200x700 windows (minimum size, typical size)
- English/French locale walkthrough
- Menu/file dialogs/clipboard interactions
- Windows/Linux packaging

Per orchestrator launch prompt:
- User stated these manual checks are not needed now (deferred follow-ups)

## Deferred Enhancements

Per orchestrator launch prompt, these are intentional follow-ups:
- Isolated data-dir override for smoke runs (user said not needed now)
- Near-win badge on winner cards
- COLUMNA dismiss confirmation
- Kover and Compose UI tests
- Removing SmokeTest
- App icons

## Archive Integrity

### Spec Sync
- **8 delta specs synced** to main spec repository
- **Source**: `openspec/changes/desktop-two-pane-port/specs/*/spec.md`
- **Destination**: `openspec/specs/*/spec.md`
- **Verification**: Each source file byte-identical to destination (diff -r confirmed)
- **.gitkeep removed** from `openspec/specs/` (now has content)

### Folder Move
- **Source**: `openspec/changes/desktop-two-pane-port/`
- **Destination**: `openspec/changes/archive/2026-10-04-desktop-two-pane-port/`
- **Method**: `git mv` (tracked files) + add (untracked verify-report.md)
- **Contents archived**:
  - proposal.md ✓
  - design.md ✓
  - specs/ (8 capability subdirs) ✓
  - tasks.md ✓
  - apply-progress.md ✓
  - verify-report.md ✓
  - archive-report.md ✓
- **Verification**: Active change folder confirmed deleted; archived folder contains all expected files

### Final Repository State
- Main branch: clean working tree, all changes committed
- Git status: no staged or unstaged changes after archive commit
- Commit message: `docs: archive desktop-two-pane-port change and sync specs` (conventional commit, no AI attribution)

## Artifact Locations

### Archived Artifacts
- Proposal: `openspec/changes/archive/2026-10-04-desktop-two-pane-port/proposal.md`
- Design: `openspec/changes/archive/2026-10-04-desktop-two-pane-port/design.md`
- Specs: `openspec/changes/archive/2026-10-04-desktop-two-pane-port/specs/*/spec.md` (8 capability specs)
- Tasks: `openspec/changes/archive/2026-10-04-desktop-two-pane-port/tasks.md`
- Apply Progress: `openspec/changes/archive/2026-10-04-desktop-two-pane-port/apply-progress.md`
- Verify Report: `openspec/changes/archive/2026-10-04-desktop-two-pane-port/verify-report.md`
- Archive Report: `openspec/changes/archive/2026-10-04-desktop-two-pane-port/archive-report.md`

### Main Specs (Synced)
- `openspec/specs/app-navigation/spec.md`
- `openspec/specs/board-export-import/spec.md`
- `openspec/specs/board-management/spec.md`
- `openspec/specs/game-play/spec.md`
- `openspec/specs/game-session/spec.md`
- `openspec/specs/theme-preference/spec.md`
- `openspec/specs/ui-localization/spec.md`
- `openspec/specs/win-prediction/spec.md`

## Engram Observation IDs

Engram observations saved during this session (hybrid mode):
- `sdd/desktop-two-pane-port/archive-report` — this report

## SDD Cycle Completion

| Phase | Status | Evidence |
|-------|--------|----------|
| sdd-propose | ✓ Complete | Proposal in archive, defines scope/approach/rollback |
| sdd-spec | ✓ Complete | 8 capability specs in archive, synced to main |
| sdd-design | ✓ Complete | Design document in archive, decisions documented |
| sdd-tasks | ✓ Complete | 56 tasks in archive, all checked as complete |
| sdd-apply | ✓ Complete | 10 work units committed on main (60a661c through de958c2) |
| sdd-verify | ✓ Complete | Verification report in archive, PASS WITH WARNINGS, 0 CRITICAL |
| sdd-archive | ✓ Complete | This report, folder archived, specs synced, commit created |

**Cycle Status**: CLOSED. The desktop-two-pane port change is fully archived and ready for reference. The main specs repository has been updated with all capability specs. Future changes reference these as their baseline.

---

**Archive created**: 2026-10-04  
**Prepared by**: sdd-archive phase executor
