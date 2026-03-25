package fr.amu.bestchoice.web.mapper;

import fr.amu.bestchoice.model.entity.MatchingCampaign;
import fr.amu.bestchoice.model.entity.MatchingCampaignType;
import fr.amu.bestchoice.web.dto.campaign.MatchingCampaignRequest;
import fr.amu.bestchoice.web.dto.campaign.MatchingCampaignResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MatchingCampaignMapper {

    // ==================== TO ENTITY ====================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "students", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "subjects", ignore = true)
    @Mapping(target = "studentPreferences", ignore = true)
    @Mapping(target = "matchingResults", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    MatchingCampaign toEntity(MatchingCampaignRequest request);

    // ==================== TO RESPONSE ====================

    @Mapping(target = "teacherId", source = "teacher.id")
    /**
     * On concatène le nom et prénom de l'enseignant responsable
     */
    @Mapping(target = "teacherName",
            expression = "java(entity.getTeacher().getUser().getFirstName() + \" \" + entity.getTeacher().getUser().getLastName())")

    /**
     * Calcul automatique du nombre d'étudiants inscrits
     */
    @Mapping(target = "studentsCount",
            expression = "java(entity.getStudents() != null ? entity.getStudents().size() : 0)")

    /**
     * Calcul dynamique du nombre d'items (Projets ou Sujets)
     * en fonction du type de campagne choisi par Jean
     */
    @Mapping(target = "itemsCount",
            expression = "java(entity.getCampaignType() == fr.amu.bestchoice.model.entity.MatchingCampaignType.PROJECT ? " +
                    "(entity.getProjects() != null ? entity.getProjects().size() : 0) : " +
                    "(entity.getSubjects() != null ? entity.getSubjects().size() : 0))")
    MatchingCampaignResponse toResponse(MatchingCampaign entity);

    List<MatchingCampaignResponse> toResponseList(List<MatchingCampaign> entities);
}