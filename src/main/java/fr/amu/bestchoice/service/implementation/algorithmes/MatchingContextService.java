package fr.amu.bestchoice.service.implementation.algorithmes;

import fr.amu.bestchoice.model.entity.MatchingCampaign;
import fr.amu.bestchoice.repository.MatchingCampaignRepository;
import fr.amu.bestchoice.web.dto.matching.MatchingRunResult;
import fr.amu.bestchoice.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchingContextService {

    private final MatchingCampaignRepository campaignRepository;
    private final List<MatchingStrategy> strategies; // Spring injecte toutes les stratégies ici

    @Transactional
    public MatchingRunResult run(Long campaignId) {
        MatchingCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NotFoundException("Campagne introuvable : " + campaignId));

        MatchingStrategy strategy = strategies.stream()
                .filter(s -> s.getAlgorithmType() == campaign.getAlgorithmType())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Algorithme non supporté : " + campaign.getAlgorithmType()));

        return strategy.execute(campaign);
    }
}