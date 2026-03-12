# Release Checklist — Steps of Babylon v1.0.0

## Build Configuration

- [x] `isMinifyEnabled = true` for release build type
- [x] `isShrinkResources = true` for release build type
- [x] ProGuard/R8 rules hardened (Room, Hilt, SQLCipher, Health Connect, sensors, WorkManager)
- [ ] `fallbackToDestructiveMigration` removed *(re-added in R05 for pre-release safety — remove before production)*
- [x] `versionName = "1.0.0"`, `versionCode = 1`
- [ ] Upload keystore generated (`release/upload-keystore.jks`)
- [ ] `keystore.properties` created with credentials
- [x] Keystore files in `.gitignore`

## Documentation

- [x] Privacy policy written (`docs/release/privacy-policy.md`)
- [x] Play Store listing text written (`docs/release/play-store-listing.md`)
- [x] Signing guide written (`docs/release/signing-guide.md`)
- [x] CHANGELOG updated with v1.0.0 release notes

## Pre-Release Verification

- [ ] Release APK installs and runs on API 34 device/emulator
- [ ] Release APK installs and runs on API 36 device/emulator
- [ ] Step counting works in background (release build)
- [ ] Health Connect permissions and step reading works
- [ ] No ANRs or crashes in 30-minute play session
- [ ] R8 didn't break any functionality (all screens load, battle runs, notifications fire)
- [ ] All notification channels work
- [ ] Widget renders correctly
- [ ] Battery usage acceptable (< 5% per day for step counting)
- [ ] All 399 unit tests pass

## Play Store Assets (Plan 31)

- [ ] App icon: 512×512 PNG
- [ ] Feature graphic: 1024×500 PNG
- [ ] Screenshots: minimum 2, recommended 8
- [ ] Privacy policy hosted at public URL
- [ ] Play App Signing enrollment

## Build Outputs

- AAB: `app/build/outputs/bundle/release/app-release.aab`
- APK: `app/build/outputs/apk/release/app-release.apk`
