package fr.amu.bestchoice.web.controller;

import fr.amu.bestchoice.service.AuthService;
import fr.amu.bestchoice.web.dto.auth.LoginRequest;
import fr.amu.bestchoice.web.dto.auth.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour l'authentification.
 *
 * Endpoints :
 * - POST /api/auth/login : Authentifier un utilisateur et obtenir un JWT
 *
 * Ce contrôleur est PUBLIC (pas besoin d'être authentifié pour y accéder).
 * Voir SecurityConfig : .requestMatchers("/api/auth/**").permitAll()
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // ==================== DÉPENDANCES ====================

    /**
     * Service de gestion de l'authentification.
     */
    private final AuthService authService;

    // ==================== ENDPOINTS ====================

    /**
     * Endpoint de connexion.
     *
     * Permet à un utilisateur de s'authentifier et d'obtenir un JWT.
     *
     * Requête :
     * POST /api/auth/login
     * Content-Type: application/json
     * Body:
     * {
     *   "email": "admin@bestchoice.local",
     *   "password": "Admin12345!"
     * }
     *
     * Réponse en cas de succès (200 OK) :
     * {
     *   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "tokenType": "Bearer",
     *   "userId": 1,
     *   "email": "admin@bestchoice.local",
     *   "firstName": "Admin",
     *   "lastName": "BestChoice",
     *   "roles": ["ADMIN"],
     *   "expiresIn": 3600000
     * }
     *
     * Réponse en cas d'échec (401 Unauthorized) :
     * {
     *   "timestamp": "2025-01-01T10:30:00Z",
     *   "status": 401,
     *   "error": "UNAUTHORIZED",
     *   "message": "Email ou mot de passe incorrect",
     *   "path": "/api/auth/login"
     * }
     *
     * @param request DTO contenant l'email et le mot de passe
     * @return LoginResponse avec le JWT et les infos utilisateur
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        log.info("Requête de login reçue pour l'email : {}", request.email());

        // Appeler le service pour authentifier l'utilisateur et générer le JWT
        LoginResponse response = authService.login(request);

        log.info("Login réussi pour l'utilisateur ID : {}", response.userId());

        // Retourner la réponse avec le code HTTP 200 OK
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de test pour vérifier que le contrôleur fonctionne.
     *
     * GET /api/auth/health
     *
     * Retourne : "Auth API is running"
     *
     * @return Message de statut
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth API is running");
    }
}