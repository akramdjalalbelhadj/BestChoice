package fr.amu.bestchoice.service.seed;

import fr.amu.bestchoice.model.entity.Keyword;
import fr.amu.bestchoice.model.entity.Skill;
import fr.amu.bestchoice.model.entity.Student;
import fr.amu.bestchoice.model.entity.User;
import fr.amu.bestchoice.model.enums.Role;
import fr.amu.bestchoice.model.enums.WorkType;
import fr.amu.bestchoice.repository.KeywordRepository;
import fr.amu.bestchoice.repository.SkillRepository;
import fr.amu.bestchoice.repository.StudentRepository;
import fr.amu.bestchoice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Seeder pour les profils étudiants (Students).
 *
 * Crée les profils Student pour les 50 utilisateurs étudiants
 * avec compétences et centres d'intérêt variés.
 *
 * Ordre d'exécution : 5 (après Users, Skills, Keywords)
 */
@Slf4j
@Component
@Order(5)
@RequiredArgsConstructor
public class StudentSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final SkillRepository skillRepository;
    private final KeywordRepository keywordRepository;

    private final Random random = new Random(42); // Seed fixe pour reproductibilité

    @Override
    public void run(String... args) {

        log.info("🔵 [5/7] Début seeding STUDENTS...");

        long initialCount = studentRepository.count();

        if (initialCount > 0) {
            log.info("ℹ️  {} profil(s) étudiant(s) déjà présent(s) - skip seeding", initialCount);
            return;
        }

        // Récupérer tous les users avec le rôle ETUDIANT
        List<User> studentUsers = userRepository.findByRole(Role.ETUDIANT);

        if (studentUsers.isEmpty()) {
            log.warn("⚠️  Aucun utilisateur ETUDIANT trouvé - impossible de créer les profils");
            return;
        }

        // Récupérer toutes les compétences et mots-clés
        List<Skill> allSkills = skillRepository.findAll();
        List<Keyword> allKeywords = keywordRepository.findAll();

        if (allSkills.isEmpty() || allKeywords.isEmpty()) {
            log.warn("⚠️  Compétences ou mots-clés manquants - profils incomplets");
        }

        // Programmes et parcours
        String[] programs = {"M2 IDL", "M2 IA", "M2 SeCReTS"};
        String[] tracks = {"Ingénierie Logicielle", "Intelligence Artificielle", "Sécurité"};
        WorkType[] workTypes = WorkType.values();

        int profilesCreated = 0;

        for (User user : studentUsers) {

            // Sélection aléatoire du programme et parcours
            String program = programs[random.nextInt(programs.length)];
            String track = tracks[random.nextInt(tracks.length)];
            Integer studyYear = 2; // M2
            WorkType preferredWorkType = workTypes[random.nextInt(workTypes.length)];

            // Sélection de 3-7 compétences aléatoires
            Set<Skill> skills = getRandomSkills(allSkills, 3 + random.nextInt(5));

            // Sélection de 2-4 centres d'intérêt
            Set<Keyword> interests = getRandomKeywords(allKeywords, 2 + random.nextInt(3));

            // URLs GitHub et LinkedIn (optionnels, ~50% des étudiants)
            String githubUrl = random.nextBoolean() ?
                    "https://github.com/" + user.getFirstName().toLowerCase() + user.getLastName().toLowerCase() :
                    null;
            String linkedinUrl = random.nextBoolean() ?
                    "https://linkedin.com/in/" + user.getFirstName().toLowerCase() + "-" + user.getLastName().toLowerCase() :
                    null;

            // Profil complet si au moins 3 compétences et 2 intérêts
            boolean profileComplete = skills.size() >= 3 && interests.size() >= 2;

            Student student = Student.builder()
                    .id(user.getId()) // @MapsId
                    .user(user)
                    .program(program)
                    .track(track)
                    .studyYear(studyYear)
                    .preferredWorkType(preferredWorkType)
                    .skills(skills)
                    .interests(interests)
                    .githubUrl(githubUrl)
                    .linkedinUrl(linkedinUrl)
                    .profileComplete(profileComplete)
                    .build();

            studentRepository.save(student);
            profilesCreated++;
        }

        long finalCount = studentRepository.count();
        log.info("✅ Seeding STUDENTS terminé : {} profils étudiants créés", finalCount);
        log.info("   ℹ️  Profils complets : {}", studentRepository.countByProfileCompleteTrue());
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