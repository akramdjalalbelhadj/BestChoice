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
 * Stable Matching (Gale-Shapley) avec capacités:
 * - préférences étudiants: tri des projets par selection manuelle de la part de l'étudiant
 * - préférences projets: tri des étudiants par score Weighted
 *
 */
@Service
@RequiredArgsConstructor
public class StableMatchingStrategy implements MatchingStrategy {

    private final MatchingResultRepository resultRepository;
    private final MatchingScoringService scoringService;
    private final StudentPreferenceRepository preferenceRepository;

    @Override
    public MatchingAlgorithmType getAlgorithmType() {
        return MatchingAlgorithmType.STABLE;
    }

    @Override
    @Transactional
    public MatchingRunResult execute(MatchingCampaign campaign) {
        Instant start = Instant.now();

        resultRepository.deleteByMatchingCampaignId(campaign.getId());

        Map<Long, Map<Long, BigDecimal>> itemScores = new HashMap<>();
        Map<Long, Deque<Long>> studentChoices = new HashMap<>();

        initializeData(campaign, itemScores, studentChoices);

        // assignments : Map<ItemID, List<StudentID>>
        Map<Long, List<Long>> assignments = new HashMap<>();
        Queue<Long> freeStudents = new LinkedList<>(studentChoices.keySet());

        while (!freeStudents.isEmpty()) {
            Long sId = freeStudents.poll();
            Deque<Long> choices = studentChoices.get(sId);

            if (choices == null || choices.isEmpty()) continue;

            Long itemId = choices.poll();
            List<Long> currentAccepted = assignments.computeIfAbsent(itemId, k -> new ArrayList<>());
            int capacity = getCapacity(itemId, campaign);

            if (currentAccepted.size() < capacity) {
                currentAccepted.add(sId);
            } else {
                // On trouve l'étudiant actuellement accepté ayant le moins bon score weighted
                Long worstStudentId = currentAccepted.stream()
                        .min(Comparator.comparing(id -> itemScores.get(itemId).get(id)))
                        .orElse(null);

                BigDecimal newScore = itemScores.get(itemId).get(sId);
                BigDecimal worstScore = itemScores.get(itemId).get(worstStudentId);

                if (newScore.compareTo(worstScore) > 0) {
                    currentAccepted.remove(worstStudentId);
                    currentAccepted.add(sId);
                    freeStudents.add(worstStudentId);
                } else {
                    freeStudents.add(sId);
                }
            }
        }

        List<MatchingResult> toSave = buildFinalResults(assignments, campaign, itemScores);
        resultRepository.saveAll(toSave);

        // Mettre à jour les statuts : ACCEPTED pour les étudiants assignés
        updatePreferenceStatuses(toSave, campaign);

        return new MatchingRunResult(
                campaign.getId(),
                MatchingAlgorithmType.STABLE,
                campaign.getStudents().size(),
                toSave.size(),
                start,
                Instant.now()
        );
    }

    private void initializeData(MatchingCampaign camp, Map<Long, Map<Long, BigDecimal>> itemScores, Map<Long, Deque<Long>> studentChoices) {
        boolean isProject = camp.getCampaignType() == MatchingCampaignType.PROJECT;

        if (isProject) camp.getProjects().forEach(p -> itemScores.put(p.getId(), new HashMap<>()));
        else camp.getSubjects().forEach(s -> itemScores.put(s.getId(), new HashMap<>()));

        for (Student s : camp.getStudents()) {
            Deque<Long> choices = new ArrayDeque<>();
            s.getPreferences().stream()
                    .filter(p -> p.getMatchingCampaign().getId().equals(camp.getId()))
                    .sorted(Comparator.comparing(StudentPreference::getRank))
                    .forEach(p -> choices.add(isProject ? p.getProject().getId() : p.getSubject().getId()));
            studentChoices.put(s.getId(), choices);

            if (isProject) {
                for (Project p : camp.getProjects()) {
                    itemScores.get(p.getId()).put(s.getId(), scoringService.computeGlobalScore(s, p, null, camp));
                }
            } else {
                for (Subject sub : camp.getSubjects()) {
                    itemScores.get(sub.getId()).put(s.getId(), scoringService.computeGlobalScore(s, null, sub, camp));
                }
            }
        }
    }

    private List<MatchingResult> buildFinalResults(Map<Long, List<Long>> assignments, MatchingCampaign camp, Map<Long, Map<Long, BigDecimal>> itemScores) {
        List<MatchingResult> results = new ArrayList<>();
        boolean isProject = camp.getCampaignType() == MatchingCampaignType.PROJECT;

        assignments.forEach((itemId, studentIds) -> {
            for (Long sId : studentIds) {
                Student student = camp.getStudents().stream().filter(s -> s.getId().equals(sId)).findFirst().orElse(null);
                Project project = isProject ? camp.getProjects().stream().filter(p -> p.getId().equals(itemId)).findFirst().orElse(null) : null;
                Subject subject = !isProject ? camp.getSubjects().stream().filter(s -> s.getId().equals(itemId)).findFirst().orElse(null) : null;

                BigDecimal sScore = scoringService.computeSkillsScore(student, project, subject);
                BigDecimal iScore = scoringService.computeInterestsScore(student, project, subject);

                results.add(MatchingResult.builder()
                        .matchingCampaign(camp)
                        .student(student)
                        .project(project)
                        .subject(subject)
                        .globalScore(itemScores.get(itemId).get(sId))
                        .skillsScore(sScore)
                        .interestsScore(iScore)
                        .skillsWeight(camp.getSkillsWeight())
                        .interestsWeight(camp.getInterestsWeight())
                        .workTypeWeight(camp.getWorkTypeWeight())
                        .algorithmUsed(MatchingAlgorithmType.STABLE)
                        .accepted(true)  // Stable matching : assigné = accepté par définition
                        .build());
            }
        });
        return results;
    }

    /**
     * Met à jour le statut des préférences pour les étudiants acceptés (Stable Matching).
     * ACCEPTED = étudiant définitivement assigné à ce projet/matière.
     */
    private void updatePreferenceStatuses(List<MatchingResult> assignedResults, MatchingCampaign campaign) {
        Long campaignId = campaign.getId();
        assignedResults.forEach(r -> {
            Long studentId = r.getStudent().getId();
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

    private int getCapacity(Long itemId, MatchingCampaign camp) {
        if (camp.getCampaignType() == MatchingCampaignType.PROJECT) {
            return camp.getProjects().stream().filter(p -> p.getId().equals(itemId)).findFirst().map(Project::getMaxStudents).orElse(1);
        }
        return camp.getSubjects().stream().filter(s -> s.getId().equals(itemId)).findFirst().map(Subject::getMaxStudents).orElse(1);
    }
}