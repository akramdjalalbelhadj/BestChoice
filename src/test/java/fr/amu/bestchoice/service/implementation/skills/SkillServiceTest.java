package fr.amu.bestchoice.service.implementation.skills;

import fr.amu.bestchoice.model.entity.Skill;
import fr.amu.bestchoice.repository.SkillRepository;
import fr.amu.bestchoice.web.dto.skill.SkillCreateRequest;
import fr.amu.bestchoice.web.dto.skill.SkillResponse;
import fr.amu.bestchoice.web.dto.skill.SkillUpdateRequest;
import fr.amu.bestchoice.web.exception.BusinessException;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.SkillMapper;
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
class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;
    @Mock
    private SkillMapper skillMapper;

    @InjectMocks
    private SkillService skillService;

    private Skill skill;
    private SkillCreateRequest createRequest;
    private SkillResponse skillResponse;

    @BeforeEach
    void setUp() {
        skill = new Skill();
        skill.setId(1L);
        skill.setName("Java");
        skill.setActive(true);

        createRequest = new SkillCreateRequest("Java", "Java language", "Backend", 3);
        skillResponse = new SkillResponse(1L, "Java", "Java language", "Backend", 3, true);
    }

    @Test
    void create_ShouldReturnSkillResponse_WhenValidRequest() {
        // Given
        when(skillRepository.existsByName(createRequest.name())).thenReturn(false);
        when(skillMapper.toEntity(createRequest)).thenReturn(skill);
        when(skillRepository.save(any(Skill.class))).thenReturn(skill);
        when(skillMapper.toResponse(any())).thenReturn(skillResponse);

        // When
        SkillResponse result = skillService.create(createRequest);

        // Then
        assertThat(result).isNotNull();
        verify(skillRepository).save(any(Skill.class));
    }

    @Test
    void create_ShouldThrowBusinessException_WhenNameAlreadyExists() {
        // Given
        when(skillRepository.existsByName(createRequest.name())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> skillService.create(createRequest))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void update_ShouldReturnUpdatedSkillResponse_WhenValidRequest() {
        // Given
        Long skillId = 1L;
        SkillUpdateRequest updateRequest = new SkillUpdateRequest("Python", null, null, null, null);
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(skill));
        when(skillRepository.existsByName("Python")).thenReturn(false);
        when(skillRepository.save(any(Skill.class))).thenReturn(skill);
        when(skillMapper.toResponse(any())).thenReturn(skillResponse);

        // When
        SkillResponse result = skillService.update(skillId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(skillMapper).updateEntityFromDto(eq(updateRequest), eq(skill));
        verify(skillRepository).save(skill);
    }

    @Test
    void delete_ShouldCallRepository() {
        // Given
        Long skillId = 1L;
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(skill));

        // When
        skillService.delete(skillId);

        // Then
        verify(skillRepository).delete(skill);
    }

    @Test
    void deactivate_ShouldSetSkillInactive() {
        // Given
        Long skillId = 1L;
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(skill));

        // When
        skillService.deactivate(skillId);

        // Then
        assertThat(skill.getActive()).isFalse();
        verify(skillRepository).save(skill);
    }

    @Test
    void findAll_ShouldReturnPageOfSkills() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        Page<Skill> skillPage = new PageImpl<>(List.of(skill));
        when(skillRepository.findAll(any(Pageable.class))).thenReturn(skillPage);
        when(skillMapper.toResponse(any())).thenReturn(skillResponse);

        // When
        Page<SkillResponse> result = skillService.findAll(0, 10, "name", "asc");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }
}
