package fr.amu.bestchoice.repository;

import fr.amu.bestchoice.model.entity.MatchingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MatchingResultRepository extends JpaRepository<MatchingResult, Long> {

    @Modifying
    @Query("DELETE FROM MatchingResult mr WHERE mr.matchingCampaign.id = :campaignId")
    void deleteByMatchingCampaignId(@Param("campaignId") Long campaignId);

    List<MatchingResult> findByMatchingCampaignIdOrderByGlobalScoreDesc(Long campaignId);

    List<MatchingResult> findByMatchingCampaignIdAndStudentIdOrderByGlobalScoreDesc(Long campaignId, Long studentId);

    List<MatchingResult> findByMatchingCampaignIdAndProjectIdOrderByGlobalScoreDesc(Long campaignId, Long projectId);

    List<MatchingResult> findByMatchingCampaignIdAndSubjectIdOrderByGlobalScoreDesc(Long campaignId, Long subjectId);

    long countByMatchingCampaignId(Long campaignId);
}