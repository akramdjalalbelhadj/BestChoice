package fr.amu.bestchoice.service.implementation.project;

import fr.amu.bestchoice.model.entity.Keyword;
import fr.amu.bestchoice.model.entity.Project;
import fr.amu.bestchoice.model.entity.Skill;
import fr.amu.bestchoice.model.entity.Teacher;
import fr.amu.bestchoice.model.entity.User;
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
    private ProjectUpdateRequest updateRequest;
    private ProjectResponse projectResponse;
    private Skill skill;
    private Keyword keyword;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");

        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setUser(user);

        project = new Project();
        project.setId(1L);
        project.setTitle("Test Project");
        project.setTeacher(teacher);
        project.setWorkTypes(Set.of(WorkType.DEVELOPPEMENT));
        project.setRequiredSkills(new HashSet<>());
        project.setKeywords(new HashSet<>());
        project.setAssignedStudents(new ArrayList<>());

        skill = new Skill();
        skill.setName("Java");

        keyword = new Keyword();
        keyword.setLabel("IA");

        createRequest = new ProjectCreateRequest(
                "Test Project", "Description", Set.of(WorkType.DEVELOPPEMENT),
                true, 1, 3, 6, 1, "2024-2025", "Master Info",
                Set.of("Java"), Set.of("IA")
        );

        updateRequest = new ProjectUpdateRequest(
                "Updated Title", "Updated Description", Set.of(WorkType.DEVELOPPEMENT),
                true, 1, 3, true, Set.of("Java"), Set.of("IA")
        );

        projectResponse = new ProjectResponse(
                1L, "Test Project", "Description", Set.of(WorkType.DEVELOPPEMENT),
                true, true, 1, 3, false, 6, 1, "2024-2025", "Master Info",
                1L, "John Doe", Set.of("Java"), Set.of("IA"), Collections.emptySet()
        );
    }

    @Test
    void create_ShouldReturnProjectResponse_WhenTeacherExists() {
        Long teacherId = 1L;
        when(teacherRepository.findByUserId(teacherId)).thenReturn(Optional.of(teacher));
        when(projectMapper.toEntity(createRequest)).thenReturn(project);
        when(skillRepository.findByName("Java")).thenReturn(Optional.of(skill));
        when(keywordRepository.findByLabel("IA")).thenReturn(Optional.of(keyword));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toResponse(any(Project.class))).thenReturn(projectResponse);

        ProjectResponse result = projectService.create(teacherId, createRequest);

        assertThat(result).isNotNull();
        verify(teacherRepository).findByUserId(teacherId);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenTeacherDoesNotExist() {
        Long teacherId = 1L;
        when(teacherRepository.findByUserId(teacherId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.create(teacherId, createRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Enseignant introuvable");
    }

    @Test
    void update_ShouldReturnUpdatedProjectResponse_WhenProjectExists() {
        Long id = 1L;
        when(projectRepository.findById(id)).thenReturn(Optional.of(project));
        when(skillRepository.findByName("Java")).thenReturn(Optional.of(skill));
        when(keywordRepository.findByLabel("IA")).thenReturn(Optional.of(keyword));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toResponse(any(Project.class))).thenReturn(projectResponse);

        ProjectResponse result = projectService.update(id, updateRequest);

        assertThat(result).isNotNull();
        verify(projectRepository).findById(id);
        verify(projectMapper).updateEntityFromDto(updateRequest, project);
    }

    @Test
    void findById_ShouldReturnProjectResponse_WhenProjectExists() {
        Long id = 1L;
        when(projectRepository.findById(id)).thenReturn(Optional.of(project));
        when(projectMapper.toResponse(project)).thenReturn(projectResponse);

        ProjectResponse result = projectService.findById(id);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
    }

    @Test
    void findAll_WithPagination_ShouldReturnPageOfProjectResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> projectPage = new PageImpl<>(List.of(project));
        when(projectRepository.findAll(any(Pageable.class))).thenReturn(projectPage);
        when(projectMapper.toResponse(project)).thenReturn(projectResponse);

        Page<ProjectResponse> result = projectService.findAll(0, 10, "id", "ASC");

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findByTeacherId_ShouldReturnListOfProjectResponses() {
        Long teacherId = 1L;
        when(projectRepository.findByTeacherId(teacherId)).thenReturn(List.of(project));
        when(projectMapper.toResponse(project)).thenReturn(projectResponse);

        List<ProjectResponse> result = projectService.findByTeacherId(teacherId);

        assertThat(result).hasSize(1);
    }

    @Test
    void activate_ShouldSetProjectActive() {
        Long id = 1L;
        project.setActive(false);
        when(projectRepository.findById(id)).thenReturn(Optional.of(project));

        projectService.activate(id);

        assertThat(project.getActive()).isTrue();
        verify(projectRepository).save(project);
    }

    @Test
    void deactivate_ShouldSetProjectInactive() {
        Long id = 1L;
        project.setActive(true);
        when(projectRepository.findById(id)).thenReturn(Optional.of(project));

        projectService.deactivate(id);

        assertThat(project.getActive()).isFalse();
        verify(projectRepository).save(project);
    }
}
