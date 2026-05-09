# CIT-Care Software Test Plan

## Project Information

- Project: CIT-Care
- System type: Campus appointment and clinic management system
- Test date: 2026-05-09
- Scope: Spring Boot backend, React web frontend, and available mobile regression check
- Refactor under test: Vertical slice package/module refactor for backend and web frontend

## Test Objectives

1. Verify that implemented functional requirements still work after vertical slice refactoring.
2. Validate authentication, registration, appointment booking, service listing, and dashboard behavior.
3. Confirm automated tests cover the major backend and frontend modules.
4. Identify regressions, bugs, environment blockers, and fixes applied.

## Functional Requirements Coverage

| Requirement ID | Functional Requirement | Backend Coverage | Frontend Coverage | Automated Coverage |
|---|---|---|---|---|
| FR-01 | Users can register with full name, email, password, and role. | `AuthService.registerUser`, `/api/v1/auth/register` | Register page form | `AuthServiceTest`, `Register.test.jsx` |
| FR-02 | Users can log in with email and password. | `AuthService.loginUser`, `/api/v1/auth/login` | Login page form | `AuthServiceTest`, `Login.test.jsx` |
| FR-03 | Duplicate registration emails are rejected. | `AuthService.registerUser` | Error display path | `AuthServiceTest` |
| FR-04 | Invalid login credentials are rejected. | `AuthService.loginUser` | Login error message | `AuthServiceTest`, `Login.test.jsx` |
| FR-05 | Google OAuth login can create or find a user. | `CustomOAuth2UserService` | Google login redirect button | Manual procedure |
| FR-06 | Authenticated users can view their dashboard. | Student appointment endpoint | Home dashboard view | `Home.test.jsx` |
| FR-07 | Users can view their scheduled appointments. | `AppointmentService.getStudentAppointments` | Dashboard appointment table | `AppointmentServiceTest`, `Home.test.jsx` |
| FR-08 | Users can book appointments. | `AppointmentService.createAppointment`, `/api/appointments/book` | Booking modal | `AppointmentServiceTest`, `BookingModal.test.jsx` |
| FR-09 | Appointments default to `PENDING`. | Appointment creation use case | Dashboard status pill | `AppointmentServiceTest`, `Home.test.jsx` |
| FR-10 | Staff/admin flows can approve appointments. | `AppointmentService.approveAppointment` | Not yet implemented in web UI | `AppointmentServiceTest` |
| FR-11 | Staff/admin flows can reject appointments with reason. | `AppointmentService.rejectAppointment` | Not yet implemented in web UI | `AppointmentServiceTest` |
| FR-12 | Services/offices can be listed for booking. | `ServiceService.getAllServices`, `/api/services` | Booking service dropdown | `ServiceServiceTest`, `BookingModal.test.jsx` |
| FR-13 | Services/offices can be added. | `ServiceService.saveService`, `/api/services/add` | Not yet implemented in web UI | `ServiceServiceTest` |
| FR-14 | Admin can update user roles. | `AdminController.updateUserRole` | Not yet implemented in web UI | Manual procedure |
| FR-15 | Admin can list users. | `AdminController.getAllUsers` | Not yet implemented in web UI | Manual procedure |
| FR-16 | Guests can view the landing page and navigate to login/register. | N/A | Home guest view | `Home.test.jsx` |

## Test Environment

- Backend: Java 17+ compatible Spring Boot application, Maven Wrapper
- Backend test database: H2 in-memory database using `test` Spring profile
- Frontend: React, Vite, Vitest, Testing Library, jsdom
- Browser target: Local Vite web app at `http://localhost:5173`
- Backend target: Spring Boot API at `http://localhost:8080`

## Automated Test Cases

| Test ID | Module | Test File | Purpose |
|---|---|---|---|
| AT-01 | Backend auth | `AuthServiceTest` | Successful registration creates an encoded user. |
| AT-02 | Backend auth | `AuthServiceTest` | Duplicate email registration is rejected. |
| AT-03 | Backend auth | `AuthServiceTest` | Successful login returns matching user. |
| AT-04 | Backend auth | `AuthServiceTest` | Invalid login credentials throw an error. |
| AT-05 | Backend appointments | `AppointmentServiceTest` | Booking creates a pending appointment. |
| AT-06 | Backend appointments | `AppointmentServiceTest` | Student appointment lookup delegates correctly. |
| AT-07 | Backend appointments | `AppointmentServiceTest` | Appointment approval sets staff, status, and timestamp. |
| AT-08 | Backend appointments | `AppointmentServiceTest` | Appointment rejection sets status, reason, and timestamp. |
| AT-09 | Backend services | `ServiceServiceTest` | Service listing, saving, lookup, and missing service handling. |
| AT-10 | Backend context | `CitcareApplicationTests` | Spring context loads using isolated H2 test profile. |
| AT-11 | Frontend auth | `Login.test.jsx` | Login success stores token/user and displays welcome message. |
| AT-12 | Frontend auth | `Login.test.jsx` | Login failure displays error message. |
| AT-13 | Frontend auth | `Register.test.jsx` | Registration success submits payload and resets form. |
| AT-14 | Frontend appointments | `BookingModal.test.jsx` | Modal renders services and submits booking form. |
| AT-15 | Frontend appointments | `BookingModal.test.jsx` | Closed modal does not render. |
| AT-16 | Frontend home | `Home.test.jsx` | Guest landing page renders booking calls to action. |
| AT-17 | Frontend home | `Home.test.jsx` | Authenticated dashboard loads and displays appointments. |

## Manual Test Cases and Test Steps

### TC-01 Register New User

1. Start backend on port `8080`.
2. Start frontend on port `5173`.
3. Open `/register`.
4. Enter full name, email, password, and role.
5. Submit the form.
6. Expected result: success message appears and the form resets.

### TC-02 Reject Duplicate Registration

1. Register a user with an email address.
2. Register again using the same email.
3. Expected result: duplicate email error is returned by backend and shown by frontend.

### TC-03 Manual Login

1. Open `/login`.
2. Enter an existing user email and password.
3. Submit the form.
4. Expected result: access token and user are stored in localStorage; user is redirected to dashboard.

### TC-04 Invalid Login

1. Open `/login`.
2. Enter a valid email with an invalid password.
3. Submit the form.
4. Expected result: invalid credentials message appears.

### TC-05 Google OAuth Login

1. Open `/login`.
2. Click "Sign in with Google".
3. Complete Google authentication.
4. Expected result: backend finds or creates a user and redirects to `/auth-success`.

### TC-06 Guest Landing Page

1. Clear localStorage.
2. Open `/`.
3. Expected result: landing page appears with booking calls to action.

### TC-07 Dashboard Appointment List

1. Log in as a student.
2. Open `/`.
3. Expected result: dashboard appears and lists the student's appointments.

### TC-08 Book Appointment

1. Log in as a student.
2. Click "New Appointment".
3. Select service, date, time, room/location, and notes.
4. Submit the modal.
5. Expected result: booking succeeds, modal closes, and appointment list refreshes with `PENDING` status.

### TC-09 List Services

1. Start backend.
2. Request `GET /api/services`.
3. Expected result: API returns available services for the booking dropdown.

### TC-10 Add Service

1. Start backend.
2. Request `POST /api/services/add` with service name, description, and duration.
3. Expected result: service is persisted and returned.

### TC-11 Admin Update User Role

1. Authenticate as an admin.
2. Request `PUT /api/admin/update-role/{userId}` with a role payload.
3. Expected result: target user's role is updated.

### TC-12 Admin List Users

1. Authenticate as an admin.
2. Request `GET /api/admin/users`.
3. Expected result: all users are returned.

## Test Scripts

Run backend automated tests:

```powershell
cd backend\citcare\citcare
cmd /c mvnw.cmd test
```

Run frontend automated tests:

```powershell
cd web\citcare
npm.cmd test
```

Run frontend lint:

```powershell
cd web\citcare
npm.cmd run lint
```

Run frontend production build:

```powershell
cd web\citcare
npm.cmd run build
```

Run mobile unit tests when Gradle wrapper path is available:

```powershell
cd mobile
cmd /c gradlew.bat test
```

