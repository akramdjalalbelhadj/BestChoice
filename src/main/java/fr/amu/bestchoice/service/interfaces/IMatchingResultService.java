package fr.amu.bestchoice.service.interfaces;

import fr.amu.bestchoice.web.dto.matching.MatchingResultResponse;
import java.util.List;

/**
 * Interface mise à jour pour la gestion par Campagne.
 */
public interface IMatchingResultService {

    List<MatchingResultResponse> findByCampaignId(Long campaignId);

    List<MatchingResultResponse> findByCampaignAndStudent(Long campaignId, Long studentId);

    List<MatchingResultResponse> findTopResultsForStudent(Long campaignId, Long studentId, int n);

    List<MatchingResultResponse> findByCampaignAndProject(Long campaignId, Long projectId);

    List<MatchingResultResponse> findByCampaignAndSubject(Long campaignId, Long subjectId);

    MatchingResultResponse findById(Long id);

    /** Tous les résultats d'un étudiant (toutes campagnes) */
    List<MatchingResultResponse> findByStudentId(Long studentId);

    void deleteByCampaignId(Long campaignId);

    long countByCampaignId(Long campaignId);
}