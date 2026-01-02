package fr.amu.bestchoice.service.seed;

import fr.amu.bestchoice.model.entity.Skill;
import fr.amu.bestchoice.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Seeder pour les compétences (Skills).
 *
 * Crée 20 compétences techniques pour les étudiants et projets.
 *
 * Ordre d'exécution : 2
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class SkillSeeder implements CommandLineRunner {

    private final SkillRepository skillRepository;

    @Override
    public void run(String... args) {

        log.info("🔵 [2/7] Début seeding SKILLS...");

        long initialCount = skillRepository.count();

        if (initialCount > 0) {
            log.info("ℹ️  {} compétence(s) déjà présente(s) - skip seeding", initialCount);
            return;
        }

        String[][] skills = {
                // name, category, description, level
                {"Java", "Backend", "Langage orienté objet pour applications d'entreprise", "3"},
                {"Python", "Backend", "Langage polyvalent pour IA, data science et backend", "3"},
                {"JavaScript", "Frontend", "Langage pour le développement web frontend", "2"},
                {"TypeScript", "Frontend", "JavaScript typé pour applications complexes", "3"},
                {"React", "Frontend", "Bibliothèque JavaScript pour interfaces utilisateur", "3"},
                {"Angular", "Frontend", "Framework TypeScript pour applications web", "3"},
                {"Vue.js", "Frontend", "Framework JavaScript progressif", "2"},
                {"Spring Boot", "Backend", "Framework Java pour applications REST", "4"},
                {"Node.js", "Backend", "Runtime JavaScript côté serveur", "3"},
                {"Django", "Backend", "Framework Python full-stack", "3"},
                {"SQL", "Database", "Langage de requêtes pour bases de données relationnelles", "2"},
                {"MongoDB", "Database", "Base de données NoSQL orientée documents", "2"},
                {"PostgreSQL", "Database", "SGBD relationnel avancé", "3"},
                {"Docker", "DevOps", "Conteneurisation d'applications", "3"},
                {"Kubernetes", "DevOps", "Orchestration de conteneurs", "4"},
                {"Git", "Tools", "Système de contrôle de version", "2"},
                {"Machine Learning", "AI", "Apprentissage automatique et modèles prédictifs", "4"},
                {"Deep Learning", "AI", "Réseaux de neurones profonds", "5"},
                {"NLP", "AI", "Traitement automatique du langage naturel", "4"},
                {"Cybersécurité", "Security", "Sécurité des systèmes et applications", "4"}
        };

        for (String[] skillData : skills) {
            Skill skill = Skill.builder()
                    .name(skillData[0])
                    .category(skillData[1])
                    .description(skillData[2])
                    .level(Integer.parseInt(skillData[3]))
                    .active(true)
                    .build();

            skillRepository.save(skill);
        }

        long finalCount = skillRepository.count();
        log.info("✅ Seeding SKILLS terminé : {} compétences créées", finalCount);
    }
}