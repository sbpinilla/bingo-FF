# UI Localization Specification

## Purpose

Spanish (default) and English UI text, selected from the OS/JVM locale.

## Requirements

### Requirement: Locale selection

The UI MUST use English when the JVM default locale language is en, and Spanish for every other locale. There MUST be no in-app language switcher.

#### Scenario: English locale
- GIVEN JVM locale en-US
- WHEN the app launches
- THEN all visible text is English

#### Scenario: Unsupported locale
- GIVEN JVM locale fr-FR
- WHEN the app launches
- THEN all visible text is Spanish

### Requirement: Key parity

Spanish and English resources MUST define exactly the same keys.

#### Scenario: Parity check
- GIVEN both resource sets
- WHEN their key sets are compared
- THEN they are identical

### Requirement: No strings in ViewModels

ViewModels MUST expose reason types (e.g. BlankIdentifier, DuplicateIdentifier, InvalidJson, InvalidNumber, LetterMismatch, DuplicateCall); the UI maps them to localized text, including parameterized ones such as the duplicated number and the import imported/skipped counts.

#### Scenario: Mapped reason
- GIVEN reason DuplicateCall(47) in an English locale
- WHEN shown
- THEN the localized message includes 47
