package fr.amu.bestchoice.service.implementation.user;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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
    private TeacherResponse teacherResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("teacher@univ-amu.fr");

        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setUser(user);
        teacher.setProjects(new ArrayList<>());

        createRequest = new TeacherCreateRequest("Informatique", "MCF", "IA", "http://web.com");
        teacherResponse = new TeacherResponse(1L, 1L, "teacher@univ-amu.fr", "John", "Doe", "Informatique", "MCF", "IA", "http://web.com", new HashSet<>());
    }

    @Test
    void create_ShouldReturnTeacherResponse_WhenValidRequest() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(teacherRepository.existsById(userId)).thenReturn(false);
        when(teacherMapper.toEntity(createRequest)).thenReturn(teacher);
        when(teacherRepository.save(any(Teacher.class))).thenReturn(teacher);
        when(teacherMapper.toResponse(any())).thenReturn(teacherResponse);

        // When
        TeacherResponse result = teacherService.create(userId, createRequest);

        // Then
        assertThat(result).isNotNull();
        verify(teacherRepository).save(any(Teacher.class));
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> teacherService.create(userId, createRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_ShouldThrowBusinessException_WhenTeacherProfileAlreadyExists() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(teacherRepository.existsById(userId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> teacherService.create(userId, createRequest))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void update_ShouldReturnUpdatedTeacherResponse_WhenValidRequest() {
        // Given
        Long teacherId = 1L;
        TeacherUpdateRequest updateRequest = new TeacherUpdateRequest("Maths", "PR", "Algèbre", "http://new.com");
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(teacherRepository.save(any(Teacher.class))).thenReturn(teacher);
        when(teacherMapper.toResponse(any())).thenReturn(teacherResponse);

        // When
        TeacherResponse result = teacherService.update(teacherId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(teacherMapper).updateEntityFromDto(eq(updateRequest), eq(teacher));
        verify(teacherRepository).save(teacher);
    }

    @Test
    void findById_ShouldReturnTeacherResponse_WhenExists() {
        // Given
        Long teacherId = 1L;
        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
        when(teacherMapper.toResponse(any())).thenReturn(teacherResponse);

        // When
        TeacherResponse result = teacherService.findById(teacherId);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void findAll_ShouldReturnPageOfTeachers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Teacher> teacherPage = new PageImpl<>(List.of(teacher));
        when(teacherRepository.findAll(any(Pageable.class))).thenReturn(teacherPage);
        when(teacherMapper.toResponse(any())).thenReturn(teacherResponse);

        // When
        Page<TeacherResponse> result = teacherService.findAll(0, 10, "id", "asc");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }
}
