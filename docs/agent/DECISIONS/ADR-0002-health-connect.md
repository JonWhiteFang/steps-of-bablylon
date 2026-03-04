# ADR-0002: Health Connect instead of Google Fit

**Date:** 2026-03-04
**Status:** Accepted
**Context:** Plan 05 was originally written as "Google Fit Integration" using the Google Fit SDK for step cross-validation, gap-filling, and Activity Minute Parity.

## Decision

Use Health Connect (`androidx.health.connect:connect-client`) instead of Google Fit.

## Rationale

- Google Fit APIs are deprecated and shutting down in 2026.
- Health Connect is the official replacement, recommended by Google's migration guide.
- On our min SDK 34 (Android 14), Health Connect is a framework module — always available, no separate app install needed.
- No OAuth or Google Play Services dependency. Uses standard Android health permissions.
- Simpler API: `HealthConnectClient.getOrCreate()`, `aggregate()` for steps, `readRecords()` for exercise sessions.
- All our use cases (step cross-validation, exercise session reading, Activity Minute Parity) map cleanly to Health Connect APIs.

## Consequences

- All documentation references updated from "Google Fit" to "Health Connect".
- Plan 05 renamed to "Health Connect Integration".
- Permissions use `android.permission.health.READ_STEPS` and `android.permission.health.READ_EXERCISE` instead of Google Fit OAuth scopes.
- Privacy policy activity required in manifest for Health Connect permissions screen.
- Plan 25 (Anti-Cheat) references should use Health Connect for cross-validation.
