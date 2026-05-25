package edu.cit.abella.citcare.auth;

import edu.cit.abella.citcare.entity.User;
import edu.cit.abella.citcare.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerUserCreatesEncodedNewUserWhenEmailIsAvailable() {
        when(userRepository.existsByEmail("student@cit.edu")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = authService.registerUser("student@cit.edu", "secret", "Student User", "STUDENT");

        assertEquals("student@cit.edu", user.getEmail());
        assertEquals("encoded-secret", user.getPasswordHash());
        assertEquals("Student User", user.getFullName());
        assertEquals("NEW", user.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUserRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("student@cit.edu")).thenReturn(true);

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> authService.registerUser("student@cit.edu", "secret", "Student User", "STUDENT"));

        assertEquals("Email already registered", error.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUserIgnoresRequestedStaffRoleAndCreatesNewUser() {
        when(userRepository.existsByEmail("guide@cit.edu")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = authService.registerUser("guide@cit.edu", "secret", "Guidance Staff", "guidance_staff");

        assertEquals("NEW", user.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUserIgnoresRequestedAdminRoleAndCreatesNewUser() {
        when(userRepository.existsByEmail("admin-request@cit.edu")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = authService.registerUser("admin-request@cit.edu", "secret", "Admin Request", "ADMIN");

        assertEquals("NEW", user.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void loginUserReturnsUserWhenPasswordMatches() {
        User user = new User();
        user.setEmail("student@cit.edu");
        user.setPasswordHash("encoded-secret");
        when(userRepository.findByEmail("student@cit.edu")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encoded-secret")).thenReturn(true);

        User result = authService.loginUser("student@cit.edu", "secret");

        assertSame(user, result);
    }

    @Test
    void loginUserRejectsInvalidCredentials() {
        when(userRepository.findByEmail("student@cit.edu")).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> authService.loginUser("student@cit.edu", "wrong"));

        assertEquals("Invalid credentials", error.getMessage());
    }
}
