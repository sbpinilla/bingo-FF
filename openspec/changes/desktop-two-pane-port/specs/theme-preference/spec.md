# Theme Preference Specification

## Purpose

User-selectable Light, Dark or System theme, persisted to a file.

## Requirements

### Requirement: Theme choices and default

The shell menu MUST offer Light, Dark and System. The default, and the fallback for a missing or unrecognized stored value, MUST be System.

#### Scenario: First launch
- GIVEN no stored preference
- WHEN the app launches
- THEN mode is System and follows the OS theme

#### Scenario: Corrupt value
- GIVEN the stored value is unrecognized
- WHEN the app launches
- THEN mode is System

### Requirement: Immediate application

Changing the mode MUST re-theme the whole window immediately without restart.

#### Scenario: Switch to Dark
- GIVEN mode Light
- WHEN the user selects Dark
- THEN the UI switches to the dark palette at once

### Requirement: Persistence

The selection MUST persist in a file-backed preference and be restored on launch.

#### Scenario: Restore
- GIVEN the user selected Dark
- WHEN the app restarts
- THEN Dark is applied
