package fr.amu.bestchoice.web.dto.project;

import fr.amu.bestchoice.model.enums.WorkType;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Set;

public record ProjectCreateRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 3000) String description,
        @NotNull WorkType workType,
        Boolean remotePossible,
        @Min(1) Integer minStudents,
        @Min(1) Integer maxStudents,
        Set<Long> requiredSkillIds,
        Set<Long> keywordIds
) {}
