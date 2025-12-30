package fr.amu.bestchoice.web.dto.student;

import fr.amu.bestchoice.model.enums.WorkType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * DTO pour compléter / modifier le profil étudiant (infos académiques + préférences).
 *
 * Exemple :
 * - compléter le profil après création du compte
 * - Ajouter ou corriger des infos académiques
 *
 * remarque:( à voir plus tard peut etre changer )
 * - Les relations (skills/interests) sont envoyées sous forme d'IDs
 * - La vérification de l'existence des IDs se fait en service.
 */

public record StudentUpdateRequest(

        @Min(value = 1, message = "Le niveau d'étude doit être au minimum 1")
        @Max(value = 5, message = "Le niveau d'étude doit être au maximum 5")
        Integer studyYear,

        // Type de travail préféré (optionnel)
        WorkType preferredWorkType,

        // IDs de compétences (Skill)
        Set<Long> skillIds,

        // IDs de centres d'intérêt (Keyword)
        Set<Long> interestKeywordIds,

        @Size(max = 255, message = "L'URL GitHub ne doit pas dépasser 255 caractères")
        String githubUrl,

        @Size(max = 255, message = "L'URL du portfolio ne doit pas dépasser 255 caractères")
        String portfolioUrl,

        String linkedinUrl
) {}
