package fr.amu.bestchoice.web.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de requête pour la connexion
 * Utilisé lors du login via POST /api/auth/login
 */
public record LoginRequest(
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Le format de l'email est invalide")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        String password
) {}
