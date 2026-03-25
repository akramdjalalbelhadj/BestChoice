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

import java.util.Collections;
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
    private KeywordUpdateRequest updateRequest;
    private KeywordResponse keywordResponse;

    @BeforeEach
    void setUp() {
        keyword = new Keyword();
        keyword.setId(1L);
        keyword.setLabel("Spring Boot");
        keyword.setDomain("Web");
        keyword.setActive(true);

        createRequest = new KeywordCreateRequest("Spring Boot", "Spring Boot framework", "Web");
        updateRequest = new KeywordUpdateRequest("Spring Boot 3", "Spring Boot 3 framework", "Web", true);
        keywordResponse = new KeywordResponse(1L, "Spring Boot", "Spring Boot framework", "Web", true);
    }

    @Test
    void create_ShouldReturnKeywordResponse_WhenLabelDoesNotExist() {
        when(keywordRepository.existsByLabel(createRequest.label())).thenReturn(false);
        when(keywordMapper.toEntity(createRequest)).thenReturn(keyword);
        when(keywordRepository.save(any(Keyword.class))).thenReturn(keyword);
        when(keywordMapper.toResponse(keyword)).thenReturn(keywordResponse);

        KeywordResponse result = keywordService.create(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.label()).isEqualTo("Spring Boot");
        verify(keywordRepository).existsByLabel(createRequest.label());
        verify(keywordRepository).save(any(Keyword.class));
    }

    @Test
    void create_ShouldThrowBusinessException_WhenLabelExists() {
        when(keywordRepository.existsByLabel(createRequest.label())).thenReturn(true);

        assertThatThrownBy(() -> keywordService.create(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("existe déjà");
    }

    @Test
    void update_ShouldReturnKeywordResponse_WhenKeywordExists() {
        when(keywordRepository.findById(1L)).thenReturn(Optional.of(keyword));
        when(keywordRepository.existsByLabel(updateRequest.label())).thenReturn(false);
        when(keywordRepository.save(any(Keyword.class))).thenReturn(keyword);
        when(keywordMapper.toResponse(keyword)).thenReturn(keywordResponse);

        KeywordResponse result = keywordService.update(1L, updateRequest);

        assertThat(result).isNotNull();
        verify(keywordMapper).updateEntityFromDto(updateRequest, keyword);
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenKeywordDoesNotExist() {
        when(keywordRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> keywordService.update(1L, updateRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findById_ShouldReturnKeywordResponse_WhenKeywordExists() {
        when(keywordRepository.findById(1L)).thenReturn(Optional.of(keyword));
        when(keywordMapper.toResponse(keyword)).thenReturn(keywordResponse);

        KeywordResponse result = keywordService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void findAll_Paginated_ShouldReturnPageOfKeywordResponse() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "label"));
        Page<Keyword> keywordPage = new PageImpl<>(Collections.singletonList(keyword));
        when(keywordRepository.findAll(pageable)).thenReturn(keywordPage);
        when(keywordMapper.toResponse(keyword)).thenReturn(keywordResponse);

        Page<KeywordResponse> result = keywordService.findAll(0, 10, "label", "ASC");

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void delete_ShouldCallRepositoryDelete_WhenKeywordExists() {
        when(keywordRepository.findById(1L)).thenReturn(Optional.of(keyword));

        keywordService.delete(1L);

        verify(keywordRepository).delete(keyword);
    }

    @Test
    void deactivate_ShouldSetKeywordInactive_WhenKeywordExistsAndIsActive() {
        when(keywordRepository.findById(1L)).thenReturn(Optional.of(keyword));

        keywordService.deactivate(1L);

        assertThat(keyword.getActive()).isFalse();
        verify(keywordRepository).save(keyword);
    }
}
