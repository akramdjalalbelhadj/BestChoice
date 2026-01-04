package fr.amu.bestchoice.web.dto.project;

import fr.amu.bestchoice.model.enums.WorkType;

import java.time.LocalDate;
import java.util.Set;

public record ProjectResponse(
        Long id,
        String title,
        String description,
        WorkType workType,
        Boolean remotePossible,
        Boolean active,

        Integer minStudents,
        Integer maxStudents,
        Boolean complet,

        Long teacherId,
        String teacherName,

        Set<String> requiredSkills,
        Set<String> keywords,
        Set<String> assignedStudentEmails
) {}
