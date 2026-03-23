package fr.amu.bestchoice.web.mapper;

import fr.amu.bestchoice.model.entity.Project;
import fr.amu.bestchoice.web.dto.project.ProjectCreateRequest;
import fr.amu.bestchoice.web.dto.project.ProjectResponse;
import fr.amu.bestchoice.web.dto.project.ProjectUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Mapper anémique pour l'entité Project.
 * Contient UNIQUEMENT du mapping DTO ↔ Entity.
 * Utilise la configuration centralisée MapStructConfig.
 *
 * Les champs requiredSkill et keyword (Set<String> dans DTO → Set<Skill>/Set<Keyword> dans Entity)
 * seront résolus dans le Service (ProjectService).
 * Le mapper ignore ces champs.
 */
@Mapper(config = MapStructConfig.class)
public interface ProjectMapper {

    // ==================== CREATE ====================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "objectives", ignore = true)
    @Mapping(target = "credits", ignore = true)
    @Mapping(target = "durationWeeks", ignore = true)
    @Mapping(target = "semester", ignore = true)
    @Mapping(target = "academicYear", ignore = true)
    @Mapping(target = "targetProgram", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "complet", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "requiredSkills", ignore = true)
    @Mapping(target = "targetSkills", ignore = true)
    @Mapping(target = "keywords", ignore = true)
    @Mapping(target = "preferences", ignore = true)
    @Mapping(target = "matchingResults", ignore = true)
    @Mapping(target = "assignedStudents", ignore = true)

    // ✅ AJOUT ICI : On ignore car le nom diffère et c'est une collection
    @Mapping(target = "workTypes", ignore = true)
    Project toEntity(ProjectCreateRequest dto);

    // ==================== UPDATE ====================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "objectives", ignore = true)
    @Mapping(target = "credits", ignore = true)
    @Mapping(target = "durationWeeks", ignore = true)
    @Mapping(target = "semester", ignore = true)
    @Mapping(target = "academicYear", ignore = true)
    @Mapping(target = "targetProgram", ignore = true)
    @Mapping(target = "complet", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "requiredSkills", ignore = true)
    @Mapping(target = "targetSkills", ignore = true)
    @Mapping(target = "keywords", ignore = true)
    @Mapping(target = "preferences", ignore = true)
    @Mapping(target = "matchingResults", ignore = true)
    @Mapping(target = "assignedStudents", ignore = true)

    // ✅ AJOUT ICI AUSSI
    @Mapping(target = "workTypes", ignore = true)
    void updateEntityFromDto(ProjectUpdateRequest dto, @MappingTarget Project entity);

    // ==================== READ ====================
    @Mapping(target = "teacherId", source = "teacher.id")
    @Mapping(target = "teacherName", ignore = true)
    @Mapping(target = "requiredSkills", ignore = true)
    @Mapping(target = "keywords", ignore = true)
    @Mapping(target = "assignedStudentEmails", ignore = true)

    // ✅ AJOUT ICI : Pour renvoyer la liste des types dans la réponse
    // Si ProjectResponse a aussi un champ 'workTypes' (Set)
    // @Mapping(target = "workTypes", source = "workTypes")
    ProjectResponse toResponse(Project entity);

    List<ProjectResponse> toResponseList(List<Project> entities);
}