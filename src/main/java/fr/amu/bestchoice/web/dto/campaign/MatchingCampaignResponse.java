package fr.amu.bestchoice.web.dto.campaign;

import fr.amu.bestchoice.model.entity.MatchingCampaignType;
import fr.amu.bestchoice.service.implementation.algorithmes.MatchingAlgorithmType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MatchingCampaignResponse(

        Long id,
        String name,
        String description,
        String academicYear,
        Integer semester,
        MatchingCampaignType campaignType,
        MatchingAlgorithmType algorithmType,
        BigDecimal skillsWeight,
        BigDecimal workTypeWeight,
        BigDecimal interestsWeight,
        Long teacherId,
        String teacherName,
        int studentsCount,
        int itemsCount,
        LocalDateTime createdAt
) {}