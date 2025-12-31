package fr.amu.bestchoice.web.mapper;

import fr.amu.bestchoice.model.entity.Keyword;
import fr.amu.bestchoice.web.dto.keyword.KeywordCreateRequest;
import fr.amu.bestchoice.web.dto.keyword.KeywordResponse;
import fr.amu.bestchoice.web.dto.keyword.KeywordUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Mapper pour l'entité Keyword.
 */
@Mapper(config = MapStructConfig.class)
public interface KeywordMapper {

    // CREATE : KeywordCreateRequest ---> Keyword

    /**
     * Convertit KeywordCreateRequest en Keyword + justif
     */

    @Mapping(target = "id", ignore = true)

    @Mapping(target = "active", constant = "true")

    // Les relations Many-to-Many sont gérées dans le Service
    @Mapping(target = "students", ignore = true)

    // Les relations Many-to-Many sont gérées dans le Service
    @Mapping(target = "projects", ignore = true)

    Keyword toEntity(KeywordCreateRequest dto);

    // UPDATE : KeywordUpdateRequest ---> Keyword

    /**
     * Met à jour un Keyword existant.
     * Les champs null dans le DTO ne modifient pas l'entité
     */
    @Mapping(target = "id", ignore = true)

    // Les relations sont gérées dans le Service
    @Mapping(target = "students", ignore = true)

    @Mapping(target = "projects", ignore = true)

    void updateEntityFromDto(KeywordUpdateRequest dto, @MappingTarget Keyword entity);

    // READ : Keyword -->  KeywordResponse

    /**
     * Convertit Keyword en KeywordResponse.
     *
     * @param entity L'entité Keyword
     * @return DTO KeywordResponse
     */
    KeywordResponse toResponse(Keyword entity);

    /**
     * Convertit une liste de Keyword en liste de KeywordResponse.
     *
     * @param entities Liste d'entités Keyword
     * @return Liste de DTOs KeywordResponse
     */
    List<KeywordResponse> toResponseList(List<Keyword> entities);
}