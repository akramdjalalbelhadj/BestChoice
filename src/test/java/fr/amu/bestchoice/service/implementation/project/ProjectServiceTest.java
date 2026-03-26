package fr.amu.bestchoice.service.implementation.project;

import fr.amu.bestchoice.model.entity.*;
import fr.amu.bestchoice.model.enums.WorkType;
import fr.amu.bestchoice.repository.KeywordRepository;
import fr.amu.bestchoice.repository.ProjectRepository;
import fr.amu.bestchoice.repository.SkillRepository;
import fr.amu.bestchoice.repository.TeacherRepository;
import fr.amu.bestchoice.web.dto.project.ProjectCreateRequest;
import fr.amu.bestchoice.web.dto.project.ProjectResponse;
import fr.amu.bestchoice.web.dto.project.ProjectUpdateRequest;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.ProjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private KeywordRepository keywordRepository;
    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    private Teacher teacher;
    private Project project;
    private ProjectCreateRequest createRequest;
    private ProjectResponse projectResponse;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("teacher@test.com");

        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setUser(user);

        project = new Project();
        project.setId(1L);
        project.setTeacher(teacher);
        project.setRequiredSkills(new HashSet<>());
        project.setKeywords(new HashSet<>());
        project.setAssignedStudents(new ArrayList<>());
        project.setActive(true);
        project.setComplet(false);

        createRequest = new ProjectCreateRequest(
                "Titre", "Description", Set.of(WorkType.DEVELOPPEMENT), true, 1, 2, null, null, null, null, Set.of("Java"), Set.of("Web")
        );

        projectResponse = new ProjectResponse(
                1L, "Titre", "Description", Set.of(WorkType.DEVELOPPEMENT), true, true, 1, 2, false, null, null, null, null, 1L, "Teacher", Set.of("Java"), Set.of("Web"), new HashSet<>()
        );
    }

    @Test
    void create_ShouldReturnProjectResponse_WhenValidRequest() {
        // Given
        Long teacherId = 1L;
        when(teacherRepository.findByUserId(teacherId)).thenReturn(Optional.of(teacher));
        when(projectMapper.toEntity(createRequest)).thenReturn(project);
        
        Skill skill = new Skill();
        skill.setName("Java");
        when(skillRepository.findByName("Java")).thenReturn(Optional.of(skill));
        
        Keyword keyword = new Keyword();
        keyword.setLabel("Web");
        when(keywordRepository.findByLabel("Web")).thenReturn(Optional.of(keyword));
        
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toResponse(any())).thenReturn(projectResponse);

        // When
        ProjectResponse result = projectService.create(teacherId, createRequest);

        // Then
        assertThat(result).isNotNull();
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenTeacherDoesNotExist() {
        // Given
        Long teacherId = 1L;
        when(teacherRepository.findByUserId(teacherId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectService.create(teacherId, createRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_ShouldReturnUpdatedProjectResponse_WhenValidRequest() {
        // Given
        Long projectId = 1L;
        ProjectUpdateRequest updateRequest = new ProjectUpdateRequest(
                "New Title", null, null, null, null, null, null, null, null
        );
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toResponse(any())).thenReturn(projectResponse);

        // When
        ProjectResponse result = projectService.update(projectId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(projectMapper).updateEntityFromDto(eq(updateRequest), eq(project));
    }

    @Test
    void findById_ShouldReturnProjectResponse_WhenExists() {
        // Given
        Long projectId = 1L;
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectMapper.toResponse(any())).thenReturn(projectResponse);

        // When
        ProjectResponse result = projectService.findById(projectId);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void findAll_ShouldReturnPageOfProjects() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Project> projectPage = new PageImpl<>(List.of(project));
        when(projectRepository.findAll(any(Pageable.class))).thenReturn(projectPage);
        when(projectMapper.toResponse(any())).thenReturn(projectResponse);

        // When
        Page<ProjectResponse> result = projectService.findAll(0, 10, "id", "asc");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void activate_ShouldSetProjectActive() {
        // Given
        Long projectId = 1L;
        project.setActive(false);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // When
        projectService.activate(projectId);

        // Then
        assertThat(project.getActive()).isTrue();
        verify(projectRepository).save(project);
    }

    @Test
    void deactivate_ShouldSetProjectInactive() {
        // Given
        Long projectId = 1L;
        project.setActive(true);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        // When
        projectService.deactivate(projectId);

        // Then
        assertThat(project.getActive()).isFalse();
        verify(projectRepository).save(project);
    }
}
