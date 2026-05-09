# CIT-Care Full Regression Test Report

## Project Information

- Project: CIT-Care
- Test date: 2026-05-09
- Tested by: Codex
- Scope: Backend Spring Boot API, React web frontend, available mobile test command
- Reason for testing: Full regression after vertical slice refactoring

## Refactoring Summary

The backend was refactored from type-based packages into feature-oriented vertical slices:

- `auth`
- `appointments`
- `services`
- `admin`

The frontend was refactored into feature folders:

- `features/auth`
- `features/home`
- `features/appointments`

Shared backend persistence classes remain in `entity` and `repository` packages to avoid changing database mappings and data access contracts during this structural refactor.

## Updated Project Structure

Backend:

```text
backend/citcare/citcare/src/main/java/edu/cit/abella/citcare
  admin/
    AdminController.java
    RoleUpdateRequest.java
  appointments/
    AppointmentController.java
    AppointmentRequest.java
    AppointmentService.java
  auth/
    AuthController.java
    AuthService.java
    CustomOAuth2UserService.java
  config/
    SecurityConfig.java
  entity/
    Appointment.java
    ServiceEntity.java
    User.java
  repository/
    AppointmentRepository.java
    ServiceRepository.java
    UserRepository.java
  services/
    ServiceController.java
    ServiceService.java
```

Frontend:

```text
web/citcare/src
  features/
    appointments/
      BookingModal.jsx
      BookingModal.css
      __tests__/BookingModal.test.jsx
    auth/
      AuthSuccess.jsx
      Login.jsx
      Login.css
      Register.jsx
      Register.css
      __tests__/
    home/
      Home.jsx
      Home.css
      __tests__/Home.test.jsx
  test/
    setupTests.jsx
  App.jsx
  main.jsx
```

## Test Plan Documentation

The complete Software Test Plan is available at:

- `docs/TEST_PLAN.md`

It includes functional requirements coverage, manual test cases, test steps, automated test cases, and command scripts.

## Automated Test Evidence

### Backend

Command:

```powershell
cd backend\citcare\citcare
cmd /c mvnw.cmd test
```

Result:

```text
Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Coverage added:

- `AuthServiceTest`
- `AppointmentServiceTest`
- `ServiceServiceTest`
- `CitcareApplicationTests` using `test` profile and H2 database

### Frontend Tests

Command:

```powershell
cd web\citcare
npm.cmd test
```

Result:

```text
Test Files  4 passed (4)
Tests       7 passed (7)
```

Coverage added:

- `Login.test.jsx`
- `Register.test.jsx`
- `BookingModal.test.jsx`
- `Home.test.jsx`

### Frontend Lint

Command:

```powershell
cd web\citcare
npm.cmd run lint
```

Result:

```text
eslint completed with 0 errors
```

### Frontend Build

Command:

```powershell
cd web\citcare
npm.cmd run build
```

Result:

```text
vite build completed successfully
50 modules transformed
```

### Mobile Regression Check

Command:

```powershell
cd mobile
cmd /c gradlew.bat test
```

Result:

```text
Failed: Unable to access jarfile ...\mobile\gradle\wrapper\gradle-wrapper.jar
```

Notes:

- The mobile Gradle wrapper could not run from the current Unicode OneDrive path because the Java process resolved part of the path as `??`.
- No mobile source code changes were made during the vertical slice refactor.
- Mobile tests should be rerun from an ASCII-only path or after fixing the wrapper path issue.

## Regression Test Results

| Area | Test Type | Result | Notes |
|---|---|---|---|
| Backend compilation | Automated | Passed | Maven compiled main and test sources. |
| Backend context startup | Automated | Passed | Uses H2 test profile instead of remote Supabase. |
| Backend auth | Automated | Passed | Registration, duplicate email, login, invalid credentials. |
| Backend appointments | Automated | Passed | Create, list by student, approve, reject. |
| Backend services | Automated | Passed | List, save, find by ID, missing service error. |
| Frontend auth | Automated | Passed | Login success/failure and registration success. |
| Frontend booking modal | Automated | Passed | Modal render, service option, submit behavior, closed state. |
| Frontend home/dashboard | Automated | Passed | Guest landing and authenticated appointment table. |
| Frontend lint | Automated | Passed | 0 ESLint errors after fixes. |
| Frontend production build | Automated | Passed | Vite build successful. |
| Mobile unit tests | Automated attempt | Blocked | Gradle wrapper path issue in Unicode OneDrive path. |

## Issues Found

| Issue ID | Description | Severity | Status |
|---|---|---|---|
| ISS-01 | Backend tests used production Supabase configuration and failed when remote tenant was unavailable. | High | Fixed |
| ISS-02 | Frontend test runner initially could not load Vite config inside sandbox path. | Medium | Worked around by running with approved unrestricted command. |
| ISS-03 | Vitest localStorage was not stable in the current environment. | Medium | Fixed |
| ISS-04 | Login test matched both manual sign-in and Google sign-in buttons. | Low | Fixed |
| ISS-05 | Booking modal test submit was blocked by required form fields. | Low | Fixed |
| ISS-06 | ESLint found unused `catch` parameters in Login and Register. | Low | Fixed |
| ISS-07 | ESLint found a React hooks warning in Home state initialization/fetch flow. | Medium | Fixed |
| ISS-08 | Mobile Gradle wrapper could not run from Unicode OneDrive path. | Medium | Open environment issue |

## Fixes Applied

1. Added H2 test dependency to backend `pom.xml`.
2. Added `application-test.properties` for isolated backend tests.
3. Updated `CitcareApplicationTests` to use the `test` profile.
4. Added backend unit tests for `auth`, `appointments`, and `services` slices.
5. Installed and configured Vitest, Testing Library, jest-dom, and jsdom for frontend tests.
6. Added frontend automated tests for login, registration, home/dashboard, and booking modal.
7. Fixed frontend unused catch parameters.
8. Refined `Home.jsx` initialization and appointment loading to satisfy React lint rules.
9. Added stable test localStorage setup for Vitest.

## Final Regression Status

Backend and web frontend regression testing passed after fixes.

Mobile regression execution is blocked by an environment path issue and should be rerun from an ASCII-only local path.

