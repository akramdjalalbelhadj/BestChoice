package fr.amu.bestchoice.service.seed;

import fr.amu.bestchoice.model.entity.Keyword;
import fr.amu.bestchoice.model.entity.Project;
import fr.amu.bestchoice.model.entity.Skill;
import fr.amu.bestchoice.model.entity.Teacher;
import fr.amu.bestchoice.model.enums.WorkType;
import fr.amu.bestchoice.repository.KeywordRepository;
import fr.amu.bestchoice.repository.ProjectRepository;
import fr.amu.bestchoice.repository.SkillRepository;
import fr.amu.bestchoice.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Seeder pour les projets (Projects).
 *
 * Crée 20 projets (4 par enseignant) avec compétences requises
 * et mots-clés variés.
 *
 * Ordre d'exécution : 6 (après Teachers, Skills, Keywords)
 */
@Slf4j
@Component
@Order(6)
@RequiredArgsConstructor
public class ProjectSeeder implements CommandLineRunner {

    private final TeacherRepository teacherRepository;
    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;
    private final KeywordRepository keywordRepository;

    private final Random random = new Random(42);

    @Override
    public void run(String... args) {

        log.info("🔵 [6/7] Début seeding PROJECTS...");

        long initialCount = projectRepository.count();

        if (initialCount > 0) {
            log.info("ℹ️  {} projet(s) déjà présent(s) - skip seeding", initialCount);
            return;
        }

        List<Teacher> teachers = teacherRepository.findAll();

        if (teachers.isEmpty()) {
            log.warn("⚠️  Aucun enseignant trouvé - impossible de créer les projets");
            return;
        }

        List<Skill> allSkills = skillRepository.findAll();
        List<Keyword> allKeywords = keywordRepository.findAll();

        // Templates de projets (5 templates x 4 = 20 projets)
        String[][] projectTemplates = {
                // title, description, workType
                {"Développement d'une application de gestion", "Création d'une application web pour la gestion d'entités métier avec interface moderne", "HYBRID"},
                {"Système de recommandation intelligent", "Implémentation d'un moteur de recommandation basé sur l'apprentissage automatique", "REMOTE"},
                {"Plateforme e-commerce responsive", "Développement d'une solution e-commerce complète avec paiement sécurisé", "ON_SITE"},
                {"Chatbot conversationnel avec NLP", "Création d'un assistant virtuel capable de comprendre le langage naturel", "REMOTE"},
                {"Application mobile cross-platform", "Développement d'une application mobile pour iOS et Android", "HYBRID"},
                {"Dashboard analytique temps réel", "Tableau de bord interactif pour la visualisation de données en temps réel", "REMOTE"},
                {"API REST sécurisée avec microservices", "Architecture microservices avec authentification JWT et documentation OpenAPI", "REMOTE"},
                {"Système de détection d'anomalies", "Détection automatique d'anomalies dans des flux de données avec ML", "HYBRID"},
                {"Plateforme collaborative de partage", "Espace collaboratif pour le partage et la gestion de ressources", "REMOTE"},
                {"Application IoT pour objets connectés", "Solution complète pour la gestion et le monitoring d'objets connectés", "ON_SITE"},
                {"Outil de CI/CD automatisé", "Pipeline d'intégration et déploiement continu avec Docker et Kubernetes", "REMOTE"},
                {"Système de blockchain pour traçabilité", "Implémentation d'une solution blockchain pour la traçabilité de produits", "HYBRID"},
                {"Application de réalité augmentée", "Développement d'une expérience AR innovante pour mobile", "ON_SITE"},
                {"Plateforme de gestion de projets agile", "Outil collaboratif pour la gestion de projets en méthodologie Scrum", "HYBRID"},
                {"Système de reconnaissance d'images", "Solution de Computer Vision pour la classification et détection d'objets", "REMOTE"},
                {"Application de streaming vidéo", "Plateforme de streaming avec encodage adaptatif et CDN", "REMOTE"},
                {"Outil d'analyse prédictive de données", "Système d'analyse et prédiction basé sur des modèles statistiques", "HYBRID"},
                {"Application de cryptomonnaie", "Portefeuille électronique et système d'échange de cryptomonnaies", "REMOTE"},
                {"Système de gestion de conteneurs", "Solution d'orchestration et monitoring de conteneurs Docker", "REMOTE"},
                {"Application de cartographie interactive", "Visualisation de données géospatiales avec interactions en temps réel", "HYBRID"}
        };

        int projectsCreated = 0;

        // Créer 4 projets par enseignant
        for (int teacherIndex = 0; teacherIndex < teachers.size(); teacherIndex++) {
            Teacher teacher = teachers.get(teacherIndex);

            for (int projectIndex = 0; projectIndex < 4; projectIndex++) {
                int templateIndex = (teacherIndex * 4 + projectIndex) % projectTemplates.length;
                String[] template = projectTemplates[templateIndex];

                // Compétences requises (2-4)
                Set<Skill> requiredSkills = getRandomSkills(allSkills, 2 + random.nextInt(3));

                // Mots-clés (1-3)
                Set<Keyword> keywords = getRandomKeywords(allKeywords, 1 + random.nextInt(3));

                // Nombre d'étudiants (1-3)
                int minStudents = 1;
                int maxStudents = 1 + random.nextInt(3);

                Project project = Project.builder()
                        .title(template[0] + " #" + (projectIndex + 1))
                        .description(template[1])
                        .workType(WorkType.valueOf(template[2]))
                        .remotePossible(random.nextBoolean())
                        .active(true)
                        .minStudents(minStudents)
                        .maxStudents(maxStudents)
                        .full(false)
                        .teacher(teacher)
                        .requiredSkills(requiredSkills)
                        .keywords(keywords)
                        .assignedStudents(new ArrayList<>())
                        .build();

                projectRepository.save(project);
                projectsCreated++;
            }

            log.debug("   ✓ 4 projets créés pour {} {}",
                    teacher.getUser().getFirstName(), teacher.getUser().getLastName());
        }

        long finalCount = projectRepository.count();
        log.info("✅ Seeding PROJECTS terminé : {} projets créés", finalCount);
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private Set<Skill> getRandomSkills(List<Skill> allSkills, int count) {
        Set<Skill> selected = new HashSet<>();
        List<Skill> shuffled = new ArrayList<>(allSkills);
        Collections.shuffle(shuffled, random);

        for (int i = 0; i < Math.min(count, shuffled.size()); i++) {
            selected.add(shuffled.get(i));
        }

        return selected;
    }

    private Set<Keyword> getRandomKeywords(List<Keyword> allKeywords, int count) {
        Set<Keyword> selected = new HashSet<>();
        List<Keyword> shuffled = new ArrayList<>(allKeywords);
        Collections.shuffle(shuffled, random);

        for (int i = 0; i < Math.min(count, shuffled.size()); i++) {
            selected.add(shuffled.get(i));
        }

        return selected;
    }
}