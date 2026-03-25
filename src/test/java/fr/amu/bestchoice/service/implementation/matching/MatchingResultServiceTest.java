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

import java.util.Collections;
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
        matchingResultResponse = mock(MatchingResultResponse.class);
    }

    @Test
    void findByCampaignId_ShouldReturnResponseList() {
        when(matchingResultRepository.findByMatchingCampaignIdOrderByGlobalScoreDesc(1L))
                .thenReturn(Collections.singletonList(matchingResult));
        when(matchingResultMapper.toResponseList(anyList()))
                .thenReturn(Collections.singletonList(matchingResultResponse));

        List<MatchingResultResponse> result = matchingResultService.findByCampaignId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void findById_ShouldReturnResponse_WhenExists() {
        when(matchingResultRepository.findById(1L)).thenReturn(Optional.of(matchingResult));
        when(matchingResultMapper.toResponse(matchingResult)).thenReturn(matchingResultResponse);

        MatchingResultResponse result = matchingResultService.findById(1L);

        assertThat(result).isNotNull();
    }

    @Test
    void findById_ShouldThrowNotFoundException_WhenNotExists() {
        when(matchingResultRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchingResultService.findById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteByCampaignId_ShouldCallRepository() {
        matchingResultService.deleteByCampaignId(1L);
        verify(matchingResultRepository).deleteByMatchingCampaignId(1L);
    }

    @Test
    void countByCampaignId_ShouldReturnCount() {
        when(matchingResultRepository.countByMatchingCampaignId(1L)).thenReturn(10L);
        long count = matchingResultService.countByCampaignId(1L);
        assertThat(count).isEqualTo(10L);
    }
}
