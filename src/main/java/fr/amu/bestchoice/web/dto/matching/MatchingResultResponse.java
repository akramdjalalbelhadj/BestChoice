package fr.amu.bestchoice.web.dto.matching;

import fr.amu.bestchoice.service.implementation.algorithmes.MatchingAlgorithmType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MatchingResultResponse(
        Long id,
        Long studentId,
        Long campaignId,
        Long projectId,
        Long subjectId,
        BigDecimal globalScore,
        BigDecimal skillsScore,
        BigDecimal interestsScore,
        Integer recommendationRank,
        MatchingAlgorithmType algorithmUsed,
        LocalDateTime calculationDate
) {}