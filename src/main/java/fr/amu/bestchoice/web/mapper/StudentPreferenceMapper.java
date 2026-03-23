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

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "project", ignore = true)

    // ✅ AJOUTS ICI pour supprimer les warnings
    @Mapping(target = "matchingCampaign", ignore = true) // Sera résolu par le Service
    @Mapping(target = "subject", ignore = true)          // Sera résolu par le Service
    StudentPreference toEntity(PreferenceCreateRequest dto);

    // ==================== READ : StudentPreference → PreferenceResponse ====================

    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "projectId", source = "project.id")

    // ✅ AJOUTS ICI pour mapper les relations vers les IDs du DTO
    @Mapping(target = "campaignId", source = "matchingCampaign.id")
    @Mapping(target = "subjectId", source = "subject.id")
    PreferenceResponse toResponse(StudentPreference entity);

    List<PreferenceResponse> toResponseList(List<StudentPreference> entities);
}