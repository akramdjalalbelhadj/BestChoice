package fr.amu.bestchoice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration Spring Security (version minimale).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                /**
                 * Désactivation CSRF :
                 * - API REST stateless
                 * - Pas de formulaires HTML
                 */
                .csrf(csrf -> csrf.disable())

                /**
                 * Pas de session serveur (API REST)
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                /**
                 * Règles d'autorisation
                 */
                .authorizeHttpRequests(auth -> auth

                        // Console H2 (DEV uniquement)
                        .requestMatchers("/h2-console/**").permitAll()

                        // Swagger / OpenAPI (sera activé à l'étape 4)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // TOUT le reste nécessite ROLE_ADMIN
                        .anyRequest().hasRole("ADMIN")
                )

                /**
                 * Authentification HTTP Basic
                 * (simple pour tests avec Swagger / Postman)
                 */
                .httpBasic(Customizer.withDefaults())

                /**
                 * Nécessaire pour afficher la console H2
                 */
                .headers(headers ->
                        headers.frameOptions(frame -> frame.disable())
                );

        return http.build();
    }
}
