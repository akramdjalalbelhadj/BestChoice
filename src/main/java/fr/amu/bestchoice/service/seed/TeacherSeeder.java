package fr.amu.bestchoice.service.seed;

import fr.amu.bestchoice.model.entity.Teacher;
import fr.amu.bestchoice.model.entity.User;
import fr.amu.bestchoice.model.enums.Role;
import fr.amu.bestchoice.repository.TeacherRepository;
import fr.amu.bestchoice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeder pour les profils enseignants (Teachers).
 *
 * Crée les profils Teacher pour les 5 utilisateurs enseignants.
 *
 * Ordre d'exécution : 4 (après Users)
 */
@Slf4j
@Component
@Order(4)
@RequiredArgsConstructor
public class TeacherSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;

    @Override
    public void run(String... args) {

        log.info("🔵 [4/7] Début seeding TEACHERS...");

        long initialCount = teacherRepository.count();

        if (initialCount > 0) {
            log.info("ℹ️  {} profil(s) enseignant(s) déjà présent(s) - skip seeding", initialCount);
            return;
        }

        // Récupérer tous les users avec le rôle ENSEIGNANT
        List<User> teacherUsers = userRepository.findByRole(Role.ENSEIGNANT);

        if (teacherUsers.isEmpty()) {
            log.warn("⚠️  Aucun utilisateur ENSEIGNANT trouvé - impossible de créer les profils");
            return;
        }

        // Données des profils enseignants
        String[][] teacherProfiles = {
                // department, academicRank, specialty, websiteUrl
                {"Informatique", "Professeur", "Intelligence Artificielle et Apprentissage Automatique", "https://www.i2m.univ-amu.fr/~dupont"},
                {"Informatique", "Maître de Conférences", "Génie Logiciel et Architecture des Systèmes", "https://www.i2m.univ-amu.fr/~martin"},
                {"Mathématiques-Informatique", "Professeur", "Bases de Données et Big Data", "https://www.i2m.univ-amu.fr/~bernard"},
                {"Informatique", "Maître de Conférences", "Sécurité Informatique et Cryptographie", "https://www.i2m.univ-amu.fr/~rousseau"},
                {"Informatique", "Professeur", "Systèmes Distribués et Cloud Computing", "https://www.i2m.univ-amu.fr/~lefevre"}
        };

        for (int i = 0; i < Math.min(teacherUsers.size(), teacherProfiles.length); i++) {
            User user = teacherUsers.get(i);
            String[] profileData = teacherProfiles[i];

            Teacher teacher = Teacher.builder()
                    .id(user.getId()) // @MapsId
                    .user(user)
                    .department(profileData[0])
                    .academicRank(profileData[1])
                    .specialty(profileData[2])
                    .websiteUrl(profileData[3])
                    .build();

            teacherRepository.save(teacher);
            log.debug("   ✓ Profil enseignant créé : {} {} ({})",
                    user.getFirstName(), user.getLastName(), profileData[1]);
        }

        long finalCount = teacherRepository.count();
        log.info("✅ Seeding TEACHERS terminé : {} profils enseignants créés", finalCount);
    }
}