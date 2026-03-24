package fr.amu.bestchoice.web.dto.subject;

import fr.amu.bestchoice.model.enums.WorkType;

import java.util.Set;

public record SubjectResponse(

        Long id,
        String title,
        String description,
        String objectives,
        Set<WorkType> workTypes,
        Integer maxStudents,
        Integer minStudents,
        Integer credits,
        Integer semester,
        String academicYear,
        Boolean active,
        Long teacherId,
        String teacherName,
        Set<String> requiredSkills,
        Set<String> keywords
) {}