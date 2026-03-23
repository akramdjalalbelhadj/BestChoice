package fr.amu.bestchoice.service.seed;

import fr.amu.bestchoice.model.entity.*;
import fr.amu.bestchoice.model.enums.*;
import fr.amu.bestchoice.repository.*;
import fr.amu.bestchoice.service.implementation.algorithmes.MatchingAlgorithmType;
import fr.amu.bestchoice.service.implementation.algorithmes.MatchingContextService;
import fr.amu.bestchoice.web.dto.matching.MatchingRunResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final SkillRepository skillRepository;
    private final KeywordRepository keywordRepository;
    private final ProjectRepository projectRepository;
    private final SubjectRepository subjectRepository; // Ajouté
    private final MatchingCampaignRepository campaignRepository; // Ajouté
    private final StudentPreferenceRepository preferenceRepository;
    private final MatchingContextService matchingContextService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            if (userRepository.count() > 0) {
                log.info("✅ Base de données déjà remplie - Pas de seeding");
                return;
            }

            log.info("🌱 Début du seeding de la base de données...");

            // 1. SKILLS & KEYWORDS
            List<Skill> skills = skillRepository.saveAll(createSkills());
            List<Keyword> keywords = keywordRepository.saveAll(createKeywords());

            // 2. USERS (Admin, Teachers, Students)
            userRepository.save(createAdmin());
            List<User> teacherUsers = userRepository.saveAll(createTeacherUsers());
            List<User> studentUsers = userRepository.saveAll(createStudentUsers());

            // 3. PROFILES
            List<Teacher> teachers = teacherRepository.saveAll(createTeachers(teacherUsers));
            List<Student> students = studentRepository.saveAll(createStudents(studentUsers, skills, keywords));

            // 4. ITEMS (Projects & Subjects)
            List<Project> projects = projectRepository.saveAll(createProjects(teachers, skills, keywords));
            // Ajout des 30 matières demandées (10 pour Jean, 10 pour Martin, 10 pour Bernard)
            List<Subject> subjects = subjectRepository.saveAll(createSubjects(teachers));

            // 5. CRÉATION D'UNE CAMPAGNE DE TEST
            log.info("📅 Création de la campagne de matching...");
            MatchingCampaign campaign = MatchingCampaign.builder()
                    .name("Campagne Master Informatique 2024")
                    .description("Affectation des projets de fin d'études")
                    .campaignType(MatchingCampaignType.PROJECT)
                    .algorithmType(MatchingAlgorithmType.STABLE)
                    .teacher(teachers.get(0))
                    .students(new HashSet<>(students))
                    .projects(new HashSet<>(projects))
                    .build();
            campaign = campaignRepository.save(campaign);

            // 6. PREFERENCES (Liées à la campagne)
            log.info("⭐ Création des vœux étudiants...");
            List<StudentPreference> preferences = createPreferences(students, projects, campaign);
            preferenceRepository.saveAll(preferences);

            // 7. LANCEMENT DU MATCHING (Version simplifiée)
            log.info("🎯 Lancement automatique du matching pour la campagne...");
            MatchingRunResult runResult = matchingContextService.run(campaign.getId());

            log.info("✅ Matching terminé : campagneId={}, algo={}, studentsProcessed={}, resultsStored={}",
                    runResult.campaignId(),
                    runResult.algorithmUsed(),
                    runResult.studentsProcessed(),
                    runResult.resultsStored()
            );

            log.info("🎉 Seeding terminé avec succès !");
        };
    }

    // ==================== CRÉATION DES SUBJECTS (30 MATIÈRES) ====================

    private List<Subject> createSubjects(List<Teacher> teachers) {
        List<Subject> subjects = new ArrayList<>();
        // Jean est l'index 0, Martin l'index 1, Bernard l'index 2
        Teacher jean = teachers.get(0);
        Teacher martin = teachers.get(1);
        Teacher bernard = teachers.get(2);

        for (int i = 1; i <= 10; i++) {
            subjects.add(Subject.builder()
                    .title("Option IA : Module " + i + " (Jean)")
                    .description("Cours optionnel de spécialisation en Intelligence Artificielle.")
                    .credits(3).maxStudents(15).active(true).teacher(jean).build());

            subjects.add(Subject.builder()
                    .title("Option Web : Architecture " + i + " (Martin)")
                    .description("Cours optionnel sur les architectures web modernes.")
                    .credits(3).maxStudents(20).active(true).teacher(martin).build());

            subjects.add(Subject.builder()
                    .title("Option Sécurité : Cryptographie " + i + " (Bernard)")
                    .description("Cours optionnel sur les systèmes de sécurité avancés.")
                    .credits(3).maxStudents(12).active(true).teacher(bernard).build());
        }
        return subjects;
    }

    // ==================== CRÉATION DES PREFERENCES (AJOUT CAMPAGNE) ====================

    private List<StudentPreference> createPreferences(List<Student> students, List<Project> projects, MatchingCampaign campaign) {
        List<StudentPreference> preferences = new ArrayList<>();
        Random random = new Random(42);

        for (Student student : students) {
            List<Project> availableProjects = new ArrayList<>(projects);
            Collections.shuffle(availableProjects, random);

            for (int rank = 1; rank <= 3; rank++) {
                preferences.add(StudentPreference.builder()
                        .student(student)
                        .project(availableProjects.get(rank - 1))
                        .matchingCampaign(campaign) // ✅ Lié à la campagne !
                        .rank(rank)
                        .status(PreferenceStatus.PENDING)
                        .build());
            }
        }
        return preferences;
    }

    // ==================== USERS & PROFILES (LOGIQUE EXISTANTE) ====================

    private User createAdmin() {
        return User.builder()
                .email("admin@bestchoice.local")
                .passwordHash(passwordEncoder.encode("Admin12345!"))
                .firstName("Admin")
                .lastName("BestChoice")
                .role(Role.ADMIN)
                .active(true)
                .build();
    }

    private List<User> createTeacherUsers() {
        return Arrays.asList(
                User.builder().email("Jean@univ-amu.fr").passwordHash(passwordEncoder.encode("Teacher123!")).firstName("Jean").lastName("Jean").role(Role.ENSEIGNANT).active(true).build(),
                User.builder().email("martin@univ-amu.fr").passwordHash(passwordEncoder.encode("Teacher123!")).firstName("Martin").lastName("Martin").role(Role.ENSEIGNANT).active(true).build(),
                User.builder().email("bernard@univ-amu.fr").passwordHash(passwordEncoder.encode("Teacher123!")).firstName("Bernard").lastName("Bernard").role(Role.ENSEIGNANT).active(true).build(),
                User.builder().email("dubois@univ-amu.fr").passwordHash(passwordEncoder.encode("Teacher123!")).firstName("Dubois").lastName("Dubois").role(Role.ENSEIGNANT).active(true).build()
        );
    }

    private List<User> createStudentUsers() {
        String[] names = {"akram", "yacine", "ikrame", "robert", "richard", "simon", "laurent", "michel", "garcia", "thomas"};
        List<User> users = new ArrayList<>();
        for (String name : names) {
            users.add(User.builder()
                    .email(name + "@etu.univ-amu.fr")
                    .passwordHash(passwordEncoder.encode("Student123!"))
                    .firstName(name.substring(0, 1).toUpperCase() + name.substring(1))
                    .lastName("Test")
                    .role(Role.ETUDIANT)
                    .active(true)
                    .build());
        }
        return users;
    }

    private List<Teacher> createTeachers(List<User> users) {
        return users.stream().map(u -> Teacher.builder().user(u).department("Informatique").build()).toList();
    }

    private List<Student> createStudents(List<User> users, List<Skill> skills, List<Keyword> keywords) {
        List<Student> students = new ArrayList<>();
        Random r = new Random(42);
        for (User u : users) {
            students.add(Student.builder()
                    .user(u)
                    .studyYear(r.nextInt(2) + 1)
                    .program("Master Info")
                    .preferredWorkTypes(Set.of(WorkType.DEVELOPPEMENT))
                    .skills(new HashSet<>(skills.subList(0, 3)))
                    .interests(new HashSet<>(keywords.subList(0, 3)))
                    .profileComplete(true)
                    .build());
        }
        return students;
    }

    private List<Project> createProjects(List<Teacher> teachers, List<Skill> skills, List<Keyword> keywords) {
        List<Project> p = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            p.add(Project.builder()
                    .title("Projet Innovant n°" + i)
                    .description("Description du projet de recherche n°" + i)
                    .workTypes(Set.of(WorkType.DEVELOPPEMENT))
                    .maxStudents(2).minStudents(1).active(true)
                    .teacher(teachers.get(i % teachers.size()))
                    .requiredSkills(new HashSet<>(skills.subList(0, 2)))
                    .keywords(new HashSet<>(keywords.subList(0, 2)))
                    .build());
        }
        return p;
    }

    private List<Skill> createSkills() {
        return Arrays.asList(
                Skill.builder().name("Java").category("Dev").active(true).build(),
                Skill.builder().name("Python").category("Dev").active(true).build(),
                Skill.builder().name("SQL").category("Data").active(true).build()
        );
    }

    private List<Keyword> createKeywords() {
        return Arrays.asList(
                Keyword.builder().label("IA").domain("IA").active(true).build(),
                Keyword.builder().label("Web").domain("Web").active(true).build(),
                Keyword.builder().label("Cloud").domain("Infrastucture").active(true).build()
        );
    }
}