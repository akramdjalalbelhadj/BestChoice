package fr.amu.bestchoice.web.mapper;

import fr.amu.bestchoice.model.entity.StudentPreference;
import fr.amu.bestchoice.web.dto.preference.PreferenceCreateRequest;
import fr.amu.bestchoice.web.dto.preference.PreferenceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper anémique pour l'entité StudentPreference.
 * Contient UNIQUEMENT du mapping DTO ↔ Entity.
 * Utilise la configuration centralisée MapStructConfig.
 */
@Mapper(config = MapStructConfig.class)
public interface StudentPreferenceMapper {

    // ==================== CREATE : PreferenceCreateRequest → StudentPreference ====================

    /**
     * Convertit PreferenceCreateRequest en StudentPreference.
     *
     * Les champs student et project seront résolus dans le Service
     * à partir de studentId et projectId.
     *
     * @param dto Les données de la nouvelle préférence
     * @return Nouvelle entité StudentPreference (pas encore sauvegardée)
     */
    @Mapping(target = "id", ignore = true)
    // L'ID est généré par la base de données

    @Mapping(target = "status", constant = "PENDING")
    // Nouvelle préférence = statut PENDING par défaut

    @Mapping(target = "createdAt", ignore = true)
    // La date est gérée par @CreationTimestamp

    @Mapping(target = "student", ignore = true)
    // La relation Student sera résolue dans le Service
    // via studentRepository.findById(dto.studentId())

    @Mapping(target = "project", ignore = true)
    // La relation Project sera résolue dans le Service
    // via projectRepository.findById(dto.projectId())

    StudentPreference toEntity(PreferenceCreateRequest dto);
    // Les champs rank, motivation, comment sont mappés automatiquement car même nom

    // ==================== PAS DE UPDATE ====================
    // Les préférences ne sont pas modifiables, uniquement création ou suppression

    // ==================== READ : StudentPreference → PreferenceResponse ====================

    /**
     * Convertit StudentPreference en PreferenceResponse.
     *
     * @param entity L'entité StudentPreference
     * @return DTO PreferenceResponse
     */
    @Mapping(target = "studentId", source = "student.id")
    // On extrait l'ID depuis preference.student.id

    @Mapping(target = "projectId", source = "project.id")
    // On extrait l'ID depuis preference.project.id

    PreferenceResponse toResponse(StudentPreference entity);

    /**
     * Convertit une liste de StudentPreference en liste de PreferenceResponse.
     *
     * @param entities Liste d'entités StudentPreference
     * @return Liste de DTOs PreferenceResponse
     */
    List<PreferenceResponse> toResponseList(List<StudentPreference> entities);
}