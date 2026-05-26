package edu.cit.abella.citcare.auth;

import edu.cit.abella.citcare.entity.User;
import edu.cit.abella.citcare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:5173")

public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        try {
            User user = authService.registerUser(
                payload.get("email"),
                payload.get("password"),
                payload.get("fullName"),
                payload.get("role")
            );
            return ResponseEntity.ok(buildSuccessResponse(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(buildErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        try {
            User user = authService.loginUser(payload.get("email"), payload.get("password"));
            return ResponseEntity.ok(buildSuccessResponse(user));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(buildErrorResponse("AUTH-001", "Invalid credentials"));
        }
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long userId,
            @RequestBody Map<String, String> payload) {
        String fullName = payload.get("fullName") == null ? "" : payload.get("fullName").trim();
        String email = payload.get("email") == null ? "" : payload.get("email").trim().toLowerCase();
        String phoneNumber = payload.get("phoneNumber") == null ? null : payload.get("phoneNumber").trim();
        String emailNotifications = payload.get("emailNotificationsEnabled");

        if ((payload.containsKey("fullName") || payload.containsKey("email")) && (fullName.isBlank() || email.isBlank())) {
            return ResponseEntity.badRequest().body(buildErrorResponse("PROFILE-001", "Full name and email are required."));
        }

        return userRepository.findById(userId)
                .map(user -> {
                    if (!email.isBlank() && !user.getEmail().equalsIgnoreCase(email) && userRepository.existsByEmail(email)) {
                        return ResponseEntity.badRequest().body(buildErrorResponse("PROFILE-002", "Email already registered."));
                    }

                    if (!fullName.isBlank()) {
                        user.setFullName(fullName);
                    }
                    if (!email.isBlank()) {
                        user.setEmail(email);
                    }
                    if (payload.containsKey("phoneNumber")) {
                        user.setPhoneNumber(phoneNumber == null || phoneNumber.isBlank() ? null : phoneNumber);
                    }
                    if (emailNotifications != null) {
                        user.setEmailNotificationsEnabled(Boolean.parseBoolean(emailNotifications));
                    }
                    userRepository.save(user);
                    return ResponseEntity.ok(buildSuccessResponse(user));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildErrorResponse("USER-001", "User not found")));
    }

    @PutMapping("/profile/{userId}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long userId,
            @RequestBody Map<String, String> payload) {
        String currentPassword = payload.get("currentPassword") == null ? "" : payload.get("currentPassword");
        String newPassword = payload.get("newPassword") == null ? "" : payload.get("newPassword");

        if (currentPassword.isBlank() || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(buildErrorResponse("PROFILE-003", "Current and new password are required."));
        }

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(buildErrorResponse("PROFILE-004", "New password must be at least 6 characters."));
        }

        return userRepository.findById(userId)
                .map(user -> {
                    if ("OAUTH2_PROVIDED".equals(user.getPasswordHash())) {
                        return ResponseEntity.badRequest().body(buildErrorResponse("PROFILE-005", "Password changes are unavailable for Google sign-in accounts."));
                    }

                    if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildErrorResponse("PROFILE-006", "Current password is incorrect."));
                    }

                    user.setPasswordHash(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    return ResponseEntity.ok(buildMessageResponse("Password updated successfully."));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildErrorResponse("USER-001", "User not found")));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildErrorResponse("AUTH-002", "No active session"));
        }

        // Google uses 'email', but sometimes 'preferred_username'
        String email = principal.getAttribute("email");
        if (email == null) email = principal.getAttribute("preferred_username");

        // Fetch the real user record from Supabase using the email
        return userRepository.findByEmail(email)
                .map(user -> ResponseEntity.ok(buildSuccessResponse(user)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildErrorResponse("USER-001", "User not found in database")));
    }

    // Helper methods to match SDD JSON format
    private Map<String, Object> buildSuccessResponse(User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("email", user.getEmail());
        userData.put("fullName", user.getFullName());
        userData.put("phoneNumber", user.getPhoneNumber());
        userData.put("role", user.getRole());
        userData.put("emailNotificationsEnabled", user.isEmailNotificationsEnabled());
        
        Map<String, Object> data = new HashMap<>();
        data.put("user", userData);
        data.put("accessToken", "jwt-token-placeholder");
        data.put("refreshToken", "jwt-refresh-placeholder");
        
        response.put("data", data);
        response.put("error", null);
        response.put("timestamp", Instant.now().toString());
        return response;
    }

    private Map<String, Object> buildErrorResponse(String message) {
        return buildErrorResponse("VALID-001", message);
    }

    private Map<String, Object> buildMessageResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", null);
        response.put("error", null);
        response.put("timestamp", Instant.now().toString());
        return response;
    }

    private Map<String, Object> buildErrorResponse(String code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("data", null);
        
        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("message", message);
        
        response.put("error", error);
        response.put("timestamp", Instant.now().toString());
        return response;
    }
}
