package edu.cit.abella.citcare.admin;

import edu.cit.abella.citcare.entity.User;
import edu.cit.abella.citcare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private static final Set<String> VALID_ROLES = Set.of("NEW", "STUDENT", "MEDICAL_STAFF", "GUIDANCE_STAFF", "ADMIN");

    @Autowired
    private UserRepository userRepository;

    @PutMapping("/update-role/{userId}")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long userId,
            @RequestBody RoleUpdateRequest roleRequest,
            @RequestHeader(value = "X-User-Role", required = false) String requesterRole) {
        if (!isAdmin(requesterRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(errorResponse("ADMIN-001", "Admin access required."));
        }

        String requestedRole = roleRequest.getRole() == null ? "" : roleRequest.getRole().trim().toUpperCase();
        if (!VALID_ROLES.contains(requestedRole)) {
            return ResponseEntity.badRequest()
                    .body(errorResponse("ADMIN-002", "Role must be NEW, STUDENT, MEDICAL_STAFF, GUIDANCE_STAFF, or ADMIN."));
        }

        return userRepository.findById(userId)
                .map(user -> {
                    user.setRole(requestedRole);
                    userRepository.save(user);
                    return ResponseEntity.ok(successResponse(user, "User " + user.getFullName() + " is now " + user.getRole() + "."));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update-name/{userId}")
    public ResponseEntity<?> updateUserName(
            @PathVariable Long userId,
            @RequestBody NameUpdateRequest nameRequest,
            @RequestHeader(value = "X-User-Role", required = false) String requesterRole) {
        return updateUser(userId, nameRequest, requesterRole);
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestBody NameUpdateRequest updateRequest,
            @RequestHeader(value = "X-User-Role", required = false) String requesterRole) {
        if (!isAdmin(requesterRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(errorResponse("ADMIN-001", "Admin access required."));
        }

        String requestedName = updateRequest.getFullName() == null ? "" : updateRequest.getFullName().trim();
        String requestedRole = updateRequest.getRole() == null ? null : updateRequest.getRole().trim().toUpperCase();

        if (requestedName.isBlank() && requestedRole == null) {
            return ResponseEntity.badRequest()
                    .body(errorResponse("ADMIN-003", "Full name or role is required."));
        }

        if (updateRequest.getFullName() != null && requestedName.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(errorResponse("ADMIN-003", "Full name is required."));
        }

        if (requestedRole != null && !VALID_ROLES.contains(requestedRole)) {
            return ResponseEntity.badRequest()
                    .body(errorResponse("ADMIN-002", "Role must be NEW, STUDENT, MEDICAL_STAFF, GUIDANCE_STAFF, or ADMIN."));
        }

        return userRepository.findById(userId)
                .map(user -> {
                    if (!requestedName.isBlank()) {
                        user.setFullName(requestedName);
                    }
                    if (requestedRole != null) {
                        user.setRole(requestedRole);
                    }
                    userRepository.save(user);
                    return ResponseEntity.ok(successResponse(user, "User " + user.getFullName() + " was updated."));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String requesterRole,
            @RequestHeader(value = "X-User-Id", required = false) Long requesterId) {
        if (!isAdmin(requesterRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(errorResponse("ADMIN-001", "Admin access required."));
        }

        if (requesterId != null && requesterId.equals(userId)) {
            return ResponseEntity.badRequest()
                    .body(errorResponse("ADMIN-004", "You cannot delete your own admin account."));
        }

        return userRepository.findById(userId)
                .map(user -> {
                    userRepository.delete(user);
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "User " + user.getFullName() + " was deleted.");
                    response.put("error", null);
                    response.put("timestamp", Instant.now().toString());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestHeader(value = "X-User-Role", required = false) String requesterRole) {
        if (!isAdmin(requesterRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(errorResponse("ADMIN-001", "Admin access required."));
        }

        List<Map<String, Object>> users = userRepository.findAll().stream()
                .map(this::userResponse)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", users);
        response.put("error", null);
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }

    private boolean isAdmin(String role) {
        return role != null && role.equalsIgnoreCase("ADMIN");
    }

    private Map<String, Object> successResponse(User user, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", userResponse(user));
        response.put("role", user.getRole());
        response.put("message", message);
        response.put("error", null);
        response.put("timestamp", Instant.now().toString());
        return response;
    }

    private Map<String, Object> userResponse(User user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("email", user.getEmail());
        userData.put("fullName", user.getFullName());
        userData.put("phoneNumber", user.getPhoneNumber());
        userData.put("role", user.getRole());
        userData.put("emailNotificationsEnabled", user.isEmailNotificationsEnabled());
        return userData;
    }

    private Map<String, Object> errorResponse(String code, String message) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("message", message);
        response.put("success", false);
        response.put("data", null);
        response.put("error", error);
        response.put("timestamp", Instant.now().toString());
        return response;
    }
}
