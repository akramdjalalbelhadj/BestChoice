package fr.amu.bestchoice.web.controller.matching;

import fr.amu.bestchoice.service.interfaces.IMatchingResultService;
import fr.amu.bestchoice.web.dto.matching.MatchingResultResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
@Tag(name = "Matching", description = "Gestion des scores et résultats par campagne")
public class MatchingResultController {

    private final IMatchingResultService matchingResultService;

    // ==================== READ - CAMPAGNE ====================

    /**
     * Récupère TOUS les résultats d'une campagne précise.
     */
    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<List<MatchingResultResponse>> getResultsByCampaign(@PathVariable Long campaignId) {
        return ResponseEntity.ok(matchingResultService.findByCampaignId(campaignId));
    }

    /**
     * Supprime tous les résultats d'une campagne.
     */
    @DeleteMapping("/campaign/{campaignId}")
    public ResponseEntity<Void> deleteByCampaign(@PathVariable Long campaignId) {
        matchingResultService.deleteByCampaignId(campaignId);
        return ResponseEntity.noContent().build();
    }

    // ==================== READ - STUDENT ====================

    /**
     * Tous les résultats de matching d'un étudiant (toutes campagnes).
     * Utilisé par le tableau de bord étudiant.
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<MatchingResultResponse>> getAllResultsForStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(matchingResultService.findByStudentId(studentId));
    }

    /**
     * Résultats d'un étudiant spécifique au sein d'une campagne.
     */
    @GetMapping("/campaign/{campaignId}/student/{studentId}")
    public ResponseEntity<List<MatchingResultResponse>> getResultsByStudent(
            @PathVariable Long campaignId,
            @PathVariable Long studentId) {
        return ResponseEntity.ok(matchingResultService.findByCampaignAndStudent(campaignId, studentId));
    }

    /**
     * Top N projets pour un étudiant dans une campagne.
     */
    @GetMapping("/campaign/{campaignId}/student/{studentId}/top/{n}")
    public ResponseEntity<List<MatchingResultResponse>> getTopForStudent(
            @PathVariable Long campaignId,
            @PathVariable Long studentId,
            @PathVariable int n) {
        return ResponseEntity.ok(matchingResultService.findTopResultsForStudent(campaignId, studentId, n));
    }

    // ==================== READ - ITEMS (Projet ou Matière) ====================

    /**
     * Résultats pour un projet spécifique dans une campagne.
     */
    @GetMapping("/campaign/{campaignId}/project/{projectId}")
    public ResponseEntity<List<MatchingResultResponse>> getResultsByProject(
            @PathVariable Long campaignId,
            @PathVariable Long projectId) {
        return ResponseEntity.ok(matchingResultService.findByCampaignAndProject(campaignId, projectId));
    }

    /**
     * Résultats pour une matière spécifique dans une campagne.
     */
    @GetMapping("/campaign/{campaignId}/subject/{subjectId}")
    public ResponseEntity<List<MatchingResultResponse>> getResultsBySubject(
            @PathVariable Long campaignId,
            @PathVariable Long subjectId) {
        return ResponseEntity.ok(matchingResultService.findByCampaignAndSubject(campaignId, subjectId));
    }

    // ==================== READ - UNIQUE ====================

    @GetMapping("/{id}")
    public ResponseEntity<MatchingResultResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(matchingResultService.findById(id));
    }
}