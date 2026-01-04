package fr.amu.bestchoice.service.implementation.auth;

import fr.amu.bestchoice.model.entity.User;
import fr.amu.bestchoice.repository.UserRepository;
import fr.amu.bestchoice.security.jwt.JwtService;
import fr.amu.bestchoice.service.interfaces.IAuthService;
import fr.amu.bestchoice.web.dto.auth.LoginRequest;
import fr.amu.bestchoice.web.dto.auth.LoginResponse;
import fr.amu.bestchoice.web.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service de gestion de l'authentification.
 *
 * Responsabilités :
 * - Vérifier l'email et le mot de passe de l'utilisateur
 * - Générer un JWT si l'authentification réussit
 * - Retourner les informations de l'utilisateur avec le token
 *
 * Flux de login :
 * 1. Recevoir LoginRequest (email + password)
 * 2. Vérifier les credentials avec AuthenticationManager
 * 3. Si OK : charger l'utilisateur complet depuis la DB
 * 4. Générer le JWT avec JwtService
 * 5. Retourner LoginResponse (token + infos utilisateur)
 * 6. Si KO : lever une exception UnauthorizedException
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService implements IAuthService {


    /**
     * AuthenticationManager : fourni par Spring Security.
     *
     * Rôle :
     * - Vérifier que l'email existe
     * - Vérifier que le mot de passe correspond au hash en base
     * - Utilise CustomUserDetailsService + PasswordEncoder
     */
    private final AuthenticationManager authenticationManager;

    /**
     * JwtService : service personnalisé pour générer les JWT.
     */
    private final JwtService jwtService;

    /**
     * UserRepository : pour charger l'utilisateur complet depuis la DB.
     */
    private final UserRepository userRepository;

    // ==================== LOGIN ====================

    /**
     * Authentifie un utilisateur et génère un JWT.
     *
     * @param request DTO contenant l'email et le mot de passe
     * @return LoginResponse contenant le JWT et les infos utilisateur
     * @throws UnauthorizedException si l'email ou le mot de passe est incorrect
     */
    public LoginResponse login(LoginRequest request) {

        log.info("Tentative de connexion pour l'email : {}", request.email());

        // ==================== ÉTAPE 1 : AUTHENTIFICATION ====================

        try {
            // Créer un token d'authentification avec l'email et le mot de passe
            // Ce token est utilisé par AuthenticationManager pour vérifier les credentials
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    request.email(),    // Principal = email (utilisé comme username)
                    request.password()  // Credentials = mot de passe en clair
            );

            // Authentifier l'utilisateur
            // AuthenticationManager va :
            // 1. Charger l'utilisateur depuis la DB via CustomUserDetailsService
            // 2. Comparer le mot de passe fourni avec le hash en base via PasswordEncoder
            // 3. Si OK : retourner un objet Authentication
            // 4. Si KO : lever une BadCredentialsException
            Authentication authentication = authenticationManager.authenticate(authToken);

            // Récupérer l'utilisateur authentifié (UserDetails)
            // Cet objet contient : email, password hash, rôles, etc.
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            log.info("Authentification réussie pour l'email : {}", request.email());

            // ==================== ÉTAPE 2 : CHARGER L'UTILISATEUR COMPLET ====================

            // Charger l'entité User complète depuis la base de données
            // On a besoin de l'entité complète (pas juste UserDetails) pour :
            // - Récupérer l'ID
            // - Récupérer le prénom et le nom
            // - Récupérer les rôles sous forme d'enum (pas GrantedAuthority)
            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new UnauthorizedException("Utilisateur non trouvé"));

            // ==================== ÉTAPE 3 : GÉNÉRER LE JWT ====================

            // Générer le token JWT
            // Le JWT contient : email, rôles, date d'expiration
            String jwt = jwtService.generateToken(userDetails);

            log.info("JWT généré avec succès pour l'utilisateur ID : {}", user.getId());

            // ==================== ÉTAPE 4 : CONSTRUIRE LA RÉPONSE ====================

            // Créer le LoginResponse avec :
            // - Le JWT (accessToken)
            // - Le type de token (Bearer)
            // - Les informations de l'utilisateur (ID, email, nom, prénom, rôles)
            // - La durée de validité du token
            return new LoginResponse(
                    jwt,                        // Token JWT
                    "Bearer",                   // Type de token (standard OAuth2)
                    user.getId(),               // ID de l'utilisateur
                    user.getEmail(),            // Email
                    user.getFirstName(),        // Prénom
                    user.getLastName(),         // Nom
                    user.getRoles(),            // Rôles (Set<Role>)
                    3600000L                    // Expiration en ms (1 heure)
            );

        } catch (BadCredentialsException e) {
            // Si l'email ou le mot de passe est incorrect
            // BadCredentialsException est levée par AuthenticationManager

            log.warn("Échec de connexion pour l'email : {} - Credentials invalides", request.email());

            // Lever une exception personnalisée
            // Cette exception sera interceptée par ApiExceptionHandler
            // et transformée en réponse HTTP 401 Unauthorized
            throw new UnauthorizedException("Email ou mot de passe incorrect");
        }
    }
}