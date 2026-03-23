package fr.amu.bestchoice.web.dto.matching;

import fr.amu.bestchoice.service.implementation.algorithmes.MatchingAlgorithmType;
import java.time.Instant;

public record MatchingRunResponse(
        Long campaignId,
        MatchingAlgorithmType algorithmUsed,
        int studentsProcessed,
        Instant startedAt,
        Instant finishedAt
) {
    public static MatchingRunResponse from(MatchingRunResult r) {
        return new MatchingRunResponse(
                r.campaignId(),
                r.algorithmUsed(),
                r.studentsProcessed(),
                r.startedAt(),
                r.finishedAt()
        );
    }
}