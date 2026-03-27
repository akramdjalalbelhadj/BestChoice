package fr.amu.bestchoice.service.seed;

import fr.amu.bestchoice.model.entity.*;
import fr.amu.bestchoice.model.enums.*;
import fr.amu.bestchoice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

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
            List<Subject> subjects = subjectRepository.saveAll(createSubjects(teachers, skills, keywords));

            log.info("🎉 Seeding terminé avec succès !");
        };
    }

    // ==================== CRÉATION DES SUBJECTS (10 MATIÈRES) ====================

    private List<Subject> createSubjects(List<Teacher> teachers, List<Skill> skills, List<Keyword> keywords) {
        Teacher jean    = teachers.get(0);
        Teacher martin  = teachers.get(1);
        Teacher bernard = teachers.get(2);

        // Skills par index :
        // 0=Java, 1=Python, 2=SQL, 3=JavaScript, 4=CSS,
        // 5=R, 6=TensorFlow, 7=Scikit-learn, 8=LaTeX,
        // 9=Spring Boot, 10=Docker, 11=Kubernetes,
        // 12=Algorithmique, 13=OWASP, 14=Pentest,
        // 15=Réseaux, 16=MATLAB, 17=UX Design

        // Keywords par index :
        // 0=IA, 1=Web, 2=Cloud, 3=Interface, 4=Ergonomie,
        // 5=Design, 6=Data, 7=Statistiques, 8=Visualisation,
        // 9=Deep Learning, 10=Modélisation, 11=Recherche,
        // 12=Publication, 13=Méthodologie, 14=Architecture,
        // 15=Microservices, 16=DevOps, 17=Infrastructure,
        // 18=Optimisation, 19=Complexité, 20=Performance,
        // 21=Sécurité, 22=Vulnérabilité, 23=Audit,
        // 24=Intrusion, 25=Algèbre, 26=Matrices, 27=Calcul

        return Arrays.asList(

            // 1. IHM — Jean
            Subject.builder()
                .title("IHM (Interface Homme Machine)")
                .description("Conception et évaluation d'interfaces utilisateur.")
                .credits(3).maxStudents(5).active(true).teacher(jean)
                .workTypes(Set.of(WorkType.DEVELOPPEMENT))
                .requiredSkills(new HashSet<>(List.of(skills.get(3), skills.get(4), skills.get(17))))
                .keywords(new HashSet<>(List.of(keywords.get(3), keywords.get(4), keywords.get(5))))
                .build(),

            // 2. Analyse des Données — Jean
            Subject.builder()
                .title("Analyse des Données")
                .description("Techniques d'exploration et de visualisation de données.")
                .credits(3).maxStudents(5).active(true).teacher(jean)
                .workTypes(Set.of(WorkType.ANALYSE))
                .requiredSkills(new HashSet<>(List.of(skills.get(1), skills.get(2), skills.get(5))))
                .keywords(new HashSet<>(List.of(keywords.get(6), keywords.get(7), keywords.get(8))))
                .build(),

            // 3. Machine Learning — Jean
            Subject.builder()
                .title("Machine Learning")
                .description("Apprentissage automatique supervisé et non supervisé.")
                .credits(3).maxStudents(5).active(true).teacher(jean)
                .workTypes(Set.of(WorkType.RECHERCHE))
                .requiredSkills(new HashSet<>(List.of(skills.get(1), skills.get(6), skills.get(7))))
                .keywords(new HashSet<>(List.of(keywords.get(0), keywords.get(9), keywords.get(10))))
                .build(),

            // 4. Recherche Scientifique — Jean
            Subject.builder()
                .title("Recherche Scientifique")
                .description("Méthodologie de recherche et rédaction académique.")
                .credits(3).maxStudents(5).active(true).teacher(jean)
                .workTypes(Set.of(WorkType.RECHERCHE))
                .requiredSkills(new HashSet<>(List.of(skills.get(1), skills.get(8), skills.get(16))))
                .keywords(new HashSet<>(List.of(keywords.get(11), keywords.get(12), keywords.get(13))))
                .build(),

            // 5. Architecture JEE — Martin
            Subject.builder()
                .title("Architecture JEE")
                .description("Conception d'applications Java EE multi-tiers.")
                .credits(3).maxStudents(5).active(true).teacher(martin)
                .workTypes(Set.of(WorkType.DEVELOPPEMENT))
                .requiredSkills(new HashSet<>(List.of(skills.get(0), skills.get(9), skills.get(2))))
                .keywords(new HashSet<>(List.of(keywords.get(14), keywords.get(15), keywords.get(1))))
                .build(),

            // 6. Cloud Computing — Martin
            Subject.builder()
                .title("Cloud Computing")
                .description("Déploiement et gestion d'infrastructures cloud.")
                .credits(3).maxStudents(5).active(true).teacher(martin)
                .workTypes(Set.of(WorkType.DEVELOPPEMENT))
                .requiredSkills(new HashSet<>(List.of(skills.get(10), skills.get(11), skills.get(0))))
                .keywords(new HashSet<>(List.of(keywords.get(2), keywords.get(16), keywords.get(17))))
                .build(),

            // 7. Optimisation des Algorithmes — Martin
            Subject.builder()
                .title("Optimisation des Algorithmes")
                .description("Complexité, structures de données et algorithmes avancés.")
                .credits(3).maxStudents(5).active(true).teacher(martin)
                .workTypes(Set.of(WorkType.RECHERCHE))
                .requiredSkills(new HashSet<>(List.of(skills.get(0), skills.get(1), skills.get(12))))
                .keywords(new HashSet<>(List.of(keywords.get(18), keywords.get(19), keywords.get(20))))
                .build(),

            // 8. Sécurité des Applications Web — Bernard
            Subject.builder()
                .title("Sécurité des Applications Web")
                .description("OWASP, injections SQL, XSS et bonnes pratiques de sécurité.")
                .credits(3).maxStudents(5).active(true).teacher(bernard)
                .workTypes(Set.of(WorkType.ANALYSE))
                .requiredSkills(new HashSet<>(List.of(skills.get(13), skills.get(3), skills.get(2))))
                .keywords(new HashSet<>(List.of(keywords.get(21), keywords.get(22), keywords.get(1))))
                .build(),

            // 9. Audit de Sécurité — Bernard
            Subject.builder()
                .title("Audit de Sécurité")
                .description("Techniques d'audit, tests d'intrusion et rapports de vulnérabilité.")
                .credits(3).maxStudents(5).active(true).teacher(bernard)
                .workTypes(Set.of(WorkType.ANALYSE))
                .requiredSkills(new HashSet<>(List.of(skills.get(14), skills.get(15), skills.get(13))))
                .keywords(new HashSet<>(List.of(keywords.get(23), keywords.get(24), keywords.get(21))))
                .build(),

            // 10. Algèbre Linéaire — Bernard
            Subject.builder()
                .title("Algèbre Linéaire")
                .description("Vecteurs, matrices et applications en informatique.")
                .credits(3).maxStudents(5).active(true).teacher(bernard)
                .workTypes(Set.of(WorkType.RECHERCHE))
                .requiredSkills(new HashSet<>(List.of(skills.get(1), skills.get(16), skills.get(12))))
                .keywords(new HashSet<>(List.of(keywords.get(25), keywords.get(26), keywords.get(27))))
                .build()
        );
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
        Teacher jean    = teachers.get(0);
        Teacher martin  = teachers.get(1);
        Teacher bernard = teachers.get(2);
        Teacher dubois  = teachers.get(3);

        // Skills : 0=Java,1=Python,2=SQL,3=JavaScript,4=CSS,5=R,6=TensorFlow,7=Scikit-learn,
        //          8=LaTeX,9=Spring Boot,10=Docker,11=Kubernetes,12=Algorithmique,
        //          13=OWASP,14=Pentest,15=Réseaux,16=MATLAB,17=UX Design
        // Keywords: 0=IA,1=Web,2=Cloud,3=Interface,4=Ergonomie,5=Design,6=Data,
        //           7=Statistiques,8=Visualisation,9=Deep Learning,10=Modélisation,
        //           11=Recherche,14=Architecture,15=Microservices,16=DevOps,17=Infrastructure,
        //           18=Optimisation,21=Sécurité,22=Vulnérabilité,23=Audit,24=Intrusion

        return Arrays.asList(

            // 1. Jean
            Project.builder()
                .title("Système de Gestion des Bibliothèques")
                .description("Application web de gestion des livres, emprunts et adhérents d'une bibliothèque.")
                .workTypes(Set.of(WorkType.DEVELOPPEMENT))
                .maxStudents(2).minStudents(1).active(true).teacher(jean)
                .requiredSkills(new HashSet<>(List.of(skills.get(0), skills.get(2), skills.get(9))))
                .keywords(new HashSet<>(List.of(keywords.get(14), keywords.get(6), keywords.get(1))))
                .build(),

            // 2. Jean
            Project.builder()
                .title("Système de Gestion des Athlètes")
                .description("Plateforme de suivi des performances, entraînements et résultats sportifs.")
                .workTypes(Set.of(WorkType.DEVELOPPEMENT))
                .maxStudents(2).minStudents(1).active(true).teacher(jean)
                .requiredSkills(new HashSet<>(List.of(skills.get(0), skills.get(2), skills.get(3))))
                .keywords(new HashSet<>(List.of(keywords.get(6), keywords.get(8), keywords.get(1))))
                .build(),

            // 3. Martin
            Project.builder()
                .title("Système de Gestion des Pièces Détachées")
                .description("Application de gestion de stock, commandes et fournisseurs de pièces automobiles.")
                .workTypes(Set.of(WorkType.DEVELOPPEMENT))
                .maxStudents(2).minStudents(1).active(true).teacher(martin)
                .requiredSkills(new HashSet<>(List.of(skills.get(0), skills.get(2), skills.get(9))))
                .keywords(new HashSet<>(List.of(keywords.get(14), keywords.get(15), keywords.get(6))))
                .build(),

            // 4. Bernard
            Project.builder()
                .title("Sécurité et Audit d'un Système d'Information")
                .description("Analyse, audit et renforcement de la sécurité d'une infrastructure réseau existante.")
                .workTypes(Set.of(WorkType.ANALYSE))
                .maxStudents(2).minStudents(1).active(true).teacher(bernard)
                .requiredSkills(new HashSet<>(List.of(skills.get(13), skills.get(14), skills.get(15))))
                .keywords(new HashSet<>(List.of(keywords.get(21), keywords.get(23), keywords.get(22))))
                .build(),

            // 5. Bernard
            Project.builder()
                .title("Automatisation OWASP Top 10 sur Projet Web")
                .description("Implémentation d'un outil qui automatise les tests de sécurité OWASP Top 10.")
                .workTypes(Set.of(WorkType.ANALYSE))
                .maxStudents(2).minStudents(1).active(true).teacher(bernard)
                .requiredSkills(new HashSet<>(List.of(skills.get(13), skills.get(3), skills.get(1))))
                .keywords(new HashSet<>(List.of(keywords.get(21), keywords.get(1), keywords.get(22))))
                .build(),

            // 6. Martin
            Project.builder()
                .title("Plateforme de E-learning")
                .description("Système de cours en ligne avec gestion des utilisateurs, modules et évaluations.")
                .workTypes(Set.of(WorkType.DEVELOPPEMENT))
                .maxStudents(2).minStudents(1).active(true).teacher(martin)
                .requiredSkills(new HashSet<>(List.of(skills.get(3), skills.get(4), skills.get(0))))
                .keywords(new HashSet<>(List.of(keywords.get(1), keywords.get(3), keywords.get(5))))
                .build(),

            // 7. Jean
            Project.builder()
                .title("Système de Recommandation par IA")
                .description("Moteur de recommandation basé sur le machine learning pour une boutique en ligne.")
                .workTypes(Set.of(WorkType.RECHERCHE))
                .maxStudents(2).minStudents(1).active(true).teacher(jean)
                .requiredSkills(new HashSet<>(List.of(skills.get(1), skills.get(6), skills.get(7))))
                .keywords(new HashSet<>(List.of(keywords.get(0), keywords.get(9), keywords.get(10))))
                .build(),

            // 8. Martin
            Project.builder()
                .title("Tableau de Bord Analytique pour PME")
                .description("Dashboard interactif de visualisation de données et KPIs pour une PME.")
                .workTypes(Set.of(WorkType.ANALYSE))
                .maxStudents(2).minStudents(1).active(true).teacher(martin)
                .requiredSkills(new HashSet<>(List.of(skills.get(1), skills.get(2), skills.get(5))))
                .keywords(new HashSet<>(List.of(keywords.get(6), keywords.get(8), keywords.get(7))))
                .build(),

            // 9. Dubois
            Project.builder()
                .title("Déploiement Cloud d'une Application Microservices")
                .description("Containerisation et déploiement Kubernetes d'une application à architecture microservices.")
                .workTypes(Set.of(WorkType.DEVELOPPEMENT))
                .maxStudents(2).minStudents(1).active(true).teacher(dubois)
                .requiredSkills(new HashSet<>(List.of(skills.get(10), skills.get(11), skills.get(1))))
                .keywords(new HashSet<>(List.of(keywords.get(2), keywords.get(16), keywords.get(17))))
                .build(),

            // 10. Dubois
            Project.builder()
                .title("Détection d'Intrusions par Machine Learning")
                .description("Modèle de détection d'anomalies réseau en temps réel à base d'apprentissage automatique.")
                .workTypes(Set.of(WorkType.RECHERCHE))
                .maxStudents(2).minStudents(1).active(true).teacher(dubois)
                .requiredSkills(new HashSet<>(List.of(skills.get(1), skills.get(6), skills.get(15))))
                .keywords(new HashSet<>(List.of(keywords.get(21), keywords.get(0), keywords.get(24))))
                .build()
        );
    }

    private List<Skill> createSkills() {
        return Arrays.asList(
            // Index 0-2 : Base
            Skill.builder().name("Java").category("Dev").active(true).build(),
            Skill.builder().name("Python").category("Dev").active(true).build(),
            Skill.builder().name("SQL").category("Data").active(true).build(),
            // Index 3-5 : Frontend / Data
            Skill.builder().name("JavaScript").category("Dev").active(true).build(),
            Skill.builder().name("CSS").category("Frontend").active(true).build(),
            Skill.builder().name("R").category("Data").active(true).build(),
            // Index 6-8 : IA / Recherche
            Skill.builder().name("TensorFlow").category("IA").active(true).build(),
            Skill.builder().name("Scikit-learn").category("IA").active(true).build(),
            Skill.builder().name("LaTeX").category("Recherche").active(true).build(),
            // Index 9-11 : Dev / DevOps
            Skill.builder().name("Spring Boot").category("Dev").active(true).build(),
            Skill.builder().name("Docker").category("DevOps").active(true).build(),
            Skill.builder().name("Kubernetes").category("DevOps").active(true).build(),
            // Index 12-14 : Algo / Sécurité
            Skill.builder().name("Algorithmique").category("Informatique").active(true).build(),
            Skill.builder().name("OWASP").category("Sécurité").active(true).build(),
            Skill.builder().name("Pentest").category("Sécurité").active(true).build(),
            // Index 15-17 : Sécurité / Maths / Design
            Skill.builder().name("Réseaux").category("Sécurité").active(true).build(),
            Skill.builder().name("MATLAB").category("Mathématiques").active(true).build(),
            Skill.builder().name("UX Design").category("Design").active(true).build()
        );
    }

    private List<Keyword> createKeywords() {
        return Arrays.asList(
            // Index 0-2 : Base
            Keyword.builder().label("IA").domain("IA").active(true).build(),
            Keyword.builder().label("Web").domain("Web").active(true).build(),
            Keyword.builder().label("Cloud").domain("Infrastructure").active(true).build(),
            // Index 3-5 : IHM
            Keyword.builder().label("Interface").domain("Design").active(true).build(),
            Keyword.builder().label("Ergonomie").domain("Design").active(true).build(),
            Keyword.builder().label("Design").domain("Design").active(true).build(),
            // Index 6-8 : Data
            Keyword.builder().label("Data").domain("Data").active(true).build(),
            Keyword.builder().label("Statistiques").domain("Data").active(true).build(),
            Keyword.builder().label("Visualisation").domain("Data").active(true).build(),
            // Index 9-10 : ML
            Keyword.builder().label("Deep Learning").domain("IA").active(true).build(),
            Keyword.builder().label("Modélisation").domain("IA").active(true).build(),
            // Index 11-13 : Recherche
            Keyword.builder().label("Recherche").domain("Académique").active(true).build(),
            Keyword.builder().label("Publication").domain("Académique").active(true).build(),
            Keyword.builder().label("Méthodologie").domain("Académique").active(true).build(),
            // Index 14-15 : Architecture JEE
            Keyword.builder().label("Architecture").domain("Dev").active(true).build(),
            Keyword.builder().label("Microservices").domain("Dev").active(true).build(),
            // Index 16-17 : Cloud
            Keyword.builder().label("DevOps").domain("Infrastructure").active(true).build(),
            Keyword.builder().label("Infrastructure").domain("Infrastructure").active(true).build(),
            // Index 18-20 : Optimisation
            Keyword.builder().label("Optimisation").domain("Informatique").active(true).build(),
            Keyword.builder().label("Complexité").domain("Informatique").active(true).build(),
            Keyword.builder().label("Performance").domain("Informatique").active(true).build(),
            // Index 21-22 : Sécurité Web
            Keyword.builder().label("Sécurité").domain("Sécurité").active(true).build(),
            Keyword.builder().label("Vulnérabilité").domain("Sécurité").active(true).build(),
            // Index 23-24 : Audit
            Keyword.builder().label("Audit").domain("Sécurité").active(true).build(),
            Keyword.builder().label("Intrusion").domain("Sécurité").active(true).build(),
            // Index 25-27 : Algèbre
            Keyword.builder().label("Algèbre").domain("Mathématiques").active(true).build(),
            Keyword.builder().label("Matrices").domain("Mathématiques").active(true).build(),
            Keyword.builder().label("Calcul").domain("Mathématiques").active(true).build()
        );
    }
}