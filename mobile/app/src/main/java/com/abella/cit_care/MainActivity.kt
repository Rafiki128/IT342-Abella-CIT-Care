package com.abella.cit_care

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val maroon = Color.rgb(139, 0, 0)
    private val maroonDark = Color.rgb(95, 0, 0)
    private val gold = Color.rgb(215, 179, 46)
    private val bgLight = Color.rgb(246, 247, 249)
    private val border = Color.rgb(228, 231, 236)
    private val textDark = Color.rgb(21, 32, 51)
    private val textMuted = Color.rgb(71, 84, 103)

    private var userId: Long = -1
    private var fullName: String = "CIT-Care User"
    private var email: String = ""
    private var phoneNumber: String = ""
    private var role: String = "STUDENT"
    private var emailNotificationsEnabled: Boolean = true
    private var services: List<ServiceItem> = emptyList()
    private var appointments: List<AppointmentItem> = emptyList()
    private var adminUsers: List<UserData> = emptyList()
    private var adminSearchTerm: String = ""
    private var adminRoleFilter: String = ""
    private var staffStatusFilter: String = ""
    private lateinit var appointmentList: LinearLayout
    private lateinit var emptyAppointments: TextView
    private lateinit var adminUserList: LinearLayout
    private lateinit var staffAppointmentList: LinearLayout
    private lateinit var adminStatsText: TextView
    private lateinit var profileSummaryText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("citcare_user", MODE_PRIVATE)
        userId = prefs.getLong("id", -1)
        fullName = prefs.getString("fullName", "CIT-Care User") ?: "CIT-Care User"
        email = prefs.getString("email", "") ?: ""
        phoneNumber = prefs.getString("phoneNumber", "") ?: ""
        role = prefs.getString("role", "STUDENT") ?: "STUDENT"
        emailNotificationsEnabled = prefs.getBoolean("emailNotificationsEnabled", true)

        if (userId <= 0) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        buildDashboard()
        when (role) {
            "ADMIN" -> loadAdminUsers()
            "MEDICAL_STAFF", "GUIDANCE_STAFF" -> loadStaffAppointments()
            else -> {
                loadServices()
                loadAppointments()
            }
        }
    }

    private fun buildDashboard() {
        val scrollView = ScrollView(this).apply {
            setBackgroundColor(bgLight)
            isFillViewport = true
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(24))
        }
        scrollView.addView(root)

        root.addView(headerCard())
        root.addView(sectionGap())
        when (role) {
            "ADMIN" -> {
                root.addView(adminStatsCard())
                root.addView(sectionGap())
                root.addView(adminUsersCard())
                root.addView(sectionGap())
                root.addView(profileCard())
            }
            "MEDICAL_STAFF", "GUIDANCE_STAFF" -> {
                root.addView(staffHeadingCard())
                root.addView(sectionGap())
                root.addView(staffAppointmentsCard())
                root.addView(sectionGap())
                root.addView(profileCard())
            }
            else -> {
                root.addView(upcomingCard())
                root.addView(sectionGap())
                root.addView(bookingCard())
                root.addView(sectionGap())
                root.addView(quickInfoCard())
                root.addView(sectionGap())
                root.addView(profileCard())
            }
        }

        setContentView(scrollView)
        animateChildren(root)
    }

    private fun headerCard(): View {
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            background = rounded(maroon, dp(12))
        }

        val titleRow = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
        }
        titleRow.addView(TextView(this).apply {
            text = "C"
            textSize = 18f
            setTextColor(maroonDark)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            background = rounded(gold, dp(8))
        }, LinearLayout.LayoutParams(dp(40), dp(40)))
        titleRow.addView(TextView(this).apply {
            text = "CIT-Care"
            textSize = 18f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(10), 0, 0, 0)
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        titleRow.addView(Button(this).apply {
            text = "Logout"
            textSize = 12f
            isAllCaps = false
            setTextColor(maroon)
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
            setOnClickListener {
                getSharedPreferences("citcare_user", MODE_PRIVATE).edit().clear().apply()
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }, LinearLayout.LayoutParams(dp(96), dp(44)))
        header.addView(titleRow)

        header.addView(TextView(this).apply {
            text = "Hello, ${displayName()}!"
            textSize = 26f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(22), 0, dp(8))
        })
        header.addView(TextView(this).apply {
            text = "Book clinic visits, review appointment updates, and keep your campus wellness support close."
            textSize = 14f
            setTextColor(Color.WHITE)
            alpha = 0.92f
            setLineSpacing(4f, 1f)
        })
        return header
    }

    private fun upcomingCard(): View {
        val card = cardContainer()
        val row = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
        }
        row.addView(sectionTitle("Upcoming Appointments"), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        row.addView(TextView(this).apply {
            text = "View All"
            textSize = 14f
            setTextColor(maroon)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(12), dp(8), dp(4), dp(8))
            setOnClickListener { showAllAppointments() }
        })
        card.addView(row)

        appointmentList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(12), 0, 0)
        }
        emptyAppointments = TextView(this).apply {
            text = "No appointments found."
            textSize = 14f
            setTextColor(textMuted)
            gravity = Gravity.CENTER
            setPadding(0, dp(20), 0, dp(12))
        }
        card.addView(appointmentList)
        return card
    }

    private fun bookingCard(): View {
        val card = cardContainer()
        card.addView(sectionTitle("Book New Appointment"))
        card.addView(TextView(this).apply {
            text = "Choose a service and the form will prefill its service type and location."
            textSize = 13f
            setTextColor(textMuted)
            setPadding(0, dp(4), 0, dp(14))
        })
        card.addView(serviceCard("Medical Clinic", "General checkups, consultations, and referrals.", false))
        card.addView(serviceCard("Guidance Counseling", "Mental health, academic, and career guidance.", true))
        return card
    }

    private fun serviceCard(title: String, description: String, guidance: Boolean): View {
        val item = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = roundedStroke(Color.WHITE, dp(10), border, 1)
        }
        item.addView(TextView(this).apply {
            text = title
            textSize = 16f
            setTextColor(textDark)
            typeface = Typeface.DEFAULT_BOLD
        })
        item.addView(TextView(this).apply {
            text = description
            textSize = 13f
            setTextColor(textMuted)
            setPadding(0, dp(6), 0, dp(12))
        })
        item.addView(Button(this).apply {
            text = "Book Appointment"
            isAllCaps = false
            isEnabled = role == "STUDENT"
            setTextColor(Color.WHITE)
            backgroundTintList = android.content.res.ColorStateList.valueOf(gold)
            setOnClickListener { showBookingDialog(guidance) }
        })
        item.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = dp(12)
        }
        return item
    }

    private fun quickInfoCard(): View {
        val card = cardContainer()
        card.addView(sectionTitle("Daily Tip"))
        card.addView(TextView(this).apply {
            text = "Taking short breaks during study sessions improves focus and retention. Try the 20-20-20 rule."
            textSize = 14f
            setTextColor(textMuted)
            setPadding(0, dp(8), 0, 0)
            setLineSpacing(4f, 1f)
        })
        return card
    }

    private fun adminStatsCard(): View {
        val card = cardContainer()
        card.addView(sectionTitle("Role Management"))
        adminStatsText = TextView(this).apply {
            text = "Loading users..."
            textSize = 14f
            setTextColor(textMuted)
            setPadding(0, dp(8), 0, 0)
            setLineSpacing(4f, 1f)
        }
        card.addView(adminStatsText)
        return card
    }

    private fun adminUsersCard(): View {
        val card = cardContainer()
        card.addView(sectionTitle("User Management"))
        card.addView(filterRow("Search users", "All Roles", listOf("", "NEW", "STUDENT", "MEDICAL_STAFF", "GUIDANCE_STAFF", "ADMIN")) { query, filter ->
            adminSearchTerm = query
            adminRoleFilter = filter
            renderAdminUsers()
        })
        adminUserList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(12), 0, 0)
        }
        card.addView(adminUserList)
        return card
    }

    private fun staffHeadingCard(): View {
        val card = cardContainer()
        val title = if (role == "MEDICAL_STAFF") "Medical Clinic Dashboard" else "Guidance Office Dashboard"
        card.addView(sectionTitle(title))
        card.addView(TextView(this).apply {
            text = "Review appointment requests and update student visit statuses."
            textSize = 14f
            setTextColor(textMuted)
            setPadding(0, dp(8), 0, 0)
        })
        return card
    }

    private fun staffAppointmentsCard(): View {
        val card = cardContainer()
        card.addView(sectionTitle("Appointments"))
        card.addView(filterRow("Search appointments", "All Statuses", listOf("", "PENDING", "APPROVED", "REJECTED", "COMPLETED")) { query, filter ->
            adminSearchTerm = query
            staffStatusFilter = filter
            renderStaffAppointments()
        })
        staffAppointmentList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(12), 0, 0)
        }
        card.addView(staffAppointmentList)
        return card
    }

    private fun profileCard(): View {
        val card = cardContainer()
        card.addView(sectionTitle("Profile & Settings"))
        profileSummaryText = TextView(this).apply {
            textSize = 14f
            setTextColor(textMuted)
            setPadding(0, dp(8), 0, dp(12))
        }
        refreshProfileSummary()
        card.addView(profileSummaryText)

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
        }
        actions.addView(smallActionButton("Edit Profile") { showProfileDialog() })
        actions.addView(smallActionButton("Password") { showPasswordDialog() })
        actions.addView(smallActionButton(if (emailNotificationsEnabled) "Email On" else "Email Off") { toggleEmailNotifications() })
        card.addView(actions)
        return card
    }

    private fun loadServices() {
        ApiClient.instance.getServices().enqueue(object : Callback<List<ServiceItem>> {
            override fun onResponse(call: Call<List<ServiceItem>>, response: Response<List<ServiceItem>>) {
                services = response.body().orEmpty()
            }

            override fun onFailure(call: Call<List<ServiceItem>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Unable to load services.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadAppointments() {
        ApiClient.instance.getStudentAppointments(userId).enqueue(object : Callback<List<AppointmentItem>> {
            override fun onResponse(call: Call<List<AppointmentItem>>, response: Response<List<AppointmentItem>>) {
                appointments = prioritizeAppointments(response.body().orEmpty())
                renderAppointments()
            }

            override fun onFailure(call: Call<List<AppointmentItem>>, t: Throwable) {
                appointments = emptyList()
                renderAppointments()
                Toast.makeText(this@MainActivity, "Unable to load appointments.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadAdminUsers() {
        ApiClient.instance.getAdminUsers().enqueue(object : Callback<AdminUsersResponse> {
            override fun onResponse(call: Call<AdminUsersResponse>, response: Response<AdminUsersResponse>) {
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    adminUsers = body.data.orEmpty().sortedBy { it.fullName ?: "" }
                    renderAdminUsers()
                } else {
                    Toast.makeText(this@MainActivity, body?.error?.message ?: "Unable to load users.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AdminUsersResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Unable to load users: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun renderAdminUsers() {
        val staffCount = adminUsers.count { it.role == "MEDICAL_STAFF" || it.role == "GUIDANCE_STAFF" }
        val studentCount = adminUsers.count { it.role == "STUDENT" }
        adminStatsText.text = "Total Users: ${adminUsers.size}\nStaff Members: $staffCount\nStudents: $studentCount"

        adminUserList.removeAllViews()
        val filteredUsers = adminUsers.filter { user ->
            val query = adminSearchTerm.trim().lowercase(Locale.getDefault())
            val matchesQuery = query.isBlank() ||
                user.fullName?.lowercase(Locale.getDefault())?.contains(query) == true ||
                user.email?.lowercase(Locale.getDefault())?.contains(query) == true ||
                user.role?.lowercase(Locale.getDefault())?.contains(query) == true
            val matchesRole = adminRoleFilter.isBlank() || user.role == adminRoleFilter
            matchesQuery && matchesRole
        }
        if (filteredUsers.isEmpty()) {
            adminUserList.addView(emptyText("No users found."))
            return
        }
        filteredUsers.forEach { user -> adminUserList.addView(adminUserRow(user)) }
    }

    private fun adminUserRow(user: UserData): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = roundedStroke(Color.WHITE, dp(10), border, 1)
        }
        row.addView(TextView(this).apply {
            text = user.fullName ?: "Unnamed user"
            textSize = 15f
            setTextColor(textDark)
            typeface = Typeface.DEFAULT_BOLD
        })
        row.addView(TextView(this).apply {
            text = "${user.email ?: "No email"}  •  ${roleLabel(user.role)}"
            textSize = 12f
            setTextColor(textMuted)
            setPadding(0, dp(4), 0, dp(10))
        })

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
        }
        actions.addView(smallActionButton("Role") { showRoleDialog(user) })
        actions.addView(smallActionButton("Name") { showNameDialog(user) })
        actions.addView(smallActionButton("Delete") { confirmDeleteUser(user) })
        row.addView(actions)
        row.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = dp(10)
        }
        return row
    }

    private fun showRoleDialog(user: UserData) {
        val roles = arrayOf("NEW", "STUDENT", "MEDICAL_STAFF", "GUIDANCE_STAFF", "ADMIN")
        val selected = roles.indexOf(user.role).coerceAtLeast(0)
        AlertDialog.Builder(this)
            .setTitle("Edit Role")
            .setSingleChoiceItems(roles.map { roleLabel(it) }.toTypedArray(), selected) { dialog, which ->
                val userIdValue = user.id ?: return@setSingleChoiceItems
                ApiClient.instance.updateUserRole(userIdValue, RoleUpdateRequest(roles[which])).enqueue(object : Callback<AdminActionResponse> {
                    override fun onResponse(call: Call<AdminActionResponse>, response: Response<AdminActionResponse>) {
                        Toast.makeText(this@MainActivity, response.body()?.message ?: "Role updated.", Toast.LENGTH_SHORT).show()
                        loadAdminUsers()
                    }

                    override fun onFailure(call: Call<AdminActionResponse>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Role update failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showNameDialog(user: UserData) {
        val input = EditText(this).apply {
            setText(user.fullName ?: "")
            setSelectAllOnFocus(true)
            setPadding(dp(12), dp(10), dp(12), dp(10))
        }
        AlertDialog.Builder(this)
            .setTitle("Edit Name")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val nextName = input.text.toString().trim()
                val userIdValue = user.id
                if (userIdValue == null || nextName.isBlank()) {
                    Toast.makeText(this, "Full name is required.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                ApiClient.instance.updateUserName(userIdValue, NameUpdateRequest(nextName)).enqueue(object : Callback<AdminActionResponse> {
                    override fun onResponse(call: Call<AdminActionResponse>, response: Response<AdminActionResponse>) {
                        Toast.makeText(this@MainActivity, response.body()?.message ?: "Name updated.", Toast.LENGTH_SHORT).show()
                        loadAdminUsers()
                    }

                    override fun onFailure(call: Call<AdminActionResponse>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Name update failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .show()
    }

    private fun confirmDeleteUser(user: UserData) {
        val targetId = user.id ?: return
        if (targetId == userId) {
            Toast.makeText(this, "You cannot delete your own admin account.", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Delete ${user.fullName ?: "this user"}? This cannot be undone.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                ApiClient.instance.deleteUser(targetId, requesterId = userId).enqueue(object : Callback<AdminActionResponse> {
                    override fun onResponse(call: Call<AdminActionResponse>, response: Response<AdminActionResponse>) {
                        Toast.makeText(this@MainActivity, response.body()?.message ?: "User deleted.", Toast.LENGTH_SHORT).show()
                        loadAdminUsers()
                    }

                    override fun onFailure(call: Call<AdminActionResponse>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Delete failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .show()
    }

    private fun loadStaffAppointments() {
        ApiClient.instance.getStaffAppointments(userId).enqueue(object : Callback<List<AppointmentItem>> {
            override fun onResponse(call: Call<List<AppointmentItem>>, response: Response<List<AppointmentItem>>) {
                appointments = prioritizeAppointments(response.body().orEmpty())
                renderStaffAppointments()
            }

            override fun onFailure(call: Call<List<AppointmentItem>>, t: Throwable) {
                appointments = emptyList()
                renderStaffAppointments()
                Toast.makeText(this@MainActivity, "Unable to load appointments: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun renderStaffAppointments() {
        staffAppointmentList.removeAllViews()
        val filteredAppointments = appointments.filter { appointment ->
            val query = adminSearchTerm.trim().lowercase(Locale.getDefault())
            val matchesQuery = query.isBlank() ||
                appointment.student?.fullName?.lowercase(Locale.getDefault())?.contains(query) == true ||
                appointment.service?.name?.lowercase(Locale.getDefault())?.contains(query) == true ||
                appointment.office?.lowercase(Locale.getDefault())?.contains(query) == true
            val matchesStatus = staffStatusFilter.isBlank() || appointment.status == staffStatusFilter
            matchesQuery && matchesStatus
        }
        if (filteredAppointments.isEmpty()) {
            staffAppointmentList.addView(emptyText("No appointments found."))
            return
        }
        filteredAppointments.forEach { appointment -> staffAppointmentList.addView(staffAppointmentRow(appointment)) }
    }

    private fun staffAppointmentRow(appointment: AppointmentItem): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = roundedStroke(Color.WHITE, dp(10), border, 1)
        }
        row.addView(TextView(this).apply {
            text = appointment.student?.fullName ?: "Unknown student"
            textSize = 15f
            setTextColor(textDark)
            typeface = Typeface.DEFAULT_BOLD
        })
        row.addView(TextView(this).apply {
            text = "${appointment.service?.name ?: "Service unavailable"}\n${appointment.appointmentDate ?: "TBA"} at ${appointment.appointmentTime ?: "TBA"}\n${statusText(appointment.status)}"
            textSize = 13f
            setTextColor(textMuted)
            setPadding(0, dp(6), 0, dp(10))
        })

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
        }
        when (appointment.status ?: "PENDING") {
            "PENDING" -> {
                actions.addView(smallActionButton("Approve") { updateStaffAppointment(appointment, "approve") })
                actions.addView(smallActionButton("Reject") { showReasonDialog(appointment, "reject") })
            }
            "APPROVED" -> {
                actions.addView(smallActionButton("Complete") { updateStaffAppointment(appointment, "complete") })
                actions.addView(smallActionButton("Cancel") { showReasonDialog(appointment, "cancel") })
            }
            else -> actions.addView(smallActionButton("Details") { showAppointmentDetails(appointment) })
        }
        row.addView(actions)
        row.setOnClickListener { showAppointmentDetails(appointment) }
        row.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = dp(10)
        }
        return row
    }

    private fun updateStaffAppointment(appointment: AppointmentItem, action: String) {
        val appointmentId = appointment.id ?: return
        ApiClient.instance.updateAppointmentStatus(appointmentId, action, StaffActionRequest(userId)).enqueue(object : Callback<AppointmentItem> {
            override fun onResponse(call: Call<AppointmentItem>, response: Response<AppointmentItem>) {
                Toast.makeText(this@MainActivity, "Appointment updated.", Toast.LENGTH_SHORT).show()
                loadStaffAppointments()
            }

            override fun onFailure(call: Call<AppointmentItem>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Update failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showReasonDialog(appointment: AppointmentItem, action: String) {
        val input = EditText(this).apply {
            hint = if (action == "cancel") "Why is this appointment being cancelled?" else "Why is this appointment being rejected?"
            minLines = 3
            gravity = Gravity.TOP
        }
        AlertDialog.Builder(this)
            .setTitle(if (action == "cancel") "Cancel Appointment" else "Reject Appointment")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Submit") { _, _ ->
                val appointmentId = appointment.id ?: return@setPositiveButton
                val reason = input.text.toString().trim()
                if (reason.isBlank()) {
                    Toast.makeText(this, "Reason is required.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                ApiClient.instance.updateAppointmentStatusWithReason(appointmentId, action, RejectionActionRequest(userId, reason)).enqueue(object : Callback<AppointmentItem> {
                    override fun onResponse(call: Call<AppointmentItem>, response: Response<AppointmentItem>) {
                        Toast.makeText(this@MainActivity, "Appointment updated.", Toast.LENGTH_SHORT).show()
                        loadStaffAppointments()
                    }

                    override fun onFailure(call: Call<AppointmentItem>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Update failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .show()
    }

    private fun showProfileDialog() {
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(8), dp(4), 0)
        }
        val nameInput = editField("Full Name", fullName, false)
        val emailInput = editField("Email", email, false)
        val phoneInput = editField("Phone Number", phoneNumber, false)

        form.addView(fieldLabel("Full Name"))
        form.addView(nameInput)
        form.addView(fieldLabel("Email"))
        form.addView(emailInput)
        form.addView(fieldLabel("Phone Number"))
        form.addView(phoneInput)

        AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(form)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val nextName = nameInput.text.toString().trim()
                val nextEmail = emailInput.text.toString().trim()
                val nextPhone = phoneInput.text.toString().trim()

                if (nextName.isBlank() || nextEmail.isBlank()) {
                    Toast.makeText(this, "Full name and email are required.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                saveProfile(nextName, nextEmail, nextPhone, emailNotificationsEnabled)
            }
            .show()
    }

    private fun showPasswordDialog() {
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(8), dp(4), 0)
        }
        val currentInput = editField("Current Password", "", false).apply {
            hint = "Current password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val newInput = editField("New Password", "", false).apply {
            hint = "New password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        form.addView(fieldLabel("Current Password"))
        form.addView(currentInput)
        form.addView(fieldLabel("New Password"))
        form.addView(newInput)

        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(form)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val current = currentInput.text.toString()
                val next = newInput.text.toString()
                if (current.isBlank() || next.length < 6) {
                    Toast.makeText(this, "Enter current password and a new password with at least 6 characters.", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                ApiClient.instance.changePassword(userId, PasswordChangeRequest(current, next)).enqueue(object : Callback<AuthResponse> {
                    override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                        val body = response.body()
                        if (response.isSuccessful && body?.success == true) {
                            Toast.makeText(this@MainActivity, body.message ?: "Password updated.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, body?.error?.message ?: "Password update failed.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                        Toast.makeText(this@MainActivity, "Password update failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .show()
    }

    private fun toggleEmailNotifications() {
        saveProfile(fullName, email, phoneNumber, !emailNotificationsEnabled)
    }

    private fun saveProfile(nextName: String, nextEmail: String, nextPhone: String, nextEmailNotifications: Boolean) {
        val request = ProfileUpdateRequest(nextName, nextEmail, nextPhone.ifBlank { null }, nextEmailNotifications.toString())
        ApiClient.instance.updateProfile(userId, request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                val body = response.body()
                val user = body?.data?.user
                if (response.isSuccessful && body?.success == true && user != null) {
                    fullName = user.fullName ?: nextName
                    email = user.email ?: nextEmail
                    phoneNumber = user.phoneNumber ?: nextPhone
                    emailNotificationsEnabled = user.emailNotificationsEnabled ?: nextEmailNotifications
                    getSharedPreferences("citcare_user", MODE_PRIVATE)
                        .edit()
                        .putString("fullName", fullName)
                        .putString("email", email)
                        .putString("phoneNumber", phoneNumber)
                        .putBoolean("emailNotificationsEnabled", emailNotificationsEnabled)
                        .apply()
                    refreshProfileSummary()
                    Toast.makeText(this@MainActivity, "Profile updated.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, body?.error?.message ?: "Profile update failed.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Profile update failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun refreshProfileSummary() {
        if (::profileSummaryText.isInitialized) {
            profileSummaryText.text = "${fullName}\n${email.ifBlank { "No email" }}\n${roleLabel(role)} Account\nEmail Notifications: ${if (emailNotificationsEnabled) "On" else "Off"}"
        }
    }

    private fun renderAppointments() {
        appointmentList.removeAllViews()
        if (appointments.isEmpty()) {
            appointmentList.addView(emptyAppointments)
            return
        }
        appointments.take(3).forEach { appointment ->
            appointmentList.addView(appointmentRow(appointment))
        }
    }

    private fun appointmentRow(appointment: AppointmentItem): View {
        val row = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = roundedStroke(Color.WHITE, dp(10), border, 1)
            isClickable = true
            isFocusable = true
            setOnClickListener { showAppointmentDetails(appointment) }
        }

        row.addView(TextView(this).apply {
            text = if (serviceType(appointment.service?.name) == "Medical") "+" else "G"
            textSize = 18f
            setTextColor(maroon)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            background = rounded(Color.rgb(255, 241, 244), dp(10))
        }, LinearLayout.LayoutParams(dp(44), dp(44)))

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, dp(8), 0)
        }
        info.addView(TextView(this).apply {
            text = appointment.service?.name ?: "Service unavailable"
            textSize = 15f
            setTextColor(textDark)
            typeface = Typeface.DEFAULT_BOLD
        })
        info.addView(TextView(this).apply {
            text = "${appointment.appointmentTime ?: "TBA"}  ${appointment.appointmentDate ?: "TBA"}"
            textSize = 12f
            setTextColor(textMuted)
            setPadding(0, dp(4), 0, 0)
        })
        info.addView(TextView(this).apply {
            text = appointment.office ?: "CIT-Care Office"
            textSize = 12f
            setTextColor(textMuted)
        })
        row.addView(info, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        row.addView(TextView(this).apply {
            text = statusText(appointment.status)
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(statusColor(appointment.status))
            setPadding(dp(10), dp(6), dp(10), dp(6))
            background = rounded(statusBackground(appointment.status), dp(100))
        })

        row.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = dp(10)
        }
        return row
    }

    private fun showAppointmentDetails(appointment: AppointmentItem) {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(8), dp(4), 0)
        }
        content.addView(detailLine("Status", statusText(appointment.status)))
        content.addView(detailLine("Service Type", serviceType(appointment.service?.name)))
        content.addView(detailLine("Date", appointment.appointmentDate ?: "TBA"))
        content.addView(detailLine("Time", appointment.appointmentTime ?: "TBA"))
        content.addView(detailLine("Room / Location", appointment.office ?: "CIT-Care Office"))
        content.addView(detailLine("Reason / Notes", appointment.reason ?: "No notes provided."))
        if (!appointment.rejectionReason.isNullOrBlank()) {
            content.addView(detailLine("Cancellation Reason", appointment.rejectionReason))
        }

        AlertDialog.Builder(this)
            .setTitle(appointment.service?.name ?: "Appointment Details")
            .setView(content)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showAllAppointments() {
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8), 0, 0)
        }
        if (appointments.isEmpty()) {
            list.addView(TextView(this).apply {
                text = "No appointments found."
                setTextColor(textMuted)
                gravity = Gravity.CENTER
                setPadding(0, dp(20), 0, dp(20))
            })
        } else {
            appointments.forEach { appointment -> list.addView(appointmentRow(appointment)) }
        }

        AlertDialog.Builder(this)
            .setTitle("All Appointments")
            .setView(ScrollView(this).apply { addView(list) })
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showBookingDialog(guidance: Boolean) {
        val service = services.firstOrNull {
            val name = it.name?.lowercase(Locale.getDefault()).orEmpty()
            if (guidance) name.contains("guidance") || name.contains("counsel") else name.contains("medical") || name.contains("clinic")
        } ?: services.firstOrNull { it.id != null }

        if (service?.id == null) {
            Toast.makeText(this, "Service list is still loading. Please try again.", Toast.LENGTH_SHORT).show()
            loadServices()
            return
        }

        val location = if (guidance) "Guidance Office" else "Medical Clinic"
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(8), dp(4), 0)
        }
        form.addView(readOnlyField("Service Type", service.name ?: if (guidance) "Guidance Counseling" else "Medical Clinic"))
        val dateInput = pickerField("Date", "Select date")
        val timeInput = pickerField("Time", "Select time")
        val officeInput = editField("Room / Location", location, false)
        val notesInput = editField("Notes", if (guidance) "I would like to request guidance support." else "I would like to request a medical visit.", true)

        dateInput.setOnClickListener { pickDate(dateInput) }
        timeInput.setOnClickListener { pickTime(timeInput) }

        form.addView(fieldLabel("Date"))
        form.addView(dateInput)
        form.addView(fieldLabel("Time"))
        form.addView(timeInput)
        form.addView(fieldLabel("Room / Location"))
        form.addView(officeInput)
        form.addView(fieldLabel("Notes"))
        form.addView(notesInput)

        AlertDialog.Builder(this)
            .setTitle("Schedule Appointment")
            .setView(form)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Confirm", null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if (dateInput.text.isBlank() || timeInput.text.isBlank() || officeInput.text.isBlank()) {
                            Toast.makeText(this@MainActivity, "Date, time, and location are required.", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        bookAppointment(service.id, dateInput.text.toString(), timeInput.text.toString(), notesInput.text.toString(), officeInput.text.toString())
                        dismiss()
                    }
                }
            }
            .show()
    }

    private fun bookAppointment(serviceId: Long, date: String, time: String, reason: String, office: String) {
        val request = AppointmentRequest(userId, serviceId, date, time, reason, office)
        ApiClient.instance.bookAppointment(request).enqueue(object : Callback<AppointmentItem> {
            override fun onResponse(call: Call<AppointmentItem>, response: Response<AppointmentItem>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Booking successful!", Toast.LENGTH_SHORT).show()
                    loadAppointments()
                } else {
                    Toast.makeText(this@MainActivity, "Booking failed.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AppointmentItem>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Booking error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun prioritizeAppointments(items: List<AppointmentItem>): List<AppointmentItem> {
        val priority = mapOf("PENDING" to 0, "APPROVED" to 1, "COMPLETED" to 2, "REJECTED" to 3)
        return items.sortedWith(compareBy<AppointmentItem> { priority[it.status ?: "PENDING"] ?: 9 }
            .thenBy { it.appointmentDate ?: "9999-12-31" }
            .thenBy { it.appointmentTime ?: "23:59" })
    }

    private fun pickDate(input: EditText) {
        val now = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            input.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day))
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun pickTime(input: EditText) {
        val now = Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
            input.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute))
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
    }

    private fun detailLine(label: String, value: String): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = roundedStroke(Color.rgb(248, 250, 252), dp(8), border, 1)
            addView(TextView(this@MainActivity).apply {
                text = label.uppercase(Locale.getDefault())
                textSize = 11f
                setTextColor(textMuted)
                typeface = Typeface.DEFAULT_BOLD
            })
            addView(TextView(this@MainActivity).apply {
                text = value
                textSize = 15f
                setTextColor(textDark)
                setPadding(0, dp(4), 0, 0)
            })
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(8)
            }
        }
    }

    private fun readOnlyField(label: String, value: String): View {
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(10))
        }
        wrapper.addView(fieldLabel(label))
        wrapper.addView(TextView(this).apply {
            text = value
            textSize = 15f
            setTextColor(textDark)
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = roundedStroke(Color.rgb(248, 250, 252), dp(8), border, 1)
        })
        return wrapper
    }

    private fun pickerField(label: String, hint: String): EditText {
        return editField(label, hint, false).apply {
            setText("")
            isFocusable = false
            isClickable = true
        }
    }

    private fun editField(label: String, value: String, multiLine: Boolean): EditText {
        return EditText(this).apply {
            hint = value
            setText(value)
            textSize = 14f
            setTextColor(textDark)
            setHintTextColor(textMuted)
            background = roundedStroke(Color.rgb(251, 252, 254), dp(8), border, 1)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            if (multiLine) {
                minLines = 3
                gravity = Gravity.TOP
            }
        }
    }

    private fun fieldLabel(label: String): TextView = TextView(this).apply {
        text = label
        textSize = 12f
        setTextColor(textDark)
        typeface = Typeface.DEFAULT_BOLD
        setPadding(0, dp(8), 0, dp(4))
    }

    private fun cardContainer(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(16), dp(16), dp(16))
        background = roundedStroke(Color.WHITE, dp(12), border, 1)
    }

    private fun sectionTitle(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 18f
        setTextColor(textDark)
        typeface = Typeface.DEFAULT_BOLD
    }

    private fun filterRow(
        searchHint: String,
        filterLabel: String,
        options: List<String>,
        onChange: (String, String) -> Unit
    ): View {
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(12), 0, 0)
        }
        val searchInput = EditText(this).apply {
            hint = searchHint
            textSize = 14f
            setSingleLine(true)
            background = roundedStroke(Color.rgb(251, 252, 254), dp(8), border, 1)
            setPadding(dp(12), dp(10), dp(12), dp(10))
        }
        val filterButton = Button(this).apply {
            text = filterLabel
            isAllCaps = false
            setTextColor(maroon)
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
        }
        var selectedFilter = ""

        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onChange(s?.toString().orEmpty(), selectedFilter)
            }
            override fun afterTextChanged(s: android.text.Editable?) = Unit
        })

        filterButton.setOnClickListener {
            val labels = options.map { if (it.isBlank()) filterLabel else roleOrStatusLabel(it) }.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle(filterLabel)
                .setItems(labels) { _, which ->
                    selectedFilter = options[which]
                    filterButton.text = labels[which]
                    onChange(searchInput.text.toString(), selectedFilter)
                }
                .show()
        }

        wrapper.addView(searchInput, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)))
        wrapper.addView(filterButton, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(46)).apply {
            topMargin = dp(8)
        })
        return wrapper
    }

    private fun smallActionButton(text: String, action: () -> Unit): Button = Button(this).apply {
        this.text = text
        textSize = 12f
        isAllCaps = false
        setTextColor(Color.WHITE)
        backgroundTintList = android.content.res.ColorStateList.valueOf(if (text == "Delete" || text == "Reject" || text == "Cancel") Color.rgb(180, 35, 24) else maroon)
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(40)).apply {
            leftMargin = dp(6)
        }
    }

    private fun emptyText(message: String): TextView = TextView(this).apply {
        text = message
        textSize = 14f
        setTextColor(textMuted)
        gravity = Gravity.CENTER
        setPadding(0, dp(18), 0, dp(18))
    }

    private fun sectionGap(): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(16))
    }

    private fun rounded(color: Int, radius: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
        }

    private fun roundedStroke(color: Int, radius: Int, strokeColor: Int, strokeWidth: Int): GradientDrawable =
        rounded(color, radius).apply { setStroke(dp(strokeWidth), strokeColor) }

    private fun animateChildren(root: LinearLayout) {
        for (index in 0 until root.childCount) {
            root.getChildAt(index).apply {
                alpha = 0f
                translationY = dp(12).toFloat()
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay((index * 45).toLong())
                    .setDuration(280)
                    .start()
            }
        }
    }

    private fun displayName(): String = fullName.trim().ifBlank { "CIT-Care User" }.split(" ").first()

    private fun serviceType(name: String?): String {
        val serviceName = name?.lowercase(Locale.getDefault()).orEmpty()
        return if (serviceName.contains("counsel") || serviceName.contains("guidance")) "Counseling" else "Medical"
    }

    private fun statusText(status: String?): String = when (status) {
        "APPROVED" -> "Confirmed"
        "REJECTED" -> "Cancelled"
        "COMPLETED" -> "Completed"
        else -> "Pending"
    }

    private fun statusColor(status: String?): Int = when (status) {
        "APPROVED" -> Color.rgb(6, 118, 71)
        "REJECTED" -> Color.rgb(180, 35, 24)
        "COMPLETED" -> Color.rgb(0, 67, 206)
        else -> Color.rgb(154, 103, 0)
    }

    private fun statusBackground(status: String?): Int = when (status) {
        "APPROVED" -> Color.rgb(220, 252, 231)
        "REJECTED" -> Color.rgb(254, 228, 226)
        "COMPLETED" -> Color.rgb(219, 234, 254)
        else -> Color.rgb(255, 247, 194)
    }

    private fun roleLabel(role: String?): String = when (role) {
        "NEW" -> "New"
        "STUDENT" -> "Student"
        "MEDICAL_STAFF" -> "Medical Staff"
        "GUIDANCE_STAFF" -> "Guidance Staff"
        "ADMIN" -> "Admin"
        else -> role ?: "User"
    }

    private fun roleOrStatusLabel(value: String): String = when (value) {
        "PENDING" -> "Pending"
        "APPROVED" -> "Confirmed"
        "REJECTED" -> "Cancelled"
        "COMPLETED" -> "Completed"
        else -> roleLabel(value)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
