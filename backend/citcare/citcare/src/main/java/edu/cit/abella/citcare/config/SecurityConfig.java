package edu.cit.abella.citcare.config;

import edu.cit.abella.citcare.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import java.util.List;

//Refactoring 2: The Proxy Pattern (Security)
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> {
                var authCors = new CorsConfiguration();
                authCors.setAllowedOrigins(List.of("http://localhost:5173"));
                authCors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                authCors.setAllowedHeaders(List.of("*"));
                authCors.setAllowCredentials(true);
                return authCors;
            }))
            .authorizeHttpRequests(auth -> auth
                // Added "/login" and "/oauth2/**" to ensure the redirect flow isn't blocked
                .requestMatchers("/api/**", "/login", "/oauth2/**").permitAll() 
                .anyRequest().authenticated()
            )
            // 1. Manual Form Login Fallback
            .formLogin(form -> form
                .loginPage("http://localhost:5173/login")
                .permitAll()
            )
            // 2. Google OAuth2 Login Setup
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService) // Connects to your "Check or Create" DB logic
                )
                // Sends the user back to your React dashboard after Google authenticates them
                .defaultSuccessUrl("http://localhost:5173/auth-success", true)
            );

        return http.build();
    }
}