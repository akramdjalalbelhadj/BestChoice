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

    // ==================== CREATE : StudentCreateRequest → Student ====================

    /**
     * Convertit StudentCreateRequest en Student.
     *
     * @param dto Les données du nouveau profil étudiant
     * @return Nouvelle entité Student (pas encore sauvegardée)
     */
    @Mapping(target = "id", ignore = true)
    // L'ID est partagé avec User (@MapsId), sera défini dans le Service

    @Mapping(target = "user", ignore = true)
    // La relation User sera définie dans le Service

    @Mapping(target = "profileComplete", constant = "false")
    // Nouveau profil = incomplet par défaut, sera calculé dans le Service

    @Mapping(target = "department", ignore = true)
    // Pas dans le DTO StudentCreateRequest

    @Mapping(target = "targetField", ignore = true)
    // Pas dans le DTO StudentCreateRequest

    @Mapping(target = "bio", ignore = true)
    // Pas dans le DTO StudentCreateRequest

    @Mapping(target = "cvUrl", ignore = true)
    // Pas dans le DTO StudentCreateRequest

    @Mapping(target = "skills", ignore = true)
    // Sera résolu dans le Service
    // Service fera : skillRepository.findByName(nom) pour chaque nom dans dto.skill()

    @Mapping(target = "interests", ignore = true)
    // Sera résolu dans le Service
    // Service fera : keywordRepository.findByLabel(label) pour chaque label dans dto.interestKeyword()

    @Mapping(target = "preferences", ignore = true)
    // Les préférences seront créées plus tard via PreferenceService

    @Mapping(target = "matchingResults", ignore = true)
    // Les résultats de matching seront créés par l'algorithme

    @Mapping(target = "assignedProject", ignore = true)
    // Pas encore assigné à un projet

    Student toEntity(StudentCreateRequest dto);

    // ==================== UPDATE : StudentUpdateRequest → Student ====================

    /**
     * Met à jour un Student existant.
     * Les champs null dans le DTO ne modifient pas l'entité.
     *
     * @param dto Les nouvelles données
     * @param entity L'entité existante à modifier
     */
    @Mapping(target = "id", ignore = true)
    // On ne change jamais l'ID

    @Mapping(target = "user", ignore = true)
    // On ne change pas la relation User ici

    @Mapping(target = "program", ignore = true)
    // Pas dans StudentUpdateRequest

    @Mapping(target = "track", ignore = true)
    // Pas dans StudentUpdateRequest

    @Mapping(target = "department", ignore = true)
    // Pas dans StudentUpdateRequest

    @Mapping(target = "targetField", ignore = true)
    // Pas dans StudentUpdateRequest

    @Mapping(target = "bio", ignore = true)
    // Pas dans StudentUpdateRequest

    @Mapping(target = "cvUrl", ignore = true)
    // Pas dans StudentUpdateRequest

    @Mapping(target = "profileComplete", ignore = true)
    // Sera recalculé dans le Service après la mise à jour

    @Mapping(target = "skills", ignore = true)
    // Sera résolu dans le Service si dto.skill() n'est pas null

    @Mapping(target = "interests", ignore = true)
    // Sera résolu dans le Service si dto.interestKeyword() n'est pas null

    @Mapping(target = "preferences", ignore = true)

    @Mapping(target = "matchingResults", ignore = true)

    @Mapping(target = "assignedProject", ignore = true)

    void updateEntityFromDto(StudentUpdateRequest dto, @MappingTarget Student entity);

    // ==================== READ : Student → StudentResponse ====================

    /**
     * Convertit Student en StudentResponse.
     *
     * Les champs skill et interestKeyword seront remplis dans le Service
     * (boucle sur student.skills et student.interests pour extraire les noms).
     *
     * @param entity L'entité Student
     * @return DTO StudentResponse
     */
    @Mapping(target = "userId", source = "user.id")
    // On prend l'ID depuis student.user.id

    @Mapping(target = "email", source = "user.email")
    // On prend l'email depuis student.user.email

    @Mapping(target = "firstName", source = "user.firstName")

    @Mapping(target = "lastName", source = "user.lastName")

    @Mapping(target = "studentNumber", source = "user.studentNumber")

    @Mapping(target = "studyYear", source = "studyYear")

    @Mapping(target = "skill", ignore = true)
    // Sera rempli dans le Service :
    // student.skills.stream().map(Skill::getName).collect(toSet())

    @Mapping(target = "interestKeyword", ignore = true)
    // Sera rempli dans le Service :
    // student.interests.stream().map(Keyword::getLabel).collect(toSet())

    @Mapping(target = "assignedProjectId", source = "assignedProject.id")
    // On extrait l'ID depuis student.assignedProject.id

    StudentResponse toResponse(Student entity);

    /**
     * Convertit une liste de Student en liste de StudentResponse.
     *
     * @param entities Liste d'entités Student
     * @return Liste de DTOs StudentResponse
     */
    List<StudentResponse> toResponseList(List<Student> entities);
}