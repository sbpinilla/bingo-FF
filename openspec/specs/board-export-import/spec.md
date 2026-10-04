# Board Export/Import Specification

## Purpose

Move boards in and out as a bare top-level JSON array `[{id, identifier, numbers}]`, with no wrapper or version field, compatible with Android exports in both directions.

## Requirements

### Requirement: Export to file

The system MUST offer export from the shell menu via a native save dialog that writes a .json file in the bare array format. It MAY also copy the same JSON to the clipboard.

#### Scenario: Export file
- GIVEN boards exist
- WHEN the user exports and picks a path
- THEN the file holds a bare JSON array of `{id, identifier, numbers}` objects, one per board

#### Scenario: Dialog cancelled
- GIVEN the save dialog is open
- WHEN the user cancels
- THEN no file is written and no error is shown

### Requirement: Round-trip compatibility

Files exported by Android MUST import on desktop, and desktop exports MUST import on Android.

#### Scenario: Round trip
- GIVEN boards exported to JSON
- WHEN the JSON is imported into an empty database
- THEN boards with identical id, identifier and numbers exist

### Requirement: Import sources

Import MUST accept a native open-file dialog (.json) and pasted JSON text.

#### Scenario: Import from file
- GIVEN a valid JSON file chosen in the open dialog
- WHEN the import runs
- THEN boards are imported and a summary is shown

#### Scenario: Blank paste
- GIVEN blank pasted text
- WHEN the user submits
- THEN a BlankInput reason is exposed and nothing is imported

### Requirement: All-or-nothing on malformed input

Any structural or type error (invalid JSON, missing or wrong-typed field, non-array root) MUST reject the whole payload, import nothing and expose an InvalidJson reason.

#### Scenario: Malformed
- GIVEN JSON where one object lacks `numbers`
- WHEN the user imports
- THEN InvalidJson is exposed and no board is added, including the valid ones

### Requirement: Dedup and summary

An entry whose id OR identifier already exists in storage, or repeats an earlier entry in the same payload, MUST be skipped (first occurrence kept). Imported boards keep their original id. After import a summary with imported and skipped counts MUST be shown and clearable; pasted text is cleared after submit.

#### Scenario: Mixed payload
- GIVEN board id 1 "A" exists and the payload has {1,"X"}, {2,"A"}, {3,"C"}, {4,"C"}
- WHEN imported
- THEN only id 3 is added and the summary is imported=1, skipped=3
