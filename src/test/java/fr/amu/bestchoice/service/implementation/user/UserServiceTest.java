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
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@univ-amu.fr");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.ETUDIANT);
        user.setActive(true);

        registerRequest = new RegisterRequest(
                "John", "Doe", "test@univ-amu.fr", "password123", "12345678", Role.ETUDIANT
        );

        userResponse = new UserResponse(
                1L, "12345678", "John", "Doe", "test@univ-amu.fr", true, Role.ETUDIANT, LocalDateTime.now()
        );
    }

    @Test
    void register_ShouldReturnRegisterResponse_WhenValidRequest() {
        // Given
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(userRepository.existsByStudentNumber(registerRequest.studentNumber())).thenReturn(false);
        when(userMapper.toEntity(registerRequest)).thenReturn(user);
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        RegisterResponse result = userService.register(registerRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(registerRequest.email());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrowBusinessException_WhenEmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("existe déjà");
    }

    @Test
    void update_ShouldReturnUserResponse_WhenValidRequest() {
        // Given
        Long userId = 1L;
        UserUpdateRequest updateRequest = new UserUpdateRequest("Jane", "Doe", "jane@univ-amu.fr", "12345678");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(updateRequest.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(any())).thenReturn(userResponse);

        // When
        UserResponse result = userService.update(userId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(userMapper).updateEntityFromDto(eq(updateRequest), eq(user));
        verify(userRepository).save(user);
    }

    @Test
    void findById_ShouldReturnUserResponse_WhenExists() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // When
        UserResponse result = userService.findById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
    }

    @Test
    void deactivate_ShouldSetUserInactive() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.deactivate(userId);

        // Then
        assertThat(user.getActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void activate_ShouldSetUserActive() {
        // Given
        Long userId = 1L;
        user.setActive(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.activate(userId);

        // Then
        assertThat(user.getActive()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void findAll_ShouldReturnPageOfUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toResponse(any())).thenReturn(userResponse);

        // When
        Page<UserResponse> result = userService.findAll(0, 10, "id", "asc");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }
}
