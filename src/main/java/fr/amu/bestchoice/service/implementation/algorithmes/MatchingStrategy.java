package fr.amu.bestchoice.service.implementation.algorithmes;

import fr.amu.bestchoice.web.dto.matching.MatchingRunRequest;
import fr.amu.bestchoice.web.dto.matching.MatchingRunResult;


public interface MatchingStrategy {

    MatchingAlgorithmType algorithmType();

    /**
     * Exécute l'algo sur la base des paramètres.
     * Persist/recompute gérés par le Context (ou par la stratégie si tu préfères).
     */
    MatchingRunResult execute(MatchingRunRequest request);
}
