package fr.amu.bestchoice.web.mapper;

import fr.amu.bestchoice.model.entity.Student;
import fr.amu.bestchoice.web.dto.student.StudentCreateRequest;
import fr.amu.bestchoice.web.dto.student.StudentResponse;
import fr.amu.bestchoice.web.dto.student.StudentUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Mapper anémique pour l'entité Student.
 * Contient UNIQUEMENT du mapping DTO ↔ Entity.
 * Utilise la configuration centralisée MapStructConfig.
 *
 * Les champs skills et interests (Set<String> dans DTO → Set<Skill>/Set<Keyword> dans Entity)
 * seront résolus dans le Service (StudentService).
 * Le mapper ignore ces champs.
 */
@Mapper(config = MapStructConfig.class)
public interface StudentMapper {

    // ==================== CREATE ====================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "profileComplete", constant = "false")
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "targetField", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "cvUrl", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "interests", ignore = true)
    @Mapping(target = "preferences", ignore = true)
    @Mapping(target = "matchingResults", ignore = true)
    @Mapping(target = "assignedProject", ignore = true)

    // ✅ AJOUT ICI : On ignore pour le gérer dans le Service
    @Mapping(target = "preferredWorkTypes", ignore = true)
    Student toEntity(StudentCreateRequest dto);

    // ==================== UPDATE ====================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "profileComplete", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "interests", ignore = true)
    @Mapping(target = "preferences", ignore = true)
    @Mapping(target = "matchingResults", ignore = true)
    @Mapping(target = "assignedProject", ignore = true)
    @Mapping(target = "preferredWorkTypes", ignore = true)

    @Mapping(target = "program", ignore = true)
    @Mapping(target = "track", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "targetField", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "cvUrl", ignore = true)

    void updateEntityFromDto(StudentUpdateRequest dto, @MappingTarget Student entity);

    // ==================== READ ====================
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "studentNumber", source = "user.studentNumber")
    @Mapping(target = "skill", ignore = true)
    @Mapping(target = "interestKeyword", ignore = true)
    @Mapping(target = "assignedProjectId", source = "assignedProject.id")

    // ✅ AJOUT ICI : Mappe le Set d'énums vers le Set du DTO (automatique si noms identiques)
    @Mapping(target = "preferredWorkTypes", source = "preferredWorkTypes")
    StudentResponse toResponse(Student entity);

    List<StudentResponse> toResponseList(List<Student> entities);
}