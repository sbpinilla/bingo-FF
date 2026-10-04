# Apply Progress: desktop-two-pane-port

Mode: Standard (strict TDD flipped on at the end of unit 1). Store: hybrid. Delivery: exception-ok, size exception accepted.

## Unit 1: scaffold (DONE, tasks 1.1-1.8)

- [x] 1.1 settings/build/gradle.properties, Gradle wrapper 9.8.0 (sha256 pinned)
- [x] 1.2 `gradle/libs.versions.toml`, all pins verified
- [x] 1.3 kotlin-test, coroutines-test, `SmokeTest.smoke_test_runs`
- [x] 1.4 compose.desktop.application, Dmg/Msi/Deb (icons deferred, none shipped yet)
- [x] 1.5 `.github/workflows/build.yml` (macos/windows/ubuntu, Temurin 21)
- [x] 1.6 `Main.kt`, `ui/AppWindow.kt` (weights 2f/1f, 1440x860 default, 1200x700 min)
- [x] 1.7 `strict_tdd: true`, `testing.runner` updated, `.gitignore`
- [x] 1.8 Commit `chore: scaffold KMP desktop project with CI and two-pane shell`

### Verified toolchain pins

| Item | Pin | Verification |
|------|-----|--------------|
| Kotlin | 2.4.20 | Maven Central metadata lists 2.4.20 (stable) |
| Compose Multiplatform | 1.12.1 | Maven Central plugin metadata and marker dir exist |
| compose material3 | 1.9.0 | Maven Central; latest stable, plugin default; versioned separately from CMP |
| Room | 2.8.5 | Google Maven metadata lists 2.8.5 |
| sqlite-bundled | 2.6.2 | room-runtime 2.8.5 POM depends on androidx.sqlite 2.6.2 (2.7.x exists but is not Room's) |
| KSP | 2.3.12 | Maven Central, latest stable, plugin marker exists; independent versioning (KSP2) |
| coroutines core/test/swing | 1.11.0 | Maven Central metadata (swing too) |
| serialization-json | 1.11.0 | Maven Central metadata |
| Gradle | 9.8.0 | services.gradle.org current stable, zip sha256 matches published checksum |
| JDK | toolchain 17 | local Temurin 17.0.19 used; CI Temurin 21 |

Room, KSP, sqlite and serialization are catalog entries only (not applied until units 3/4).

### Work Unit Evidence

| Evidence | Value |
|---|---|
| Focused test command | `./gradlew test compileKotlinDesktop`: BUILD SUCCESSFUL; SmokeTest[desktop] tests=1 skipped=0 failures=0 |
| Runtime harness | `./gradlew packageDistributionForCurrentOS`: BUILD SUCCESSFUL, produced `BingoFF-1.0.0.dmg` (macOS). Window not launched (GUI would block). |
| Rollback boundary | Revert the unit 1 commit; leaves openspec planning docs only |

### Deviations / notes

- KMP has no lifecycle `test` task, so `build.gradle.kts` registers `test` as an alias of `desktopTest`.
- Icons (task 1.4) not added: no assets exist; jpackage uses its default icon. Add later with `iconFile`.
- material3 pinned explicitly because the `compose.material3` accessor is deprecated.
