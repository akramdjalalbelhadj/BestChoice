package fr.amu.bestchoice.web.controller.auth;

import fr.amu.bestchoice.service.implementation.auth.AuthService;
import fr.amu.bestchoice.web.dto.auth.LoginRequest;
import fr.amu.bestchoice.web.dto.auth.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour l'authentification.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Endpoints d'authentification (login, JWT)")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Connexion utilisateur",
            description = """
                    Authentifie un utilisateur avec son email et mot de passe.
                    
                    En cas de succès, retourne un JWT (JSON Web Token) valide pendant 1 heure.
                    Ce token doit être inclus dans l'en-tête `Authorization` de toutes les requêtes suivantes.
                    
                    **Compte admin par défaut (DEV) :**
                    - Email : `admin@bestchoice.local`
                    - Password : `Admin12345!`
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentification réussie - JWT généré",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(
                                    name = "Login réussi",
                                    value = """
                                            {
                                              "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6WyJBRE1JTiJdLCJzdWIiOiJhZG1pbkBiZXN0Y2hvaWNlLmxvY2FsIiwiaWF0IjoxNzM1NjQ4MDAwLCJleHAiOjE3MzU2NTE2MDB9.signature",
                                              "tokenType": "Bearer",
                                              "userId": 1,
                                              "email": "admin@bestchoice.local",
                                              "firstName": "Admin",
                                              "lastName": "BestChoice",
                                              "roles": ["ADMIN"],
                                              "expiresIn": 3600000
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Email ou mot de passe incorrect",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Erreur d'authentification",
                                    value = """
                                            {
                                              "timestamp": "2025-01-01T10:30:00.123Z",
                                              "status": 401,
                                              "error": "UNAUTHORIZED",
                                              "message": "Email ou mot de passe incorrect",
                                              "path": "/api/auth/login",
                                              "details": {}
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation échouée - Email ou password manquant/invalide",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Validation error",
                                    value = """
                                            {
                                              "timestamp": "2025-01-01T10:30:00.123Z",
                                              "status": 400,
                                              "error": "BAD_REQUEST",
                                              "message": "Validation échouée",
                                              "path": "/api/auth/login",
                                              "details": {
                                                "email": "L'email est obligatoire"
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credentials de connexion (email + password)",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "Login admin",
                                    value = """
                                            {
                                              "email": "admin@bestchoice.local",
                                              "password": "Admin12345!"
                                            }
                                            """
                            )
                    )
            )
            @Valid @RequestBody LoginRequest request) {

        log.info("POST /api/auth/login - Requête de login pour l'email : {}", request.email());
        LoginResponse response = authService.login(request);
        log.info("POST /api/auth/login - Login réussi pour l'utilisateur ID : {}", response.userId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Health check",
            description = "Vérifie que l'API d'authentification fonctionne correctement"
    )
    @ApiResponse(
            responseCode = "200",
            description = "API opérationnelle",
            content = @Content(
                    mediaType = "text/plain",
                    examples = @ExampleObject(value = "Auth API is running")
            )
    )
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.debug("GET /api/auth/health - Health check");
        return ResponseEntity.ok("Auth API is running");
    }
}