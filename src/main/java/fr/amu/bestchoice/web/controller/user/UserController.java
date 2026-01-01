package fr.amu.bestchoice.web.controller.user;

import fr.amu.bestchoice.service.auth.RegisterService;
import fr.amu.bestchoice.service.user.UserService;
import fr.amu.bestchoice.web.dto.auth.RegisterRequest;
import fr.amu.bestchoice.web.dto.auth.RegisterResponse;
import fr.amu.bestchoice.web.dto.user.UserResponse;
import fr.amu.bestchoice.web.dto.user.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des utilisateurs (Users).
 *
 * Endpoints disponibles :
 * - GET    /api/users           : Récupérer tous les utilisateurs
 * - GET    /api/users/active    : Récupérer les utilisateurs actifs uniquement
 * - GET    /api/users/{id}      : Récupérer un utilisateur par ID
 * - POST   /api/users/register  : Créer un nouvel utilisateur (inscription admin)
 * - PUT    /api/users/{id}      : Modifier un utilisateur
 * - PATCH  /api/users/{id}/deactivate : Désactiver un utilisateur
 * - PATCH  /api/users/{id}/activate   : Activer un utilisateur
 *
 * Tous les endpoints nécessitent le rôle ADMIN.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Utilisateurs", description = "Gestion des utilisateurs")
public class UserController {


    private final UserService userService;
    private final RegisterService registerService;

    // ==================== READ ====================

    /**
     * Récupère tous les utilisateurs.
     */
    @Operation(
            summary = "Récupérer tous les utilisateurs",
            description = "Retourne la liste complète des utilisateurs (actifs et inactifs)"
    )
    @ApiResponse(responseCode = "200", description = "Liste des utilisateurs retournée")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        log.debug("GET /api/users - Récupération de tous les utilisateurs");

        List<UserResponse> users = userService.findAll();

        log.info("GET /api/users - {} utilisateurs retournés", users.size());

        return ResponseEntity.ok(users);
    }

    /**
     * Récupère uniquement les utilisateurs actifs.
     */
    @GetMapping("/active")
    public ResponseEntity<List<UserResponse>> getActiveUsers() {

        log.debug("GET /api/users/active - Récupération des utilisateurs actifs");

        List<UserResponse> users = userService.findAllActive();

        log.info("GET /api/users/active - {} utilisateurs actifs retournés", users.size());

        return ResponseEntity.ok(users);
    }

    /**
     * Récupère un utilisateur par son ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {

        log.debug("GET /api/users/{} - Récupération de l'utilisateur", id);

        UserResponse user = userService.findById(id);

        log.info("GET /api/users/{} - Utilisateur retourné : email={}", id, user.email());

        return ResponseEntity.ok(user);
    }

    // ==================== CREATE ====================

    /**
     * Crée un nouvel utilisateur (inscription par l'admin).
     */
    @Operation(
            summary = "Créer un nouvel utilisateur",
            description = """
                    Inscrit un nouvel utilisateur (admin uniquement).
                    
                    Crée automatiquement le profil associé selon le rôle :
                    - ETUDIANT → Profil Student vide
                    - ENSEIGNANT → Profil Teacher vide
                    - ADMIN → Pas de profil
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Email ou numéro étudiant déjà utilisé"),
            @ApiResponse(responseCode = "401", description = "Non authentifié - JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle ADMIN requis")
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@Valid @RequestBody RegisterRequest request) {

        log.info("POST /api/users/register - Inscription d'un utilisateur : email={}, roles={}",
                request.email(), request.roles());

        RegisterResponse registeredUser = registerService.register(request);

        log.info("POST /api/users/register - Utilisateur inscrit avec succès : id={}, email={}",
                registeredUser.id(), registeredUser.email());

        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    // ==================== UPDATE ====================

    /**
     * Met à jour un utilisateur existant.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {

        log.info("PUT /api/users/{} - Mise à jour de l'utilisateur", id);

        UserResponse updatedUser = userService.update(id, request);

        log.info("PUT /api/users/{} - Utilisateur mis à jour avec succès : email={}",
                id, updatedUser.email());

        return ResponseEntity.ok(updatedUser);
    }

    // ==================== ACTIVATION / DÉSACTIVATION ====================

    /**
     * Désactive un utilisateur.
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {

        log.info("PATCH /api/users/{}/deactivate - Désactivation de l'utilisateur", id);

        userService.deactivate(id);

        log.info("PATCH /api/users/{}/deactivate - Utilisateur désactivé avec succès", id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Active un utilisateur.
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {

        log.info("PATCH /api/users/{}/activate - Activation de l'utilisateur", id);

        userService.activate(id);

        log.info("PATCH /api/users/{}/activate - Utilisateur activé avec succès", id);

        return ResponseEntity.noContent().build();
    }
}