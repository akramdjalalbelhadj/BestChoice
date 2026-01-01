package fr.amu.bestchoice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration Swagger/OpenAPI pour la documentation de l'API BestChoice.
 *
 * Cette configuration :
 * - Définit les informations de l'API (titre, description, version, contact)
 * - Configure l'authentification JWT (Bearer token)
 * - Organise les endpoints par tags (domaines métier)
 * - Définit les serveurs disponibles (dev, prod)
 *
 * Accès à la documentation :
 * - Swagger UI : http://localhost:8081/swagger-ui.html
 * - OpenAPI JSON : http://localhost:8081/v3/api-docs
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI bestChoiceOpenAPI() {

        // ==================== INFORMATIONS API ====================

        Info apiInfo = new Info()
                .title("BestChoice API")
                .description("""
                        API REST pour la plateforme de matching étudiant-projet.
                        
                        ## Fonctionnalités principales
                        
                        ### Authentification
                        - Connexion avec email/password
                        - Génération de JWT (Bearer token)
                        - Durée de validité : 1 heure
                        
                        ### Gestion des utilisateurs
                        - Inscription par l'admin (rôles : ADMIN, ENSEIGNANT, ETUDIANT)
                        - Profils étudiants (compétences, centres d'intérêt, préférences)
                        - Profils enseignants (département, spécialité, projets)
                        
                        ### Gestion des projets
                        - Création par les enseignants
                        - Compétences requises et mots-clés
                        - Type de travail (individuel, binôme, groupe)
                        - Capacité min/max d'étudiants
                        
                        ### Préférences étudiantes
                        - Choix de 1 à 10 projets par ordre de préférence
                        - Motivation et commentaires
                        - Statuts : PENDING, ACCEPTED, REJECTED
                        
                        ### Algorithme de matching
                        - Calcul de compatibilité étudiant-projet
                        - Scores : compétences, centres d'intérêt, global
                        - Recommandations personnalisées
                        
                        ## Workflow typique
                        
                        1. **Admin** crée les compétences et mots-clés
                        2. **Admin** inscrit les utilisateurs (étudiants + enseignants)
                        3. **Étudiants** complètent leur profil (compétences, intérêts)
                        4. **Enseignants** créent leurs projets
                        5. **Étudiants** expriment leurs préférences (1-10 projets)
                        6. **Algorithme** calcule les matchings
                        7. **Admin** valide les affectations
                        
                        ## Sécurité
                        
                        - Tous les endpoints (sauf /api/auth/*) nécessitent un JWT valide
                        - Cliquez sur "Authorize" 🔒 et saisissez votre token
                        - Format : `Bearer <votre_token>`
                        
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Équipe BestChoice - M2 IDL - université aix-marseille")
                        .email("akram-djalal.BELHADJ@etu.univ-amu.fr")
                        .url("https://github.com/bestchoice/bestchoice-api"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));

        // ==================== SÉCURITÉ JWT ====================

        // Définir le schéma de sécurité JWT
        SecurityScheme jwtSecurityScheme = new SecurityScheme()
                .name("JWT Authentication")
                .description("""
                        Authentification par JWT (JSON Web Token).
                        """)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER);

        // Exiger le JWT pour tous les endpoints (sauf login)
        SecurityRequirement jwtSecurityRequirement = new SecurityRequirement()
                .addList("JWT Authentication");

        // ==================== SERVEURS ====================

        Server devServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Serveur de développement (H2)");

        Server prodServer = new Server()
                .url("https://api.bestchoice.local")
                .description("Serveur de production (MySQL)");

        // ==================== TAGS (ORGANISATION) ====================

        Tag authTag = new Tag()
                .name("Authentification")
                .description("Connexion et gestion des tokens JWT");

        Tag usersTag = new Tag()
                .name("Utilisateurs")
                .description("Gestion des utilisateurs (inscription, modification, activation/désactivation)");

        Tag studentsTag = new Tag()
                .name("Étudiants")
                .description("Gestion des profils étudiants (compétences, centres d'intérêt, année d'étude)");

        Tag teachersTag = new Tag()
                .name("Enseignants")
                .description("Gestion des profils enseignants (département, spécialité, projets)");

        Tag projectsTag = new Tag()
                .name("Projets")
                .description("Gestion des projets (création, modification, compétences requises, mots-clés)");

        Tag preferencesTag = new Tag()
                .name("Préférences")
                .description("Gestion des choix des étudiants (1-10 projets par ordre de préférence)");

        Tag matchingTag = new Tag()
                .name("Matching")
                .description("Résultats de l'algorithme de matching (scores de compatibilité)");

        Tag skillsTag = new Tag()
                .name("Compétences")
                .description("Référentiel des compétences (Java, Python, Machine Learning, etc.)");

        Tag keywordsTag = new Tag()
                .name("Mots-clés")
                .description("Référentiel des mots-clés / centres d'intérêt (IA, DevOps, Cybersécurité, etc.)");

        // ==================== CONSTRUCTION OPENAPI ====================

        return new OpenAPI()
                .info(apiInfo)
                .servers(List.of(devServer, prodServer))
                .components(new Components()
                        .addSecuritySchemes("JWT Authentication", jwtSecurityScheme))
                .addSecurityItem(jwtSecurityRequirement)
                .tags(List.of(
                        authTag,
                        usersTag,
                        studentsTag,
                        teachersTag,
                        projectsTag,
                        preferencesTag,
                        matchingTag,
                        skillsTag,
                        keywordsTag
                ));
    }
}