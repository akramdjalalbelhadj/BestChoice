package fr.amu.bestchoice.service.implementation.algorithmes;

import fr.amu.bestchoice.model.entity.MatchingCampaign;
import fr.amu.bestchoice.model.entity.Student;
import fr.amu.bestchoice.model.entity.StudentPreference;
import fr.amu.bestchoice.repository.MatchingCampaignRepository;
import fr.amu.bestchoice.web.dto.matching.MatchingRunResult;
import fr.amu.bestchoice.web.exception.BusinessException;
import fr.amu.bestchoice.web.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingContextServiceTest {

    @Mock
    private MatchingCampaignRepository campaignRepository;
    @Mock
    private MatchingStrategy strategy;

    private MatchingContextService contextService;
    private MatchingCampaign campaign;

    @BeforeEach
    void setUp() {
        contextService = new MatchingContextService(campaignRepository, Collections.singletonList(strategy));
        campaign = new MatchingCampaign();
        campaign.setId(1L);
        campaign.setAlgorithmType(MatchingAlgorithmType.WEIGHTED);
    }

    @Test
    void run_ShouldExecuteStrategy_WhenCampaignExists() {
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(strategy.getAlgorithmType()).thenReturn(MatchingAlgorithmType.WEIGHTED);
        MatchingRunResult mockResult = mock(MatchingRunResult.class);
        when(strategy.execute(campaign)).thenReturn(mockResult);

        MatchingRunResult result = contextService.run(1L);

        assertThat(result).isEqualTo(mockResult);
        verify(strategy).execute(campaign);
    }

    @Test
    void run_ShouldThrowNotFoundException_WhenCampaignDoesNotExist() {
        when(campaignRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contextService.run(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void run_ShouldThrowBusinessException_WhenStableMatchingNotReady() {
        campaign.setAlgorithmType(MatchingAlgorithmType.STABLE);
        Student student = new Student();
        student.setId(1L);
        student.setPreferences(Collections.emptyList());
        campaign.setStudents(Set.of(student));

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        assertThatThrownBy(() -> contextService.run(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("n'ont pas encore saisi leurs vœux");
    }

    @Test
    void run_ShouldSucceed_WhenStableMatchingIsReady() {
        campaign.setAlgorithmType(MatchingAlgorithmType.STABLE);
        Student student = new Student();
        student.setId(1L);
        
        StudentPreference pref = new StudentPreference();
        pref.setMatchingCampaign(campaign);
        student.setPreferences(List.of(pref));
        
        campaign.setStudents(Set.of(student));

        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        when(strategy.getAlgorithmType()).thenReturn(MatchingAlgorithmType.STABLE);
        MatchingRunResult mockResult = mock(MatchingRunResult.class);
        when(strategy.execute(campaign)).thenReturn(mockResult);

        MatchingRunResult result = contextService.run(1L);

        assertThat(result).isEqualTo(mockResult);
    }
}
