package fr.amu.bestchoice.service.implementation.user;

import fr.amu.bestchoice.model.entity.Keyword;
import fr.amu.bestchoice.model.entity.Skill;
import fr.amu.bestchoice.model.entity.Student;
import fr.amu.bestchoice.model.entity.User;
import fr.amu.bestchoice.repository.KeywordRepository;
import fr.amu.bestchoice.repository.SkillRepository;
import fr.amu.bestchoice.repository.StudentRepository;
import fr.amu.bestchoice.repository.UserRepository;
import fr.amu.bestchoice.web.dto.student.StudentCreateRequest;
import fr.amu.bestchoice.web.dto.student.StudentResponse;
import fr.amu.bestchoice.web.dto.student.StudentUpdateRequest;
import fr.amu.bestchoice.web.exception.BusinessException;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.StudentMapper;
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
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private KeywordRepository keywordRepository;
    @Mock
    private StudentMapper studentMapper;

    @InjectMocks
    private StudentService studentService;

    private User user;
    private Student student;
    private StudentCreateRequest createRequest;
    private StudentResponse studentResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@univ-amu.fr");

        student = new Student();
        student.setId(1L);
        student.setUser(user);

        createRequest = new StudentCreateRequest(
                3, "Informatique", "Génie Logiciel",
                new HashSet<>(), Set.of("Java"), Set.of("Web"),
                "url", "url", "url"
        );

        studentResponse = new StudentResponse(
                1L, 1L, "test@univ-amu.fr", "John", "Doe", "123456",
                3, new HashSet<>(), Set.of("Java"), Set.of("Web"),
                "url", "url", null
        );
    }

    @Test
    void create_ShouldReturnStudentResponse_WhenValidRequest() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(studentRepository.existsById(userId)).thenReturn(false);
        when(studentMapper.toEntity(createRequest)).thenReturn(student);
        
        Skill skill = new Skill();
        skill.setName("Java");
        when(skillRepository.findByName("Java")).thenReturn(Optional.of(skill));
        
        Keyword keyword = new Keyword();
        keyword.setLabel("Web");
        when(keywordRepository.findByLabel("Web")).thenReturn(Optional.of(keyword));
        
        when(studentRepository.save(any(Student.class))).thenReturn(student);
        when(studentMapper.toResponse(any())).thenReturn(studentResponse);

        // When
        StudentResponse result = studentService.create(userId, createRequest);

        // Then
        assertThat(result).isNotNull();
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> studentService.create(userId, createRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_ShouldThrowBusinessException_WhenStudentProfileAlreadyExists() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(studentRepository.existsById(userId)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> studentService.create(userId, createRequest))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void update_ShouldReturnUpdatedStudentResponse_WhenValidRequest() {
        // Given
        Long studentId = 1L;
        StudentUpdateRequest updateRequest = new StudentUpdateRequest(
                3, null, null, null, null, null, null
        );
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenReturn(student);
        when(studentMapper.toResponse(any())).thenReturn(studentResponse);

        // When
        StudentResponse result = studentService.update(studentId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(studentMapper).updateEntityFromDto(eq(updateRequest), any(Student.class));
        verify(studentRepository).save(student);
    }

    @Test
    void findById_ShouldReturnStudentResponse_WhenExists() {
        // Given
        Long studentId = 1L;
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentMapper.toResponse(any())).thenReturn(studentResponse);

        // When
        StudentResponse result = studentService.findById(studentId);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void findById_ShouldThrowNotFoundException_WhenDoesNotExist() {
        // Given
        Long studentId = 1L;
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> studentService.findById(studentId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findAll_ShouldReturnPageOfStudents() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Student> studentPage = new PageImpl<>(List.of(student));
        when(studentRepository.findAll(any(Pageable.class))).thenReturn(studentPage);
        when(studentMapper.toResponse(any())).thenReturn(studentResponse);

        // When
        Page<StudentResponse> result = studentService.findAll(0, 10, "id", "asc");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }
}
