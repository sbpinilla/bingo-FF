# Game Session Specification

## Purpose

Choose a win mode, start one game, keep it across restarts, and end it. Modes: COLUMNA, O, L, I, CARTON_COMPLETO (default selection COLUMNA).

## Requirements

### Requirement: Mode selection and start

The right pane in setup state MUST show mode chips and a start action. Start MUST be available only when at least one board exists.

#### Scenario: Start
- GIVEN boards exist and mode L is selected
- WHEN the user starts
- THEN a game with mode L and no called numbers is active and the right pane shows play

#### Scenario: No boards
- GIVEN no boards exist
- WHEN setup is shown
- THEN start is disabled and an empty hint is displayed

### Requirement: Single active game

The system MUST hold at most one active game; starting is unavailable while one is active.

#### Scenario: Game active
- GIVEN a game is active
- WHEN the right pane renders
- THEN it shows Play, not Setup

### Requirement: Right-pane Setup/Play navigation

Within the right pane (about 1/3 of the window), Setup MUST lead to Play on start; ending a game with confirmation MUST return to Setup; a restored active game MUST open directly in Play.

#### Scenario: Restored opens in Play
- GIVEN a persisted active game
- WHEN the app launches
- THEN the right pane opens in Play

### Requirement: Persistence across restarts

The active game's mode, called numbers (in order) and dismissed COLUMNA letters MUST persist on every change and be restored on launch. Announced wins are not stored; they are replayed from the calls with dismissed COLUMNA letters voiding their column wins.

#### Scenario: Restore
- GIVEN an active COLUMNA game with calls 5, 20 and dismissed letter B
- WHEN the app is closed and relaunched
- THEN the game is active with the same mode, calls in order and dismissed set

#### Scenario: Clean launch
- GIVEN no game was active or the game was ended
- WHEN the app launches
- THEN setup is shown

### Requirement: End game with confirmation

Ending MUST require confirmation, and on confirm MUST clear the persisted game and return to setup. Boards are unaffected.

#### Scenario: Confirm end
- GIVEN an active game
- WHEN the user ends and confirms
- THEN persisted game state is cleared and setup is shown

#### Scenario: Cancel end
- GIVEN the end confirmation is shown
- WHEN the user dismisses it
- THEN the game continues unchanged
