package fr.amu.bestchoice.web.dto.project;

import fr.amu.bestchoice.model.enums.WorkType;
import jakarta.validation.constraints.*;

import java.util.Set;

public record ProjectCreateRequest(

        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 150, message = "Le titre ne doit pas dépasser 150 caractères")
        String title,

        @NotBlank(message = "La description est obligatoire")
        @Size(max = 3000, message = "La description ne doit pas dépasser 3000 caractères")
        String description,

        @NotNull(message = "Le type de travail est obligatoire")
        WorkType workType,

        Boolean remotePossible,

        @Min(value = 1, message = "Le nombre minimum d'étudiants doit être au moins 1")
        Integer minStudents,

        @Min(value = 1, message = "Le nombre maximum d'étudiants doit être au moins 1")
        Integer maxStudents,

        Set<String> requiredSkill,
        Set<String> keyword
) {}
