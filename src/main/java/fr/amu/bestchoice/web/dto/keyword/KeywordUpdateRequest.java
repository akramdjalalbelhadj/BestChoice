package fr.amu.bestchoice.web.dto.keyword;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KeywordUpdateRequest(
        @NotBlank @Size(max = 100) String label,
        @Size(max = 300) String description,
        @Size(max = 50) String domain,
        Boolean active
) {}
