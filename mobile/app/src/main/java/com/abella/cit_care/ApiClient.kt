package com.abella.cit_care

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT

interface ApiService {
    @POST("/api/v1/auth/register")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>

    @POST("/api/v1/auth/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @PUT("/api/v1/auth/profile/{userId}")
    fun updateProfile(
        @Path("userId") userId: Long,
        @Body request: ProfileUpdateRequest
    ): Call<AuthResponse>

    @PUT("/api/v1/auth/profile/{userId}/password")
    fun changePassword(
        @Path("userId") userId: Long,
        @Body request: PasswordChangeRequest
    ): Call<AuthResponse>

    @GET("/api/services")
    fun getServices(): Call<List<ServiceItem>>

    @GET("/api/appointments/student/{studentId}")
    fun getStudentAppointments(@Path("studentId") studentId: Long): Call<List<AppointmentItem>>

    @POST("/api/appointments/book")
    fun bookAppointment(@Body request: AppointmentRequest): Call<AppointmentItem>

    @GET("/api/admin/users")
    fun getAdminUsers(@Header("X-User-Role") role: String = "ADMIN"): Call<AdminUsersResponse>

    @PUT("/api/admin/update-role/{userId}")
    fun updateUserRole(
        @Path("userId") userId: Long,
        @Body request: RoleUpdateRequest,
        @Header("X-User-Role") role: String = "ADMIN"
    ): Call<AdminActionResponse>

    @PUT("/api/admin/users/{userId}")
    fun updateUserName(
        @Path("userId") userId: Long,
        @Body request: NameUpdateRequest,
        @Header("X-User-Role") role: String = "ADMIN"
    ): Call<AdminActionResponse>

    @DELETE("/api/admin/users/{userId}")
    fun deleteUser(
        @Path("userId") userId: Long,
        @Header("X-User-Role") role: String = "ADMIN",
        @Header("X-User-Id") requesterId: Long
    ): Call<AdminActionResponse>

    @GET("/api/appointments/staff/{staffId}")
    fun getStaffAppointments(@Path("staffId") staffId: Long): Call<List<AppointmentItem>>

    @PUT("/api/appointments/{appointmentId}/{action}")
    fun updateAppointmentStatus(
        @Path("appointmentId") appointmentId: Long,
        @Path("action") action: String,
        @Body request: StaffActionRequest
    ): Call<AppointmentItem>

    @PUT("/api/appointments/{appointmentId}/{action}")
    fun updateAppointmentStatusWithReason(
        @Path("appointmentId") appointmentId: Long,
        @Path("action") action: String,
        @Body request: RejectionActionRequest
    ): Call<AppointmentItem>
}

object ApiClient {
    private const val BASE_URL = "http://192.168.31.196:8080"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }
}
