package fr.amu.bestchoice.web.dto.matching;

import fr.amu.bestchoice.service.implementation.algorithmes.MatchingAlgorithmType;

import java.time.Instant;
import java.util.List;

// ✅ IMPORTANT : import du record MatchingRunResult
import fr.amu.bestchoice.web.dto.matching.MatchingRunResult;

public record MatchingRunResponse(
        String sessionId,
        MatchingAlgorithmType algorithmUsed,
        int studentsProcessed,
        int projectsConsidered,
        int resultsComputed,
        int resultsSaved,
        boolean recompute,
        Instant startedAt,
        Instant finishedAt,
        List<String> warnings
) {
    public static MatchingRunResponse from(MatchingRunResult r) {
        return new MatchingRunResponse(
                r.sessionId(),
                r.algorithmUsed(),
                r.studentsProcessed(),
                r.projectsConsidered(),
                r.resultsComputed(),
                r.resultsSaved(),
                r.recompute(),
                r.startedAt(),
                r.finishedAt(),
                r.warnings()
        );
    }
}
