package fr.amu.bestchoice.service.implementation.algorithmes;

import fr.amu.bestchoice.model.entity.MatchingCampaign;
import fr.amu.bestchoice.web.dto.matching.MatchingRunResult;

public interface MatchingStrategy {

    MatchingAlgorithmType getAlgorithmType();
    MatchingRunResult execute(MatchingCampaign campaign);

}