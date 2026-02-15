package fr.amu.bestchoice.service.seed;

import fr.amu.bestchoice.model.entity.*;
import fr.amu.bestchoice.model.enums.PreferenceStatus;
import fr.amu.bestchoice.model.enums.Role;
import fr.amu.bestchoice.model.enums.WorkType;
import fr.amu.bestchoice.repository.*;
import fr.amu.bestchoice.service.implementation.algorithmes.MatchingAlgorithmType;
import fr.amu.bestchoice.service.implementation.algorithmes.MatchingContextService;
import fr.amu.bestchoice.service.implementation.algorithmes.MatchingScope;
import fr.amu.bestchoice.web.dto.matching.MatchingRunRequest;
import fr.amu.bestchoice.web.dto.matching.MatchingRunResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.*;

/**
 * ⚙️ SEEDER - Charge des données de test au démarrage de l'application
 *
 * ✅ Version "nouveau matching" :
 * - Seed data
 * - Puis lance MatchingContextService.run() avec l'algo choisi (HYBRID par défaut)
 * - recompute=true pour recalculer proprement
 */
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
    private final StudentPreferenceRepository preferenceRepository;

    // ✅ nouveau matching
    private final MatchingContextService matchingContextService;

    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            // ⚠️ Ne charger les données que si la base est vide
            if (userRepository.count() > 0) {
                log.info("✅ Base de données déjà remplie - Pas de seeding");
                return;
            }

            log.info("🌱 Début du seeding de la base de données...");

            // ==================== ÉTAPE 1 : SKILLS ====================
            log.info("📚 Création des compétences...");
            List<Skill> skills = createSkills();
            skillRepository.saveAll(skills);
            log.info("✅ {} compétences créées", skills.size());

            // ==================== ÉTAPE 2 : KEYWORDS ====================
            log.info("🏷️ Création des mots-clés...");
            List<Keyword> keywords = createKeywords();
            keywordRepository.saveAll(keywords);
            log.info("✅ {} mots-clés créés", keywords.size());

            // ==================== ÉTAPE 3 : USERS ====================
            log.info("👤 Création des utilisateurs...");

            // Admin
            User admin = createAdmin();
            userRepository.save(admin);
            log.info("✅ Admin créé : {}", admin.getEmail());

            // Enseignants
            List<User> teacherUsers = createTeacherUsers();
            userRepository.saveAll(teacherUsers);
            log.info("✅ {} utilisateurs enseignants créés", teacherUsers.size());

            // Étudiants
            List<User> studentUsers = createStudentUsers();
            userRepository.saveAll(studentUsers);
            log.info("✅ {} utilisateurs étudiants créés", studentUsers.size());

            // ==================== ÉTAPE 4 : TEACHERS ====================
            log.info("👨‍🏫 Création des profils enseignants...");
            List<Teacher> teachers = createTeachers(teacherUsers);
            teacherRepository.saveAll(teachers);
            log.info("✅ {} profils enseignants créés", teachers.size());

            // ==================== ÉTAPE 5 : STUDENTS ====================
            log.info("🎓 Création des profils étudiants...");
            List<Student> students = createStudents(studentUsers, skills, keywords);
            studentRepository.saveAll(students);
            log.info("✅ {} profils étudiants créés", students.size());

            // ==================== ÉTAPE 6 : PROJECTS ====================
            log.info("📋 Création des projets...");
            List<Project> projects = createProjects(teachers, skills, keywords);
            projectRepository.saveAll(projects);
            log.info("✅ {} projets créés", projects.size());

            // ==================== ÉTAPE 7 : PREFERENCES ====================
            log.info("⭐ Création des préférences étudiantes...");
            List<StudentPreference> preferences = createPreferences(students, projects);
            preferenceRepository.saveAll(preferences);
            log.info("✅ {} préférences créées", preferences.size());

            // ==================== ÉTAPE 8 : MATCHING (NEW WAY) ====================
            // ✅ Maintenant on ne seed plus MatchingResult "à la main"
            // ✅ On utilise MatchingContextService + Strategies
            log.info("🎯 Lancement du matching via MatchingContextService (nouveau code) ...");

            MatchingRunRequest matchingRequest = new MatchingRunRequest(
                    MatchingAlgorithmType.HYBRID,     // algorithm
                    MatchingScope.ALL_STUDENTS,       // scope
                    null,                             // studentId (null car ALL_STUDENTS)
                    true,                             // recompute
                    true,                             // persist
                    new BigDecimal("0.50"),           // threshold (optionnel)
                    Map.of(                           // weights (optionnel)
                            "skills", new BigDecimal("0.50"),
                            "interests", new BigDecimal("0.30"),
                            "workType", new BigDecimal("0.20")
                    )
            );

            MatchingRunResult runResult = matchingContextService.run(matchingRequest);

            log.info("✅ Matching terminé : sessionId={}, algo={}, studentsProcessed={}, resultsSaved={}",
                    runResult.sessionId(),
                    runResult.algorithmUsed(),
                    runResult.studentsProcessed(),
                    runResult.resultsSaved()
            );

            if (runResult.warnings() != null && !runResult.warnings().isEmpty()) {
                log.warn("⚠️ Matching warnings: {}", runResult.warnings());
            }

            // ==================== FIN ====================
            log.info("🎉 Seeding terminé avec succès !");
            log.info("📊 Résumé :");
            log.info("   - {} utilisateurs", userRepository.count());
            log.info("   - {} étudiants", studentRepository.count());
            log.info("   - {} enseignants", teacherRepository.count());
            log.info("   - {} compétences", skillRepository.count());
            log.info("   - {} mots-clés", keywordRepository.count());
            log.info("   - {} projets", projectRepository.count());
            log.info("   - {} préférences", preferenceRepository.count());
            log.info("");
            log.info("🔐 Credentials de test :");
            log.info("   Admin     : admin@bestchoice.local / Admin12345!");
            log.info("   Enseignant: Jean@univ-amu.fr / Teacher123!");
            log.info("   Étudiant  : akram@etu.univ-amu.fr / Student123!");
        };
    }

    // ==================== CRÉATION DES SKILLS ====================

    private List<Skill> createSkills() {
        return Arrays.asList(
                Skill.builder().name("Java").description("Langage de programmation orienté objet").category("Programmation").level(2).active(true).build(),
                Skill.builder().name("Python").description("Langage de programmation polyvalent").category("Programmation").level(1).active(true).build(),
                Skill.builder().name("JavaScript").description("Langage de programmation web").category("Programmation").level(2).active(true).build(),
                Skill.builder().name("C++").description("Langage de programmation système").category("Programmation").level(3).active(true).build(),
                Skill.builder().name("PHP").description("Langage de programmation serveur").category("Programmation").level(2).active(true).build(),

                Skill.builder().name("SQL").description("Langage de requêtes pour bases de données").category("Base de données").level(2).active(true).build(),
                Skill.builder().name("MongoDB").description("Base de données NoSQL").category("Base de données").level(2).active(true).build(),
                Skill.builder().name("PostgreSQL").description("Système de gestion de base de données").category("Base de données").level(3).active(true).build(),

                Skill.builder().name("Spring Boot").description("Framework Java pour applications web").category("Framework").level(3).active(true).build(),
                Skill.builder().name("React").description("Bibliothèque JavaScript pour interfaces").category("Framework").level(2).active(true).build(),
                Skill.builder().name("Angular").description("Framework JavaScript front-end").category("Framework").level(3).active(true).build(),
                Skill.builder().name("Django").description("Framework Python web").category("Framework").level(2).active(true).build(),

                Skill.builder().name("Git").description("Système de contrôle de version").category("DevOps").level(1).active(true).build(),
                Skill.builder().name("Docker").description("Plateforme de conteneurisation").category("DevOps").level(2).active(true).build(),
                Skill.builder().name("Kubernetes").description("Orchestration de conteneurs").category("DevOps").level(4).active(true).build(),

                Skill.builder().name("Machine Learning").description("Apprentissage automatique").category("IA").level(3).active(true).build(),
                Skill.builder().name("Deep Learning").description("Apprentissage profond").category("IA").level(4).active(true).build(),
                Skill.builder().name("TensorFlow").description("Framework de machine learning").category("IA").level(3).active(true).build(),
                Skill.builder().name("Pandas").description("Bibliothèque Python pour analyse de données").category("Data Science").level(2).active(true).build(),
                Skill.builder().name("Scikit-learn").description("Bibliothèque Python pour ML").category("IA").level(2).active(true).build()
        );
    }

    // ==================== CRÉATION DES KEYWORDS ====================

    private List<Keyword> createKeywords() {
        return Arrays.asList(
                Keyword.builder().label("Intelligence Artificielle").description("IA et apprentissage automatique").domain("IA").active(true).build(),
                Keyword.builder().label("Machine Learning").description("Apprentissage automatique").domain("IA").active(true).build(),
                Keyword.builder().label("Deep Learning").description("Apprentissage profond").domain("IA").active(true).build(),
                Keyword.builder().label("Computer Vision").description("Vision par ordinateur").domain("IA").active(true).build(),
                Keyword.builder().label("NLP").description("Traitement du langage naturel").domain("IA").active(true).build(),

                Keyword.builder().label("Développement Web").description("Applications web").domain("Web").active(true).build(),
                Keyword.builder().label("Frontend").description("Interface utilisateur").domain("Web").active(true).build(),
                Keyword.builder().label("Backend").description("Côté serveur").domain("Web").active(true).build(),
                Keyword.builder().label("Full Stack").description("Frontend + Backend").domain("Web").active(true).build(),
                Keyword.builder().label("API REST").description("Services web RESTful").domain("Web").active(true).build(),

                Keyword.builder().label("Base de données").description("Stockage et gestion de données").domain("Data").active(true).build(),
                Keyword.builder().label("Big Data").description("Traitement de grandes quantités de données").domain("Data").active(true).build(),
                Keyword.builder().label("Data Science").description("Science des données").domain("Data").active(true).build(),
                Keyword.builder().label("Data Mining").description("Exploration de données").domain("Data").active(true).build(),

                Keyword.builder().label("Cybersécurité").description("Sécurité informatique").domain("Sécurité").active(true).build(),
                Keyword.builder().label("Cryptographie").description("Chiffrement et sécurité").domain("Sécurité").active(true).build(),

                Keyword.builder().label("DevOps").description("Développement et opérations").domain("DevOps").active(true).build(),
                Keyword.builder().label("Cloud Computing").description("Informatique en nuage").domain("Cloud").active(true).build(),
                Keyword.builder().label("Microservices").description("Architecture microservices").domain("Architecture").active(true).build(),
                Keyword.builder().label("IoT").description("Internet des objets").domain("IoT").active(true).build()
        );
    }

    // ==================== CRÉATION DES USERS ====================

    private User createAdmin() {
        return User.builder()
                .email("admin@bestchoice.local")
                .passwordHash(passwordEncoder.encode("Admin12345!"))
                .firstName("Admin")
                .lastName("BestChoice")
                .studentNumber(null)
                .active(true)
                .role(Role.ADMIN)
                .build();
    }

    private List<User> createTeacherUsers() {
        List<User> teachers = new ArrayList<>();

        teachers.add(User.builder()
                .email("Jean@univ-amu.fr")
                .passwordHash(passwordEncoder.encode("Teacher123!"))
                .firstName("Jean")
                .lastName("Jean")
                .active(true)
                .role(Role.ENSEIGNANT)
                .build());

        teachers.add(User.builder()
                .email("martin@univ-amu.fr")
                .passwordHash(passwordEncoder.encode("Teacher123!"))
                .firstName("Martin")
                .lastName("Martin")
                .active(true)
                .role(Role.ENSEIGNANT)
                .build());

        teachers.add(User.builder()
                .email("bernard@univ-amu.fr")
                .passwordHash(passwordEncoder.encode("Teacher123!"))
                .firstName("Bernard")
                .lastName("Bernard")
                .active(true)
                .role(Role.ENSEIGNANT)
                .build());

        teachers.add(User.builder()
                .email("dubois@univ-amu.fr")
                .passwordHash(passwordEncoder.encode("Teacher123!"))
                .firstName("Dubois")
                .lastName("Dubois")
                .active(true)
                .role(Role.ENSEIGNANT)
                .build());

        return teachers;
    }

    private List<User> createStudentUsers() {
        List<User> students = new ArrayList<>();

        students.add(User.builder()
                .email("akram@etu.univ-amu.fr")
                .passwordHash(passwordEncoder.encode("Student123!"))
                .firstName("Akram")
                .lastName("BELHADJ")
                .studentNumber("24025877")
                .active(true)
                .role(Role.ETUDIANT)
                .build());

        students.add(User.builder()
                .email("yacine@etu.univ-amu.fr")
                .passwordHash(passwordEncoder.encode("Student123!"))
                .firstName("Yacine")
                .lastName("KARTOUT")
                .studentNumber("22001235")
                .active(true)
                .role(Role.ETUDIANT)
                .build());

        students.add(User.builder()
                .email("ikrame@etu.univ-amu.fr")
                .passwordHash(passwordEncoder.encode("Student123!"))
                .firstName("Ikrame")
                .lastName("Loukridi")
                .studentNumber("22001236")
                .active(true)
                .role(Role.ETUDIANT)
                .build());

        students.add(User.builder()
                .email("robert@etu.univ-amu.fr")
                .passwordHash(passwordEncoder.encode("Student123!"))
                .firstName("David")
                .lastName("Robert")
                .studentNumber("22001237")
                .active(true)
                .role(Role.ETUDIANT)
                .build());

        students.add(User.builder()
                .email("richard@etu.univ-amu.fr")
                .passwordHash(passwordEncoder.encode("Student123!"))
                .firstName("Emma")
                .lastName("Richard")
                .studentNumber("22001238")
                .active(true)
                .role(Role.ETUDIANT)
                .build());

        students.add(User.builder()
                .email("simon@etu.univ-amu.fr")
                .passwordHash(passwordEncoder.encode("Student123!"))
                .firstName("Felix")
                .lastName("Simon")
                .studentNumber("22001239")
                .active(true)
                .role(Role.ETUDIANT)
                .build());

        students.add(User.builder()
                .email("laurent@etu.univ-amu.fr")
                .passwordHash(passwordEncoder.encode("Student123!"))
                .firstName("Grace")
                .lastName("Laurent")
                .studentNumber("22001240")
                .active(true)
                .role(Role.ETUDIANT)
                .build());

        students.add(User.builder()
                .email("michel@etu.univ-amu.fr")
                .passwordHash(passwordEncoder.encode("Student123!"))
                .firstName("Hugo")
                .lastName("Michel")
                .studentNumber("22001241")
                .active(true)
                .role(Role.ETUDIANT)
                .build());

        students.add(User.builder()
                .email("garcia@etu.univ-amu.fr")
                .passwordHash(passwordEncoder.encode("Student123!"))
                .firstName("Iris")
                .lastName("Garcia")
                .studentNumber("22001242")
                .active(true)
                .role(Role.ETUDIANT)
                .build());

        students.add(User.builder()
                .email("thomas@etu.univ-amu.fr")
                .passwordHash(passwordEncoder.encode("Student123!"))
                .firstName("Jules")
                .lastName("Thomas")
                .studentNumber("22001243")
                .active(true)
                .role(Role.ETUDIANT)
                .build());

        return students;
    }

    // ==================== CRÉATION DES TEACHERS ====================

    private List<Teacher> createTeachers(List<User> teacherUsers) {
        List<Teacher> teachers = new ArrayList<>();

        teachers.add(Teacher.builder()
                .user(teacherUsers.get(0))
                .department("Informatique")
                .academicRank("Maître de Conférences")
                .specialty("Intelligence Artificielle et Machine Learning")
                .websiteUrl("https://dupont.univ-amu.fr")
                .build());

        teachers.add(Teacher.builder()
                .user(teacherUsers.get(1))
                .department("Informatique")
                .academicRank("Professeur des Universités")
                .specialty("Développement Web et Bases de Données")
                .websiteUrl("https://martin.univ-amu.fr")
                .build());

        teachers.add(Teacher.builder()
                .user(teacherUsers.get(2))
                .department("Informatique")
                .academicRank("Maître de Conférences")
                .specialty("Cybersécurité et Cryptographie")
                .websiteUrl("https://bernard.univ-amu.fr")
                .build());

        teachers.add(Teacher.builder()
                .user(teacherUsers.get(3))
                .department("Mathématiques et Informatique")
                .academicRank("Maître de Conférences")
                .specialty("Big Data et Data Science")
                .websiteUrl("https://dubois.univ-amu.fr")
                .build());

        return teachers;
    }

    // ==================== CRÉATION DES STUDENTS ====================

    private List<Student> createStudents(List<User> studentUsers, List<Skill> skills, List<Keyword> keywords) {
        List<Student> students = new ArrayList<>();
        Random random = new Random(42);

        for (User user : studentUsers) {

            Set<Skill> studentSkills = new HashSet<>();
            int nbSkills = 3 + random.nextInt(4);
            for (int j = 0; j < nbSkills; j++) {
                studentSkills.add(skills.get(random.nextInt(skills.size())));
            }

            Set<Keyword> studentInterests = new HashSet<>();
            int nbInterests = 2 + random.nextInt(4);
            for (int j = 0; j < nbInterests; j++) {
                studentInterests.add(keywords.get(random.nextInt(keywords.size())));
            }

            Student student = Student.builder()
                    .user(user)
                    .program("Master Informatique")
                    .studyYear(1 + random.nextInt(2))
                    .track(getRandomTrack(random))
                    .department("UFR Sciences")
                    .preferredWorkType(getRandomWorkType(random))
                    .targetField(getRandomTargetField(random))
                    .bio("Étudiant passionné par l'informatique et les nouvelles technologies.")
                    .skills(studentSkills)
                    .interests(studentInterests)
                    .profileComplete(true)
                    .githubUrl("https://github.com/" + user.getEmail().split("@")[0])
                    .linkedinUrl("https://linkedin.com/in/" + user.getEmail().split("@")[0])
                    .build();

            students.add(student);
        }
        return students;
    }

    // ==================== CRÉATION DES PROJECTS ====================

    private List<Project> createProjects(List<Teacher> teachers, List<Skill> skills, List<Keyword> keywords) {
        List<Project> projects = new ArrayList<>();

        projects.add(Project.builder()
                .title("Système de recommandation basé sur le Machine Learning")
                .description("Développement d'un système de recommandation intelligent utilisant des algorithmes de ML.")
                .objectives("Implémenter un moteur de recommandation et évaluer les performances.")
                .workType(WorkType.DEVELOPPEMENT)
                .credits(6)
                .durationWeeks(12)
                .maxStudents(2)
                .minStudents(1)
                .semester(1)
                .academicYear("2024-2025")
                .targetProgram("Master Informatique")
                .remotePossible(true)
                .active(true)
                .complet(false)
                .teacher(teachers.get(0))
                .requiredSkills(Set.of(
                        getSkillByName(skills, "Python"),
                        getSkillByName(skills, "Machine Learning"),
                        getSkillByName(skills, "Pandas")
                ))
                .targetSkills(Set.of(
                        getSkillByName(skills, "Machine Learning"),
                        getSkillByName(skills, "Scikit-learn")
                ))
                .keywords(Set.of(
                        getKeywordByLabel(keywords, "Machine Learning"),
                        getKeywordByLabel(keywords, "Intelligence Artificielle"),
                        getKeywordByLabel(keywords, "Data Science")
                ))
                .build());

        projects.add(Project.builder()
                .title("Classification d'images avec Deep Learning")
                .description("Création d'un modèle de deep learning pour classifier des images médicales.")
                .objectives("Comprendre les CNN, utiliser TensorFlow, optimiser un modèle.")
                .workType(WorkType.RECHERCHE)
                .credits(6)
                .durationWeeks(14)
                .maxStudents(2)
                .minStudents(1)
                .semester(1)
                .academicYear("2024-2025")
                .targetProgram("Master Informatique")
                .remotePossible(true)
                .active(true)
                .complet(false)
                .teacher(teachers.get(0))
                .requiredSkills(Set.of(
                        getSkillByName(skills, "Python"),
                        getSkillByName(skills, "TensorFlow"),
                        getSkillByName(skills, "Deep Learning")
                ))
                .keywords(Set.of(
                        getKeywordByLabel(keywords, "Deep Learning"),
                        getKeywordByLabel(keywords, "Computer Vision"),
                        getKeywordByLabel(keywords, "Intelligence Artificielle")
                ))
                .build());

        projects.add(Project.builder()
                .title("Plateforme e-commerce complète")
                .description("Développement d'une application web de e-commerce avec React et Spring Boot.")
                .objectives("Concevoir une API REST et un front moderne.")
                .workType(WorkType.DEVELOPPEMENT)
                .credits(6)
                .durationWeeks(12)
                .maxStudents(3)
                .minStudents(2)
                .semester(1)
                .academicYear("2024-2025")
                .targetProgram("Master Informatique")
                .remotePossible(true)
                .active(true)
                .complet(false)
                .teacher(teachers.get(1))
                .requiredSkills(Set.of(
                        getSkillByName(skills, "Java"),
                        getSkillByName(skills, "Spring Boot"),
                        getSkillByName(skills, "React"),
                        getSkillByName(skills, "SQL")
                ))
                .keywords(Set.of(
                        getKeywordByLabel(keywords, "Développement Web"),
                        getKeywordByLabel(keywords, "Full Stack"),
                        getKeywordByLabel(keywords, "API REST")
                ))
                .build());

        projects.add(Project.builder()
                .title("Audit de sécurité d'une application web")
                .description("Audit sécurité : recherche de vulnérabilités, tests de pénétration, recommandations.")
                .objectives("Identifier les vulnérabilités (OWASP), rédiger un rapport.")
                .workType(WorkType.ANALYSE)
                .credits(6)
                .durationWeeks(10)
                .maxStudents(2)
                .minStudents(1)
                .semester(1)
                .academicYear("2024-2025")
                .targetProgram("Master Informatique")
                .remotePossible(false)
                .active(true)
                .complet(false)
                .teacher(teachers.get(2))
                .requiredSkills(Set.of(
                        getSkillByName(skills, "JavaScript"),
                        getSkillByName(skills, "SQL"),
                        getSkillByName(skills, "Python")
                ))
                .keywords(Set.of(
                        getKeywordByLabel(keywords, "Cybersécurité"),
                        getKeywordByLabel(keywords, "Développement Web")
                ))
                .build());

        projects.add(Project.builder()
                .title("Analyse de données massives avec Spark")
                .description("Traitement et analyse de grandes quantités de données avec Spark.")
                .objectives("Concevoir des pipelines de traitement et visualiser les résultats.")
                .workType(WorkType.ANALYSE)
                .credits(6)
                .durationWeeks(12)
                .maxStudents(2)
                .minStudents(1)
                .semester(1)
                .academicYear("2024-2025")
                .targetProgram("Master Informatique")
                .remotePossible(true)
                .active(true)
                .complet(false)
                .teacher(teachers.get(3))
                .requiredSkills(Set.of(
                        getSkillByName(skills, "Python"),
                        getSkillByName(skills, "SQL"),
                        getSkillByName(skills, "Pandas")
                ))
                .keywords(Set.of(
                        getKeywordByLabel(keywords, "Big Data"),
                        getKeywordByLabel(keywords, "Data Science"),
                        getKeywordByLabel(keywords, "Data Mining")
                ))
                .build());

        projects.add(Project.builder()
                .title("Pipeline CI/CD avec Docker et Kubernetes")
                .description("Mise en place d'un pipeline CI/CD pour une application microservices.")
                .objectives("Automatiser build/test/deploy avec Docker et Kubernetes.")
                .workType(WorkType.DEVELOPPEMENT)
                .credits(6)
                .durationWeeks(10)
                .maxStudents(2)
                .minStudents(1)
                .semester(1)
                .academicYear("2024-2025")
                .targetProgram("Master Informatique")
                .remotePossible(true)
                .active(true)
                .complet(false)
                .teacher(teachers.get(1))
                .requiredSkills(Set.of(
                        getSkillByName(skills, "Docker"),
                        getSkillByName(skills, "Kubernetes"),
                        getSkillByName(skills, "Git")
                ))
                .keywords(Set.of(
                        getKeywordByLabel(keywords, "DevOps"),
                        getKeywordByLabel(keywords, "Cloud Computing"),
                        getKeywordByLabel(keywords, "Microservices")
                ))
                .build());

        projects.add(Project.builder()
                .title("Chatbot intelligent avec NLP")
                .description("Développement d'un chatbot capable de comprendre et répondre aux questions des utilisateurs.")
                .objectives("Comprendre le NLP, intégrer un moteur de Q/R.")
                .workType(WorkType.DEVELOPPEMENT)
                .credits(6)
                .durationWeeks(12)
                .maxStudents(2)
                .minStudents(1)
                .semester(1)
                .academicYear("2024-2025")
                .targetProgram("Master Informatique")
                .remotePossible(true)
                .active(true)
                .complet(false)
                .teacher(teachers.get(0))
                .requiredSkills(Set.of(
                        getSkillByName(skills, "Python"),
                        getSkillByName(skills, "Machine Learning")
                ))
                .keywords(Set.of(
                        getKeywordByLabel(keywords, "NLP"),
                        getKeywordByLabel(keywords, "Intelligence Artificielle"),
                        getKeywordByLabel(keywords, "Machine Learning")
                ))
                .build());

        projects.add(Project.builder()
                .title("Application mobile et API REST")
                .description("Création d'une application mobile avec backend API REST pour la gestion de tâches.")
                .objectives("Développer une API REST, gérer l'authentification.")
                .workType(WorkType.DEVELOPPEMENT)
                .credits(6)
                .durationWeeks(14)
                .maxStudents(3)
                .minStudents(2)
                .semester(1)
                .academicYear("2024-2025")
                .targetProgram("Master Informatique")
                .remotePossible(true)
                .active(true)
                .complet(false)
                .teacher(teachers.get(1))
                .requiredSkills(Set.of(
                        getSkillByName(skills, "Java"),
                        getSkillByName(skills, "Spring Boot"),
                        getSkillByName(skills, "SQL")
                ))
                .keywords(Set.of(
                        getKeywordByLabel(keywords, "Développement Web"),
                        getKeywordByLabel(keywords, "API REST"),
                        getKeywordByLabel(keywords, "Backend")
                ))
                .build());

        return projects;
    }

    // ==================== CRÉATION DES PREFERENCES ====================

    private List<StudentPreference> createPreferences(List<Student> students, List<Project> projects) {
        List<StudentPreference> preferences = new ArrayList<>();
        Random random = new Random(42);

        for (Student student : students) {
            int nbPreferences = 3 + random.nextInt(3);

            List<Project> availableProjects = new ArrayList<>(projects);
            Collections.shuffle(availableProjects, random);

            for (int rank = 1; rank <= nbPreferences && rank <= availableProjects.size(); rank++) {
                Project project = availableProjects.get(rank - 1);

                StudentPreference preference = StudentPreference.builder()
                        .student(student)
                        .project(project)
                        .rank(rank)
                        .motivation("Je suis très intéressé par ce projet car il correspond à mes compétences et mes aspirations professionnelles.")
                        .comment(rank == 1 ? "Mon premier choix !" : "")
                        .status(PreferenceStatus.PENDING)
                        .build();

                preferences.add(preference);
            }
        }
        return preferences;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private String getRandomTrack(Random random) {
        String[] tracks = {"IAAA", "IDL", "SID", "GIG", "AIGLE"};
        return tracks[random.nextInt(tracks.length)];
    }

    private WorkType getRandomWorkType(Random random) {
        WorkType[] types = WorkType.values();
        return types[random.nextInt(types.length)];
    }

    private String getRandomTargetField(Random random) {
        String[] fields = {
                "Développement Web",
                "Intelligence Artificielle",
                "Data Science",
                "Cybersécurité",
                "DevOps",
                "Architecture logicielle",
                "Cloud Computing"
        };
        return fields[random.nextInt(fields.length)];
    }

    private Skill getSkillByName(List<Skill> skills, String name) {
        return skills.stream()
                .filter(s -> s.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Skill not found: " + name));
    }

    private Keyword getKeywordByLabel(List<Keyword> keywords, String label) {
        return keywords.stream()
                .filter(k -> k.getLabel().equals(label))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Keyword not found: " + label));
    }
}
