package fr.amu.bestchoice.web.mapper;

import fr.amu.bestchoice.model.entity.Skill;
import fr.amu.bestchoice.web.dto.skill.SkillCreateRequest;
import fr.amu.bestchoice.web.dto.skill.SkillResponse;
import fr.amu.bestchoice.web.dto.skill.SkillUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Mapper anémique pour l'entité Skill.
 * Contient UNIQUEMENT du mapping DTO ↔ Entity.
 * Utilise la configuration centralisée MapStructConfig.
 */
@Mapper(config = MapStructConfig.class)
public interface SkillMapper {

    // ==================== CREATE : SkillCreateRequest → Skill ====================

    /**
     * Convertit SkillCreateRequest en Skill.
     *
     * @param dto Les données de la nouvelle compétence
     * @return Nouvelle entité Skill (pas encore sauvegardée)
     */
    @Mapping(target = "id", ignore = true)
    // L'ID est généré par la base de données

    @Mapping(target = "active", constant = "true")
    // Nouvelle compétence = active par défaut

    @Mapping(target = "students", ignore = true)
    // Les relations Many-to-Many sont gérées dans le Service

    @Mapping(target = "projectsAsRequired", ignore = true)
    // Les relations Many-to-Many sont gérées dans le Service

    @Mapping(target = "projectsAsTarget", ignore = true)
    // Les relations Many-to-Many sont gérées dans le Service

    Skill toEntity(SkillCreateRequest dto);

    // ==================== UPDATE : SkillUpdateRequest → Skill ====================

    /**
     * Met à jour une Skill existante.
     * Les champs null dans le DTO ne modifient pas l'entité.
     *
     * @param dto Les nouvelles données
     * @param entity L'entité existante à modifier
     */
    @Mapping(target = "id", ignore = true)
    // On ne change jamais l'ID

    @Mapping(target = "students", ignore = true)
    // Les relations sont gérées dans le Service

    @Mapping(target = "projectsAsRequired", ignore = true)

    @Mapping(target = "projectsAsTarget", ignore = true)

    void updateEntityFromDto(SkillUpdateRequest dto, @MappingTarget Skill entity);

    // ==================== READ : Skill → SkillResponse ====================

    /**
     * Convertit Skill en SkillResponse.
     *
     * @param entity L'entité Skill
     * @return DTO SkillResponse
     */
    SkillResponse toResponse(Skill entity);

    /**
     * Convertit une liste de Skill en liste de SkillResponse.
     *
     * @param entities Liste d'entités Skill
     * @return Liste de DTOs SkillResponse
     */
    List<SkillResponse> toResponseList(List<Skill> entities);
}