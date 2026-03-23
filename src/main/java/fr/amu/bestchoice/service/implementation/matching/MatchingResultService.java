package fr.amu.bestchoice.service.implementation.matching;

import fr.amu.bestchoice.model.entity.MatchingResult;
import fr.amu.bestchoice.repository.MatchingResultRepository;
import fr.amu.bestchoice.service.interfaces.IMatchingResultService;
import fr.amu.bestchoice.web.dto.matching.MatchingResultResponse;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.MatchingResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingResultService implements IMatchingResultService {

    private final MatchingResultRepository matchingResultRepository;
    private final MatchingResultMapper matchingResultMapper;

    @Override
    public List<MatchingResultResponse> findByCampaignId(Long campaignId) {
        log.debug("Récupération de tous les résultats pour la campagne : {}", campaignId);
        List<MatchingResult> results = matchingResultRepository.findByMatchingCampaignIdOrderByGlobalScoreDesc(campaignId);
        return matchingResultMapper.toResponseList(results);
    }

    @Override
    public List<MatchingResultResponse> findByCampaignAndStudent(Long campaignId, Long studentId) {
        log.debug("Résultats pour l'étudiant {} dans la campagne {}", studentId, campaignId);
        List<MatchingResult> results = matchingResultRepository.findByMatchingCampaignIdAndStudentIdOrderByGlobalScoreDesc(campaignId, studentId);
        return matchingResultMapper.toResponseList(results);
    }

    @Override
    public List<MatchingResultResponse> findTopResultsForStudent(Long campaignId, Long studentId, int n) {
        log.debug("Top {} résultats pour l'étudiant {} dans la campagne {}", n, studentId, campaignId);
        List<MatchingResult> all = matchingResultRepository.findByMatchingCampaignIdAndStudentIdOrderByGlobalScoreDesc(campaignId, studentId);
        // On limite les résultats directement en Java pour rester simple
        List<MatchingResult> limited = all.stream().limit(n).toList();
        return matchingResultMapper.toResponseList(limited);
    }

    @Override
    public List<MatchingResultResponse> findByCampaignAndProject(Long campaignId, Long projectId) {
        log.debug("Résultats pour le projet {} dans la campagne {}", projectId, campaignId);
        List<MatchingResult> results = matchingResultRepository.findByMatchingCampaignIdAndProjectIdOrderByGlobalScoreDesc(campaignId, projectId);
        return matchingResultMapper.toResponseList(results);
    }

    @Override
    public List<MatchingResultResponse> findByCampaignAndSubject(Long campaignId, Long subjectId) {
        log.debug("Résultats pour la matière {} dans la campagne {}", subjectId, campaignId);
        List<MatchingResult> results = matchingResultRepository.findByMatchingCampaignIdAndSubjectIdOrderByGlobalScoreDesc(campaignId, subjectId);
        return matchingResultMapper.toResponseList(results);
    }

    @Override
    public MatchingResultResponse findById(Long id) {
        return matchingResultRepository.findById(id)
                .map(matchingResultMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Résultat de matching introuvable avec l'ID : " + id));
    }

    @Override
    @Transactional
    public void deleteByCampaignId(Long campaignId) {
        log.warn("Suppression des résultats de la campagne : {}", campaignId);
        matchingResultRepository.deleteByMatchingCampaignId(campaignId);
    }

    @Override
    public long countByCampaignId(Long campaignId) {
        return matchingResultRepository.countByMatchingCampaignId(campaignId);
    }
}