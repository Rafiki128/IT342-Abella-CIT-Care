package edu.cit.abella.citcare.config;

import edu.cit.abella.citcare.entity.User;
import edu.cit.abella.citcare.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@citcare.test";
    private static final String ADMIN_PASSWORD = "Admin123!";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        ensureDefaultRole();

        userRepository.findByEmail(ADMIN_EMAIL).ifPresentOrElse(
                user -> {
                    user.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
                    if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                        user.setRole("ADMIN");
                    }
                    userRepository.save(user);
                },
                () -> {
                    User admin = new User();
                    admin.setEmail(ADMIN_EMAIL);
                    admin.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
                    admin.setFullName("Test Admin");
                    admin.setRole("ADMIN");
                    userRepository.save(admin);
                }
        );
    }

    private void ensureDefaultRole() {
        jdbcTemplate.update("UPDATE users SET role = 'NEW' WHERE role IS NULL OR TRIM(role) = ''");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN role SET DEFAULT 'NEW'");
    }
}
