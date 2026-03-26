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
class StableMatchingStrategyTest {

    @Mock
    private MatchingResultRepository resultRepository;
    @Mock
    private MatchingScoringService scoringService;
    @Mock
    private StudentPreferenceRepository preferenceRepository;

    @InjectMocks
    private StableMatchingStrategy strategy;

    private MatchingCampaign campaign;
    private Student student;
    private Project project;

    @BeforeEach
    void setUp() {
        campaign = new MatchingCampaign();
        campaign.setId(1L);
        campaign.setCampaignType(MatchingCampaignType.PROJECT);
        
        student = new Student();
        student.setId(1L);
        student.setPreferences(new ArrayList<>());
        
        project = new Project();
        project.setId(1L);
        project.setMaxStudents(1);
        
        campaign.setStudents(new HashSet<>(List.of(student)));
        campaign.setProjects(new HashSet<>(List.of(project)));
        
        when(scoringService.computeGlobalScore(any(), any(), any(), any())).thenReturn(new BigDecimal("0.8"));
    }

    @Test
    void execute_ShouldAssignStudentToProject_WhenPreferenceExists() {
        // Given
        StudentPreference pref = new StudentPreference();
        pref.setStudent(student);
        pref.setProject(project);
        pref.setRank(1);
        pref.setMatchingCampaign(campaign);
        student.getPreferences().add(pref);

        // When
        MatchingRunResult result = strategy.execute(campaign);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.resultsStored()).isEqualTo(1);
        verify(resultRepository).saveAll(anyList());
    }

    @Test
    void execute_ShouldHandleCompetitionForProject() {
        // Given
        Student student2 = new Student();
        student2.setId(2L);
        student2.setPreferences(new ArrayList<>());
        campaign.getStudents().add(student2);

        // Both want the same project
        StudentPreference pref1 = new StudentPreference();
        pref1.setStudent(student); pref1.setProject(project); pref1.setRank(1); pref1.setMatchingCampaign(campaign);
        student.getPreferences().add(pref1);

        StudentPreference pref2 = new StudentPreference();
        pref2.setStudent(student2); pref2.setProject(project); pref2.setRank(1); pref2.setMatchingCampaign(campaign);
        student2.getPreferences().add(pref2);

        // student2 has better score
        when(scoringService.computeGlobalScore(eq(student), eq(project), any(), any())).thenReturn(new BigDecimal("0.7"));
        when(scoringService.computeGlobalScore(eq(student2), eq(project), any(), any())).thenReturn(new BigDecimal("0.9"));

        // When
        MatchingRunResult result = strategy.execute(campaign);

        // Then
        assertThat(result.resultsStored()).isEqualTo(1);
        verify(resultRepository).saveAll(argThat(results -> {
            List<MatchingResult> list = (List<MatchingResult>) results;
            return list.get(0).getStudent().getId().equals(2L);
        }));
    }
}
