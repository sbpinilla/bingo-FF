# Game Play Specification

## Purpose

Call numbers during an active game, detect wins live and present state. Win patterns (cells satisfied when FREE or called): COLUMNA = any one column (ids COLUMN_B..COLUMN_O); O = rows 1 and 5 of all columns plus rows 2-4 of B and O (id O); L = whole B column plus row 5 (id L); I = rows 1 and 5 plus whole N column (id I); CARTON_COMPLETO = all 25 cells (id FULL_CARD). Letter ranges: B 1-15, I 16-30, N 31-45, G 46-60, O 61-75.

## Requirements

### Requirement: Play destination in the right pane

Play MUST be the right pane's second internal destination (about 1/3 of the window width), reached from Setup, and its content MUST fit and remain usable at that width, scrolling vertically if needed. Calls update the left-pane compact cards live.

#### Scenario: Live left-pane update
- GIVEN Play is shown with boards in the left grid
- WHEN a number is called
- THEN compact cards containing it are marked and badges and highlights update immediately

### Requirement: Number input

The right pane MUST provide a number input. The letter is derived automatically from the typed number unless the user manually selected a letter (override).

#### Scenario: Derived letter
- GIVEN no override
- WHEN the user types 47
- THEN letter G is selected

#### Scenario: Accept call
- GIVEN 47 is valid and not yet called
- WHEN the user submits
- THEN 47 is appended to the called numbers, persisted, and the input is cleared

### Requirement: Input errors

Submit MUST be rejected with a reason type (not a string): InvalidNumber for non-numeric or outside 1..75; LetterMismatch when an overridden letter differs from the number's real letter; DuplicateCall(number) when already called, which also clears the input.

#### Scenario: Invalid
- GIVEN input "80"
- WHEN submitted
- THEN InvalidNumber is exposed and no call is added

#### Scenario: Mismatch
- GIVEN letter B manually selected and input 47
- WHEN submitted
- THEN LetterMismatch is exposed and no call is added

#### Scenario: Duplicate
- GIVEN 47 was already called
- WHEN 47 is submitted again
- THEN DuplicateCall(47) is exposed and the input is cleared

### Requirement: Called grid

The pane MUST show called numbers grouped per letter, in call order, plus the total called count.

#### Scenario: Grouping
- GIVEN calls 47, 3, 52
- WHEN rendered
- THEN G lists 47, 52; B lists 3; count is 3

### Requirement: Win detection and announcements

For every board and pattern of the active mode, a win MUST be announced exactly once when all pattern cells are satisfied (evaluated per board, never a global tally). Announcements show board identifier and pattern. Announced pairs are rebuilt by replaying the called numbers in order, so they survive restarts. Dismissed COLUMNA letters MUST NOT affect win detection.

#### Scenario: New win
- GIVEN a board needs only B7 for column B
- WHEN 7 is called
- THEN one announcement for that board and COLUMN_B appears

#### Scenario: No re-announce
- GIVEN that win was announced
- WHEN further numbers are called
- THEN it is not announced again

#### Scenario: Restart replay
- GIVEN a game with an announced win
- WHEN the app restarts
- THEN the same winners are rebuilt from the call order

#### Scenario: Dismissed letter still wins
- GIVEN letter B dismissed in COLUMNA
- WHEN a board completes column B
- THEN a win is still announced

### Requirement: Dismiss COLUMNA letter

In COLUMNA mode the user MUST be able to toggle a letter as dismissed via a per-row toggle or right-click. The set persists.

#### Scenario: Toggle
- GIVEN letter G is not dismissed
- WHEN the user toggles it twice
- THEN it is dismissed, then restored
