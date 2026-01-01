package fr.amu.bestchoice.web.controller.matching;

import fr.amu.bestchoice.service.implementation.matching.MatchingResultService;
import fr.amu.bestchoice.web.dto.matching.MatchingResultResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des résultats de matching (MatchingResults).
 *
 * Endpoints disponibles :
 * - GET    /api/matching/student/{studentId}         : Résultats pour un étudiant
 * - GET    /api/matching/student/{studentId}/top/{n} : Top N projets pour un étudiant
 * - GET    /api/matching/project/{projectId}         : Résultats pour un projet
 * - GET    /api/matching/project/{projectId}/top/{n} : Top N étudiants pour un projet
 * - GET    /api/matching/session/{sessionId}         : Résultats d'une session
 * - GET    /api/matching/{id}                        : Résultat par ID
 * - DELETE /api/matching/session/{sessionId}         : Supprimer les résultats d'une session
 *
 * IMPORTANT : Ce contrôleur est en LECTURE SEULE.
 * Les résultats de matching sont créés par l'algorithme de matching, pas via l'API REST.
 *
 * Tous les endpoints nécessitent le rôle ADMIN.
 */
@Slf4j
@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Matching", description = "Résultats de l'algorithme de matching (scores de compatibilité)")
public class MatchingResultController {

    private final MatchingResultService matchingResultService;

    // ==================== READ - STUDENT ====================

    /**
     * Récupère tous les résultats de matching pour un étudiant.
     *
     * GET /api/matching/student/{studentId}
     *
     * Retourne les scores de compatibilité de cet étudiant avec tous les projets
     * analysés lors de la dernière session de matching.
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<MatchingResultResponse>> getMatchingResultsByStudent(@PathVariable Long studentId) {

        log.debug("GET /api/matching/student/{} - Récupération des résultats de matching", studentId);

        List<MatchingResultResponse> results = matchingResultService.findByStudentId(studentId);

        log.info("GET /api/matching/student/{} - {} résultat(s) retourné(s)", studentId, results.size());

        return ResponseEntity.ok(results);
    }

    /**
     * Récupère le top N des meilleurs projets pour un étudiant.
     *
     * GET /api/matching/student/{studentId}/top/{n}
     *
     * Utile pour afficher les recommandations à l'étudiant.
     */
    @GetMapping("/student/{studentId}/top/{n}")
    public ResponseEntity<List<MatchingResultResponse>> getTopProjectsForStudent(
            @PathVariable Long studentId,
            @PathVariable int n) {

        log.debug("GET /api/matching/student/{}/top/{} - Récupération du top {} projets", studentId, n, n);

        List<MatchingResultResponse> results = matchingResultService.findTopProjectsForStudent(studentId, n);

        log.info("GET /api/matching/student/{}/top/{} - {} résultat(s) retourné(s)",
                studentId, n, results.size());

        return ResponseEntity.ok(results);
    }

    // ==================== READ - PROJECT ====================

    /**
     * Récupère tous les résultats de matching pour un projet.
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<MatchingResultResponse>> getMatchingResultsByProject(@PathVariable Long projectId) {

        log.debug("GET /api/matching/project/{} - Récupération des résultats de matching", projectId);

        List<MatchingResultResponse> results = matchingResultService.findByProjectId(projectId);

        log.info("GET /api/matching/project/{} - {} résultat(s) retourné(s)", projectId, results.size());

        return ResponseEntity.ok(results);
    }

    /**
     * Récupère le top N des meilleurs étudiants pour un projet.
     *
     * Utile pour l'enseignant pour voir quels étudiants sont les plus compatibles avec son projet.
     */
    @GetMapping("/project/{projectId}/top/{n}")
    public ResponseEntity<List<MatchingResultResponse>> getTopStudentsForProject(
            @PathVariable Long projectId,
            @PathVariable int n) {

        log.debug("GET /api/matching/project/{}/top/{} - Récupération du top {} étudiants", projectId, n, n);

        List<MatchingResultResponse> results = matchingResultService.findTopStudentsForProject(projectId, n);

        log.info("GET /api/matching/project/{}/top/{} - {} résultat(s) retourné(s)",
                projectId, n, results.size());

        return ResponseEntity.ok(results);
    }

    // ==================== READ - SESSION ====================

    /**
     * Récupère tous les résultats d'une session de matching.
     *
     * GET /api/matching/session/{sessionId}
     *
     * Une session de matching est identifiée par un sessionId unique (UUID).
     * Tous les résultats calculés en même temps partagent le même sessionId.
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<MatchingResultResponse>> getMatchingResultsBySession(@PathVariable String sessionId) {

        log.debug("GET /api/matching/session/{} - Récupération des résultats de la session", sessionId);

        List<MatchingResultResponse> results = matchingResultService.findBySessionId(sessionId);

        log.info("GET /api/matching/session/{} - {} résultat(s) retourné(s)", sessionId, results.size());

        return ResponseEntity.ok(results);
    }

    /**
     * Récupère un résultat de matching par son ID.
     *
     * GET /api/matching/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<MatchingResultResponse> getMatchingResultById(@PathVariable Long id) {

        log.debug("GET /api/matching/{} - Récupération du résultat de matching", id);

        MatchingResultResponse result = matchingResultService.findById(id);

        log.info("GET /api/matching/{} - Résultat retourné : studentId={}, projectId={}, globalScore={}",
                id, result.studentId(), result.projectId(), result.globalScore());

        return ResponseEntity.ok(result);
    }

    // ==================== DELETE ====================

    /**
     * Supprime tous les résultats d'une session de matching.
     *
     * DELETE /api/matching/session/{sessionId}
     *
     * Utilisé pour nettoyer les anciens résultats avant de lancer un nouveau matching.
     *
     * ATTENTION : Cette opération est irréversible !
     */
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Void> deleteMatchingResultsBySession(@PathVariable String sessionId) {

        log.warn("DELETE /api/matching/session/{} - ⚠️ Suppression des résultats de la session", sessionId);

        matchingResultService.deleteBySessionId(sessionId);

        log.info("DELETE /api/matching/session/{} - Résultats supprimés avec succès", sessionId);

        return ResponseEntity.noContent().build();
    }
}