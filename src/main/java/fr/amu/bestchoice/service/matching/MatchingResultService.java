package fr.amu.bestchoice.service.matching;

import fr.amu.bestchoice.model.entity.MatchingResult;
import fr.amu.bestchoice.repository.MatchingResultRepository;
import fr.amu.bestchoice.repository.ProjectRepository;
import fr.amu.bestchoice.repository.StudentRepository;
import fr.amu.bestchoice.web.dto.matching.MatchingResultResponse;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.MatchingResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de gestion des résultats de matching (MatchingResults).
 *
 * Opérations disponibles :
 * - Récupérer les résultats de matching pour un étudiant
 * - Récupérer les résultats de matching pour un projet
 * - Récupérer les résultats d'une session de matching
 * - Récupérer un résultat par ID
 * - Supprimer les résultats d'une session
 *
 * IMPORTANT : Les MatchingResult sont créés par l'algorithme de matching,
 * PAS via ce service. Ce service est en LECTURE SEULE pour l'API REST.
 *
 * L'algorithme de matching créera directement les MatchingResult en base.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingResultService {

    // ==================== DÉPENDANCES ====================

    private final MatchingResultRepository matchingResultRepository;
    private final StudentRepository studentRepository;
    private final ProjectRepository projectRepository;
    private final MatchingResultMapper matchingResultMapper;

    // ==================== READ ====================

    /**
     * Récupère tous les résultats de matching pour un étudiant.
     *
     * Retourne les scores de compatibilité de cet étudiant avec tous les projets
     * analysés lors de la dernière session de matching.
     *
     * @param studentId L'ID de l'étudiant
     * @return Liste de MatchingResultResponse triée par score décroissant
     * @throws NotFoundException Si l'étudiant n'existe pas
     */
    public List<MatchingResultResponse> findByStudentId(Long studentId) {

        log.debug("Recherche des résultats de matching pour l'étudiant : studentId={}", studentId);

        // Vérifier que l'étudiant existe
        if (!studentRepository.existsById(studentId)) {
            log.error("Étudiant introuvable : studentId={}", studentId);
            throw new NotFoundException("Étudiant introuvable avec l'ID : " + studentId);
        }

        // Récupérer les résultats triés par score global décroissant (meilleur match en premier)
        List<MatchingResult> results = matchingResultRepository.findByStudentIdOrderByGlobalScoreDesc(studentId);

        log.info("Résultats de matching trouvés pour l'étudiant {} : {} résultat(s)", studentId, results.size());

        return matchingResultMapper.toResponseList(results);
    }

    /**
     * Récupère tous les résultats de matching pour un projet.
     *
     * Retourne les scores de compatibilité de tous les étudiants avec ce projet.
     *
     * @param projectId L'ID du projet
     * @return Liste de MatchingResultResponse triée par score décroissant
     * @throws NotFoundException Si le projet n'existe pas
     */
    public List<MatchingResultResponse> findByProjectId(Long projectId) {

        log.debug("Recherche des résultats de matching pour le projet : projectId={}", projectId);

        // Vérifier que le projet existe
        if (!projectRepository.existsById(projectId)) {
            log.error("Projet introuvable : projectId={}", projectId);
            throw new NotFoundException("Projet introuvable avec l'ID : " + projectId);
        }

        // Récupérer les résultats triés par score global décroissant
        List<MatchingResult> results = matchingResultRepository.findByProjectIdOrderByGlobalScoreDesc(projectId);

        log.info("Résultats de matching trouvés pour le projet {} : {} résultat(s)", projectId, results.size());

        return matchingResultMapper.toResponseList(results);
    }

    /**
     * Récupère tous les résultats d'une session de matching.
     *
     * Une session de matching est identifiée par un sessionId unique (UUID).
     * Tous les résultats calculés en même temps partagent le même sessionId.
     *
     * @param sessionId L'ID de la session de matching
     * @return Liste de MatchingResultResponse
     */
    public List<MatchingResultResponse> findBySessionId(String sessionId) {

        log.debug("Recherche des résultats de matching pour la session : sessionId={}", sessionId);

        List<MatchingResult> results = matchingResultRepository.findBySessionId(sessionId);

        log.info("Résultats de matching trouvés pour la session {} : {} résultat(s)", sessionId, results.size());

        return matchingResultMapper.toResponseList(results);
    }

    /**
     * Récupère un résultat de matching par son ID.
     *
     * @param id L'ID du résultat
     * @return MatchingResultResponse
     * @throws NotFoundException Si le résultat n'existe pas
     */
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
     * Récupère le top N des meilleurs projets pour un étudiant.
     *
     * Utile pour afficher les recommandations à l'étudiant.
     *
     * @param studentId L'ID de l'étudiant
     * @param n Le nombre de résultats à retourner (par défaut 5)
     * @return Liste de MatchingResultResponse (top N)
     * @throws NotFoundException Si l'étudiant n'existe pas
     */
    public List<MatchingResultResponse> findTopProjectsForStudent(Long studentId, int n) {

        log.debug("Recherche du top {} des meilleurs projets pour l'étudiant : studentId={}", n, studentId);

        // Vérifier que l'étudiant existe
        if (!studentRepository.existsById(studentId)) {
            log.error("Étudiant introuvable : studentId={}", studentId);
            throw new NotFoundException("Étudiant introuvable avec l'ID : " + studentId);
        }

        // Récupérer les top N résultats
        List<MatchingResult> results = matchingResultRepository.findTopNByStudentIdOrderByGlobalScoreDesc(studentId, n);

        log.info("Top {} projets trouvés pour l'étudiant {} : {} résultat(s)", n, studentId, results.size());

        return matchingResultMapper.toResponseList(results);
    }

    /**
     * Récupère le top N des meilleurs étudiants pour un projet.
     *
     * Utile pour l'enseignant pour voir quels étudiants sont les plus compatibles avec son projet.
     *
     * @param projectId L'ID du projet
     * @param n Le nombre de résultats à retourner (par défaut 10)
     * @return Liste de MatchingResultResponse (top N)
     * @throws NotFoundException Si le projet n'existe pas
     */
    public List<MatchingResultResponse> findTopStudentsForProject(Long projectId, int n) {

        log.debug("Recherche du top {} des meilleurs étudiants pour le projet : projectId={}", n, projectId);

        // Vérifier que le projet existe
        if (!projectRepository.existsById(projectId)) {
            log.error("Projet introuvable : projectId={}", projectId);
            throw new NotFoundException("Projet introuvable avec l'ID : " + projectId);
        }

        // Récupérer les top N résultats
        List<MatchingResult> results = matchingResultRepository.findTopNByProjectIdOrderByGlobalScoreDesc(projectId, n);

        log.info("Top {} étudiants trouvés pour le projet {} : {} résultat(s)", n, projectId, results.size());

        return matchingResultMapper.toResponseList(results);
    }

    // ==================== DELETE ====================

    /**
     * Supprime tous les résultats d'une session de matching.
     *
     * Utilisé pour nettoyer les anciens résultats avant de lancer un nouveau matching.
     *
     * ATTENTION : Cette opération est irréversible !
     *
     * @param sessionId L'ID de la session à supprimer
     */
    @Transactional
    public void deleteBySessionId(String sessionId) {

        log.info("Début suppression des résultats de matching : sessionId={}", sessionId);

        long deletedCount = matchingResultRepository.deleteBySessionId(sessionId);

        log.info("Résultats de matching supprimés avec succès : sessionId={}, count={}", sessionId, deletedCount);
    }

    /**
     * Supprime tous les résultats de matching.
     *
     * ATTENTION : Cette opération supprime TOUS les résultats de TOUTES les sessions !
     * À utiliser avec précaution (uniquement en DEV ou pour réinitialiser).
     */
    @Transactional
    public void deleteAll() {

        log.warn("⚠️ Début suppression de TOUS les résultats de matching");

        long countBefore = matchingResultRepository.count();
        matchingResultRepository.deleteAll();

        log.warn("⚠️ TOUS les résultats de matching ont été supprimés : count={}", countBefore);
    }

    // ==================== STATISTIQUES ====================

    /**
     * Compte le nombre de résultats pour une session.
     *
     * @param sessionId L'ID de la session
     * @return Le nombre de résultats
     */
    public long countBySessionId(String sessionId) {

        log.debug("Comptage des résultats pour la session : sessionId={}", sessionId);

        long count = matchingResultRepository.countBySessionId(sessionId);

        log.debug("Nombre de résultats pour la session {} : {}", sessionId, count);

        return count;
    }
}