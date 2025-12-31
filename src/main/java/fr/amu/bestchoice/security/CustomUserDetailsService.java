package fr.amu.bestchoice.security;

import fr.amu.bestchoice.model.entity.User;
import fr.amu.bestchoice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Service personnalisé pour charger les utilisateurs depuis la base de données.
 *
 * Implémente UserDetailsService de Spring Security.
 *
 * Rôle :
 * - Charger un utilisateur depuis la base de données par son email
 * - Convertir l'entité User en UserDetails (format attendu par Spring Security)
 *
 * Ce service est utilisé par :
 * - JwtAuthenticationFilter (pour charger l'utilisateur après validation du JWT)
 * - AuthenticationManager (pour vérifier l'email/password lors du login)
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * Repository pour accéder aux utilisateurs en base de données.
     */
    private final UserRepository userRepository;

    /**
     * Charge un utilisateur par son email (utilisé comme username).
     *
     * Cette méthode est appelée par Spring Security pour :
     * - Vérifier l'email/password lors du login
     * - Charger l'utilisateur après validation du JWT
     *
     * @param email L'email de l'utilisateur (username)
     * @return UserDetails contenant les informations de l'utilisateur
     * @throws UsernameNotFoundException Si l'utilisateur n'existe pas
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // Chercher l'utilisateur dans la base de données par son email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur non trouvé avec l'email : " + email
                ));

        // Convertir l'entité User en UserDetails (format Spring Security)
        return buildUserDetails(user);
    }

    /**
     * Convertit une entité User en UserDetails.
     *
     * UserDetails est l'interface utilisée par Spring Security pour représenter un utilisateur.
     * Elle contient :
     * - Username (email dans notre cas)
     * - Password (hash BCrypt)
     * - Authorities (rôles : ROLE_ADMIN, ROLE_ENSEIGNANT, ROLE_ETUDIANT)
     * - Flags : compte actif, non expiré, non verrouillé, credentials non expirées
     *
     * @param user L'entité User (depuis la base de données)
     * @return UserDetails pour Spring Security
     */
    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())              // Username = email
                .password(user.getPasswordHash())       // Password = hash BCrypt
                .authorities(getAuthorities(user))      // Authorities = rôles
                .accountExpired(false)                  // Compte non expiré
                .accountLocked(!user.getActive())       // Compte verrouillé si inactif
                .credentialsExpired(false)              // Credentials non expirées
                .disabled(!user.getActive())            // Désactivé si inactif
                .build();
    }

    /**
     * Convertit les rôles de l'utilisateur en GrantedAuthority.
     *
     * Spring Security utilise le préfixe "ROLE_" pour les rôles.
     *
     * Exemples :
     * - ADMIN → ROLE_ADMIN
     * - ENSEIGNANT → ROLE_ENSEIGNANT
     * - ETUDIANT → ROLE_ETUDIANT
     *
     * Ces rôles sont utilisés par :
     * - @PreAuthorize("hasRole('ADMIN')")
     * - .hasRole("ADMIN") dans SecurityConfig
     *
     * @param user L'utilisateur
     * @return Collection de GrantedAuthority (rôles avec préfixe "ROLE_")
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());
    }
}