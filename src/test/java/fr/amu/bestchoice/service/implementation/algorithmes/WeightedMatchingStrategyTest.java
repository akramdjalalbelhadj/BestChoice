package fr.amu.bestchoice.service.implementation.algorithmes;

import fr.amu.bestchoice.model.entity.*;
import fr.amu.bestchoice.repository.MatchingResultRepository;
import fr.amu.bestchoice.repository.StudentPreferenceRepository;
import fr.amu.bestchoice.web.dto.matching.MatchingRunResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeightedMatchingStrategyTest {

    @Mock
    private MatchingResultRepository resultRepository;
    @Mock
    private MatchingScoringService scoringService;
    @Mock
    private StudentPreferenceRepository preferenceRepository;

    @InjectMocks
    private WeightedMatchingStrategy strategy;

    private MatchingCampaign campaign;
    private Student student;
    private Project project;

    @BeforeEach
    void setUp() {
        campaign = new MatchingCampaign();
        campaign.setId(1L);
        campaign.setCampaignType(MatchingCampaignType.PROJECT);
        campaign.setAlgorithmType(MatchingAlgorithmType.WEIGHTED);
        
        student = new Student();
        student.setId(1L);
        campaign.setStudents(Set.of(student));
        
        project = new Project();
        project.setId(1L);
        campaign.setProjects(Set.of(project));
        
        when(scoringService.computeGlobalScore(any(), any(), any(), any())).thenReturn(new BigDecimal("0.75"));
    }

    @Test
    void execute_ShouldReturnRunResultAndSaveResults() {
        // When
        MatchingRunResult result = strategy.execute(campaign);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.algorithmUsed()).isEqualTo(MatchingAlgorithmType.WEIGHTED);
        assertThat(result.studentsProcessed()).isEqualTo(1);
        verify(resultRepository).deleteByMatchingCampaignId(campaign.getId());
        verify(resultRepository).saveAll(anyList());
    }

    @Test
    void execute_ShouldRankResultsCorrectly() {
        // Given
        Project project2 = new Project();
        project2.setId(2L);
        campaign.setProjects(new HashSet<>(List.of(project, project2)));
        
        // Mock different scores
        when(scoringService.computeGlobalScore(eq(student), eq(project), any(), any())).thenReturn(new BigDecimal("0.9"));
        when(scoringService.computeGlobalScore(eq(student), eq(project2), any(), any())).thenReturn(new BigDecimal("0.6"));

        // When
        strategy.execute(campaign);

        // Then
        verify(resultRepository).saveAll(argThat(results -> {
            List<MatchingResult> list = (List<MatchingResult>) results;
            list.sort(Comparator.comparing(MatchingResult::getRecommendationRank));
            return list.get(0).getGlobalScore().equals(new BigDecimal("0.9")) && list.get(0).getRecommendationRank() == 1
                && list.get(1).getGlobalScore().equals(new BigDecimal("0.6")) && list.get(1).getRecommendationRank() == 2;
        }));
    }
}
