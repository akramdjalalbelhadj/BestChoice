package fr.amu.bestchoice.security;

import fr.amu.bestchoice.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration de Spring Security avec JWT.
 *
 * Cette configuration :
 * - Désactive les sessions (API REST stateless)
 * - Active l'authentification JWT
 * - Protège tous les endpoints (sauf /api/auth/login et /h2-console)
 * - Nécessite le rôle ADMIN pour accéder aux endpoints
 *
 * Flux d'authentification :
 * 1. Client envoie POST /api/auth/login avec email + password
 * 2. AuthController vérifie les credentials et génère un JWT
 * 3. Client reçoit le JWT
 * 4. Client envoie ce JWT dans le header "Authorization: Bearer <token>" pour chaque requête
 * 5. JwtAuthenticationFilter valide le JWT et charge l'utilisateur
 * 6. Spring Security autorise ou refuse l'accès selon les rôles
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Active @PreAuthorize, @Secured, etc.
@RequiredArgsConstructor
public class SecurityConfig {

    // ==================== DÉPENDANCES ====================

    /**
     * Filtre JWT personnalisé.
     * Intercepte toutes les requêtes pour valider le JWT.
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Service pour charger les utilisateurs depuis la base de données.
     */
    private final UserDetailsService userDetailsService;

    /**
     * PasswordEncoder pour hasher et vérifier les mots de passe.
     */
    private final PasswordEncoder passwordEncoder;

    // ==================== CONFIGURATION SÉCURITÉ ====================

    /**
     * Configure la chaîne de filtres de sécurité.
     *
     * @param http HttpSecurity pour configurer la sécurité
     * @return SecurityFilterChain configurée
     * @throws Exception si erreur de configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ==================== CSRF ====================
                // Désactiver CSRF car API REST stateless (pas de cookies de session)
                .csrf(AbstractHttpConfigurer::disable)

                // ==================== AUTORISATION ====================
                .authorizeHttpRequests(auth -> auth
                        // ===== ENDPOINTS PUBLICS (pas d'authentification requise) =====

                        // Console H2 (DEV uniquement - à désactiver en production)
                        .requestMatchers("/h2-console/**").permitAll()

                        // Authentification (login)
                        .requestMatchers("/api/auth/**").permitAll()

                        // Swagger / OpenAPI (documentation)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // ===== ENDPOINTS PROTÉGÉS =====

                        // TOUS les autres endpoints nécessitent le rôle ADMIN
                        .anyRequest().hasRole("ADMIN")
                )

                // ==================== SESSION ====================
                // API REST stateless : pas de session serveur
                // Chaque requête doit contenir le JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ==================== PROVIDER ====================
                // Définir le provider d'authentification
                .authenticationProvider(authenticationProvider())

                // ==================== JWT FILTER ====================
                // Ajouter le filtre JWT AVANT le filtre d'authentification par défaut
                // Ordre : JwtAuthenticationFilter → UsernamePasswordAuthenticationFilter → ...
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // ==================== HEADERS ====================
                // Désactiver X-Frame-Options pour permettre l'affichage de H2 Console
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                );

        return http.build();
    }

    // ==================== AUTHENTICATION PROVIDER ====================

    /**
     * Configure le provider d'authentification.
     *
     * Le provider est responsable de :
     * - Charger l'utilisateur depuis la base de données (via UserDetailsService)
     * - Vérifier que le mot de passe fourni correspond au hash stocké (via PasswordEncoder)
     *
     * Ce provider est utilisé par AuthenticationManager lors du login.
     *
     * @return AuthenticationProvider configuré
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        // Définir le service pour charger les utilisateurs
        provider.setUserDetailsService(userDetailsService);

        // Définir l'encodeur de mots de passe (BCrypt)
        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    // ==================== AUTHENTICATION MANAGER ====================

    /**
     * Bean AuthenticationManager.
     *
     * L'AuthenticationManager est utilisé par AuthService pour :
     * - Vérifier l'email et le mot de passe lors du login
     * - Authentifier l'utilisateur
     *
     * Spring Boot le configure automatiquement, mais on doit l'exposer comme bean
     * pour pouvoir l'injecter dans AuthService.
     *
     * @param config Configuration d'authentification (fournie par Spring)
     * @return AuthenticationManager
     * @throws Exception si erreur de configuration
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}