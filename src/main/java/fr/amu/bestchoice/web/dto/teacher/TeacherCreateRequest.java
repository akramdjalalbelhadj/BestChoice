package fr.amu.bestchoice.web.dto.teacher;

import jakarta.validation.constraints.Size;

public record TeacherCreateRequest(
        @Size(max = 120) String department,
        @Size(max = 120) String academicRank,
        @Size(max = 200) String specialty,
        @Size(max = 255) String websiteUrl
) {}