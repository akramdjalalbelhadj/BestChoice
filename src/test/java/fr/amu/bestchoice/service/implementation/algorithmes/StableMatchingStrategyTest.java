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
class StableMatchingStrategyTest {

    @Mock
    private MatchingResultRepository resultRepository;

    @Mock
    private MatchingScoringService scoringService;

    @InjectMocks
    private StableMatchingStrategy stableMatchingStrategy;

    private MatchingCampaign campaign;
    private Student student1;
    private Student student2;
    private Project project1;
    private Project project2;

    @BeforeEach
    void setUp() {
        student1 = Student.builder().id(1L).preferences(new ArrayList<>()).build();
        student2 = Student.builder().id(2L).preferences(new ArrayList<>()).build();

        project1 = Project.builder().id(10L).title("Project 1").maxStudents(1).build();
        project2 = Project.builder().id(11L).title("Project 2").maxStudents(1).build();

        campaign = MatchingCampaign.builder()
                .id(1L)
                .name("Test Campaign")
                .campaignType(MatchingCampaignType.PROJECT)
                .algorithmType(MatchingAlgorithmType.STABLE)
                .students(new HashSet<>(Arrays.asList(student1, student2)))
                .projects(new HashSet<>(Arrays.asList(project1, project2)))
                .build();
        
        // Setup preferences
        student1.getPreferences().add(StudentPreference.builder()
                .matchingCampaign(campaign).project(project1).rank(1).build());
        student1.getPreferences().add(StudentPreference.builder()
                .matchingCampaign(campaign).project(project2).rank(2).build());

        student2.getPreferences().add(StudentPreference.builder()
                .matchingCampaign(campaign).project(project1).rank(1).build());
        student2.getPreferences().add(StudentPreference.builder()
                .matchingCampaign(campaign).project(project2).rank(2).build());
    }

    @Test
    void getAlgorithmType_ShouldReturnStable() {
        assertThat(stableMatchingStrategy.getAlgorithmType()).isEqualTo(MatchingAlgorithmType.STABLE);
    }

    @Test
    void execute_ShouldAssignStudentsToProjects_BasedOnPreferencesAndScores() {
        // Given
        // Student 1 has better score for Project 1 than Student 2
        when(scoringService.computeGlobalScore(eq(student1), eq(project1), isNull(), eq(campaign)))
                .thenReturn(new BigDecimal("0.9"));
        when(scoringService.computeGlobalScore(eq(student1), eq(project2), isNull(), eq(campaign)))
                .thenReturn(new BigDecimal("0.8"));
        when(scoringService.computeGlobalScore(eq(student2), eq(project1), isNull(), eq(campaign)))
                .thenReturn(new BigDecimal("0.7"));
        when(scoringService.computeGlobalScore(eq(student2), eq(project2), isNull(), eq(campaign)))
                .thenReturn(new BigDecimal("0.6"));

        // When
        MatchingRunResult runResult = stableMatchingStrategy.execute(campaign);

        // Then
        verify(resultRepository).deleteByMatchingCampaignId(campaign.getId());
        
        ArgumentCaptor<List<MatchingResult>> resultsCaptor = ArgumentCaptor.forClass(List.class);
        verify(resultRepository).saveAll(resultsCaptor.capture());
        
        List<MatchingResult> savedResults = resultsCaptor.getValue();
        assertThat(savedResults).hasSize(2);

        // Student 1 should get Project 1 (Rank 1)
        MatchingResult res1 = savedResults.stream()
                .filter(r -> r.getStudent().getId().equals(1L))
                .findFirst().orElseThrow();
        assertThat(res1.getProject().getId()).isEqualTo(10L);

        // Student 2 should get Project 2 (Rank 2) because Project 1 is taken by someone with better score
        MatchingResult res2 = savedResults.stream()
                .filter(r -> r.getStudent().getId().equals(2L))
                .findFirst().orElseThrow();
        assertThat(res2.getProject().getId()).isEqualTo(11L);

        assertThat(runResult.algorithmUsed()).isEqualTo(MatchingAlgorithmType.STABLE);
    }

    @Test
    void execute_ShouldHandleSubjects() {
        // Given
        Subject subject1 = Subject.builder().id(20L).maxStudents(2).build();
        campaign.setCampaignType(MatchingCampaignType.SUBJECT);
        campaign.setProjects(new HashSet<>());
        campaign.setSubjects(new HashSet<>(Collections.singletonList(subject1)));

        student1.setPreferences(new ArrayList<>(Collections.singletonList(
                StudentPreference.builder().matchingCampaign(campaign).subject(subject1).rank(1).build()
        )));
        student2.setPreferences(new ArrayList<>(Collections.singletonList(
                StudentPreference.builder().matchingCampaign(campaign).subject(subject1).rank(1).build()
        )));

        when(scoringService.computeGlobalScore(any(), isNull(), eq(subject1), eq(campaign)))
                .thenReturn(new BigDecimal("0.5"));

        // When
        stableMatchingStrategy.execute(campaign);

        // Then
        verify(resultRepository).saveAll(anyList());
    }
}
