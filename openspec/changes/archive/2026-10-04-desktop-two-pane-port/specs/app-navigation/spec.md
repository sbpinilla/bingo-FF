# App Navigation Specification

## Purpose

A single two-pane desktop window replaces multi-screen navigation.

## Requirements

### Requirement: Two-pane shell

The window MUST show a left pane and a right pane side by side at all times, with no narrow or stacked layout and a minimum window size of about 1200x700.

#### Scenario: Minimum size
- GIVEN the window
- WHEN the user shrinks it
- THEN it cannot go below about 1200x700 and both panes remain visible

### Requirement: Pane proportions

The left (boards) pane MUST take about 2/3 of the window width and the right (game) pane about 1/3.

#### Scenario: Width split
- GIVEN a 1920x1080 window
- WHEN the shell renders
- THEN the left pane is about 1280 px wide and the right pane about 640 px

### Requirement: Adaptive boards grid

The left pane MUST render boards as an adaptive grid of compact cards (5x5 each, FREE at N3). The column count MUST adapt to the available width, and the grid MUST scroll vertically when boards exceed the viewport. On a typical 1920x1080 window about 15 boards (e.g. 5 columns x 3 rows) SHOULD be visible at once. Called marks, near-win badges and winner highlight MUST remain legible at the compact size.

#### Scenario: Typical window
- GIVEN 20 boards and a 1920x1080 window
- WHEN the left pane renders
- THEN about 15 cards are visible (5 columns x 3 rows) and the rest are reachable by vertical scroll

#### Scenario: Narrower left pane
- GIVEN the window is resized to its minimum width
- WHEN the left pane renders
- THEN fewer columns are used, cards keep their size and the grid scrolls vertically

### Requirement: Left pane

The left pane MUST show all boards in the adaptive grid with called numbers marked live, boards likely to win badged with their missing count, winners strongly highlighted, per-card delete, and an action opening the creation dialog.

#### Scenario: Live mark
- GIVEN an active game
- WHEN a number is called
- THEN that number is marked on every board containing it immediately

#### Scenario: Winner highlight
- GIVEN a board completes a pattern
- WHEN the left pane renders
- THEN the card is strongly highlighted

#### Scenario: No game
- GIVEN no active game
- WHEN the left pane renders
- THEN boards show without marks or badges

#### Scenario: Empty
- GIVEN no boards
- WHEN rendered
- THEN an empty state with the create action is shown

### Requirement: Right pane

The right pane MUST use internal navigation between two destinations: Setup (mode chips, start) and Play (number input, per-letter called grid, announcements, possible winners, end game). Start moves Setup to Play; ending a game with confirmation moves Play back to Setup. When an active game is restored at launch, the pane MUST open directly in Play.

#### Scenario: Switch on start/end
- GIVEN Setup is shown
- WHEN a game starts, then ends with confirmation
- THEN the pane shows Play, then Setup

#### Scenario: Restored game
- GIVEN a persisted active game
- WHEN the app launches
- THEN the right pane opens directly in Play, never flashing Setup

### Requirement: Shell menu

A menu MUST provide import, export and theme selection.

#### Scenario: Menu entries
- GIVEN the menu is opened
- WHEN inspected
- THEN import, export and Light/Dark/System are available
