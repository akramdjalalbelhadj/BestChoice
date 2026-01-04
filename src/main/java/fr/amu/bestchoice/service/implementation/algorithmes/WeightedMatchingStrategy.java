package fr.amu.bestchoice.service.implementation.algorithmes;

import fr.amu.bestchoice.model.entity.MatchingResult;
import fr.amu.bestchoice.model.entity.Project;
import fr.amu.bestchoice.model.entity.Student;
import fr.amu.bestchoice.repository.MatchingResultRepository;
import fr.amu.bestchoice.repository.ProjectRepository;
import fr.amu.bestchoice.repository.StudentRepository;
import fr.amu.bestchoice.web.dto.matching.MatchingRunRequest;
import fr.amu.bestchoice.web.dto.matching.MatchingRunResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    private final StudentRepository studentRepository;
    private final ProjectRepository projectRepository;
    private final MatchingResultRepository matchingResultRepository;

    @Override
    public MatchingAlgorithmType algorithmType() {
        return MatchingAlgorithmType.WEIGHTED;
    }

    @Override
    @Transactional
    public MatchingRunResult execute(MatchingRunRequest request) {
        Instant start = Instant.now();
        List<String> warnings = new ArrayList<>();

        String sessionId = "SESSION-" + UUID.randomUUID().toString().substring(0, 8);

        BigDecimal threshold = request.threshold() != null ? request.threshold() : new BigDecimal("0.50");

        // weights: defaults
        BigDecimal wSkills = weightOrDefault(request.weights(), "skills", new BigDecimal("0.50"));
        BigDecimal wInterests = weightOrDefault(request.weights(), "interests", new BigDecimal("0.30"));
        BigDecimal wWorkType = weightOrDefault(request.weights(), "workType", new BigDecimal("0.20"));

        // normalisation simple (évite que quelqu’un mette 50/50/50)
        BigDecimal sum = wSkills.add(wInterests).add(wWorkType);
        if (sum.compareTo(BigDecimal.ZERO) <= 0) {
            warnings.add("Weights sum is 0. Using defaults 0.50/0.30/0.20");
            wSkills = new BigDecimal("0.50");
            wInterests = new BigDecimal("0.30");
            wWorkType = new BigDecimal("0.20");
            sum = BigDecimal.ONE;
        }
        wSkills = wSkills.divide(sum, 6, RoundingMode.HALF_UP);
        wInterests = wInterests.divide(sum, 6, RoundingMode.HALF_UP);
        wWorkType = wWorkType.divide(sum, 6, RoundingMode.HALF_UP);

        // Chargement étudiants
        List<Student> students = loadStudentsByScope(request, warnings);

        // Chargement projets
        List<Project> projects = projectRepository.findAll();
        int projectsConsidered = projects.size();

        int resultsComputed = 0;
        int resultsSaved = 0;

        for (Student student : students) {
            if (request.recompute()) {
                // Supprime anciens résultats pour ce student (simple)
                matchingResultRepository.deleteByStudentId(student.getId());
            }

            List<MatchingResult> results = new ArrayList<>(projects.size());

            for (Project project : projects) {
                BigDecimal skillsScore = computeSkillsScore(student, project);
                BigDecimal interestsScore = computeInterestsScore(student, project);
                BigDecimal workTypeScore = computeWorkTypeScore(student, project);

                BigDecimal globalScore =
                        skillsScore.multiply(wSkills)
                                .add(interestsScore.multiply(wInterests))
                                .add(workTypeScore.multiply(wWorkType))
                                .setScale(6, RoundingMode.HALF_UP);

                MatchingResult mr = MatchingResult.builder()
                        .sessionId(sessionId)
                        .student(student)
                        .project(project)
                        .globalScore(globalScore)

                        .skillsScore(skillsScore)
                        .interestsScore(interestsScore)
                        .workTypeScore(workTypeScore)

                        .skillsWeight(wSkills)
                        .interestsWeight(wInterests)
                        .workTypeWeight(wWorkType)

                        .thresholdUsed(threshold)
                        .aboveThreshold(globalScore.compareTo(threshold) >= 0)

                        .algorithmUsed(MatchingAlgorithmType.WEIGHTED.name())
                        .build();

                results.add(mr);
                resultsComputed++;
            }

            // tri + rank
            results.sort((a, b) -> b.getGlobalScore().compareTo(a.getGlobalScore()));
            for (int i = 0; i < results.size(); i++) {
                results.get(i).setRecommendationRank(i + 1);
            }

            if (request.persist()) {
                matchingResultRepository.saveAll(results);
                resultsSaved += results.size();
            }
        }

        Instant end = Instant.now();
        return new MatchingRunResult(
                sessionId,
                MatchingAlgorithmType.WEIGHTED,
                students.size(),
                projectsConsidered,
                resultsComputed,
                resultsSaved,
                request.recompute(),
                start,
                end,
                warnings
        );
    }

    // ========================= Helpers =========================

    private List<Student> loadStudentsByScope(MatchingRunRequest request, List<String> warnings) {
        if (request.scope() == MatchingScope.ONE_STUDENT) {
            if (request.studentId() == null) {
                throw new IllegalArgumentException("studentId is required when scope=ONE_STUDENT");
            }
            Student s = studentRepository.findById(request.studentId())
                    .orElseThrow(() -> new IllegalArgumentException("Student not found: " + request.studentId()));
            return List.of(s);
        }

        // ALL_STUDENTS
        return studentRepository.findAll();
    }

    private BigDecimal weightOrDefault(Map<String, BigDecimal> weights, String key, BigDecimal def) {
        if (weights == null) return def;
        BigDecimal v = weights.get(key);
        return v != null ? v : def;
    }

    private BigDecimal computeSkillsScore(Student student, Project project) {
        if (project.getRequiredSkills() == null || project.getRequiredSkills().isEmpty()) {
            return new BigDecimal("0.50");
        }
        if (student.getSkills() == null || student.getSkills().isEmpty()) {
            return BigDecimal.ZERO;
        }
        long matching = student.getSkills().stream()
                .filter(project.getRequiredSkills()::contains)
                .count();

        BigDecimal score = BigDecimal.valueOf(matching)
                .divide(BigDecimal.valueOf(project.getRequiredSkills().size()), 6, RoundingMode.HALF_UP);

        return clamp01(score);
    }

    private BigDecimal computeInterestsScore(Student student, Project project) {
        if (project.getKeywords() == null || project.getKeywords().isEmpty()) {
            return new BigDecimal("0.50");
        }
        if (student.getInterests() == null || student.getInterests().isEmpty()) {
            return BigDecimal.ZERO;
        }
        long matching = student.getInterests().stream()
                .filter(project.getKeywords()::contains)
                .count();

        BigDecimal score = BigDecimal.valueOf(matching)
                .divide(BigDecimal.valueOf(project.getKeywords().size()), 6, RoundingMode.HALF_UP);

        return clamp01(score);
    }

    private BigDecimal computeWorkTypeScore(Student student, Project project) {
        if (student.getPreferredWorkType() == null || project.getWorkType() == null) {
            return new BigDecimal("0.50");
        }
        return student.getPreferredWorkType() == project.getWorkType()
                ? BigDecimal.ONE
                : new BigDecimal("0.50");
    }

    private BigDecimal clamp01(BigDecimal v) {
        if (v.compareTo(BigDecimal.ZERO) < 0) return BigDecimal.ZERO;
        if (v.compareTo(BigDecimal.ONE) > 0) return BigDecimal.ONE;
        return v;
    }
}
