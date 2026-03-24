package fr.amu.bestchoice.service.implementation.algorithmes;

import fr.amu.bestchoice.model.entity.MatchingCampaign;
import fr.amu.bestchoice.repository.MatchingCampaignRepository;
import fr.amu.bestchoice.web.dto.matching.MatchingRunResult;
import fr.amu.bestchoice.web.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchingContextServiceTest {

    @Mock
    private MatchingCampaignRepository campaignRepository;
    
    @Spy
    private List<MatchingStrategy> strategies = new ArrayList<>();

    @InjectMocks
    private MatchingContextService matchingContextService;

    private MatchingCampaign campaign;

    @BeforeEach
    void setUp() {
        campaign = new MatchingCampaign();
        campaign.setId(1L);
        campaign.setAlgorithmType(MatchingAlgorithmType.WEIGHTED);
    }

    @Test
    void run_ShouldExecuteCorrectStrategy_WhenCampaignExists() {
        // Given
        MatchingStrategy weightedStrategy = mock(MatchingStrategy.class);
        when(weightedStrategy.getAlgorithmType()).thenReturn(MatchingAlgorithmType.WEIGHTED);
        
        MatchingRunResult expectedResult = new MatchingRunResult(1L, MatchingAlgorithmType.WEIGHTED, 10, 50, Instant.now(), Instant.now());
        when(weightedStrategy.execute(campaign)).thenReturn(expectedResult);
        
        strategies.add(weightedStrategy);
        
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

        // When
        MatchingRunResult result = matchingContextService.run(1L);

        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void run_ShouldThrowNotFoundException_WhenCampaignDoesNotExist() {
        // Given
        when(campaignRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> matchingContextService.run(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void run_ShouldThrowIllegalArgumentException_WhenNoStrategyFound() {
        // Given
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
        // strategies list is empty

        // When & Then
        assertThatThrownBy(() -> matchingContextService.run(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
