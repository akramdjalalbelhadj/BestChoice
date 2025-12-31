package fr.amu.bestchoice.web.dto.skill;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SkillCreateRequest(

        @NotBlank(message = "Le nom de la compétence est obligatoire")
        @Size(max = 100, message = "Le nom de la compétence ne doit pas dépasser 100 caractères")
        String name,

        @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
        String description,

        @Size(max = 50, message = "La catégorie ne doit pas dépasser 50 caractères")
        String category,

        @Min(value = 1, message = "Le niveau doit être au minimum 1")
        @Max(value = 5, message = "Le niveau doit être au maximum 5")
        Integer level
) {}
