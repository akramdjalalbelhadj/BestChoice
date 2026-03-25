package fr.amu.bestchoice.service.interfaces;

import fr.amu.bestchoice.web.dto.campaign.MatchingCampaignRequest;
import fr.amu.bestchoice.web.dto.campaign.MatchingCampaignResponse;
import java.util.List;

public interface IMatchingCampaignService {

    MatchingCampaignResponse create(MatchingCampaignRequest request);
    MatchingCampaignResponse findById(Long id);
    List<MatchingCampaignResponse> findByTeacherId(Long teacherId);
    List<MatchingCampaignResponse> findByStudentId(Long studentId);
    void addStudentsToCampaign(Long campaignId, List<Long> studentIds);
    void addItemsToCampaign(Long campaignId, List<Long> itemIds);
    void delete(Long id);
}