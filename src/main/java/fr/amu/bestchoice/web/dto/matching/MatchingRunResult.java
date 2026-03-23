package fr.amu.bestchoice.web.dto.matching;

import fr.amu.bestchoice.service.implementation.algorithmes.MatchingAlgorithmType;

import java.time.Instant;
import java.util.List;

/**
 * Résultat du run de matching côté service.
 */
public record MatchingRunResult(
        Long campaignId,
        MatchingAlgorithmType algorithmUsed,
        int studentsProcessed,
        int resultsStored,
        Instant startedAt,
        Instant finishedAt
) {
}