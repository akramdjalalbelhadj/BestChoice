package fr.amu.bestchoice.service.implementation.auth;

import fr.amu.bestchoice.model.entity.Student;
import fr.amu.bestchoice.model.entity.Teacher;
import fr.amu.bestchoice.model.entity.User;
import fr.amu.bestchoice.model.enums.Role;
import fr.amu.bestchoice.repository.StudentRepository;
import fr.amu.bestchoice.repository.TeacherRepository;
import fr.amu.bestchoice.repository.UserRepository;
import fr.amu.bestchoice.web.dto.auth.RegisterRequest;
import fr.amu.bestchoice.web.dto.auth.RegisterResponse;
import fr.amu.bestchoice.web.exception.BusinessException;
import fr.amu.bestchoice.web.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterService registerService;

    private User user;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@univ-amu.fr");
        user.setFirstName("John");
        user.setLastName("Doe");

        registerRequest = new RegisterRequest(
                "John", "Doe", "test@univ-amu.fr", "password", "12345678", Role.ETUDIANT
        );
    }

    @Test
    void register_ShouldCreateStudentProfile_WhenRoleIsEtudiant() {
        // Given
        user.setRole(Role.ETUDIANT);
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(userRepository.existsByStudentNumber(registerRequest.studentNumber())).thenReturn(false);
        when(userMapper.toEntity(registerRequest)).thenReturn(user);
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        RegisterResponse result = registerService.register(registerRequest);

        // Then
        assertThat(result).isNotNull();
        verify(studentRepository).save(any(Student.class));
        verify(teacherRepository, never()).save(any(Teacher.class));
    }

    @Test
    void register_ShouldCreateTeacherProfile_WhenRoleIsEnseignant() {
        // Given
        RegisterRequest teacherRequest = new RegisterRequest(
                "Jane", "Doe", "jane@univ-amu.fr", "password", null, Role.ENSEIGNANT
        );
        user.setRole(Role.ENSEIGNANT);
        when(userRepository.existsByEmail(teacherRequest.email())).thenReturn(false);
        when(userMapper.toEntity(teacherRequest)).thenReturn(user);
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        registerService.register(teacherRequest);

        // Then
        verify(teacherRepository).save(any(Teacher.class));
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void register_ShouldNotCreateProfile_WhenRoleIsAdmin() {
        // Given
        RegisterRequest adminRequest = new RegisterRequest(
                "Admin", "User", "admin@univ-amu.fr", "password", null, Role.ADMIN
        );
        user.setRole(Role.ADMIN);
        when(userRepository.existsByEmail(adminRequest.email())).thenReturn(false);
        when(userMapper.toEntity(adminRequest)).thenReturn(user);
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        registerService.register(adminRequest);

        // Then
        verify(studentRepository, never()).save(any(Student.class));
        verify(teacherRepository, never()).save(any(Teacher.class));
    }

    @Test
    void register_ShouldThrowBusinessException_WhenEmailExists() {
        // Given
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> registerService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("existe déjà");
    }
}
