package fr.amu.bestchoice.web.dto.preference;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de requête pour créer une préférence (choix de projet)
 *
 * Permet à un étudiant d'exprimer son intérêt pour un projet
 * et de le classer dans ses préférences (1er choix, 2ème choix, )
 */

public record PreferenceCreateRequest(

        @NotNull(message = "L'ID de l'étudiant est obligatoire")
        Long studentId,

        @NotNull(message = "Le projectId est obligatoire")
        Long projectId,

        @NotNull(message = "Le rank est obligatoire")
        @Min(value = 1, message = "Le rank doit être >= 1")
        @Max(value = 10, message = "Le rank doit être <= 10")
        Integer rank,

        String motivation,
        String comment

) {}
