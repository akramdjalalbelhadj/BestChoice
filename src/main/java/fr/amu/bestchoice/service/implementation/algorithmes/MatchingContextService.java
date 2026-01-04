package fr.amu.bestchoice.service.implementation.algorithmes;

import fr.amu.bestchoice.web.dto.matching.MatchingRunRequest;
import fr.amu.bestchoice.web.dto.matching.MatchingRunResult;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class MatchingContextService {

    private final Map<MatchingAlgorithmType, MatchingStrategy> strategies;

    // ✅ Spring injecte automatiquement tous les beans MatchingStrategy ici
    public MatchingContextService(List<MatchingStrategy> list) {
        Map<MatchingAlgorithmType, MatchingStrategy> map = new EnumMap<>(MatchingAlgorithmType.class);
        for (MatchingStrategy s : list) {
            map.put(s.algorithmType(), s);
        }
        this.strategies = Map.copyOf(map);
    }

    public MatchingRunResult run(MatchingRunRequest request) {
        MatchingStrategy strategy = strategies.get(request.algorithm());
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown algorithm: " + request.algorithm());
        }
        return strategy.execute(request);
    }
}
