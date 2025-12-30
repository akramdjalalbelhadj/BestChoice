package fr.amu.bestchoice.web.dto.project;

import fr.amu.bestchoice.model.enums.WorkType;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public record ProjectUpdateRequest(
        @Size(max = 150) String title,
        @Size(max = 3000) String description,
        WorkType workType,
        Boolean remotePossible,
        Integer minStudents,
        Integer maxStudents,
        Boolean active,
        LocalDate startDate,
        LocalDate endDate,
        Set<Long> requiredSkillIds,
        Set<Long> keywordIds
) {}
