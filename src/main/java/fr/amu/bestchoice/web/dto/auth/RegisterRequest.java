package fr.amu.bestchoice.web.dto.auth;

import fr.amu.bestchoice.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * DTO de requête pour l'inscription d'un nouvel utilisateur
 * Utilisé lors de l'inscription via POST /api/auth/register
 *
 * Note importante : Les roles sont attribués  par l'admin --> pas par l'utilisateur
 * Ce champ est présent uniquement pour que l'admin qui puisse créer des comptes
 * avec des rôles spécifiques.
 *
 * endpoint ADMIN ONLY (Spring Security)
 */
public record RegisterRequest(

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
        String firstName,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
        String lastName,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email doit être au format valide (exemple@etu.univ-amu.fr)")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, max = 64, message = "Le mot de passe doit contenir au moins 8 caractères")
        String password,

        /**
         * numero étudiant
         * (optionnel --> uniquement pour les étudiants)
         * ignoré pour les enseignants*/

        @Size(max = 20, message = "Le numéro étudiant ne doit pas dépasser 20 caractères")
        String studentNumber,

        // roles attribués (géré par le admin, pas par l'utilisateur)
        @NotNull(message = "Le rôle est obligatoire")
        Set<Role> roles
) {}
