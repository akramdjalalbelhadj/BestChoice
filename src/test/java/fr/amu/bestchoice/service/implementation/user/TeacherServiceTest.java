package fr.amu.bestchoice.service.implementation.user;

import fr.amu.bestchoice.model.entity.Project;
import fr.amu.bestchoice.model.entity.Teacher;
import fr.amu.bestchoice.model.entity.User;
import fr.amu.bestchoice.repository.TeacherRepository;
import fr.amu.bestchoice.repository.UserRepository;
import fr.amu.bestchoice.web.dto.teacher.TeacherCreateRequest;
import fr.amu.bestchoice.web.dto.teacher.TeacherResponse;
import fr.amu.bestchoice.web.dto.teacher.TeacherUpdateRequest;
import fr.amu.bestchoice.web.exception.BusinessException;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.TeacherMapper;
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
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeacherMapper teacherMapper;

    @InjectMocks
    private TeacherService teacherService;

    private User user;
    private Teacher teacher;
    private TeacherCreateRequest createRequest;
    private TeacherUpdateRequest updateRequest;
    private TeacherResponse teacherResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("teacher@univ-amu.fr");
        user.setFirstName("John");
        user.setLastName("Doe");

        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setUser(user);
        teacher.setDepartment("Informatique");
        teacher.setProjects(new ArrayList<>());

        createRequest = new TeacherCreateRequest("Informatique", "Maitre de Conférences", "IA", "http://john-doe.fr");
        updateRequest = new TeacherUpdateRequest("Informatique", "Professeur", "Data Science", "http://john-doe-updated.fr");
        
        teacherResponse = new TeacherResponse(
                1L, 1L, "teacher@univ-amu.fr", "John", "Doe",
                "Informatique", "Maitre de Conférences", "IA", "http://john-doe.fr",
                Collections.emptySet()
        );
    }

    @Test
    void create_ShouldReturnTeacherResponse_WhenUserExistsAndProfileDoesNotExist() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(teacherRepository.existsById(userId)).thenReturn(false);
        when(teacherMapper.toEntity(createRequest)).thenReturn(teacher);
        when(teacherRepository.save(any(Teacher.class))).thenReturn(teacher);
        when(teacherMapper.toResponse(teacher)).thenReturn(teacherResponse);

        // When
        TeacherResponse result = teacherService.create(userId, createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        verify(userRepository).findById(userId);
        verify(teacherRepository).existsById(userId);
        verify(teacherRepository).save(any(Teacher.class));
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> teacherService.create(userId, createRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Utilisateur introuvable avec l'ID : " + userId);
        
        verify(teacherRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowBusinessException_WhenTeacherProfileAlreadyExists() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(teacherRepository.existsById(userId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> teacherService.create(userId, createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cet utilisateur a déjà un profil enseignant");

        verify(teacherRepository, never()).save(any());
    }

    @Test
    void update_ShouldReturnUpdatedTeacherResponse_WhenTeacherExists() {
        // Given
        Long teacherId = 1L;
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(teacherRepository.save(any(Teacher.class))).thenReturn(teacher);
        when(teacherMapper.toResponse(teacher)).thenReturn(teacherResponse);

        // When
        TeacherResponse result = teacherService.update(teacherId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(teacherMapper).updateEntityFromDto(updateRequest, teacher);
        verify(teacherRepository).save(teacher);
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenTeacherDoesNotExist() {
        // Given
        Long teacherId = 1L;
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> teacherService.update(teacherId, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Profil enseignant introuvable avec l'ID : " + teacherId);

        verify(teacherRepository, never()).save(any());
    }

    @Test
    void findById_ShouldReturnTeacherResponse_WhenTeacherExists() {
        // Given
        Long teacherId = 1L;
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(teacherMapper.toResponse(teacher)).thenReturn(teacherResponse);

        // When
        TeacherResponse result = teacherService.findById(teacherId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(teacherId);
        verify(teacherRepository).findById(teacherId);
    }

    @Test
    void findById_ShouldThrowNotFoundException_WhenTeacherDoesNotExist() {
        // Given
        Long teacherId = 1L;
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> teacherService.findById(teacherId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Profil enseignant introuvable avec l'ID : " + teacherId);
    }

    @Test
    void findAll_Paginated_ShouldReturnPageOfTeacherResponses() {
        // Given
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String sortDirection = "ASC";
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sortBy));
        
        Page<Teacher> teachersPage = new PageImpl<>(List.of(teacher));
        when(teacherRepository.findAll(any(Pageable.class))).thenReturn(teachersPage);
        when(teacherMapper.toResponse(teacher)).thenReturn(teacherResponse);

        // When
        Page<TeacherResponse> result = teacherService.findAll(page, size, sortBy, sortDirection);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(teacherRepository).findAll(any(Pageable.class));
    }

    @Test
    void findAll_List_ShouldReturnListOfTeacherResponses() {
        // Given
        when(teacherRepository.findAll()).thenReturn(List.of(teacher));
        when(teacherMapper.toResponse(teacher)).thenReturn(teacherResponse);

        // When
        List<TeacherResponse> result = teacherService.findAll();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        verify(teacherRepository).findAll();
    }

    @Test
    void toTeacherResponse_ShouldCorrectlyMapProjects() {
        // Given
        Project project = new Project();
        project.setTitle("AI Project");
        teacher.setProjects(List.of(project));
        
        when(teacherMapper.toResponse(teacher)).thenReturn(teacherResponse);

        // When
        TeacherResponse result = teacherService.toTeacherResponse(teacher);

        // Then
        assertThat(result.project()).containsExactly("AI Project");
    }
}
