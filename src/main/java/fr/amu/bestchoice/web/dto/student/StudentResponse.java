package fr.amu.bestchoice.web.dto.student;

import fr.amu.bestchoice.model.enums.WorkType;

import java.util.Set;

/**
 * DTO de sortie représentant un profil étudiant.
 */
public record StudentResponse(
        Long id,

        // lien vers l'utilisateur
        Long userId,
        String email,
        String firstName,
        String lastName,
        String studentNumber,

        // infos académiques
        Integer yearLevel,
        WorkType preferredWorkType,

        // relations sous forme d'IDs
        Set<Long> skillIds,
        Set<Long> interestKeywordIds,

        String githubUrl,
        String portfolioUrl,

        // affectation actuelle (si existante)
        Long assignedProjectId
) {}
