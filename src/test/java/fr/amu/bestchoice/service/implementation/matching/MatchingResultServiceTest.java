package fr.amu.bestchoice.service.implementation.matching;

import fr.amu.bestchoice.model.entity.MatchingResult;
import fr.amu.bestchoice.repository.MatchingResultRepository;
import fr.amu.bestchoice.web.dto.matching.MatchingResultResponse;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.MatchingResultMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingResultServiceTest {

    @Mock
    private MatchingResultRepository matchingResultRepository;
    @Mock
    private MatchingResultMapper matchingResultMapper;

    @InjectMocks
    private MatchingResultService matchingResultService;

    private MatchingResult matchingResult;
    private MatchingResultResponse matchingResultResponse;

    @BeforeEach
    void setUp() {
        matchingResult = new MatchingResult();
        matchingResult.setId(1L);

        matchingResultResponse = new MatchingResultResponse(1L, 1L, 1L, 1L, null, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 1, null, LocalDateTime.now(), null, null);
    }

    @Test
    void findByCampaignId_ShouldReturnList() {
        // Given
        Long campaignId = 1L;
        when(matchingResultRepository.findByMatchingCampaignIdOrderByGlobalScoreDesc(campaignId)).thenReturn(List.of(matchingResult));
        when(matchingResultMapper.toResponseList(anyList())).thenReturn(List.of(matchingResultResponse));

        // When
        List<MatchingResultResponse> result = matchingResultService.findByCampaignId(campaignId);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void findById_ShouldReturnResponse_WhenExists() {
        // Given
        Long id = 1L;
        when(matchingResultRepository.findById(id)).thenReturn(Optional.of(matchingResult));
        when(matchingResultMapper.toResponse(matchingResult)).thenReturn(matchingResultResponse);

        // When
        MatchingResultResponse result = matchingResultService.findById(id);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void findById_ShouldThrowNotFoundException_WhenNotExists() {
        // Given
        Long id = 1L;
        when(matchingResultRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> matchingResultService.findById(id))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteByCampaignId_ShouldCallRepository() {
        // Given
        Long campaignId = 1L;

        // When
        matchingResultService.deleteByCampaignId(campaignId);

        // Then
        verify(matchingResultRepository).deleteByMatchingCampaignId(campaignId);
    }
}
