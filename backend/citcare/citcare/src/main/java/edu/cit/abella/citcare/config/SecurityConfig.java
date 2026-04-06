package edu.cit.abella.citcare.config;

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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

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
                return authCors;
            }))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll() // This fixes your 403 error
                .anyRequest().authenticated()
            );

        return http.build();
    }
}