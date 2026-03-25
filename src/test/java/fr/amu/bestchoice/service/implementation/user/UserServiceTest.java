package fr.amu.bestchoice.service.implementation.user;

import fr.amu.bestchoice.model.entity.User;
import fr.amu.bestchoice.model.enums.Role;
import fr.amu.bestchoice.repository.UserRepository;
import fr.amu.bestchoice.web.dto.auth.RegisterRequest;
import fr.amu.bestchoice.web.dto.auth.RegisterResponse;
import fr.amu.bestchoice.web.dto.user.UserResponse;
import fr.amu.bestchoice.web.dto.user.UserUpdateRequest;
import fr.amu.bestchoice.web.exception.BusinessException;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private RegisterRequest registerRequest;
    private UserUpdateRequest updateRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@univ-amu.fr");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPasswordHash("hashedPassword");
        user.setRole(Role.ETUDIANT);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        registerRequest = new RegisterRequest(
                "John", "Doe", "test@univ-amu.fr", "password123", "20210001", Role.ETUDIANT
        );

        updateRequest = new UserUpdateRequest(
                "John", "Doe", "updated@univ-amu.fr", "20210001"
        );

        userResponse = new UserResponse(
                1L, "20210001", "John", "Doe", "test@univ-amu.fr", true, Role.ETUDIANT, LocalDateTime.now()
        );
    }

    @Test
    void register_ShouldReturnRegisterResponse_WhenEmailAndStudentNumberAreUnique() {
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(userRepository.existsByStudentNumber(registerRequest.studentNumber())).thenReturn(false);
        when(userMapper.toEntity(registerRequest)).thenReturn(user);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        RegisterResponse result = userService.register(registerRequest);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(user.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrowBusinessException_WhenEmailExists() {
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("existe déjà");
    }

    @Test
    void update_ShouldReturnUpdatedUserResponse_WhenUserExists() {
        Long id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(updateRequest.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.update(id, updateRequest);

        assertThat(result).isNotNull();
        verify(userMapper).updateEntityFromDto(updateRequest, user);
        verify(userRepository).save(user);
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        Long id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(id, updateRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Utilisateur introuvable");
    }

    @Test
    void findById_ShouldReturnUserResponse_WhenUserExists() {
        Long id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.findById(id);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
    }

    @Test
    void findAll_WithPagination_ShouldReturnPageOfUserResponses() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        Page<UserResponse> result = userService.findAll(0, 10, "id", "ASC");

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findAll_List_ShouldReturnListOfUserResponses() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toResponseList(anyList())).thenReturn(List.of(userResponse));

        List<UserResponse> result = userService.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void findAllActive_ShouldReturnListOfActiveUserResponses() {
        when(userRepository.findByActiveTrue()).thenReturn(List.of(user));
        when(userMapper.toResponseList(anyList())).thenReturn(List.of(userResponse));

        List<UserResponse> result = userService.findAllActive();

        assertThat(result).hasSize(1);
    }

    @Test
    void deactivate_ShouldSetUserInactive_WhenUserExists() {
        Long id = 1L;
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.deactivate(id);

        assertThat(user.getActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void activate_ShouldSetUserActive_WhenUserExists() {
        Long id = 1L;
        user.setActive(false);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.activate(id);

        assertThat(user.getActive()).isTrue();
        verify(userRepository).save(user);
    }
}
