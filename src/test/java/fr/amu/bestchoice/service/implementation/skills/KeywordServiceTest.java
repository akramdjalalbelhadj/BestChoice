package fr.amu.bestchoice.service.implementation.skills;

import fr.amu.bestchoice.model.entity.Keyword;
import fr.amu.bestchoice.repository.KeywordRepository;
import fr.amu.bestchoice.web.dto.keyword.KeywordCreateRequest;
import fr.amu.bestchoice.web.dto.keyword.KeywordResponse;
import fr.amu.bestchoice.web.dto.keyword.KeywordUpdateRequest;
import fr.amu.bestchoice.web.exception.BusinessException;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.KeywordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeywordServiceTest {

    @Mock
    private KeywordRepository keywordRepository;
    @Mock
    private KeywordMapper keywordMapper;

    @InjectMocks
    private KeywordService keywordService;

    private Keyword keyword;
    private KeywordCreateRequest createRequest;
    private KeywordResponse keywordResponse;

    @BeforeEach
    void setUp() {
        keyword = new Keyword();
        keyword.setId(1L);
        keyword.setLabel("Web");
        keyword.setActive(true);

        createRequest = new KeywordCreateRequest("Web", "Web tech", "IT");
        keywordResponse = new KeywordResponse(1L, "Web", "Web tech", "IT", true);
    }

    @Test
    void create_ShouldReturnKeywordResponse_WhenValidRequest() {
        // Given
        when(keywordRepository.existsByLabel(createRequest.label())).thenReturn(false);
        when(keywordMapper.toEntity(createRequest)).thenReturn(keyword);
        when(keywordRepository.save(any(Keyword.class))).thenReturn(keyword);
        when(keywordMapper.toResponse(any())).thenReturn(keywordResponse);

        // When
        KeywordResponse result = keywordService.create(createRequest);

        // Then
        assertThat(result).isNotNull();
        verify(keywordRepository).save(any(Keyword.class));
    }

    @Test
    void create_ShouldThrowBusinessException_WhenLabelAlreadyExists() {
        // Given
        when(keywordRepository.existsByLabel(createRequest.label())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> keywordService.create(createRequest))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void update_ShouldReturnUpdatedKeywordResponse_WhenValidRequest() {
        // Given
        Long keywordId = 1L;
        KeywordUpdateRequest updateRequest = new KeywordUpdateRequest("Mobile", null, null, true);
        when(keywordRepository.findById(keywordId)).thenReturn(Optional.of(keyword));
        when(keywordRepository.existsByLabel("Mobile")).thenReturn(false);
        when(keywordRepository.save(any(Keyword.class))).thenReturn(keyword);
        when(keywordMapper.toResponse(any())).thenReturn(keywordResponse);

        // When
        KeywordResponse result = keywordService.update(keywordId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(keywordMapper).updateEntityFromDto(eq(updateRequest), eq(keyword));
        verify(keywordRepository).save(keyword);
    }

    @Test
    void delete_ShouldCallRepository() {
        // Given
        Long keywordId = 1L;
        when(keywordRepository.findById(keywordId)).thenReturn(Optional.of(keyword));

        // When
        keywordService.delete(keywordId);

        // Then
        verify(keywordRepository).delete(keyword);
    }

    @Test
    void deactivate_ShouldSetKeywordInactive() {
        // Given
        Long keywordId = 1L;
        when(keywordRepository.findById(keywordId)).thenReturn(Optional.of(keyword));

        // When
        keywordService.deactivate(keywordId);

        // Then
        assertThat(keyword.getActive()).isFalse();
        verify(keywordRepository).save(keyword);
    }

    @Test
    void findAll_ShouldReturnPageOfKeywords() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "label"));
        Page<Keyword> keywordPage = new PageImpl<>(List.of(keyword));
        when(keywordRepository.findAll(any(Pageable.class))).thenReturn(keywordPage);
        when(keywordMapper.toResponse(any())).thenReturn(keywordResponse);

        // When
        Page<KeywordResponse> result = keywordService.findAll(0, 10, "label", "asc");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }
}
