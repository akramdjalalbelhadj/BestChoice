package fr.amu.bestchoice.service.implementation.user;

import fr.amu.bestchoice.model.entity.Keyword;
import fr.amu.bestchoice.model.entity.Skill;
import fr.amu.bestchoice.model.entity.Student;
import fr.amu.bestchoice.model.entity.User;
import fr.amu.bestchoice.model.enums.WorkType;
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
import jakarta.persistence.EntityNotFoundException;
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
    private StudentUpdateRequest updateRequest;
    private StudentResponse studentResponse;
    private Skill skill;
    private Keyword keyword;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("student@univ-amu.fr");
        user.setFirstName("Jane");
        user.setLastName("Doe");

        student = new Student();
        student.setId(1L);
        student.setUser(user);
        student.setProgram("Master Informatique");
        student.setStudyYear(1);
        student.setPreferredWorkTypes(Set.of(WorkType.DEVELOPPEMENT));
        student.setSkills(new HashSet<>());
        student.setInterests(new HashSet<>());

        skill = new Skill();
        skill.setId(1L);
        skill.setName("Java");

        keyword = new Keyword();
        keyword.setId(1L);
        keyword.setLabel("IA");

        createRequest = new StudentCreateRequest(
                1, "Master Informatique", "IAAA",
                Set.of(WorkType.DEVELOPPEMENT), Set.of("Java"), Set.of("IA"),
                "github.com/jane", "portfolio.com/jane", "linkedin.com/in/jane"
        );

        updateRequest = new StudentUpdateRequest(
                2, Set.of(WorkType.TEST), Set.of("Java"), Set.of("IA"),
                "github.com/jane-updated", "portfolio.com/jane-updated", "linkedin.com/in/jane-updated"
        );

        studentResponse = new StudentResponse(
                1L, 1L, "student@univ-amu.fr", "Jane", "Doe", "20210001",
                1, Set.of(WorkType.DEVELOPPEMENT), Set.of("Java"), Set.of("IA"),
                "github.com/jane", "linkedin.com/in/jane", null
        );
    }

    @Test
    void create_ShouldReturnStudentResponse_WhenUserExistsAndProfileDoesNotExist() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(studentRepository.existsById(userId)).thenReturn(false);
        when(studentMapper.toEntity(createRequest)).thenReturn(student);
        when(skillRepository.findByName("Java")).thenReturn(Optional.of(skill));
        when(keywordRepository.findByLabel("IA")).thenReturn(Optional.of(keyword));
        when(studentRepository.save(any(Student.class))).thenReturn(student);
        when(studentMapper.toResponse(any(Student.class))).thenReturn(studentResponse);

        StudentResponse result = studentService.create(userId, createRequest);

        assertThat(result).isNotNull();
        verify(userRepository).findById(userId);
        verify(studentRepository).existsById(userId);
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.create(userId, createRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Utilisateur introuvable");
    }

    @Test
    void create_ShouldThrowBusinessException_WhenProfileAlreadyExists() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(studentRepository.existsById(userId)).thenReturn(true);

        assertThatThrownBy(() -> studentService.create(userId, createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("déjà un profil étudiant");
    }

    @Test
    void update_ShouldReturnUpdatedStudentResponse_WhenProfileExists() {
        Long id = 1L;
        when(studentRepository.findById(id)).thenReturn(Optional.of(student));
        when(skillRepository.findByName("Java")).thenReturn(Optional.of(skill));
        when(keywordRepository.findByLabel("IA")).thenReturn(Optional.of(keyword));
        when(studentRepository.save(any(Student.class))).thenReturn(student);
        when(studentMapper.toResponse(any(Student.class))).thenReturn(studentResponse);

        StudentResponse result = studentService.update(id, updateRequest);

        assertThat(result).isNotNull();
        verify(studentRepository).findById(id);
        verify(studentMapper).updateEntityFromDto(updateRequest, student);
        verify(studentRepository).save(student);
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenProfileDoesNotExist() {
        Long id = 1L;
        when(studentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.update(id, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Profil étudiant introuvable");
    }

    @Test
    void findById_ShouldReturnStudentResponse_WhenProfileExists() {
        Long id = 1L;
        when(studentRepository.findById(id)).thenReturn(Optional.of(student));
        when(studentMapper.toResponse(student)).thenReturn(studentResponse);

        StudentResponse result = studentService.findById(id);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
    }

    @Test
    void findById_ShouldThrowNotFoundException_WhenProfileDoesNotExist() {
        Long id = 1L;
        when(studentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.findById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Profil étudiant introuvable");
    }

    @Test
    void findAll_WithPagination_ShouldReturnPageOfStudentResponses() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<Student> studentPage = new PageImpl<>(List.of(student));
        when(studentRepository.findAll(any(Pageable.class))).thenReturn(studentPage);
        when(studentMapper.toResponse(student)).thenReturn(studentResponse);

        Page<StudentResponse> result = studentService.findAll(0, 10, "id", "ASC");

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findAll_List_ShouldReturnListOfStudentResponses() {
        when(studentRepository.findAll()).thenReturn(List.of(student));
        when(studentMapper.toResponse(student)).thenReturn(studentResponse);

        List<StudentResponse> result = studentService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void findAllComplete_ShouldReturnListOfCompleteStudentResponses() {
        when(studentRepository.findByProfileCompleteTrue()).thenReturn(List.of(student));
        when(studentMapper.toResponse(student)).thenReturn(studentResponse);

        List<StudentResponse> result = studentService.findAllComplete();

        assertThat(result).hasSize(1);
    }

    @Test
    void findByUserId_ShouldReturnStudentResponse_WhenProfileExists() {
        Long userId = 1L;
        when(studentRepository.findByUserId(userId)).thenReturn(Optional.of(student));
        when(studentMapper.toResponse(student)).thenReturn(studentResponse);

        StudentResponse result = studentService.findByUserId(userId);

        assertThat(result).isNotNull();
    }

    @Test
    void findByUserId_ShouldThrowEntityNotFoundException_WhenProfileDoesNotExist() {
        Long userId = 1L;
        when(studentRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.findByUserId(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Profil étudiant non trouvé");
    }
}
