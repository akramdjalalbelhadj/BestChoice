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
import static org.mockito.Mockito.*;

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

        loginRequest = new LoginRequest("test@univ-amu.fr", "password123");
    }

    @Test
    void login_ShouldReturnLoginResponse_WhenCredentialsAreValid() {
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(userDetails)).thenReturn("mocked-jwt-token");

        LoginResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("mocked-jwt-token");
        assertThat(response.email()).isEqualTo(user.getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(loginRequest.email());
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void login_ShouldThrowUnauthorizedException_WhenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Email ou mot de passe incorrect");
    }

    @Test
    void login_ShouldThrowUnauthorizedException_WhenUserNotFoundAfterAuth() {
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Utilisateur non trouvé");
    }
}
