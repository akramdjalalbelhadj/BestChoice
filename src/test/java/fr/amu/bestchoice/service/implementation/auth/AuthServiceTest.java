package fr.amu.bestchoice.service.implementation.auth;

import fr.amu.bestchoice.model.entity.User;
import fr.amu.bestchoice.model.enums.Role;
import fr.amu.bestchoice.repository.UserRepository;
import fr.amu.bestchoice.security.jwt.JwtService;
import fr.amu.bestchoice.web.dto.auth.LoginRequest;
import fr.amu.bestchoice.web.dto.auth.LoginResponse;
import fr.amu.bestchoice.web.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private User user;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@univ-amu.fr");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.ETUDIANT);

        loginRequest = new LoginRequest("test@univ-amu.fr", "password");
    }

    @Test
    void login_ShouldReturnLoginResponse_WhenCredentialsAreValid() {
        // Given
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        // When
        LoginResponse result = authService.login(loginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("jwt-token");
        assertThat(result.email()).isEqualTo(user.getEmail());
    }

    @Test
    void login_ShouldThrowUnauthorizedException_WhenCredentialsAreInvalid() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Email ou mot de passe incorrect");
    }

    @Test
    void login_ShouldThrowUnauthorizedException_WhenUserNotFoundInDb() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Utilisateur non trouvé");
    }
}
