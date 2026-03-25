package fr.amu.bestchoice.web.dto.subject;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import fr.amu.bestchoice.model.enums.WorkType;

import java.util.Set;


public record SubjectCreateRequest(

        @NotBlank
        @Size(max = 200)
        String title,

        @NotBlank
        @Size(max = 5000)
        String description,

        String objectives,

        @NotEmpty
        Set<WorkType> workTypes,

        @Min(1)
        Integer maxStudents,

        @Min(1)
        Integer minStudents,

        @Min(1)
        @Max(30)
        Integer credits,

        @Min(1)
        @Max(2)
        Integer semester,

        @Size(max = 9)
        String academicYear,

        Set<String> requiredSkills,

        Set<String> keywords
) {}
