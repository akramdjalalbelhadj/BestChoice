package fr.amu.bestchoice.web.dto.matching;

import fr.amu.bestchoice.service.implementation.algorithmes.MatchingAlgorithmType;

import java.time.Instant;
import java.util.List;

/**
 * Résultat du run de matching côté service.
 */
public record MatchingRunResult(
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
}
