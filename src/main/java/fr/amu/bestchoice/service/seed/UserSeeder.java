package fr.amu.bestchoice.service.seed;

import fr.amu.bestchoice.model.entity.User;
import fr.amu.bestchoice.model.enums.Role;
import fr.amu.bestchoice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Seeder pour les utilisateurs (Users).
 *
 * Crée :
 * - 1 Admin
 * - 5 Enseignants
 * - 50 Étudiants
 *
 * Ordre d'exécution : 1 (premier après DataSeeder)
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Mot de passe par défaut pour tous les utilisateurs
    private static final String DEFAULT_PASSWORD = "Password123!";

    @Override
    public void run(String... args) {

        log.info("🔵 [1/7] Début seeding USERS...");

        long initialCount = userRepository.count();

        if (initialCount > 0) {
            log.info("ℹ️  {} utilisateur(s) déjà présent(s) - skip seeding", initialCount);
            return;
        }

        // ==================== ADMIN ====================
        createAdmin();

        // ==================== ENSEIGNANTS ====================
        createTeachers();

        // ==================== ÉTUDIANTS ====================
        createStudents();

        long finalCount = userRepository.count();
        log.info("✅ Seeding USERS terminé : {} utilisateurs créés", finalCount);
    }

    // ==================== ADMIN ====================

    private void createAdmin() {

        User admin = User.builder()
                .firstName("Admin")
                .lastName("BestChoice")
                .email("admin@bestchoice.local")
                .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                .active(true)
                .roles(Set.of(Role.ADMIN))
                .build();

        userRepository.save(admin);
        log.info("   ✓ Admin créé : {}", admin.getEmail());
    }

    // ==================== ENSEIGNANTS ====================

    private void createTeachers() {

        String[][] teachers = {
                {"Marie", "Dupont", "marie.dupont@univ-amu.fr"},
                {"Jean", "Martin", "jean.martin@univ-amu.fr"},
                {"Sophie", "Bernard", "sophie.bernard@univ-amu.fr"},
                {"Pierre", "Rousseau", "pierre.rousseau@univ-amu.fr"},
                {"Claire", "Lefevre", "claire.lefevre@univ-amu.fr"}
        };

        for (String[] teacherData : teachers) {
            User teacher = User.builder()
                    .firstName(teacherData[0])
                    .lastName(teacherData[1])
                    .email(teacherData[2])
                    .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                    .active(true)
                    .roles(Set.of(Role.ENSEIGNANT))
                    .build();

            userRepository.save(teacher);
            log.debug("   ✓ Enseignant créé : {}", teacher.getEmail());
        }

        log.info("   ✓ {} enseignants créés", teachers.length);
    }

    // ==================== ÉTUDIANTS ====================

    private void createStudents() {

        String[][] firstLastNames = {
                // 50 paires prénom/nom
                {"Lucas", "Moreau"}, {"Emma", "Simon"}, {"Hugo", "Laurent"},
                {"Léa", "Michel"}, {"Louis", "Garcia"}, {"Chloé", "David"},
                {"Arthur", "Bertrand"}, {"Manon", "Roux"}, {"Nathan", "Morel"},
                {"Camille", "Fournier"}, {"Tom", "Girard"}, {"Sarah", "Andre"},
                {"Jules", "Leroy"}, {"Inès", "Mercier"}, {"Gabriel", "Blanc"},
                {"Zoé", "Guerin"}, {"Raphaël", "Boyer"}, {"Lola", "Garnier"},
                {"Adam", "Chevalier"}, {"Juliette", "Francois"}, {"Antoine", "Legrand"},
                {"Mathilde", "Gauthier"}, {"Maxime", "Perrin"}, {"Clara", "Robin"},
                {"Alexandre", "Clement"}, {"Lisa", "Morin"}, {"Victor", "Nicolas"},
                {"Laura", "Henry"}, {"Paul", "Roussel"}, {"Alice", "Mathieu"},
                {"Théo", "Gautier"}, {"Louise", "Masson"}, {"Thomas", "Marchand"},
                {"Jade", "Duval"}, {"Simon", "Denis"}, {"Léna", "Dumont"},
                {"Mathis", "Marie"}, {"Eva", "Lemaire"}, {"Maxence", "Noel"},
                {"Anna", "Meyer"}, {"Noah", "Dumas"}, {"Rose", "Brunet"},
                {"Ethan", "Schmitt"}, {"Olivia", "Perrot"}, {"Valentin", "Picard"},
                {"Maëlys", "Roger"}, {"Enzo", "Vidal"}, {"Pauline", "Bouvier"},
                {"Robin", "Fernandez"}, {"Anaïs", "Leclerc"}
        };

        for (int i = 0; i < firstLastNames.length; i++) {
            String firstName = firstLastNames[i][0];
            String lastName = firstLastNames[i][1];
            String studentNumber = String.format("22%05d", i + 1); // 2200001, 2200002, ...
            String email = String.format("%s.%s@etu.univ-amu.fr",
                    firstName.toLowerCase().replace("é", "e").replace("è", "e").replace("ï", "i").replace("ë", "e").replace("ô", "o"),
                    lastName.toLowerCase());

            User student = User.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(email)
                    .studentNumber(studentNumber)
                    .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                    .active(true)
                    .roles(Set.of(Role.ETUDIANT))
                    .build();

            userRepository.save(student);
        }

        log.info("   ✓ {} étudiants créés", firstLastNames.length);
    }
}