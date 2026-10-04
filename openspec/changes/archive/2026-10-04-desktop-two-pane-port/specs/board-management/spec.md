# Board Management Specification

## Purpose

Create, list and delete bingo boards. A board has a numeric id (auto-assigned, ascending), a unique identifier and 24 numbers in column-major order B1-B5, I1-I5, N1,N2,N4,N5, G1-G5, O1-O5 (the N3 cell is FREE). Boards persist across restarts. No editing.

## Requirements

### Requirement: Board creation dialog

The system MUST let the user create a board from a modal dialog opened from the left pane, with an identifier field and 24 number fields grouped by letter (B 1-15, I 16-30, N 31-45 with 4 fields, G 46-60, O 61-75).

#### Scenario: Valid board is created
- GIVEN the dialog is open with a non-blank unused identifier and 24 distinct in-range numbers
- WHEN the user submits
- THEN the board is persisted, the dialog closes and the board appears in the left pane

#### Scenario: Blank identifier
- GIVEN the identifier is blank or whitespace
- WHEN the user submits
- THEN a BlankIdentifier reason is exposed, nothing is persisted and the dialog stays open

### Requirement: Number field validation

Validation MUST follow computeFieldErrors: a non-integer value is invalid; a value outside its letter range is out_of_range; a value duplicated among in-range values anywhere on the board is duplicate. Range takes precedence over duplicate and out-of-range values never count as duplicates. Blank fields are flagged only on submit (flagBlank=true); while editing after a failed submit they are not flagged. Submit MUST be blocked while any field has an error.

#### Scenario: Out of range
- GIVEN the B-column field holds 16
- WHEN the user submits
- THEN that field has an out_of_range error and nothing is persisted

#### Scenario: Duplicate across columns
- GIVEN two in-range fields both hold 7
- WHEN the user submits
- THEN both fields report duplicate

#### Scenario: Blank flagged only on submit
- GIVEN a prior failed submit and one blank field
- WHEN the user edits another field
- THEN the blank field is not flagged, but it is flagged on the next submit

### Requirement: Unique identifier

The system MUST reject a board whose identifier already exists. ViewModels expose a DuplicateIdentifier reason, not a string.

#### Scenario: Duplicate identifier
- GIVEN a board with identifier "A1" exists
- WHEN the user submits a valid board with identifier "A1"
- THEN DuplicateIdentifier is exposed and no board is added

### Requirement: Delete with confirmation

Each board card MUST offer delete; the board is removed only after the user confirms. There is no edit action.

#### Scenario: Confirm delete
- GIVEN a board card
- WHEN the user requests delete and confirms
- THEN the board is removed from persistence and the left pane

#### Scenario: Cancel delete
- GIVEN a pending delete confirmation
- WHEN the user dismisses it
- THEN the board remains

### Requirement: Compact card presentation

Each board MUST be shown as a compact 5x5 card inside the adaptive left-pane grid (about 2/3 of the window; about 15 cards visible on 1920x1080), with the identifier and a delete action, remaining legible.

#### Scenario: Many boards
- GIVEN 30 boards
- WHEN the left pane renders on 1920x1080
- THEN about 15 compact cards are visible and the grid scrolls vertically to the rest

### Requirement: Persistence and ordering

Boards MUST survive restarts and be listed ordered by id ascending.

#### Scenario: Restart
- GIVEN boards 3 and 1 were created
- WHEN the app restarts
- THEN both boards are listed, id 1 before id 3
