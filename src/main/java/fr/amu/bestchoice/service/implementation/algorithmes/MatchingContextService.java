package fr.amu.bestchoice.service.implementation.algorithmes;

import fr.amu.bestchoice.model.entity.MatchingCampaign;
import fr.amu.bestchoice.repository.MatchingCampaignRepository;
import fr.amu.bestchoice.web.dto.matching.MatchingRunRequest;
import fr.amu.bestchoice.web.dto.matching.MatchingRunResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MatchingContextService {

    private final MatchingCampaignRepository campaignRepository;
    private final Map<MatchingAlgorithmType, MatchingStrategy> strategies;

    @Transactional
    public MatchingRunResult run(Long campaignId) {
        MatchingCampaign campaign = campaignRepository.findWithDetailsById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campagne introuvable : " + campaignId));

        MatchingStrategy strategy = strategies.get(campaign.getAlgorithmType());
        if (strategy == null) {
            throw new IllegalArgumentException("Algorithme non supporté : " + campaign.getAlgorithmType());
        }

        return strategy.execute(campaign);
    }
}