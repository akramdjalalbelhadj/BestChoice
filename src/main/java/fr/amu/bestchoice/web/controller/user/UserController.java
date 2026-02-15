package fr.amu.bestchoice.web.controller.user;

import fr.amu.bestchoice.service.implementation.auth.RegisterService;
import fr.amu.bestchoice.service.implementation.user.UserService;
import fr.amu.bestchoice.service.interfaces.IUserService;
import fr.amu.bestchoice.web.dto.PageResponseDto;
import fr.amu.bestchoice.web.dto.auth.RegisterRequest;
import fr.amu.bestchoice.web.dto.auth.RegisterResponse;
import fr.amu.bestchoice.web.dto.user.UserResponse;
import fr.amu.bestchoice.web.dto.user.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Utilisateurs", description = "Gestion des utilisateurs")
public class UserController {

    private final IUserService userService;
    private final RegisterService registerService;

    // ==================== READ ====================

    @Operation(
            summary = "Récupérer tous les utilisateurs (paginé)",
            description = "Retourne une page d'utilisateurs avec métadonnées de pagination"
    )
    @GetMapping("/paginated")
    public ResponseEntity<PageResponseDto<UserResponse>> getAllUsersPaginated(
            @Parameter(description = "Numéro de page (commence à 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Taille de page", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Champ de tri", example = "email")
            @RequestParam(required = false) String sortBy,

            @Parameter(description = "Direction du tri (ASC/DESC)", example = "ASC")
            @RequestParam(required = false) String sortDirection
    ) {
        log.debug("🌐 GET /api/users/paginated - page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);

        Page<UserResponse> usersPage = userService.findAll(page, size, sortBy, sortDirection);
        PageResponseDto<UserResponse> response = PageResponseDto.of(usersPage);

        log.info("🌐 GET /api/users/paginated - {} utilisateurs retournés (page {}/{})",
                response.content().size(), response.pageNumber() + 1, response.totalPages());

        return ResponseEntity.ok(response);
    }

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

    @GetMapping("/active")
    public ResponseEntity<List<UserResponse>> getActiveUsers() {
        log.debug("GET /api/users/active - Récupération des utilisateurs actifs");
        List<UserResponse> users = userService.findAllActive();
        log.info("GET /api/users/active - {} utilisateurs actifs retournés", users.size());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.debug("GET /api/users/{} - Récupération de l'utilisateur", id);
        UserResponse user = userService.findById(id);
        log.info("GET /api/users/{} - Utilisateur retourné : email={}", id, user.email());
        return ResponseEntity.ok(user);
    }

    // ==================== CREATE ====================

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
                request.email(), request.role());
        RegisterResponse registeredUser = registerService.register(request);
        log.info("POST /api/users/register - Utilisateur inscrit avec succès : id={}, email={}",
                registeredUser.id(), registeredUser.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    // ==================== UPDATE ====================

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        log.info("PUT /api/users/{} - Mise à jour de l'utilisateur", id);
        UserResponse updatedUser = userService.update(id, request);
        log.info("PUT /api/users/{} - Utilisateur mis à jour avec succès : email={}",
                id, updatedUser.email());
        return ResponseEntity.ok(updatedUser);
    }

    // ==================== ACTIVATION / DÉSACTIVATION ====================

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        log.info("PATCH /api/users/{}/deactivate - Désactivation de l'utilisateur", id);
        userService.deactivate(id);
        log.info("PATCH /api/users/{}/deactivate - Utilisateur désactivé avec succès", id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        log.info("PATCH /api/users/{}/activate - Activation de l'utilisateur", id);
        userService.activate(id);
        log.info("PATCH /api/users/{}/activate - Utilisateur activé avec succès", id);
        return ResponseEntity.noContent().build();
    }
}
