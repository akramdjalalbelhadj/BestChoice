package fr.amu.bestchoice.web.mapper;

import fr.amu.bestchoice.model.entity.Teacher;
import fr.amu.bestchoice.web.dto.teacher.TeacherCreateRequest;
import fr.amu.bestchoice.web.dto.teacher.TeacherResponse;
import fr.amu.bestchoice.web.dto.teacher.TeacherUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Mapper anémique pour l'entité Teacher.
 * Contient UNIQUEMENT du mapping DTO ↔ Entity.
 * Utilise la configuration centralisée MapStructConfig.
 */
@Mapper(config = MapStructConfig.class)
public interface TeacherMapper {

    // ==================== CREATE : TeacherCreateRequest → Teacher ====================

    /**
     * Convertit TeacherCreateRequest en Teacher.
     *
     * @param dto Les données du nouveau profil enseignant
     * @return Nouvelle entité Teacher (pas encore sauvegardée)
     */
    @Mapping(target = "id", ignore = true)
    // L'ID est partagé avec User (@MapsId), sera défini dans le Service

    @Mapping(target = "user", ignore = true)
    // La relation User sera définie dans le Service

    @Mapping(target = "projects", ignore = true)
    // Les projets seront créés plus tard via ProjectService

    Teacher toEntity(TeacherCreateRequest dto);

    // ==================== UPDATE : TeacherUpdateRequest → Teacher ====================

    /**
     * Met à jour un Teacher existant.
     * Les champs null dans le DTO ne modifient pas l'entité.
     *
     * @param dto Les nouvelles données
     * @param entity L'entité existante à modifier
     */
    @Mapping(target = "id", ignore = true)
    // On ne change jamais l'ID

    @Mapping(target = "user", ignore = true)
    // On ne change pas la relation User ici

    @Mapping(target = "projects", ignore = true)
    // Les projets ne sont pas modifiés via TeacherUpdateRequest

    void updateEntityFromDto(TeacherUpdateRequest dto, @MappingTarget Teacher entity);

    // ==================== READ : Teacher → TeacherResponse ====================

    /**
     * Convertit Teacher en TeacherResponse.
     *
     * Les champs userId, email, firstName, lastName viennent de teacher.user.
     * Le champ projectIds sera rempli dans le Service (extraction des IDs).
     *
     * @param entity L'entité Teacher
     * @return DTO TeacherResponse
     */
    @Mapping(target = "userId", source = "user.id")
    // On prend l'ID depuis teacher.user.id

    @Mapping(target = "email", source = "user.email")
    // On prend l'email depuis teacher.user.email

    @Mapping(target = "firstName", source = "user.firstName")
    // On prend le prénom depuis teacher.user.firstName

    @Mapping(target = "lastName", source = "user.lastName")
    // On prend le nom depuis teacher.user.lastName

    @Mapping(target = "project", ignore = true)
    // Les projectIds seront extraits dans le Service
    // Boucle sur teacher.projects pour récupérer les IDs

    TeacherResponse toResponse(Teacher entity);

    /**
     * Convertit une liste de Teacher en liste de TeacherResponse.
     *
     * @param entities Liste d'entités Teacher
     * @return Liste de DTOs TeacherResponse
     */
    List<TeacherResponse> toResponseList(List<Teacher> entities);
}