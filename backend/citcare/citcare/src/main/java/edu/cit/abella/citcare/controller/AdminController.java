package edu.cit.abella.citcare.controller;

import edu.cit.abella.citcare.dto.RoleUpdateRequest;
import edu.cit.abella.citcare.entity.User;
import edu.cit.abella.citcare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    // This is the "Job" you mentioned: Updating roles of registered users
    @PutMapping("/update-role/{userId}")
    public ResponseEntity<?> updateUserRole(@PathVariable Long userId, @RequestBody RoleUpdateRequest roleRequest) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setRole(roleRequest.getRole().toUpperCase());
                    userRepository.save(user);
                    return ResponseEntity.ok("User " + user.getFullName() + " is now " + user.getRole());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Optional: Admin might want to see all users to know whose role to change
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
