package fr.amu.bestchoice.web.dto.matching;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MatchingResultResponse(
        Long id,
        String sessionId,
        Long studentId,
        Long projectId,
        BigDecimal globalScore,
        BigDecimal skillsScore,
        BigDecimal interestsScore,
        LocalDateTime calculationDate
) {}
