# NeuroVerse Auth App (ACT-3831)

Android authentication app in Kotlin (Jetpack Compose, MVVM + Repository, Coroutines).

## Status: partial submission

### Done
- 6 screens: Welcome, Registration, Login, Forgot Password, Home, User Profile
- Input validation (name, email, password minimum 8 characters)
- Show/hide password on login
- Logout returns to Welcome
- Basic route guard: Home and Profile redirect to Welcome when there is no signed-in user
- Neutral forgot-password message that does not reveal whether an account exists
- 85 unit tests passing (`./gradlew :app:testDebugUnitTest`)

### Not done (pending)
- Firebase Auth integration. The app currently uses an in-memory FakeAuthRepository, so accounts are lost when the app process restarts.
- Persistent session restored on restart, and secure token storage
- Full route protection backed by a real auth session
- Documented device test results for the 10 scenarios (only Registration was checked on a device)
- Architecture and auth-flow documents

### Assumptions (not yet approved by NeuroVerse)
- Stack: Compose, MVVM, Firebase Auth (Email/Password)
- Password minimum of 8 characters

## Build
1. Open in Android Studio, or run: `./gradlew :app:assembleDebug`
2. APK: `app/build/outputs/apk/debug/app-debug.apk`

## Notes
- `google-services.json` is git-ignored and is not included.
