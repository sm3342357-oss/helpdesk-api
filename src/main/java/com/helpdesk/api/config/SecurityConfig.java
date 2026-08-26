package com.helpdesk.api.config;

import com.helpdesk.api.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas
                .requestMatchers("/api/auth/registro", "/api/auth/login", "/api/auth/refresh", "/api/ping").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                // Rutas de admin
                .requestMatchers(HttpMethod.POST, "/api/admin/soporte").hasRole("ADMIN")
                // Rutas de soporte
                .requestMatchers(HttpMethod.GET, "/api/tickets").hasAnyRole("SOPORTE", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/tickets/*/estado").hasAnyRole("SOPORTE", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/tickets/vencidos").hasAnyRole("SOPORTE", "ADMIN")
                // Todas las demás rutas requieren autenticación
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
