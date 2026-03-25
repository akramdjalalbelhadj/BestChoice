package fr.amu.bestchoice.service.implementation.algorithmes;

import fr.amu.bestchoice.model.entity.MatchingCampaign;
import fr.amu.bestchoice.model.entity.Student;
import fr.amu.bestchoice.repository.MatchingCampaignRepository;
import fr.amu.bestchoice.web.dto.matching.MatchingRunResult;
import fr.amu.bestchoice.web.exception.BusinessException;
import fr.amu.bestchoice.web.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MatchingContextService {

    private final MatchingCampaignRepository campaignRepository;
    private final List<MatchingStrategy> strategies;

    @Transactional
    public MatchingRunResult run(Long campaignId) {
        MatchingCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NotFoundException("Campagne introuvable : " + campaignId));

        if (campaign.getAlgorithmType() == MatchingAlgorithmType.STABLE) {
            validateStableMatchingReadiness(campaign);
        }

        MatchingStrategy strategy = strategies.stream()
                .filter(s -> s.getAlgorithmType() == campaign.getAlgorithmType())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Algorithme non supporté"));

        return strategy.execute(campaign);
    }

    private void validateStableMatchingReadiness(MatchingCampaign campaign) {
        Set<Student> students = campaign.getStudents();

        long studentsWithPreferences = students.stream()
                .filter(s -> s.getPreferences().stream()
                        .anyMatch(p -> p.getMatchingCampaign().getId().equals(campaign.getId())))
                .count();

        if (studentsWithPreferences < students.size()) {
            long missing = students.size() - studentsWithPreferences;
            throw new BusinessException(
                    String.format("Calcul impossible : %d étudiant(s) n'ont pas encore saisi leurs vœux pour cette campagne.", missing)
            );
        }
    }
}