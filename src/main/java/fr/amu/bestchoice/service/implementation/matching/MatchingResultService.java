package fr.amu.bestchoice.service.implementation.matching;

import fr.amu.bestchoice.model.entity.MatchingResult;
import fr.amu.bestchoice.model.entity.Project;
import fr.amu.bestchoice.model.entity.Student;
import fr.amu.bestchoice.repository.MatchingResultRepository;
import fr.amu.bestchoice.repository.ProjectRepository;
import fr.amu.bestchoice.repository.StudentRepository;
import fr.amu.bestchoice.service.interfaces.IMatchingResultService;
import fr.amu.bestchoice.web.dto.matching.MatchingResultResponse;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.MatchingResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de gestion des résultats de matching (MatchingResults).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingResultService implements IMatchingResultService {

    // ==================== DÉPENDANCES ====================

    private final MatchingResultRepository matchingResultRepository;
    private final StudentRepository studentRepository;
    private final ProjectRepository projectRepository;
    private final MatchingResultMapper matchingResultMapper;

    // ==================== READ ====================

    public List<MatchingResultResponse> findByStudentId(Long studentId) {

        log.debug("Recherche des résultats de matching pour l'étudiant : studentId={}", studentId);

        if (!studentRepository.existsById(studentId)) {
            log.error("Étudiant introuvable : studentId={}", studentId);
            throw new NotFoundException("Étudiant introuvable avec l'ID : " + studentId);
        }

        List<MatchingResult> results = matchingResultRepository.findByStudentIdOrderByGlobalScoreDesc(studentId);

        log.info("Résultats de matching trouvés pour l'étudiant {} : {} résultat(s)", studentId, results.size());

        return matchingResultMapper.toResponseList(results);
    }

    public List<MatchingResultResponse> findByProjectId(Long projectId) {

        log.debug("Recherche des résultats de matching pour le projet : projectId={}", projectId);

        if (!projectRepository.existsById(projectId)) {
            log.error("Projet introuvable : projectId={}", projectId);
            throw new NotFoundException("Projet introuvable avec l'ID : " + projectId);
        }

        List<MatchingResult> results = matchingResultRepository.findByProjectIdOrderByGlobalScoreDesc(projectId);

        log.info("Résultats de matching trouvés pour le projet {} : {} résultat(s)", projectId, results.size());

        return matchingResultMapper.toResponseList(results);
    }

    /**
     * ✅ CORRECTION 1 : Utiliser findBySessionIdOrderByStudentAndScore au lieu de findBySessionId
     */
    public List<MatchingResultResponse> findBySessionId(String sessionId) {

        log.debug("Recherche des résultats de matching pour la session : sessionId={}", sessionId);

        // ✅ CORRIGÉ : Utiliser la bonne méthode du repository
        List<MatchingResult> results = matchingResultRepository.findBySessionIdOrderByStudentAndScore(sessionId);

        log.info("Résultats de matching trouvés pour la session {} : {} résultat(s)", sessionId, results.size());

        return matchingResultMapper.toResponseList(results);
    }

    public MatchingResultResponse findById(Long id) {

        log.debug("Recherche résultat de matching par ID : id={}", id);

        MatchingResult result = matchingResultRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Résultat de matching introuvable : id={}", id);
                    return new NotFoundException("Résultat de matching introuvable avec l'ID : " + id);
                });

        log.debug("Résultat de matching trouvé : studentId={}, projectId={}, globalScore={}",
                result.getStudent().getId(), result.getProject().getId(), result.getGlobalScore());

        return matchingResultMapper.toResponse(result);
    }

    /**
     * ✅ CORRECTION 2 : Utiliser Pageable au lieu de int
     */
    public List<MatchingResultResponse> findTopProjectsForStudent(Long studentId, int n) {

        log.debug("Recherche du top {} des meilleurs projets pour l'étudiant : studentId={}", n, studentId);

        if (!studentRepository.existsById(studentId)) {
            log.error("Étudiant introuvable : studentId={}", studentId);
            throw new NotFoundException("Étudiant introuvable avec l'ID : " + studentId);
        }

        // ✅ CORRIGÉ : Utiliser Pageable
        Pageable pageable = PageRequest.of(0, n);
        List<MatchingResult> results = matchingResultRepository.findTopNByStudentId(studentId, pageable);

        log.info("Top {} projets trouvés pour l'étudiant {} : {} résultat(s)", n, studentId, results.size());

        return matchingResultMapper.toResponseList(results);
    }

    /**
     * ✅ CORRECTION 3 : Créer une nouvelle méthode dans le repository OU utiliser une solution alternative
     */
    public List<MatchingResultResponse> findTopStudentsForProject(Long projectId, int n) {

        log.debug("Recherche du top {} des meilleurs étudiants pour le projet : projectId={}", n, projectId);

        if (!projectRepository.existsById(projectId)) {
            log.error("Projet introuvable : projectId={}", projectId);
            throw new NotFoundException("Projet introuvable avec l'ID : " + projectId);
        }

        // ✅ SOLUTION ALTERNATIVE : Récupérer tous les résultats et limiter en Java
        List<MatchingResult> allResults = matchingResultRepository.findByProjectIdOrderByGlobalScoreDesc(projectId);

        // Limiter au top N
        List<MatchingResult> topResults = allResults.stream()
                .limit(n)
                .toList();

        log.info("Top {} étudiants trouvés pour le projet {} : {} résultat(s)", n, projectId, topResults.size());

        return matchingResultMapper.toResponseList(topResults);
    }

    // ==================== DELETE ====================

    /**
     * ✅ CORRECTION 4 : deleteBySessionId retourne void, pas long
     */
    @Transactional
    public void deleteBySessionId(String sessionId) {

        log.info("Début suppression des résultats de matching : sessionId={}", sessionId);

        // ✅ SOLUTION 1 : Compter AVANT de supprimer
        long countBefore = matchingResultRepository.countBySessionId(sessionId);

        // Supprimer (retourne void)
        matchingResultRepository.deleteBySessionId(sessionId);

        log.info("Résultats de matching supprimés avec succès : sessionId={}, count={}", sessionId, countBefore);
    }

    @Transactional
    public void deleteAll() {

        log.warn("⚠️ Début suppression de TOUS les résultats de matching");

        long countBefore = matchingResultRepository.count();
        matchingResultRepository.deleteAll();

        log.warn("⚠️ TOUS les résultats de matching ont été supprimés : count={}", countBefore);
    }

    // ==================== STATISTIQUES ====================

    public long countBySessionId(String sessionId) {

        log.debug("Comptage des résultats pour la session : sessionId={}", sessionId);

        long count = matchingResultRepository.countBySessionId(sessionId);

        log.debug("Nombre de résultats pour la session {} : {}", sessionId, count);

        return count;
    }
}