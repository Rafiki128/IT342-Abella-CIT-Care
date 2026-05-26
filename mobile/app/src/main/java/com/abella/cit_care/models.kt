package com.abella.cit_care

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val role: String
)
data class LoginRequest(val email: String, val password: String)

data class ProfileUpdateRequest(
    val fullName: String,
    val email: String,
    val phoneNumber: String?,
    val emailNotificationsEnabled: String? = null
)

data class PasswordChangeRequest(
    val currentPassword: String,
    val newPassword: String
)

data class AuthResponse(
    val success: Boolean?,
    val data: AuthData?,
    val error: ApiError?,
    val message: String?
)

data class AuthData(
    val user: UserData?,
    val accessToken: String?,
    val refreshToken: String?
)

data class UserData(
    val id: Long?,
    val email: String?,
    val fullName: String?,
    val phoneNumber: String?,
    val role: String?,
    val emailNotificationsEnabled: Boolean?
)

data class ApiError(
    val code: String?,
    val message: String?
)

data class ServiceItem(
    val id: Long?,
    val name: String?
)

data class AppointmentItem(
    val id: Long?,
    val appointmentDate: String?,
    val appointmentTime: String?,
    val reason: String?,
    val status: String?,
    val office: String?,
    val rejectionReason: String?,
    val service: ServiceItem?,
    val student: UserData?
)

data class AppointmentRequest(
    val studentId: Long,
    val serviceId: Long,
    val appointmentDate: String,
    val appointmentTime: String,
    val reason: String,
    val office: String
)

data class AdminUsersResponse(
    val success: Boolean?,
    val data: List<UserData>?,
    val error: ApiError?,
    val message: String?
)

data class AdminActionResponse(
    val success: Boolean?,
    val user: UserData?,
    val role: String?,
    val message: String?,
    val error: ApiError?
)

data class RoleUpdateRequest(val role: String)

data class NameUpdateRequest(
    val fullName: String,
    val role: String? = null
)

data class StaffActionRequest(val staffId: Long)

data class RejectionActionRequest(
    val staffId: Long,
    val reason: String
)
