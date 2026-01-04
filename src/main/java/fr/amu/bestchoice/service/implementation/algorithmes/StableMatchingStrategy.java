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
 * Stable Matching (Gale-Shapley) avec capacités:
 * - préférences étudiants: tri des projets par score Weighted
 * - préférences projets: tri des étudiants par score Weighted
 *
 * Simple et suffisant pour un MVP.
 */
@Service
@RequiredArgsConstructor
public class StableMatchingStrategy implements MatchingStrategy {

    private final StudentRepository studentRepository;
    private final ProjectRepository projectRepository;
    private final MatchingResultRepository matchingResultRepository;

    @Override
    public MatchingAlgorithmType algorithmType() {
        return MatchingAlgorithmType.STABLE;
    }

    @Override
    @Transactional
    public MatchingRunResult execute(MatchingRunRequest request) {
        Instant start = Instant.now();
        List<String> warnings = new ArrayList<>();

        String sessionId = "SESSION-" + UUID.randomUUID().toString().substring(0, 8);
        BigDecimal threshold = request.threshold() != null ? request.threshold() : new BigDecimal("0.50");

        // weights (defaults) + normalisation
        BigDecimal wSkills = weightOrDefault(request.weights(), "skills", new BigDecimal("0.50"));
        BigDecimal wInterests = weightOrDefault(request.weights(), "interests", new BigDecimal("0.30"));
        BigDecimal wWorkType = weightOrDefault(request.weights(), "workType", new BigDecimal("0.20"));
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

        List<Student> students = loadStudentsByScope(request);
        List<Project> projects = projectRepository.findAll();

        int projectsConsidered = projects.size();

        if (request.recompute()) {
            if (request.scope() == MatchingScope.ONE_STUDENT) {
                matchingResultRepository.deleteByStudentId(request.studentId());
            } else {
                // simple MVP : on wipe tout avant stable run global
                // (si tu veux version + fine par session, dis-moi)
                matchingResultRepository.deleteAll();
            }
        }

        // Pré-calcul des scores student<->project (pour préférences)
        Map<Long, List<ProjectPreference>> studentPrefs = buildStudentPreferences(students, projects, wSkills, wInterests, wWorkType);
        Map<Long, Map<Long, BigDecimal>> scoreMatrix = buildScoreMatrix(studentPrefs);

        // Assignations courantes par projet (listes d'étudiants)
        Map<Long, List<Student>> assignedByProject = new HashMap<>();
        for (Project p : projects) {
            assignedByProject.put(p.getId(), new ArrayList<>());
        }

        // Prochain index de proposition pour chaque étudiant
        Map<Long, Integer> nextProposalIndex = new HashMap<>();
        for (Student s : students) {
            nextProposalIndex.put(s.getId(), 0);
        }

        // Etudiants libres
        ArrayDeque<Student> freeStudents = new ArrayDeque<>(students);

        while (!freeStudents.isEmpty()) {
            Student s = freeStudents.poll();
            List<ProjectPreference> prefs = studentPrefs.get(s.getId());
            if (prefs == null || prefs.isEmpty()) {
                continue; // pas de projets
            }

            int idx = nextProposalIndex.getOrDefault(s.getId(), 0);
            if (idx >= prefs.size()) {
                continue; // a proposé à tout le monde
            }

            // s propose à son prochain projet préféré
            Project p = prefs.get(idx).project();
            nextProposalIndex.put(s.getId(), idx + 1);

            List<Student> assigned = assignedByProject.get(p.getId());
            int capacity = Math.max(1, p.getMaxStudents()); // sécurité

            if (assigned.size() < capacity) {
                // place dispo
                assigned.add(s);
            } else {
                // projet plein -> voir si s est meilleur que le pire actuel
                Student worst = findWorstStudentForProject(p.getId(), assigned, scoreMatrix);
                if (worst == null) {
                    // fallback: rejette
                    freeStudents.add(s);
                    continue;
                }

                BigDecimal scoreS = scoreMatrix.get(s.getId()).get(p.getId());
                BigDecimal scoreWorst = scoreMatrix.get(worst.getId()).get(p.getId());

                if (scoreS.compareTo(scoreWorst) > 0) {
                    // remplace
                    assigned.remove(worst);
                    assigned.add(s);
                    freeStudents.add(worst); // l'autre redevient libre
                } else {
                    // rejet
                    freeStudents.add(s);
                }
            }
        }

        // Construire MatchingResult pour les affectations finales
        List<MatchingResult> toSave = new ArrayList<>();
        int resultsComputed = 0;

        for (Project p : projects) {
            List<Student> assigned = assignedByProject.get(p.getId());
            if (assigned == null) continue;

            // Tri des assignés par score décroissant (optionnel)
            assigned.sort((a, b) -> scoreMatrix.get(b.getId()).get(p.getId())
                    .compareTo(scoreMatrix.get(a.getId()).get(p.getId())));

            for (int i = 0; i < assigned.size(); i++) {
                Student s = assigned.get(i);
                BigDecimal global = scoreMatrix.get(s.getId()).get(p.getId());

                // on recalcule les sous-scores (pour les champs details)
                BigDecimal skillsScore = computeSkillsScore(s, p);
                BigDecimal interestsScore = computeInterestsScore(s, p);
                BigDecimal workTypeScore = computeWorkTypeScore(s, p);

                MatchingResult mr = MatchingResult.builder()
                        .sessionId(sessionId)
                        .student(s)
                        .project(p)
                        .globalScore(global)

                        .skillsScore(skillsScore)
                        .interestsScore(interestsScore)
                        .workTypeScore(workTypeScore)

                        .skillsWeight(wSkills)
                        .interestsWeight(wInterests)
                        .workTypeWeight(wWorkType)

                        .thresholdUsed(threshold)
                        .aboveThreshold(global.compareTo(threshold) >= 0)

                        .algorithmUsed(MatchingAlgorithmType.STABLE.name())
                        .recommendationRank(i + 1) // rang dans le projet (pas rang global étudiant)
                        .build();

                toSave.add(mr);
                resultsComputed++;
            }
        }

        int resultsSaved = 0;
        if (request.persist()) {
            matchingResultRepository.saveAll(toSave);
            resultsSaved = toSave.size();
        }

        Instant end = Instant.now();
        return new MatchingRunResult(
                sessionId,
                MatchingAlgorithmType.STABLE,
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

    // ========================= Preferences build =========================

    private record ProjectPreference(Project project, BigDecimal score) {}

    private Map<Long, List<ProjectPreference>> buildStudentPreferences(
            List<Student> students,
            List<Project> projects,
            BigDecimal wSkills,
            BigDecimal wInterests,
            BigDecimal wWorkType
    ) {
        Map<Long, List<ProjectPreference>> map = new HashMap<>();

        for (Student s : students) {
            List<ProjectPreference> prefs = new ArrayList<>(projects.size());
            for (Project p : projects) {
                BigDecimal global = computeGlobalScore(s, p, wSkills, wInterests, wWorkType);
                prefs.add(new ProjectPreference(p, global));
            }
            // Tri par score décroissant
            prefs.sort((a, b) -> b.score().compareTo(a.score()));
            map.put(s.getId(), prefs);
        }
        return map;
    }

    private Map<Long, Map<Long, BigDecimal>> buildScoreMatrix(Map<Long, List<ProjectPreference>> studentPrefs) {
        Map<Long, Map<Long, BigDecimal>> matrix = new HashMap<>();
        for (var entry : studentPrefs.entrySet()) {
            Long studentId = entry.getKey();
            Map<Long, BigDecimal> scores = new HashMap<>();
            for (ProjectPreference pp : entry.getValue()) {
                scores.put(pp.project().getId(), pp.score());
            }
            matrix.put(studentId, scores);
        }
        return matrix;
    }

    private Student findWorstStudentForProject(Long projectId, List<Student> assigned, Map<Long, Map<Long, BigDecimal>> matrix) {
        Student worst = null;
        BigDecimal worstScore = null;

        for (Student s : assigned) {
            BigDecimal sc = matrix.get(s.getId()).get(projectId);
            if (worst == null || sc.compareTo(worstScore) < 0) {
                worst = s;
                worstScore = sc;
            }
        }
        return worst;
    }

    // ========================= Load students =========================

    private List<Student> loadStudentsByScope(MatchingRunRequest request) {
        if (request.scope() == MatchingScope.ONE_STUDENT) {
            if (request.studentId() == null) {
                throw new IllegalArgumentException("studentId is required when scope=ONE_STUDENT");
            }
            Student s = studentRepository.findById(request.studentId())
                    .orElseThrow(() -> new IllegalArgumentException("Student not found: " + request.studentId()));
            return List.of(s);
        }
        return studentRepository.findAll();
    }

    private BigDecimal weightOrDefault(Map<String, BigDecimal> weights, String key, BigDecimal def) {
        if (weights == null) return def;
        BigDecimal v = weights.get(key);
        return v != null ? v : def;
    }

    // ========================= Scoring (same logic as Weighted) =========================

    private BigDecimal computeGlobalScore(Student student, Project project,
                                          BigDecimal wSkills, BigDecimal wInterests, BigDecimal wWorkType) {
        BigDecimal skillsScore = computeSkillsScore(student, project);
        BigDecimal interestsScore = computeInterestsScore(student, project);
        BigDecimal workTypeScore = computeWorkTypeScore(student, project);

        BigDecimal global =
                skillsScore.multiply(wSkills)
                        .add(interestsScore.multiply(wInterests))
                        .add(workTypeScore.multiply(wWorkType))
                        .setScale(6, RoundingMode.HALF_UP);

        return clamp01(global);
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
