package fr.amu.bestchoice.web.dto.auth;

import fr.amu.bestchoice.model.enums.Role;

import java.util.Set;

public record LoginResponse(

        // Token JWT pour les requêtes authentifiées
        String accessToken,

        // Type de token (toujours "Bearer")
        String tokenType,

        // Informations de l'utilisateur connecté
        Long userId,
        String email,
        String firstName,
        String lastName,

        // Rôles de l'utilisateur (ETUDIANT, ENSEIGNANT, ADMIN)
        Set<Role> roles,

        // Durée de validité du token en millisecondes
        Long expiresIn
) {
    /**
     * Constructeur avec tokenType par défaut "Bearer"
     */
    public LoginResponse(String token, Long userId, String email, String firstName, String lastName, Set<Role> roles, Long expiresIn) {
        this(token, "Bearer", userId, email, firstName, lastName, roles, expiresIn);
    }
}
