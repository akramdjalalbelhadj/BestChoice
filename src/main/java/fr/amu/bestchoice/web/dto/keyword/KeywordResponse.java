package fr.amu.bestchoice.web.dto.keyword;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KeywordResponse(
        Long id,
        String label,
        String description,
        String domain,
        Boolean active
) {}

