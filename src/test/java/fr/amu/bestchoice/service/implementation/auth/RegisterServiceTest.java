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

    private RegisterRequest studentRequest;
    private RegisterRequest teacherRequest;
    private User user;

    @BeforeEach
    void setUp() {
        studentRequest = new RegisterRequest("John", "Doe", "john@etu.univ-amu.fr", "password123", "20240001", Role.ETUDIANT);
        teacherRequest = new RegisterRequest("Jane", "Doe", "jane@univ-amu.fr", "password123", null, Role.ENSEIGNANT);
        
        user = new User();
        user.setId(1L);
        user.setEmail("test@univ-amu.fr");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.ETUDIANT);
    }

    @Test
    void register_ShouldCreateStudentProfile_WhenRoleIsEtudiant() {
        when(userRepository.existsByEmail(studentRequest.email())).thenReturn(false);
        when(userRepository.existsByStudentNumber(studentRequest.studentNumber())).thenReturn(false);
        when(userMapper.toEntity(studentRequest)).thenReturn(user);
        when(passwordEncoder.encode(studentRequest.password())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        RegisterResponse response = registerService.register(studentRequest);

        assertThat(response).isNotNull();
        verify(studentRepository).save(any(Student.class));
        verify(teacherRepository, never()).save(any(Teacher.class));
    }

    @Test
    void register_ShouldCreateTeacherProfile_WhenRoleIsEnseignant() {
        user.setRole(Role.ENSEIGNANT);
        when(userRepository.existsByEmail(teacherRequest.email())).thenReturn(false);
        when(userMapper.toEntity(teacherRequest)).thenReturn(user);
        when(passwordEncoder.encode(teacherRequest.password())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        RegisterResponse response = registerService.register(teacherRequest);

        assertThat(response).isNotNull();
        verify(teacherRepository).save(any(Teacher.class));
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void register_ShouldThrowBusinessException_WhenEmailExists() {
        when(userRepository.existsByEmail(studentRequest.email())).thenReturn(true);

        assertThatThrownBy(() -> registerService.register(studentRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("existe déjà");
    }

    @Test
    void register_ShouldThrowBusinessException_WhenStudentNumberExists() {
        when(userRepository.existsByEmail(studentRequest.email())).thenReturn(false);
        when(userRepository.existsByStudentNumber(studentRequest.studentNumber())).thenReturn(true);

        assertThatThrownBy(() -> registerService.register(studentRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("existe déjà");
    }

    @Test
    void register_ShouldNotCreateProfile_WhenRoleIsAdmin() {
        user.setRole(Role.ADMIN);
        RegisterRequest adminRequest = new RegisterRequest("Admin", "User", "admin@univ-amu.fr", "password123", null, Role.ADMIN);
        
        when(userRepository.existsByEmail(adminRequest.email())).thenReturn(false);
        when(userMapper.toEntity(adminRequest)).thenReturn(user);
        when(passwordEncoder.encode(adminRequest.password())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        registerService.register(adminRequest);

        verify(studentRepository, never()).save(any(Student.class));
        verify(teacherRepository, never()).save(any(Teacher.class));
    }
}
