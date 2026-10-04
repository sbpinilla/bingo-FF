# Win Prediction Specification

## Purpose

Surface (board, pattern) pairs close to winning. Follows the Android code, not its stale spec text: there is NO ceiling on the number of calls.

## Requirements

### Requirement: Qualification thresholds

A pair qualifies when missing = pattern cell count minus satisfied cells (FREE counts as satisfied) is at most: COLUMNA 2, O 3, L 3, I 3, CARTON_COMPLETO 10. Qualification depends only on the pair's own missing count, however many unrelated numbers were called.

#### Scenario: COLUMNA boundary
- GIVEN a board column with 3 uncalled numbers, then 1 more is called
- WHEN predictions refresh
- THEN the column qualifies with missing=2 and did not before

#### Scenario: No call ceiling
- GIVEN 60 numbers called elsewhere and a column at missing=2
- WHEN predictions refresh
- THEN the column is still a candidate

#### Scenario: FULL threshold
- GIVEN CARTON_COMPLETO and a board with 11 uncalled cells
- WHEN 1 more is called
- THEN the board qualifies with missing=10

### Requirement: Exclusions

A pair already announced as a win MUST be excluded, and in COLUMNA a candidate whose letter is dismissed MUST be excluded from the displayed list. A dismissed letter's column win is voided (it is not announced), so its candidates stay excluded by the letter filter alone; reopening the letter restores both its win and its candidates.

#### Scenario: Announced excluded
- GIVEN a board completed column B
- WHEN predictions refresh
- THEN no B candidate exists for that board

#### Scenario: Dismissed filtered
- GIVEN letter G dismissed
- WHEN predictions refresh
- THEN no G candidates are listed

#### Scenario: Dismissed completed column stays hidden
- GIVEN letter B dismissed and a board with column B complete, so its win is voided
- WHEN predictions refresh
- THEN no B candidate is listed for that board

### Requirement: Ordering

Candidates MUST sort by missing ascending, then boardId ascending, then letter ordinal (B,I,N,G,O); non-COLUMNA candidates have no letter.

#### Scenario: Order
- GIVEN candidates (board 2, B, missing 1), (board 1, G, missing 2), (board 1, B, missing 2)
- WHEN sorted
- THEN order is board 2 B; board 1 B; board 1 G

### Requirement: Live display

The possible-winners list in the right pane and a badge showing the missing count on each likely board in the left pane MUST update live on each call and on restore.

#### Scenario: Badge
- GIVEN a qualifying board with missing=2
- WHEN the left pane renders
- THEN its card shows a badge with 2
