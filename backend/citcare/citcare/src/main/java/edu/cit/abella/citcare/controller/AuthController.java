package edu.cit.abella.citcare.controller;

import edu.cit.abella.citcare.entity.User;
import edu.cit.abella.citcare.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:5173")

public class AuthController {

    @Autowired
    private AuthService authService;

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

    // Helper methods to match SDD JSON format
    private Map<String, Object> buildSuccessResponse(User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("email", user.getEmail());
        userData.put("fullName", user.getFullName());
        userData.put("role", user.getRole());
        
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