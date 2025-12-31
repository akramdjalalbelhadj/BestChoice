package fr.amu.bestchoice.web.controller;

import fr.amu.bestchoice.security.jwt.JwtService;
import fr.amu.bestchoice.web.dto.auth.LoginRequest;
import fr.amu.bestchoice.web.dto.auth.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller pour l'authentification.
 *
 * Endpoints :
 * - POST /api/auth/login : Connexion et génération du token JWT
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Endpoint de connexion.
     *
     * Étapes :
     * 1. Authentifier l'utilisateur (email + password)
     * 2. Générer un token JWT
     * 3. Retourner le token + infos utilisateur
     *
     * @param request Credentials (email + password)
     * @return LoginResponse avec le token JWT
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        // 1. Authentifier l'utilisateur
        // Si les credentials sont incorrects, Spring Security lève une exception
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // 2. Récupérer les détails de l'utilisateur authentifié
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 3. Générer le token JWT
        String token = jwtService.generateToken(userDetails);

        // 4. Extraire les rôles (sans le préfixe "ROLE_")
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .collect(Collectors.toSet());

        // 5. Construire la réponse
        LoginResponse response = new LoginResponse(
                token,
                "Bearer",
                null, // userId sera ajouté plus tard (nécessite requête DB)
                userDetails.getUsername(),
                null, // firstName sera ajouté plus tard
                null, // lastName sera ajouté plus tard
                roles.stream().map(fr.amu.bestchoice.model.enums.Role::valueOf).collect(Collectors.toSet()),
                3600000L // 1 heure (même valeur que app.jwt.expiration-ms)
        );

        return ResponseEntity.ok(response);
    }
}