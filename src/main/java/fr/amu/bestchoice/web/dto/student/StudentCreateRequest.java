package fr.amu.bestchoice.web.dto.student;

import fr.amu.bestchoice.model.enums.WorkType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record StudentCreateRequest(

        @Min(value = 1, message = "L'année d'étude doit être au minimum 1")
        @Max(value = 8, message = "L'année d'étude doit être au maximum 8")
        Integer studyYear,

        @Size(max = 100, message = "La formation ne doit pas dépasser 100 caractères")
        String program,

        @Size(max = 100, message = "Le parcours ne doit pas dépasser 100 caractères")
        String track,

        WorkType preferredWorkType,

        Set<String> skill,

        Set<String> interestKeyword,

        @Size(max = 255, message = "L'URL GitHub ne doit pas dépasser 255 caractères")
        String githubUrl,

        @Size(max = 255, message = "L'URL du portfolio ne doit pas dépasser 255 caractères")
        String portfolioUrl,

        @Size(max = 255, message = "L'URL LinkedIn ne doit pas dépasser 255 caractères")
        String linkedinUrl
) {}
