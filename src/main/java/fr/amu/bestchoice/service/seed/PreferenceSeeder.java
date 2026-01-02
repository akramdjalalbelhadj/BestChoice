package fr.amu.bestchoice.service.seed;

import fr.amu.bestchoice.model.entity.Project;
import fr.amu.bestchoice.model.entity.Student;
import fr.amu.bestchoice.model.entity.StudentPreference;
import fr.amu.bestchoice.model.enums.PreferenceStatus;
import fr.amu.bestchoice.repository.ProjectRepository;
import fr.amu.bestchoice.repository.StudentPreferenceRepository;
import fr.amu.bestchoice.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Seeder pour les préférences étudiantes (StudentPreferences).
 *
 * Crée 60 préférences :
 * - 20 étudiants avec 3 préférences chacun
 * - Rangs : 1er choix, 2ème choix, 3ème choix
 *
 * Ordre d'exécution : 7 (après Students et Projects)
 */
@Slf4j
@Component
@Order(7)
@RequiredArgsConstructor
public class PreferenceSeeder implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final ProjectRepository projectRepository;
    private final StudentPreferenceRepository preferenceRepository;

    private final Random random = new Random(42);

    @Override
    public void run(String... args) {

        log.info("🔵 [7/7] Début seeding PREFERENCES...");

        long initialCount = preferenceRepository.count();

        if (initialCount > 0) {
            log.info("ℹ️  {} préférence(s) déjà présente(s) - skip seeding", initialCount);
            return;
        }

        // Récupérer tous les étudiants avec profil complet
        List<Student> students = studentRepository.findByProfileCompleteTrue();

        if (students.isEmpty()) {
            log.warn("⚠️  Aucun étudiant avec profil complet - impossible de créer les préférences");
            return;
        }

        // Récupérer tous les projets actifs
        List<Project> projects = projectRepository.findByActiveTrue();

        if (projects.isEmpty()) {
            log.warn("⚠️  Aucun projet actif - impossible de créer les préférences");
            return;
        }

        // Sélectionner 20 étudiants aléatoires (ou moins s'il n'y en a pas assez)
        Collections.shuffle(students, random);
        int studentsWithPreferences = Math.min(20, students.size());

        int preferencesCreated = 0;

        for (int i = 0; i < studentsWithPreferences; i++) {
            Student student = students.get(i);

            // Créer 3 préférences par étudiant
            Set<Project> selectedProjects = selectRandomProjects(projects, 3);

            int rank = 1;
            for (Project project : selectedProjects) {

                // Motivation aléatoire (optionnelle ~70%)
                String motivation = random.nextDouble() < 0.7 ?
                        generateMotivation(student, project, rank) :
                        null;

                // Commentaire aléatoire (optionnel ~30%)
                String comment = random.nextDouble() < 0.3 ?
                        "Projet très intéressant qui correspond à mes compétences" :
                        null;

                StudentPreference preference = StudentPreference.builder()
                        .student(student)
                        .project(project)
                        .rank(rank)
                        .status(PreferenceStatus.PENDING)
                        .motivation(motivation)
                        .comment(comment)
                        .build();

                preferenceRepository.save(preference);
                preferencesCreated++;
                rank++;
            }

            log.debug("   ✓ 3 préférences créées pour {} {}",
                    student.getUser().getFirstName(), student.getUser().getLastName());
        }

        long finalCount = preferenceRepository.count();
        log.info("✅ Seeding PREFERENCES terminé : {} préférences créées pour {} étudiants",
                finalCount, studentsWithPreferences);
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    /**
     * Sélectionne N projets aléatoires sans doublon
     */
    private Set<Project> selectRandomProjects(List<Project> allProjects, int count) {
        Set<Project> selected = new LinkedHashSet<>(); // LinkedHashSet pour conserver l'ordre
        List<Project> shuffled = new ArrayList<>(allProjects);
        Collections.shuffle(shuffled, random);

        for (int i = 0; i < Math.min(count, shuffled.size()); i++) {
            selected.add(shuffled.get(i));
        }

        return selected;
    }

    /**
     * Génère une motivation réaliste selon le rang
     */
    private String generateMotivation(Student student, Project project, int rank) {

        String[] motivations1erChoix = {
                "Ce projet correspond parfaitement à mes compétences et à mon projet professionnel.",
                "Je suis très motivé par ce sujet qui rejoint mes centres d'intérêt principaux.",
                "Ce projet me permettrait de développer des compétences clés pour ma future carrière.",
                "J'ai une forte appétence pour ce domaine et souhaite approfondir mes connaissances."
        };

        String[] motivations2emeChoix = {
                "Ce projet m'intéresse également car il touche à des technologies que je souhaite maîtriser.",
                "J'aimerais travailler sur ce sujet pour élargir mon champ de compétences.",
                "Ce projet représente une belle opportunité d'apprentissage dans un domaine connexe.",
                "Je trouve ce sujet pertinent et en lien avec mes objectifs de formation."
        };

        String[] motivations3emeChoix = {
                "Ce projet pourrait être une alternative intéressante pour diversifier mes compétences.",
                "Je serais prêt à travailler sur ce sujet pour découvrir de nouveaux horizons.",
                "Ce projet présente des aspects techniques qui m'intéressent.",
                "J'aimerais explorer ce domaine pour compléter ma formation."
        };

        String[] selectedMotivations = switch (rank) {
            case 1 -> motivations1erChoix;
            case 2 -> motivations2emeChoix;
            default -> motivations3emeChoix;
        };

        return selectedMotivations[random.nextInt(selectedMotivations.length)];
    }
}