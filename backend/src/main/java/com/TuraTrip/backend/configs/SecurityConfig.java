package com.TuraTrip.backend.configs;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

import com.TuraTrip.backend.security.JwtTokenFilter;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.PUT;

@Configuration
@EnableWebSecurity
// Sin esto, los @PreAuthorize("hasRole('ADMIN')") de CategoriaController no se aplicaban.
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers(GET, "/api/v1/usuarios/perfil").permitAll()
                .requestMatchers(GET, "/api/v1/usuarios/perfil/*").permitAll()
                .requestMatchers(GET, "/api/v1/usuarios/perfil/{id:\\d+}").permitAll()
                .requestMatchers(GET, "/api/v1/categorias").permitAll()
                .requestMatchers(GET, "/api/v1/usuarios/buscar").permitAll()
                .requestMatchers(GET, "/api/v1/usuarios/*/seguidores", "/api/v1/usuarios/*/seguidos", "/api/v1/usuarios/*/comentarios").permitAll()
                .requestMatchers(GET, "/api/v1/publicaciones/mias").authenticated()
                .requestMatchers(GET, "/api/v1/publicaciones/siguiendo", "/api/v1/publicaciones/me-gusta", "/api/v1/publicaciones/guardadas").authenticated()
                .requestMatchers(GET, "/api/v1/publicaciones/usuario/*").permitAll()
                .requestMatchers(POST, "/api/v1/publicaciones/**").authenticated()
                .requestMatchers(PUT, "/api/v1/publicaciones/**").authenticated()
                .requestMatchers(DELETE, "/api/v1/publicaciones/**").authenticated()
                .requestMatchers(GET, "/api/v1/publicaciones/**").permitAll()
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontendUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Se definen explícitamente las cabeceras permitidas y expuestas para evitar roces con credentials
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
