package fr.amu.bestchoice.service.implementation.algorithmes;

import fr.amu.bestchoice.model.entity.*;
import fr.amu.bestchoice.model.enums.PreferenceStatus;
import fr.amu.bestchoice.repository.MatchingResultRepository;
import fr.amu.bestchoice.repository.StudentPreferenceRepository;
import fr.amu.bestchoice.web.dto.matching.MatchingRunResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Weighted Matching:
 * - calcule score(E,P) = wSkills*skills + wInterests*interests + wWorkType*workType
 * - sort décroissant, stocke recommendationRank
 */
@Service
@RequiredArgsConstructor
public class WeightedMatchingStrategy implements MatchingStrategy {

    private final MatchingResultRepository resultRepository;
    private final MatchingScoringService scoringService;
    private final StudentPreferenceRepository preferenceRepository;

    @Override
    public MatchingAlgorithmType getAlgorithmType() { return MatchingAlgorithmType.WEIGHTED; }

    @Override
    @Transactional
    public MatchingRunResult execute(MatchingCampaign campaign) {
        Instant start = Instant.now();

        resultRepository.deleteByMatchingCampaignId(campaign.getId());

        List<MatchingResult> allResults = new ArrayList<>();
        int itemsCount = 0;

        for (Student student : campaign.getStudents()) {
            List<MatchingResult> studentResults = new ArrayList<>();

            if (campaign.getCampaignType() == MatchingCampaignType.PROJECT) {
                itemsCount = campaign.getProjects().size();
                for (Project p : campaign.getProjects()) {
                    studentResults.add(buildResult(student, p, null, campaign));
                }
            } else {
                itemsCount = campaign.getSubjects().size();
                for (Subject s : campaign.getSubjects()) {
                    studentResults.add(buildResult(student, null, s, campaign));
                }
            }

            studentResults.sort((a, b) -> b.getGlobalScore().compareTo(a.getGlobalScore()));
            for (int i = 0; i < studentResults.size(); i++) {
                studentResults.get(i).setRecommendationRank(i + 1);
            }
            allResults.addAll(studentResults);
        }

        resultRepository.saveAll(allResults);

        // Mettre à jour le statut des préférences : ACCEPTED pour le meilleur match (rank 1)
        updatePreferenceStatuses(allResults, campaign);

        return new MatchingRunResult(
                campaign.getId(),
                MatchingAlgorithmType.WEIGHTED,
                campaign.getStudents().size(),
                itemsCount,
                start,
                Instant.now()
        );
    }

    /**
     * Met à jour le statut des préférences :
     * - ACCEPTED pour le meilleur résultat de matching (rank 1) de chaque étudiant
     * - Les autres préférences restent PENDING
     */
    private void updatePreferenceStatuses(List<MatchingResult> allResults, MatchingCampaign campaign) {
        allResults.stream()
            .filter(r -> r.getRecommendationRank() != null && r.getRecommendationRank() == 1)
            .forEach(r -> {
                Long studentId = r.getStudent().getId();
                Long campaignId = campaign.getId();
                if (r.getProject() != null) {
                    preferenceRepository
                        .findByStudentIdAndProjectIdAndMatchingCampaignId(studentId, r.getProject().getId(), campaignId)
                        .ifPresent(pref -> {
                            pref.setStatus(PreferenceStatus.ACCEPTED);
                            preferenceRepository.save(pref);
                        });
                } else if (r.getSubject() != null) {
                    preferenceRepository
                        .findByStudentIdAndSubjectIdAndMatchingCampaignId(studentId, r.getSubject().getId(), campaignId)
                        .ifPresent(pref -> {
                            pref.setStatus(PreferenceStatus.ACCEPTED);
                            preferenceRepository.save(pref);
                        });
                }
            });
    }

    private MatchingResult buildResult(Student s, Project p, Subject sub, MatchingCampaign camp) {
        BigDecimal skillsScore   = scoringService.computeSkillsScore(s, p, sub);
        BigDecimal interestsScore = scoringService.computeInterestsScore(s, p, sub);
        BigDecimal globalScore   = scoringService.computeGlobalScore(s, p, sub, camp);

        return MatchingResult.builder()
                .matchingCampaign(camp)
                .student(s)
                .project(p)
                .subject(sub)
                .globalScore(globalScore)
                .skillsScore(skillsScore)
                .interestsScore(interestsScore)
                .skillsWeight(camp.getSkillsWeight())
                .interestsWeight(camp.getInterestsWeight())
                .workTypeWeight(camp.getWorkTypeWeight())
                .algorithmUsed(MatchingAlgorithmType.WEIGHTED)
                .build();
    }
}