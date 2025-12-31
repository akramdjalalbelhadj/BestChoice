package fr.amu.bestchoice.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service de gestion des JWT (JSON Web Tokens).
 *
 * Responsabilités :
 * - Générer un JWT après authentification réussie
 * - Valider un JWT reçu dans une requête HTTP
 * - Extraire les informations du JWT (email, rôles, expiration)
 *
 * Un JWT contient 3 parties séparées par des points :
 * - Header (algorithme de signature)
 * - Payload (données : email, rôles, expiration)
 * - Signature (pour vérifier que le token n'a pas été modifié)
 *
 * Exemple : eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBiZXN0Y2hvaWNlLmxvY2FsIiwicm9sZXMiOlsiQURNSU4iXSwiZXhwIjoxNzA0MDY3MjAwfQ.signature
 */
@Service
public class JwtService {

    // ==================== CONFIGURATION ====================

    /**
     * Clé secrète pour signer les JWT.
     * Définie dans application.properties : app.jwt.secret
     *
     * IMPORTANT : Cette clé doit être :
     * - Longue (minimum 256 bits = 32 caractères)
     * - Aléatoire
     * - Gardée SECRÈTE (ne JAMAIS la commit dans Git)
     */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /**
     * Durée de validité du JWT en millisecondes.
     * Définie dans application.properties : app.jwt.expiration-ms
     *
     * Par défaut : 3600000 ms = 1 heure
     */
    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    // ==================== GÉNÉRATION DU JWT ====================

    /**
     * Génère un JWT pour un utilisateur authentifié.
     *
     * Le JWT contient :
     * - sub (subject) : l'email de l'utilisateur
     * - roles : la liste des rôles (ADMIN, ENSEIGNANT, ETUDIANT)
     * - iat (issued at) : date de création du token
     * - exp (expiration) : date d'expiration du token
     *
     * @param userDetails L'utilisateur authentifié (vient de CustomUserDetailsService)
     * @return Le JWT sous forme de String (à envoyer au client)
     */
    public String generateToken(UserDetails userDetails) {
        // Créer les "claims" (données) à mettre dans le JWT
        Map<String, Object> claims = new HashMap<>();

        // Extraire les rôles de l'utilisateur et les ajouter dans le JWT
        // Exemple : ["ROLE_ADMIN"] devient ["ADMIN"]
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", "")) // Enlever le préfixe "ROLE_"
                .collect(Collectors.toList());

        claims.put("roles", roles);

        // Construire le JWT
        return Jwts.builder()
                .setClaims(claims)                          // Ajouter les claims (roles)
                .setSubject(userDetails.getUsername())      // Subject = email de l'utilisateur
                .setIssuedAt(new Date())                    // Date de création = maintenant
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // Date d'expiration
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Signer avec HS256
                .compact();                                 // Générer le String final
    }

    // ==================== VALIDATION DU JWT ====================

    /**
     * Valide un JWT.
     *
     * Vérifie que :
     * - La signature est correcte (le token n'a pas été modifié)
     * - Le token n'est pas expiré
     * - L'email dans le token correspond à l'utilisateur donné
     *
     * @param token Le JWT à valider
     * @param userDetails L'utilisateur chargé depuis la base de données
     * @return true si le token est valide, false sinon
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            // Extraire l'email du JWT
            String email = getEmailFromToken(token);

            // Vérifier que l'email correspond et que le token n'est pas expiré
            return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            // Si une erreur se produit (signature invalide, token malformé, etc.)
            return false;
        }
    }

    // ==================== EXTRACTION DES DONNÉES DU JWT ====================

    /**
     * Extrait l'email de l'utilisateur depuis le JWT.
     *
     * L'email est stocké dans le claim "sub" (subject).
     *
     * @param token Le JWT
     * @return L'email de l'utilisateur
     */
    public String getEmailFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    /**
     * Extrait la date d'expiration du JWT.
     *
     * @param token Le JWT
     * @return La date d'expiration
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    /**
     * Extrait un claim spécifique du JWT.
     *
     * Un "claim" est une information stockée dans le JWT.
     * Exemples de claims : sub (email), roles, exp (expiration), iat (création)
     *
     * @param token Le JWT
     * @param claimsResolver Fonction pour extraire le claim voulu
     * @param <T> Type du claim
     * @return La valeur du claim
     */
    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrait TOUS les claims du JWT.
     *
     * Cette méthode décode le JWT et vérifie la signature.
     * Si la signature est invalide ou si le token est malformé, une exception est levée.
     *
     * @param token Le JWT
     * @return Tous les claims du JWT
     */
    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())  // Définir la clé de signature
                .build()
                .parseClaimsJws(token)           // Parser et vérifier la signature
                .getBody();                      // Récupérer le payload (claims)
    }

    // ==================== VÉRIFICATION EXPIRATION ====================

    /**
     * Vérifie si le JWT est expiré.
     *
     * @param token Le JWT
     * @return true si expiré, false sinon
     */
    private boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date()); // Expiration < Maintenant ?
    }

    // ==================== CLÉ DE SIGNATURE ====================

    /**
     * Génère la clé de signature à partir du secret.
     *
     * La clé est générée à partir de la String jwtSecret (définie dans application.properties).
     * Cette clé est utilisée pour :
     * - Signer le JWT lors de sa création
     * - Vérifier la signature lors de la validation
     *
     * @return La clé de signature
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}