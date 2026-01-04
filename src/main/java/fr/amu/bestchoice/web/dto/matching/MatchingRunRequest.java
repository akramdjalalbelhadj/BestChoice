package fr.amu.bestchoice.web.dto.matching;

import fr.amu.bestchoice.service.implementation.algorithmes.MatchingAlgorithmType;
import fr.amu.bestchoice.service.implementation.algorithmes.MatchingScope;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Requête pour lancer un matching.
 * - algorithm : WEIGHTED | STABLE | HYBRID
 * - scope : ALL_STUDENTS | ONE_STUDENT
 * - studentId : requis si scope=ONE_STUDENT
 * - recompute : true => recalculer même si déjà existant
 * - persist : true => sauvegarder en DB (MatchingResult)
 * - threshold : optionnel (ex: 0.50)
 * - weights : optionnel (weights pour Weighted/Hybrid)
 */
public record MatchingRunRequest(
        MatchingAlgorithmType algorithm,
        MatchingScope scope,
        Long studentId,
        boolean recompute,
        boolean persist,
        BigDecimal threshold,
        Map<String, BigDecimal> weights
) {

    public MatchingRunRequest withRecompute(boolean recompute) {
        return new MatchingRunRequest(
                this.algorithm(),
                this.scope(),
                this.studentId(),
                recompute,
                this.persist(),
                this.threshold(),
                this.weights()
        );
    }

}
