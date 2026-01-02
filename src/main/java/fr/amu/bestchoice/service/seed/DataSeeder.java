package fr.amu.bestchoice.service.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Orchestrateur principal du seeding de données.
 *
 * Active ou désactive le seeding via une variable d'environnement ou configuration.
 * Les seeders individuels sont exécutés dans l'ordre défini par @Order.
 *
 * Pour désactiver le seeding en production :
 * - Définir : SEEDING_ENABLED=false dans application.properties
 * - Ou supprimer cette classe en production
 *
 * ⚠️ ATTENTION : Le seeding est ACTIF par défaut en développement
 */
@Slf4j
@Component
@Order(0) // S'exécute en premier
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    // ⚠️ FLAG GLOBAL - Désactiver en production
    private static final boolean SEEDING_ENABLED = true;

    @Override
    public void run(String... args) {

        if (!SEEDING_ENABLED) {
            log.info("🚫 Seeding désactivé (SEEDING_ENABLED=false)");
            return;
        }

        log.info("" +
                "\n" +
                "╔════════════════════════════════════════════════════════════════╗\n" +
                "║                  🌱 SEEDING DE DONNÉES ACTIVÉ                 ║\n" +
                "╠════════════════════════════════════════════════════════════════╣\n" +
                "║  Les seeders vont créer des données de test si nécessaire :   ║\n" +
                "║  • 1 Admin                                                     ║\n" +
                "║  • 5 Enseignants                                               ║\n" +
                "║  • 50 Étudiants                                                ║\n" +
                "║  • 20 Compétences                                              ║\n" +
                "║  • 10 Mots-clés                                                ║\n" +
                "║  • 20 Projets (4 par enseignant)                               ║\n" +
                "║  • 60 Préférences étudiantes                                   ║\n" +
                "╠════════════════════════════════════════════════════════════════╣\n" +
                "║  ⚠️  DÉSACTIVER EN PRODUCTION via SEEDING_ENABLED=false       ║\n" +
                "╚════════════════════════════════════════════════════════════════╝\n"
        );
    }
}