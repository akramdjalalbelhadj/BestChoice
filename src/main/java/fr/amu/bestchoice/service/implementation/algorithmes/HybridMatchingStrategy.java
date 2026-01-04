package fr.amu.bestchoice.service.implementation.algorithmes;

import fr.amu.bestchoice.web.dto.matching.MatchingRunRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import fr.amu.bestchoice.web.dto.matching.MatchingRunResult;
/**
 * HYBRID = Weighted (scoring) + Stable (allocation stable).
 *
 * Design simple :
 * - On appelle d'abord Weighted pour produire scores & ranking
 * - Puis on appelle Stable pour produire affectations stables
 */
@Service
@RequiredArgsConstructor
public class HybridMatchingStrategy implements MatchingStrategy {

    private final WeightedMatchingStrategy weightedStrategy;
    private final StableMatchingStrategy stableStrategy;

    @Override
    public MatchingAlgorithmType algorithmType() {
        return MatchingAlgorithmType.HYBRID;
    }

    @Override
    public MatchingRunResult execute(MatchingRunRequest request) {
        Instant start = Instant.now();
        List<String> warnings = new ArrayList<>();

        // 1) Weighted => calc scores/ranking (et persist si request.persist=true selon ton design)
        MatchingRunResult weightedRes = weightedStrategy.execute(request);

        // 2) Stable => utilise ranking/préférences produites par weighted (ou les recalcule)
        MatchingRunResult stableRes = stableStrategy.execute(request);

        // 3) Fusion "métadonnées" (simple)
        Instant end = Instant.now();

        // On additionne grossièrement, à ajuster selon ta vraie implémentation
        int studentsProcessed = Math.max(weightedRes.studentsProcessed(), stableRes.studentsProcessed());
        int projectsConsidered = Math.max(weightedRes.projectsConsidered(), stableRes.projectsConsidered());

        // resultsComputed : weighted calc des scores pour tous couples, stable calc des affectations
        int resultsComputed = weightedRes.resultsComputed() + stableRes.resultsComputed();
        int resultsSaved = weightedRes.resultsSaved() + stableRes.resultsSaved();

        warnings.addAll(weightedRes.warnings());
        warnings.addAll(stableRes.warnings());

        return new MatchingRunResult(
                stableRes.sessionId(), // ou un nouveau sessionId si tu veux
                MatchingAlgorithmType.HYBRID,
                studentsProcessed,
                projectsConsidered,
                resultsComputed,
                resultsSaved,
                request.recompute(),
                start,
                end,
                warnings
        );
    }
}
