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

import java.util.Collections;
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
    private SkillUpdateRequest updateRequest;
    private SkillResponse skillResponse;

    @BeforeEach
    void setUp() {
        skill = new Skill();
        skill.setId(1L);
        skill.setName("Java");
        skill.setDescription("Java programming");
        skill.setCategory("Development");
        skill.setActive(true);

        createRequest = new SkillCreateRequest("Java", "Java programming", "Development", 1);
        updateRequest = new SkillUpdateRequest("Java 17", "Java 17 programming", "Development", 2, true);
        skillResponse = new SkillResponse(1L, "Java", "Java programming", "Development", 1, true);
    }

    @Test
    void create_ShouldReturnSkillResponse_WhenNameDoesNotExist() {
        when(skillRepository.existsByName(createRequest.name())).thenReturn(false);
        when(skillMapper.toEntity(createRequest)).thenReturn(skill);
        when(skillRepository.save(any(Skill.class))).thenReturn(skill);
        when(skillMapper.toResponse(skill)).thenReturn(skillResponse);

        SkillResponse result = skillService.create(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Java");
        verify(skillRepository).existsByName(createRequest.name());
        verify(skillRepository).save(any(Skill.class));
    }

    @Test
    void create_ShouldThrowBusinessException_WhenNameExists() {
        when(skillRepository.existsByName(createRequest.name())).thenReturn(true);

        assertThatThrownBy(() -> skillService.create(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("existe déjà");
    }

    @Test
    void update_ShouldReturnSkillResponse_WhenSkillExists() {
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(skillRepository.existsByName(updateRequest.name())).thenReturn(false);
        when(skillRepository.save(any(Skill.class))).thenReturn(skill);
        when(skillMapper.toResponse(skill)).thenReturn(skillResponse);

        SkillResponse result = skillService.update(1L, updateRequest);

        assertThat(result).isNotNull();
        verify(skillMapper).updateEntityFromDto(updateRequest, skill);
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenSkillDoesNotExist() {
        when(skillRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.update(1L, updateRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findById_ShouldReturnSkillResponse_WhenSkillExists() {
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(skillMapper.toResponse(skill)).thenReturn(skillResponse);

        SkillResponse result = skillService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void findAll_Paginated_ShouldReturnPageOfSkillResponse() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        Page<Skill> skillPage = new PageImpl<>(Collections.singletonList(skill));
        when(skillRepository.findAll(pageable)).thenReturn(skillPage);
        when(skillMapper.toResponse(skill)).thenReturn(skillResponse);

        Page<SkillResponse> result = skillService.findAll(0, 10, "name", "ASC");

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void delete_ShouldCallRepositoryDelete_WhenSkillExists() {
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));

        skillService.delete(1L);

        verify(skillRepository).delete(skill);
    }

    @Test
    void deactivate_ShouldSetSkillInactive_WhenSkillExistsAndIsActive() {
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));

        skillService.deactivate(1L);

        assertThat(skill.getActive()).isFalse();
        verify(skillRepository).save(skill);
    }
}
