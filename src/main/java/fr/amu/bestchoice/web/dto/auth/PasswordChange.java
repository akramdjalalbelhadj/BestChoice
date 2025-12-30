package fr.amu.bestchoice.web.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
/**
 * DTO de requête pour changer le mot de passe
 * Utilisé par PUT /api/auth/change-password
 *
 * à faire plus trad :
 * La règle hash de mot de passe saisie  == hash currentPassword sera vérifie en service
 * La règle "newPassword == confirmNewPassword" sera vérifie en service
 */

public record PasswordChange(

        @NotBlank(message = "L'ancien mot de passe est obligatoire")
        String currentPassword,

        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Size(min = 8, message = "Le nouveau mot de passe doit contenir au moins 8 caractères")
        String newPassword,

        @NotBlank(message = "La confirmation du nouveau mot de passe est obligatoire")
        String confirmNewPassword
) {}