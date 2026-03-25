package fr.amu.bestchoice.service.implementation.algorithmes;

import fr.amu.bestchoice.model.entity.*;
import fr.amu.bestchoice.repository.MatchingResultRepository;
import fr.amu.bestchoice.web.dto.matching.MatchingRunResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    @InjectMocks
    private WeightedMatchingStrategy weightedMatchingStrategy;

    private MatchingCampaign campaign;
    private Student student1;
    private Student student2;
    private Project project1;
    private Project project2;

    @BeforeEach
    void setUp() {
        student1 = Student.builder().id(1L).build();
        student2 = Student.builder().id(2L).build();

        project1 = Project.builder().id(10L).title("Project 1").build();
        project2 = Project.builder().id(11L).title("Project 2").build();

        campaign = MatchingCampaign.builder()
                .id(1L)
                .name("Test Campaign")
                .campaignType(MatchingCampaignType.PROJECT)
                .algorithmType(MatchingAlgorithmType.WEIGHTED)
                .skillsWeight(new BigDecimal("0.5"))
                .interestsWeight(new BigDecimal("0.3"))
                .workTypeWeight(new BigDecimal("0.2"))
                .students(new HashSet<>(Arrays.asList(student1, student2)))
                .projects(new HashSet<>(Arrays.asList(project1, project2)))
                .build();
    }

    @Test
    void getAlgorithmType_ShouldReturnWeighted() {
        assertThat(weightedMatchingStrategy.getAlgorithmType()).isEqualTo(MatchingAlgorithmType.WEIGHTED);
    }

    @Test
    void execute_ShouldComputeScoresAndSaveResults_ForProjects() {
        // Given
        when(scoringService.computeGlobalScore(eq(student1), eq(project1), isNull(), eq(campaign)))
                .thenReturn(new BigDecimal("0.8"));
        when(scoringService.computeGlobalScore(eq(student1), eq(project2), isNull(), eq(campaign)))
                .thenReturn(new BigDecimal("0.6"));
        when(scoringService.computeGlobalScore(eq(student2), eq(project1), isNull(), eq(campaign)))
                .thenReturn(new BigDecimal("0.4"));
        when(scoringService.computeGlobalScore(eq(student2), eq(project2), isNull(), eq(campaign)))
                .thenReturn(new BigDecimal("0.9"));

        // When
        MatchingRunResult runResult = weightedMatchingStrategy.execute(campaign);

        // Then
        verify(resultRepository).deleteByMatchingCampaignId(campaign.getId());
        
        ArgumentCaptor<List<MatchingResult>> resultsCaptor = ArgumentCaptor.forClass(List.class);
        verify(resultRepository).saveAll(resultsCaptor.capture());
        
        List<MatchingResult> savedResults = resultsCaptor.getValue();
        assertThat(savedResults).hasSize(4);

        // Verify recommendation ranks for student 1
        MatchingResult s1p1 = savedResults.stream()
                .filter(r -> r.getStudent().getId().equals(1L) && r.getProject().getId().equals(10L))
                .findFirst().orElseThrow();
        MatchingResult s1p2 = savedResults.stream()
                .filter(r -> r.getStudent().getId().equals(1L) && r.getProject().getId().equals(11L))
                .findFirst().orElseThrow();
        
        assertThat(s1p1.getRecommendationRank()).isEqualTo(1); // 0.8 > 0.6
        assertThat(s1p2.getRecommendationRank()).isEqualTo(2);

        // Verify recommendation ranks for student 2
        MatchingResult s2p1 = savedResults.stream()
                .filter(r -> r.getStudent().getId().equals(2L) && r.getProject().getId().equals(10L))
                .findFirst().orElseThrow();
        MatchingResult s2p2 = savedResults.stream()
                .filter(r -> r.getStudent().getId().equals(2L) && r.getProject().getId().equals(11L))
                .findFirst().orElseThrow();
        
        assertThat(s2p1.getRecommendationRank()).isEqualTo(2); // 0.4 < 0.9
        assertThat(s2p2.getRecommendationRank()).isEqualTo(1);

        assertThat(runResult.algorithmUsed()).isEqualTo(MatchingAlgorithmType.WEIGHTED);
        assertThat(runResult.studentsProcessed()).isEqualTo(2);
    }

    @Test
    void execute_ShouldComputeScoresAndSaveResults_ForSubjects() {
        // Given
        Subject subject1 = Subject.builder().id(20L).title("Subject 1").build();
        campaign.setCampaignType(MatchingCampaignType.SUBJECT);
        campaign.setProjects(new HashSet<>());
        campaign.setSubjects(new HashSet<>(Collections.singletonList(subject1)));

        when(scoringService.computeGlobalScore(eq(student1), isNull(), eq(subject1), eq(campaign)))
                .thenReturn(new BigDecimal("0.7"));
        when(scoringService.computeGlobalScore(eq(student2), isNull(), eq(subject1), eq(campaign)))
                .thenReturn(new BigDecimal("0.5"));

        // When
        weightedMatchingStrategy.execute(campaign);

        // Then
        verify(resultRepository).saveAll(anyList());
        verify(scoringService, times(2)).computeGlobalScore(any(), isNull(), eq(subject1), eq(campaign));
    }
}
