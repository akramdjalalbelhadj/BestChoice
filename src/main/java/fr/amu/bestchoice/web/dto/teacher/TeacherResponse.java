package fr.amu.bestchoice.web.dto.teacher;

import java.util.Set;

/**
 * DTO de sortie représentant un profil enseignant.
 */
public record TeacherResponse(
        Long id,

        Long userId,
        String email,
        String firstName,
        String lastName,

        String department,
        String academicRank,
        String specialty,
        String websiteUrl,

        // projets liés (IDs)
        Set<Long> projectIds
) {}
