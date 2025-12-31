package fr.amu.bestchoice.web.dto.keyword;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KeywordCreateRequest(

        @NotBlank(message = "Le libellé du mot-clé est obligatoire")
        @Size(max = 100, message = "Le libellé ne doit pas dépasser 100 caractères")
        String label,

        @Size(max = 300, message = "La description ne doit pas dépasser 300 caractères")
        String description,

        @Size(max = 50, message = "Le domaine ne doit pas dépasser 50 caractères")
        String domain
) {}
