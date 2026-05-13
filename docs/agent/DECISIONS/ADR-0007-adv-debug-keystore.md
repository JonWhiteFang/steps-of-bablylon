# ADR-0007 — Android Developer Verification: register the package via the debug keystore

- **Status**: Accepted
- **Date**: 2026-05-13
- **Decision-maker**: solo dev (jpawhite) + AI agent
- **Related**: ADR-0005 (Billing SDK), Plan 31 walk-through, `docs/release/plan-31-walkthrough.md`

## Context

Google Play Console enforces **Android Developer Verification (ADV)** at package-name registration time — a 2025–2026 policy intended to combat package-name squatting and impersonation. ADV requires a developer to prove ownership of a public-key fingerprint that signs the app, before Play Console will let the developer publish anything against that package name.

There are two ADV paths (per Google's "Registering Android package names" support article):

- **Step 2A — registering a "new" package name.** For a package name that has never been seen on Android. The developer just submits any public-key certificate and Play Console accepts it.
- **Step 2B — registering an "existing" package name.** For a package name Google's known-package-names registry has seen on at least one Android device. Play Console offers a list of "eligible" public-key fingerprints (those that have signed installs of this package on Google-account-signed-in devices) and asks the developer to either:
  - Pick one of the eligible fingerprints (no rationale, immediate proof-of-ownership flow), OR
  - Expand "other keys", upload a different public cert, submit a written rationale, and wait for Google review (rejection risk).

When we attempted to register `com.whitefang.stepsofbabylon` during Plan 31's Phase E1, Play Console routed us into Step 2B with **exactly one eligible fingerprint** in the list: `47:E8:9F:0A:3D:C1:8C:EA:B4:F5:A5:80:4D:74:B0:9E:C6:67:92:3B:C6:49:5E:C6:05:2A:26:AD:48:9D:75:5D`. That was not the production release upload keystore we generated in Phase C (whose SHA-256 is `C4:00:72:90:D8:40:32:92:86:06:C0:E1:E4:CB:8E:86:95:80:6A:FE:54:81:A1:15:9A:74:93:62:F2:BE:BA:E8`).

Forensic check via `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android` confirmed the offered fingerprint is the **local Android debug keystore** at `~/.android/debug.keystore`. Every prior debug install of the app on a Google-account-signed-in device (developer phone, work emulators, partner test devices) had registered `com.whitefang.stepsofbabylon` + the debug fingerprint in Google's known-package-names registry.

This routes us into Step 2B against our intent. We need to make a choice.

## Decision

**Register the package name with the local Android debug keystore.** Pick the only eligible fingerprint from the list, run the proof-of-ownership flow with a debug-signed APK that bundles the Play-Console-issued snippet at `app/src/main/assets/adi-registration.properties`.

The production release upload keystore (`release/upload-keystore.jks`) is **not** the ADV-verified key. It is still the Play App Signing **upload key** for AAB uploads; ADV identity-verification and Play App Signing key roles are decoupled.

Optionally, in a future session, register the release upload keystore as an **additional** ADV key via the "Adding additional keys" flow (Play Console support article 16762301) so the release keystore can also sign verified ownership claims.

## Considered alternatives

### Alternative A — register the release upload keystore via the "other keys" path

- **Pros:**
  - Clean long-term: the release keystore is the ADV-verified key from day one. No coupling to a debug keystore that could regenerate or be lost when switching machines.
  - One key to track for both ADV and Play App Signing.
- **Cons:**
  - Requires a written rationale to Google.
  - Google review can take hours to days.
  - Rejection risk (unknown but non-zero — Google's own docs say the request "may be rejected").
  - Adds latency to a multi-day Plan 31 timeline that's already pressed by the new closed-test-before-production policy.

### Alternative B — register the debug keystore (chosen)

- **Pros:**
  - Path of least resistance: no rationale, no Google review, no rejection risk. The debug keystore is already eligible.
  - Unblocks Plan 31 immediately. Proof-of-ownership is a single 70 MB debug-APK upload.
  - ADV is identity verification, not signing-key authority. It doesn't matter cryptographically which of *our* keys signs that ownership assertion, as long as we control both. Both keystores are local-only.
- **Cons:**
  - Couples ADV ownership to a regenerable file. If `~/.android/debug.keystore` is deleted or the dev switches machines, the ADV-registered fingerprint is no longer locally signable. Mitigated by:
    - The debug keystore is regenerated automatically by AGP / Android Studio with new content; the *new* file would have a different fingerprint.
    - ADV registration is one-time. Once the package is registered, the developer owns it regardless of whether they keep the original debug keystore.
    - The release upload keystore is preserved (it's in the user's password manager) and can be added as an additional ADV key later if desired.
  - The debug keystore's password is universally `android` and the file is on a per-developer-machine basis. This isn't actually a security issue (each dev's debug keystore has its own private key), but it's tonally weird — usually you don't authoritatively use a debug artifact for production identity.

### Alternative C — use Step 2A (treat the package as "new")

- **Not available.** Play Console determines whether a package is "new" or "existing" via the known-packages registry; we cannot opt out of Step 2B once the registry has seen the package + a debug-signed install.

## Consequences

- **Immediate:** Phase E1 unblocked. Package `com.whitefang.stepsofbabylon` is registered to jpawhite's Play Console developer account.
- **Future signing flow unchanged:** AAB uploads in Phase G are signed by the release upload keystore (`release/upload-keystore.jks`) per `keystore.properties` and `app/build.gradle.kts`'s `signingConfigs.release` block. Play App Signing accepts the upload key, generates per-device APKs server-side, signs them with the Google-managed app signing key. ADV doesn't intercept this flow.
- **Brittleness:** if the developer's `~/.android/debug.keystore` is lost (machine wipe, accident), the ADV-registered fingerprint loses its local signing capability. Recovery options:
  - Add the release upload keystore as an additional ADV key now (Play Console article 16762301). Belt-and-braces.
  - Or, if recovery is needed later, the upload keystore is in the user's password manager and can be registered as an additional key on demand (subject to whatever Play Console rules apply at that point).
- **Documentation legacy:** the `docs/release/plan-31-walkthrough.md` doc was written before ADV existed. The walk-through's Phase C / Phase E1 blocks describe an older Play Console flow. A future doc-sync pass should add an "ADV" subsection capturing the debug-keystore decision so the next person walking through Plan 31 doesn't have to re-derive the choice. Note inline in `STATE.md` for now.

## Followups

- [ ] Optional: add release upload keystore (`C4:00:72:90:...`) as an additional ADV key via the article 16762301 flow. Could be done opportunistically — not blocking any release work.
- [ ] Update the Plan 31 walk-through to document the ADV step (will land alongside the next walk-through revision when production release is in progress).
- [ ] If the debug keystore is ever lost: register the release upload keystore as an additional key while the existing one is still valid, then the system is recoverable.
