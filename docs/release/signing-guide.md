# Signing Guide

## Upload Keystore Generation

Generate the upload keystore (one-time, keep the file safe):

```bash
mkdir -p release
keytool -genkeypair -v \
  -keystore release/upload-keystore.jks \
  -keyalg RSA -keysize 2048 \
  -validity 10000 \
  -alias upload
```

## keystore.properties

Create `keystore.properties` in the project root (gitignored):

```properties
storeFile=release/upload-keystore.jks
storePassword=<your-password>
keyAlias=upload
keyPassword=<your-password>
```

## Building a Release

```bash
./gradlew bundleRelease   # AAB for Play Store
./gradlew assembleRelease  # APK for direct install
```

Output locations:
- AAB: `app/build/outputs/bundle/release/app-release.aab`
- APK: `app/build/outputs/apk/release/app-release.apk`

## Play App Signing

We recommend enrolling in [Google Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756) during your first Play Console upload. Google manages the actual signing key; the upload keystore above becomes your upload key only. This protects against keystore loss.

## Backup

Store a backup of `release/upload-keystore.jks` and `keystore.properties` in a secure location outside the repository (e.g., password manager, encrypted cloud storage). If you lose the upload key after enrolling in Play App Signing, you can request a key reset through Play Console. If you lose it *without* Play App Signing, you cannot update your app.
