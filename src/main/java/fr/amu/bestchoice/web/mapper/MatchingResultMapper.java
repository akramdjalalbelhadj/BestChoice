package fr.amu.bestchoice.web.mapper;

import fr.amu.bestchoice.model.entity.MatchingResult;
import fr.amu.bestchoice.web.dto.matching.MatchingResultResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper anémique pour l'entité MatchingResult.
 * Contient UNIQUEMENT du mapping Entity → DTO (lecture seule).
 * Utilise la configuration centralisée MapStructConfig.
 *
 * Les MatchingResult sont créés uniquement par l'algorithme de matching,
 * donc pas de CREATE ni UPDATE.
 */
@Mapper(config = MapStructConfig.class)
public interface MatchingResultMapper {

    // ==================== PAS DE CREATE/UPDATE ====================
    // Les MatchingResult sont créés directement par l'algorithme de matching

    // ==================== READ : MatchingResult → MatchingResultResponse ====================

    /**
     * Convertit MatchingResult en MatchingResultResponse.
     *
     * Le DTO ne contient que les champs essentiels (version simplifiée).
     * Les autres champs (workTypeScore, weights, details, etc.) restent en base
     * mais ne sont pas exposés via l'API.
     *
     * @param entity L'entité MatchingResult
     * @return DTO MatchingResultResponse
     */
    @Mapping(target = "studentId", source = "student.id")
    // On extrait l'ID depuis matchingResult.student.id

    @Mapping(target = "projectId", source = "project.id")
    // On extrait l'ID depuis matchingResult.project.id

    MatchingResultResponse toResponse(MatchingResult entity);
    // Les champs id, sessionId, globalScore, skillsScore, interestsScore, calculationDate
    // sont mappés automatiquement car même nom dans Entity et DTO

    /**
     * Convertit une liste de MatchingResult en liste de MatchingResultResponse.
     *
     * @param entities Liste d'entités MatchingResult
     * @return Liste de DTOs MatchingResultResponse
     */
    List<MatchingResultResponse> toResponseList(List<MatchingResult> entities);
}