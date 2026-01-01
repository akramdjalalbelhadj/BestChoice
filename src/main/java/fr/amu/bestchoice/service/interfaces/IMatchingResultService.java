package fr.amu.bestchoice.service.interfaces;

import fr.amu.bestchoice.web.dto.matching.MatchingResultResponse;

import java.util.List;

/**
 * Interface du service de gestion des résultats de matching.
 */
public interface IMatchingResultService {

    /**
     * Récupère tous les résultats de matching d'un étudiant.
     *
     * @param studentId L'ID de l'étudiant
     * @return La liste des résultats de matching de l'étudiant
     */
    List<MatchingResultResponse> findByStudentId(Long studentId);

    /**
     * Récupère tous les résultats de matching pour un projet.
     *
     * @param projectId L'ID du projet
     * @return La liste des résultats de matching pour le projet
     */
    List<MatchingResultResponse> findByProjectId(Long projectId);

    /**
     * Récupère tous les résultats de matching pour une session.
     *
     * @param sessionId L'ID de la session
     * @return La liste des résultats de matching de la session
     */
    List<MatchingResultResponse> findBySessionId(String sessionId);

    /**
     * Récupère un résultat de matching par son ID.
     *
     * @param id L'ID du résultat de matching
     * @return Le résultat de matching
     */
    MatchingResultResponse findById(Long id);

    /**
     * Récupère les N meilleurs projets pour un étudiant.
     *
     * @param studentId L'ID de l'étudiant
     * @param n Le nombre de résultats à retourner
     * @return La liste des N meilleurs résultats
     */
    List<MatchingResultResponse> findTopProjectsForStudent(Long studentId, int n);

    /**
     * Récupère les N meilleurs étudiants pour un projet.
     *
     * @param projectId L'ID du projet
     * @param n Le nombre de résultats à retourner
     * @return La liste des N meilleurs résultats
     */
    List<MatchingResultResponse> findTopStudentsForProject(Long projectId, int n);

    /**
     * Supprime tous les résultats d'une session de matching.
     *
     * @param sessionId L'ID de la session
     */
    void deleteBySessionId(String sessionId);

    /**
     * Supprime tous les résultats de matching.
     */
    void deleteAll();

    /**
     * Compte le nombre de résultats pour une session.
     *
     * @param sessionId L'ID de la session
     * @return Le nombre de résultats
     */
    long countBySessionId(String sessionId);
}
