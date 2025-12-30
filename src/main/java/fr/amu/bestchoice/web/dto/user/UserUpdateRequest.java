package fr.amu.bestchoice.web.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO utilisé par l'ADMIN pour mettre à jour
 * les informations d'identité d'un utilisateur.
 *
 *
 * ⚠️ Important :
 * - L'admin peut modifier uniquement l'identité (nom, prénom, email, numéro étudiant)
 * - Les rôles et le mot de passe ne sont PAS modifiables via ce DTO
 */
public record UserUpdateRequest(

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
        String firstName,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
        String lastName,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Le format de l'email est invalide")
        String email,

        /**
         * Numéro étudiant :
         * - obligatoire uniquement pour les étudiants
         * - ignoré ou null pour les enseignants / admins
         * La cohérence est vérifiée côté service.
         */
        @Size(max = 20, message = "Le numéro étudiant ne doit pas dépasser 20 caractères")
        String studentNumber
) {}
