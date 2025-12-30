package fr.amu.bestchoice.web.dto.teacher;

import jakarta.validation.constraints.Size;

/**
 * DTO pour compléter / modifier le profil enseignant.
 *
 * remarqus :
 * - Les infos d'identité (nom/prénom/email) sont modifiées via UserUpdateRequest (admin)
 * - Ici ---> uniquement les informations propres au profil enseignant
 */
public record TeacherUpdateRequest(

        @Size(max = 120, message = "Le département ne doit pas dépasser 120 caractères")
        String department,

        @Size(max = 120, message = "Le grade ne doit pas dépasser 120 caractères")
        String academicRank,

        @Size(max = 200, message = "La spécialité ne doit pas dépasser 200 caractères")
        String specialty,

        @Size(max = 255, message = "L'URL du site web ne doit pas dépasser 255 caractères")
        String websiteUrl
) {}
