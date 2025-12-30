package fr.amu.bestchoice.web.dto.student;

import fr.amu.bestchoice.model.enums.WorkType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record StudentCreateRequest(
        @Min(1) @Max(8) Integer studyYear,
        @Size(max = 100) String program,
        @Size(max = 100) String track,
        WorkType preferredWorkType,
        Set<Long> skillIds,
        Set<Long> interestKeywordIds,
        String githubUrl,
        String portfolioUrl,
        String linkedinUrl
) {}