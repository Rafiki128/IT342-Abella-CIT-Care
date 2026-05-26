package com.abella.cit_care

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        animatePageIn()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(email, password)
            ApiClient.instance.login(request).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    val body = response.body()
                    val user = body?.data?.user
                    if (response.isSuccessful && body?.success == true && user?.id != null) {
                        getSharedPreferences("citcare_user", MODE_PRIVATE)
                            .edit()
                            .putLong("id", user.id)
                            .putString("fullName", user.fullName ?: "CIT-Care User")
                            .putString("email", user.email ?: "")
                            .putString("phoneNumber", user.phoneNumber ?: "")
                            .putString("role", user.role ?: "STUDENT")
                            .putBoolean("emailNotificationsEnabled", user.emailNotificationsEnabled ?: true)
                            .putString("accessToken", body.data.accessToken ?: "")
                            .apply()
                        Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, body?.error?.message ?: "Invalid Credentials", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun animatePageIn() {
        window.decorView.alpha = 0f
        window.decorView.translationY = 18f
        window.decorView.animate().alpha(1f).translationY(0f).setDuration(280).start()
    }
}
