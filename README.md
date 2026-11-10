# Arindom Neon X

Android-first social platform project. The repository is being upgraded in milestones from an interface prototype to a production application.

## Current milestone: native Android + managed backend foundation

- Native Kotlin + Jetpack Compose launcher (the old WebView prototype remains in the repository as a reference).
- Firebase SDK foundations for Authentication, Cloud Firestore, Cloud Storage and Cloud Messaging.
- Email/password sign-in and account creation UI, activated after Firebase is configured.
- Initial Firestore and Storage security rules.
- CI build on the production-foundation feature branch.

## Firebase setup (required to activate cloud services)

1. Create a Firebase project in the Firebase Console.
2. Add an Android app with package name `com.arindom.social`.
3. Enable **Authentication → Sign-in method → Email/Password**.
4. Create a Cloud Firestore database and a Storage bucket.
5. Download the Android `google-services.json` file and place it at `app/google-services.json` locally. This file is ignored by Git.
6. Publish the repository's `firestore.rules` and `storage.rules` in the Firebase Console.
7. For CI builds with Firebase enabled, add a GitHub Actions repository secret named `GOOGLE_SERVICES_JSON` containing the complete JSON file contents.

Without Firebase configuration the app still builds and opens, but authentication and cloud publishing remain disabled. Never put service-account private keys or admin credentials in the Android app.

## Build

The GitHub Actions workflow runs `gradle assembleDebug` and uploads the APK artifact. The app uses JDK 17 and Gradle 8.9.

## Next milestones

1. Connect Firebase project configuration and verify real sign-up/sign-in.
2. Add user profile documents and account lifecycle.
3. Implement image/video uploads with Storage and cloud feed rendering.
4. Add follows, likes, comments, notifications and real-time messaging.
5. Add moderation, privacy controls, analytics, accessibility and release hardening.

## Security notes

The initial rules are a starting point, not a complete production policy. Public post reads are currently allowed; private accounts, visibility controls, abuse prevention, moderation and rate limits must be implemented and tested before a public launch.
