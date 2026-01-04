package fr.amu.bestchoice.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre d'authentification JWT.
 *
 * Ce filtre intercepte TOUTES les requêtes HTTP entrantes.
 *
 * Rôle :
 * 1. Extraire le JWT du header HTTP "Authorization: Bearer <token>"
 * 2. Valider le JWT (signature, expiration)
 * 3. Extraire l'email de l'utilisateur depuis le JWT
 * 4. Charger l'utilisateur depuis la base de données
 * 5. Mettre l'utilisateur dans le contexte de sécurité Spring
 * 6. Laisser passer la requête
 *
 * OncePerRequestFilter : Garantit que le filtre s'exécute UNE SEULE fois par requête
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    /**
     * Service pour générer et valider les JWT.
     */
    private final JwtService jwtService;

    /**
     * Service pour charger les utilisateurs depuis la base de données.
     * Implémentation : CustomUserDetailsService
     */
    private final UserDetailsService userDetailsService;

    // ==================== FILTRE ====================

    /**
     * Méthode principale du filtre.
     * Appelée pour CHAQUE requête HTTP.
     *
     * @param request La requête HTTP entrante
     * @param response La réponse HTTP sortante
     * @param filterChain La chaîne de filtres (permet de passer au filtre suivant)
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // ==================== ÉTAPE 1 : EXTRAIRE LE JWT ====================

        // Récupérer le header "Authorization" de la requête
        // Exemple : "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        String authHeader = request.getHeader("Authorization");

        // Si le header est absent ou ne commence pas par "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Pas de JWT → passer au filtre suivant (la requête sera bloquée par Spring Security plus tard)
            filterChain.doFilter(request, response);
            return;
        }

        // Extraire le JWT (enlever "Bearer " du début)
        // "Bearer eyJhbGc..." devient "eyJhbGc..."
        String jwt = authHeader.substring(7);

        // ==================== ÉTAPE 2 : EXTRAIRE L'EMAIL DU JWT ====================

        String userEmail;
        try {
            // Extraire l'email (claim "sub") du JWT
            userEmail = jwtService.getEmailFromToken(jwt);
        } catch (Exception e) {
            // Si le JWT est malformé ou invalide, on arrête ici
            filterChain.doFilter(request, response);
            return;
        }

        // ==================== ÉTAPE 3 : VÉRIFIER SI L'UTILISATEUR N'EST PAS DÉJÀ AUTHENTIFIÉ ====================

        // SecurityContextHolder.getContext().getAuthentication() :
        // Contient l'utilisateur actuellement authentifié (null si pas encore authentifié)
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // ==================== ÉTAPE 4 : CHARGER L'UTILISATEUR DEPUIS LA BASE ====================

            // Charger l'utilisateur depuis la base de données via CustomUserDetailsService
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            // ==================== ÉTAPE 5 : VALIDER LE JWT ====================

            // Vérifier que :
            // - La signature est correcte
            // - Le token n'est pas expiré
            // - L'email correspond
            if (jwtService.validateToken(jwt, userDetails)) {

                // ==================== ÉTAPE 6 : CRÉER L'AUTHENTIFICATION ====================

                // Créer un objet d'authentification Spring Security
                // Cet objet contient :
                // - L'utilisateur (UserDetails)
                // - Ses rôles (authorities)
                // - Le fait qu'il est authentifié
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,           // Principal = l'utilisateur
                        null,                  // Credentials = null (on n'a pas besoin du mot de passe ici)
                        userDetails.getAuthorities() // Authorities = les rôles (ROLE_ADMIN, etc.)
                );

                // Ajouter des détails sur la requête (IP, user-agent, etc.)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // ==================== ÉTAPE 7 : METTRE L'UTILISATEUR DANS LE CONTEXTE ====================

                // Mettre l'authentification dans le contexte de sécurité Spring
                // À partir de maintenant, l'utilisateur est considéré comme authentifié
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // ==================== ÉTAPE 8 : CONTINUER LA CHAÎNE DE FILTRES ====================

        // Passer au filtre suivant (ou au contrôleur si c'est le dernier filtre)
        filterChain.doFilter(request, response);
    }
}