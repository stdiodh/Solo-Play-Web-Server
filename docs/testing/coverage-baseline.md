# Test coverage baseline

Measured on 2026-07-30 (Asia/Seoul).

## Source baseline

- Branch: `develop`
- Requested reference commit: `de03af7`
- Measured commit: `378fb10`
- Difference: three local cleanup/documentation commits after `de03af7`
  (`41fbdcd`, `3bf6bfb`, `378fb10`)
- Existing tests: 31 passed, 0 failed, 0 skipped

The commits after `de03af7` update the README, remove unused code and a
duplicate test dependency, and normalize the `JwtProvider` file name. The
coverage baseline below is therefore the actual pre-change state of the
current `develop` branch.

## Initial coverage

JaCoCo 0.8.12 was run without exclusions or a coverage gate. The measurement
includes all compiled production code under `src/main/kotlin`.

| Counter | Covered | Missed | Total | Coverage |
| --- | ---: | ---: | ---: | ---: |
| LINE | 528 | 131 | 659 | 80.12% |
| BRANCH | 41 | 165 | 206 | 19.90% |

## Final coverage and ratchet

After the regression scenarios were added, the same unfiltered production
scope measured as follows:

| Counter | Covered | Missed | Total | Coverage | Gate |
| --- | ---: | ---: | ---: | ---: | ---: |
| LINE | 593 | 87 | 680 | 87.21% | 87% |
| BRANCH | 165 | 61 | 226 | 73.01% | 73% |

The verification thresholds are the measured percentages rounded down to
whole percentage points. Both limits apply to the full JaCoCo bundle; no
custom package, class, or source exclusions are configured.

Final tests: 99 passed, comprising the preserved 31 existing tests and 68
new regression tests.

Baseline commands:

```text
./gradlew clean test --no-daemon
./gradlew jacocoTestReport --no-daemon
```

Final verification commands:

```text
./gradlew clean test --no-daemon
./gradlew jacocoTestReport jacocoTestCoverageVerification --no-daemon
./gradlew clean check --no-daemon
```

Reports:

- XML: `build/reports/jacoco/test/jacocoTestReport.xml`
- HTML: `build/reports/jacoco/test/html/index.html`
