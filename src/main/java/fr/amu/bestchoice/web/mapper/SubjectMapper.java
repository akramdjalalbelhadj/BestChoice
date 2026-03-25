package fr.amu.bestchoice.web.mapper;

import fr.amu.bestchoice.model.entity.Subject;
import fr.amu.bestchoice.web.dto.subject.SubjectCreateRequest;
import fr.amu.bestchoice.web.dto.subject.SubjectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Mapper pour l'entité Subject.
 * Les champs complexes (requiredSkills, keywords) sont résolus manuellement dans SubjectService.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubjectMapper {

    // ==================== CREATE ====================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "preferences", ignore = true)
    @Mapping(target = "requiredSkills", ignore = true)
    @Mapping(target = "targetSkills", ignore = true)
    @Mapping(target = "keywords", ignore = true)
    @Mapping(target = "matchingCampaigns", ignore = true)
    Subject toEntity(SubjectCreateRequest request);

    // ==================== READ ====================
    @Mapping(target = "teacherId", source = "teacher.id")
    @Mapping(target = "teacherName",
            expression = "java(entity.getTeacher().getUser().getFirstName() + \" \" + entity.getTeacher().getUser().getLastName())")
    // La transformation en Set<String> est faite manuellement ou via une méthode default
    @Mapping(target = "requiredSkills", ignore = true)
    @Mapping(target = "keywords", ignore = true)
    SubjectResponse toResponse(Subject entity);

    List<SubjectResponse> toResponseList(List<Subject> entities);
}