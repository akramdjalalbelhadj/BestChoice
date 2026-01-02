package fr.amu.bestchoice.service.seed;

import fr.amu.bestchoice.model.entity.Keyword;
import fr.amu.bestchoice.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Seeder pour les mots-clés (Keywords).
 *
 * Crée 10 mots-clés représentant les domaines et centres d'intérêt.
 *
 * Ordre d'exécution : 3
 */
@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class KeywordSeeder implements CommandLineRunner {

    private final KeywordRepository keywordRepository;

    @Override
    public void run(String... args) {

        log.info("🔵 [3/7] Début seeding KEYWORDS...");

        long initialCount = keywordRepository.count();

        if (initialCount > 0) {
            log.info("ℹ️  {} mot(s)-clé(s) déjà présent(s) - skip seeding", initialCount);
            return;
        }

        String[][] keywords = {
                // label, domain, description
                {"Intelligence Artificielle", "AI", "Apprentissage automatique, deep learning, NLP"},
                {"Développement Web", "Web", "Applications web, frontend, backend, full-stack"},
                {"Développement Mobile", "Mobile", "Applications iOS, Android, React Native, Flutter"},
                {"Cloud Computing", "Cloud", "AWS, Azure, GCP, architectures cloud"},
                {"Cybersécurité", "Security", "Sécurité des systèmes, cryptographie, pentesting"},
                {"DevOps", "DevOps", "CI/CD, conteneurisation, orchestration, automatisation"},
                {"Data Science", "Data", "Analyse de données, visualisation, big data"},
                {"Internet des Objets", "IoT", "Systèmes embarqués, capteurs, domotique"},
                {"Blockchain", "Blockchain", "Cryptomonnaies, smart contracts, DeFi"},
                {"Réalité Virtuelle", "VR/AR", "VR, AR, metaverse, jeux vidéo"}
        };

        for (String[] keywordData : keywords) {
            Keyword keyword = Keyword.builder()
                    .label(keywordData[0])
                    .domain(keywordData[1])
                    .description(keywordData[2])
                    .active(true)
                    .build();

            keywordRepository.save(keyword);
        }

        long finalCount = keywordRepository.count();
        log.info("✅ Seeding KEYWORDS terminé : {} mots-clés créés", finalCount);
    }
}