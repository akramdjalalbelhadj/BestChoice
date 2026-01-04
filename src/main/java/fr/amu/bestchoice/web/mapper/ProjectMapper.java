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

    // ==================== CREATE : ProjectCreateRequest → Project ====================

    /**
     * Convertit ProjectCreateRequest en Project.
     *
     * @param dto Les données du nouveau projet
     * @return Nouvelle entité Project (pas encore sauvegardée)
     */
    @Mapping(target = "id", ignore = true)
    // L'ID est généré par la base de données

    @Mapping(target = "objectives", ignore = true)
    // Pas dans ProjectCreateRequest

    @Mapping(target = "credits", ignore = true)
    // Pas dans ProjectCreateRequest

    @Mapping(target = "durationWeeks", ignore = true)
    // Pas dans ProjectCreateRequest

    @Mapping(target = "semester", ignore = true)
    // Pas dans ProjectCreateRequest

    @Mapping(target = "academicYear", ignore = true)
    // Pas dans ProjectCreateRequest

    @Mapping(target = "targetProgram", ignore = true)
    // Pas dans ProjectCreateRequest

    @Mapping(target = "active", constant = "true")
    // Nouveau projet = actif par défaut

    @Mapping(target = "complet", constant = "false")
    // Nouveau projet = pas complet par défaut

    @Mapping(target = "createdAt", ignore = true)
    // Géré par @CreationTimestamp

    @Mapping(target = "teacher", ignore = true)
    // La relation Teacher sera définie dans le Service
    // via teacherRepository.findById(teacherId)

    @Mapping(target = "requiredSkills", ignore = true)
    // Sera résolu dans le Service
    // Service fera : skillRepository.findByName(nom) pour chaque nom dans dto.requiredSkill()

    @Mapping(target = "targetSkills", ignore = true)
    // Pas dans ProjectCreateRequest (peut être ajouté plus tard)

    @Mapping(target = "keywords", ignore = true)
    // Sera résolu dans le Service
    // Service fera : keywordRepository.findByLabel(label) pour chaque label dans dto.keyword()

    @Mapping(target = "preferences", ignore = true)
    // Les préférences seront créées par les étudiants plus tard

    @Mapping(target = "matchingResults", ignore = true)
    // Les résultats seront créés par l'algorithme de matching

    @Mapping(target = "assignedStudents", ignore = true)
    // Pas encore d'étudiants assignés

    Project toEntity(ProjectCreateRequest dto);

    // ==================== UPDATE : ProjectUpdateRequest → Project ====================

    /**
     * Met à jour un Project existant.
     * Les champs null dans le DTO ne modifient pas l'entité.
     *
     * @param dto Les nouvelles données
     * @param entity L'entité existante à modifier
     */
    @Mapping(target = "id", ignore = true)
    // On ne change jamais l'ID

    @Mapping(target = "objectives", ignore = true)
    // Pas dans ProjectUpdateRequest

    @Mapping(target = "credits", ignore = true)
    // Pas dans ProjectUpdateRequest

    @Mapping(target = "durationWeeks", ignore = true)
    // Pas dans ProjectUpdateRequest

    @Mapping(target = "semester", ignore = true)
    // Pas dans ProjectUpdateRequest

    @Mapping(target = "academicYear", ignore = true)
    // Pas dans ProjectUpdateRequest

    @Mapping(target = "targetProgram", ignore = true)
    // Pas dans ProjectUpdateRequest

    @Mapping(target = "complet", ignore = true)
    // Sera calculé automatiquement dans le Service (quand maxStudents atteint)

    @Mapping(target = "createdAt", ignore = true)
    // La date de création ne change jamais

    @Mapping(target = "teacher", ignore = true)
    // On ne change pas l'enseignant responsable via update

    @Mapping(target = "requiredSkills", ignore = true)
    // Sera résolu dans le Service si dto.requiredSkill() n'est pas null

    @Mapping(target = "targetSkills", ignore = true)

    @Mapping(target = "keywords", ignore = true)
    // Sera résolu dans le Service si dto.keyword() n'est pas null

    @Mapping(target = "preferences", ignore = true)

    @Mapping(target = "matchingResults", ignore = true)

    @Mapping(target = "assignedStudents", ignore = true)

    void updateEntityFromDto(ProjectUpdateRequest dto, @MappingTarget Project entity);

    // ==================== READ : Project → ProjectResponse ====================

    /**
     * Convertit Project en ProjectResponse.
     *
     * Le champ teacherName sera rempli dans le Service (concaténation firstName + lastName).
     * Les champs requiredSkills et keywords seront remplis dans le Service
     * (boucles pour extraire les noms).
     *
     * @param entity L'entité Project
     * @return DTO ProjectResponse
     */
    @Mapping(target = "teacherId", source = "teacher.id")
    // On extrait l'ID depuis project.teacher.id

    @Mapping(target = "teacherName", ignore = true)
    // Sera rempli dans le Service :
    // teacher.user.firstName + " " + teacher.user.lastName

    @Mapping(target = "requiredSkills", ignore = true)
    // Sera rempli dans le Service :
    // project.requiredSkills.stream().map(Skill::getName).collect(toSet())

    @Mapping(target = "keywords", ignore = true)

    @Mapping(target = "assignedStudentEmails", ignore = true)

    ProjectResponse toResponse(Project entity);

    /**
     * Convertit une liste de Project en liste de ProjectResponse.
     *
     * @param entities Liste d'entités Project
     * @return Liste de DTOs ProjectResponse
     */
    List<ProjectResponse> toResponseList(List<Project> entities);
}