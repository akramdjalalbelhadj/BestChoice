package fr.amu.bestchoice.service;

import fr.amu.bestchoice.model.entity.Keyword;
import fr.amu.bestchoice.model.entity.Skill;
import fr.amu.bestchoice.model.entity.Student;
import fr.amu.bestchoice.model.entity.User;
import fr.amu.bestchoice.repository.KeywordRepository;
import fr.amu.bestchoice.repository.SkillRepository;
import fr.amu.bestchoice.repository.StudentRepository;
import fr.amu.bestchoice.repository.UserRepository;
import fr.amu.bestchoice.web.dto.student.StudentCreateRequest;
import fr.amu.bestchoice.web.dto.student.StudentResponse;
import fr.amu.bestchoice.web.dto.student.StudentUpdateRequest;
import fr.amu.bestchoice.web.exception.BusinessException;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service de gestion des profils étudiants (Students).
 *
 * Opérations disponibles :
 * - Créer un profil étudiant (lié à un User existant)
 * - Modifier un profil étudiant
 * - Récupérer un profil par ID
 * - Récupérer tous les profils
 * - Calculer la complétude du profil
 *
 * IMPORTANT : La résolution des Set<String> (skills, interests) → Set<Entity>
 * est gérée dans ce service, PAS dans le mapper.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    // ==================== DÉPENDANCES ====================

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final KeywordRepository keywordRepository;
    private final StudentMapper studentMapper;

    // ==================== CREATE ====================

    /**
     * Crée un profil étudiant pour un utilisateur existant.
     *
     * @param userId L'ID de l'utilisateur (doit avoir le rôle ETUDIANT)
     * @param dto Les données du profil étudiant
     * @return StudentResponse avec les données du profil créé
     * @throws NotFoundException Si l'utilisateur n'existe pas
     * @throws BusinessException Si l'utilisateur a déjà un profil étudiant
     */
    @Transactional
    public StudentResponse create(Long userId, StudentCreateRequest dto) {

        log.info("Début création profil étudiant : userId={}", userId);

        // ===== VALIDATION UTILISATEUR =====

        // Vérifier que l'utilisateur existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Utilisateur introuvable : userId={}", userId);
                    return new NotFoundException("Utilisateur introuvable avec l'ID : " + userId);
                });

        // Vérifier que l'utilisateur n'a pas déjà un profil étudiant
        if (studentRepository.existsById(userId)) {
            log.warn("Tentative de création d'un profil étudiant existant : userId={}", userId);
            throw new BusinessException("Cet utilisateur a déjà un profil étudiant");
        }

        log.debug("Utilisateur trouvé : email={}", user.getEmail());

        // ===== MAPPING DTO → ENTITY =====

        Student student = studentMapper.toEntity(dto);
        student.setId(userId); // L'ID du Student = ID du User (@MapsId)
        student.setUser(user);

        log.debug("Student mappé : userId={}", student.getId());

        // ===== RÉSOLUTION DES SKILLS (Set<String> → Set<Skill>) =====

        if (dto.skill() != null && !dto.skill().isEmpty()) {

            log.debug("Résolution de {} compétences", dto.skill().size());

            Set<Skill> skills = resolveSkills(dto.skill());
            student.setSkills(skills);

            log.info("Compétences résolues : {} compétences trouvées", skills.size());
        } else {
            student.setSkills(new HashSet<>());
            log.debug("Aucune compétence à résoudre");
        }

        // ===== RÉSOLUTION DES INTERESTS (Set<String> → Set<Keyword>) =====

        if (dto.interestKeyword() != null && !dto.interestKeyword().isEmpty()) {

            log.debug("Résolution de {} centres d'intérêt", dto.interestKeyword().size());

            Set<Keyword> interests = resolveKeywords(dto.interestKeyword());
            student.setInterests(interests);

            log.info("Centres d'intérêt résolus : {} mots-clés trouvés", interests.size());
        } else {
            student.setInterests(new HashSet<>());
            log.debug("Aucun centre d'intérêt à résoudre");
        }

        // ===== CALCUL COMPLÉTUDE PROFIL =====

        boolean isComplete = calculateProfileComplete(student);
        student.setProfileComplete(isComplete);

        log.debug("Profil complet : {}", isComplete);

        // ===== SAUVEGARDE =====

        Student savedStudent = studentRepository.save(student);

        log.info("Profil étudiant créé avec succès : id={}, profileComplete={}",
                savedStudent.getId(), savedStudent.getProfileComplete());

        // ===== MAPPING ENTITY → DTO =====

        return toStudentResponse(savedStudent);
    }

    // ==================== UPDATE ====================

    /**
     * Met à jour un profil étudiant existant.
     *
     * @param id L'ID du profil étudiant (= ID de l'utilisateur)
     * @param dto Les nouvelles données
     * @return StudentResponse avec les données mises à jour
     * @throws NotFoundException Si le profil n'existe pas
     */
    @Transactional
    public StudentResponse update(Long id, StudentUpdateRequest dto) {

        log.info("Début mise à jour profil étudiant : id={}", id);

        // ===== RÉCUPÉRATION =====

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Profil étudiant introuvable : id={}", id);
                    return new NotFoundException("Profil étudiant introuvable avec l'ID : " + id);
                });

        log.debug("Profil étudiant trouvé : userId={}", student.getId());

        // ===== MAPPING DTO → ENTITY =====

        studentMapper.updateEntityFromDto(dto, student);

        // ===== RÉSOLUTION DES SKILLS (si fournis) =====

        if (dto.skill() != null) {

            if (!dto.skill().isEmpty()) {
                log.debug("Mise à jour des compétences : {} compétences", dto.skill().size());
                Set<Skill> skills = resolveSkills(dto.skill());
                student.setSkills(skills);
                log.info("Compétences mises à jour : {} compétences", skills.size());
            } else {
                // Si Set vide fourni, supprimer toutes les compétences
                log.debug("Suppression de toutes les compétences");
                student.setSkills(new HashSet<>());
            }
        }

        // ===== RÉSOLUTION DES INTERESTS (si fournis) =====

        if (dto.interestKeyword() != null) {

            if (!dto.interestKeyword().isEmpty()) {
                log.debug("Mise à jour des centres d'intérêt : {} mots-clés", dto.interestKeyword().size());
                Set<Keyword> interests = resolveKeywords(dto.interestKeyword());
                student.setInterests(interests);
                log.info("Centres d'intérêt mis à jour : {} mots-clés", interests.size());
            } else {
                // Si Set vide fourni, supprimer tous les centres d'intérêt
                log.debug("Suppression de tous les centres d'intérêt");
                student.setInterests(new HashSet<>());
            }
        }

        // ===== RECALCUL COMPLÉTUDE PROFIL =====

        boolean isComplete = calculateProfileComplete(student);
        student.setProfileComplete(isComplete);

        log.debug("Profil complet après mise à jour : {}", isComplete);

        // ===== SAUVEGARDE =====

        Student updatedStudent = studentRepository.save(student);

        log.info("Profil étudiant mis à jour avec succès : id={}, profileComplete={}",
                updatedStudent.getId(), updatedStudent.getProfileComplete());

        // ===== MAPPING ENTITY → DTO =====

        return toStudentResponse(updatedStudent);
    }

    // ==================== READ ====================

    /**
     * Récupère un profil étudiant par son ID.
     *
     * @param id L'ID du profil étudiant
     * @return StudentResponse
     * @throws NotFoundException Si le profil n'existe pas
     */
    public StudentResponse findById(Long id) {

        log.debug("Recherche profil étudiant par ID : id={}", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Profil étudiant introuvable : id={}", id);
                    return new NotFoundException("Profil étudiant introuvable avec l'ID : " + id);
                });

        log.debug("Profil étudiant trouvé : userId={}", student.getId());

        return toStudentResponse(student);
    }

    /**
     * Récupère tous les profils étudiants.
     *
     * @return Liste de StudentResponse
     */
    public List<StudentResponse> findAll() {

        log.debug("Récupération de tous les profils étudiants");

        List<Student> students = studentRepository.findAll();

        log.info("Nombre de profils étudiants trouvés : {}", students.size());

        return students.stream()
                .map(this::toStudentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère tous les profils étudiants complets uniquement.
     *
     * @return Liste de StudentResponse
     */
    public List<StudentResponse> findAllComplete() {

        log.debug("Récupération des profils étudiants complets uniquement");

        List<Student> students = studentRepository.findByProfileCompleteTrue();

        log.info("Nombre de profils complets trouvés : {}", students.size());

        return students.stream()
                .map(this::toStudentResponse)
                .collect(Collectors.toList());
    }

    // ==================== MÉTHODES PRIVÉES ====================

    /**
     * Résout un Set de noms de compétences en Set d'entités Skill.
     *
     * @param skillNames Les noms des compétences (ex: ["Java", "Python"])
     * @return Set<Skill>
     * @throws NotFoundException Si une compétence n'existe pas
     */
    private Set<Skill> resolveSkills(Set<String> skillNames) {

        Set<Skill> skills = new HashSet<>();

        for (String skillName : skillNames) {
            Skill skill = skillRepository.findByName(skillName)
                    .orElseThrow(() -> {
                        log.error("Compétence introuvable : name={}", skillName);
                        return new NotFoundException("Compétence introuvable : " + skillName);
                    });
            skills.add(skill);
            log.debug("Compétence résolue : name={}, id={}", skillName, skill.getId());
        }

        return skills;
    }

    /**
     * Résout un Set de labels de mots-clés en Set d'entités Keyword.
     *
     * @param keywordLabels Les labels des mots-clés (ex: ["IA", "ML"])
     * @return Set<Keyword>
     * @throws NotFoundException Si un mot-clé n'existe pas
     */
    private Set<Keyword> resolveKeywords(Set<String> keywordLabels) {

        Set<Keyword> keywords = new HashSet<>();

        for (String label : keywordLabels) {
            Keyword keyword = keywordRepository.findByLabel(label)
                    .orElseThrow(() -> {
                        log.error("Mot-clé introuvable : label={}", label);
                        return new NotFoundException("Mot-clé introuvable : " + label);
                    });
            keywords.add(keyword);
            log.debug("Mot-clé résolu : label={}, id={}", label, keyword.getId());
        }

        return keywords;
    }

    /**
     * Calcule si le profil étudiant est complet.
     *
     * Un profil est considéré complet si :
     * - Le programme est renseigné
     * - L'année d'étude est renseignée
     * - Au moins 1 compétence est renseignée
     * - Au moins 1 centre d'intérêt est renseigné
     * - Le type de travail préféré est renseigné
     *
     * @param student Le profil étudiant
     * @return true si complet, false sinon
     */
    private boolean calculateProfileComplete(Student student) {

        boolean hasProgram = student.getProgram() != null;
        boolean hasStudyYear = student.getStudyYear() != null;
        boolean hasSkills = student.getSkills() != null && !student.getSkills().isEmpty();
        boolean hasInterests = student.getInterests() != null && !student.getInterests().isEmpty();
        boolean hasPreferredWorkType = student.getPreferredWorkType() != null;

        boolean isComplete = hasProgram && hasStudyYear && hasSkills && hasInterests && hasPreferredWorkType;

        log.debug("Calcul complétude profil : program={}, studyYear={}, skills={}, interests={}, workType={} → {}",
                hasProgram, hasStudyYear, hasSkills, hasInterests, hasPreferredWorkType, isComplete);

        return isComplete;
    }

    /**
     * Convertit Student en StudentResponse.
     *
     * Le mapper ne peut pas gérer la conversion Set<Skill> → Set<String> automatiquement,
     * donc on le fait manuellement ici.
     *
     * @param student L'entité Student
     * @return StudentResponse
     */
    private StudentResponse toStudentResponse(Student student) {

        // Mapping de base (sans skills et interests)
        StudentResponse response = studentMapper.toResponse(student);

        // Extraire les noms des compétences (Set<Skill> → Set<String>)
        Set<String> skillNames = student.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toSet());

        // Extraire les labels des centres d'intérêt (Set<Keyword> → Set<String>)
        Set<String> interestLabels = student.getInterests().stream()
                .map(Keyword::getLabel)
                .collect(Collectors.toSet());

        // Créer le StudentResponse complet avec tous les champs
        return new StudentResponse(
                response.id(),
                response.userId(),
                response.email(),
                response.firstName(),
                response.lastName(),
                response.studentNumber(),
                response.studyYear(),
                response.preferredWorkType(),
                skillNames,           // Set<String> des noms de compétences
                interestLabels,       // Set<String> des labels de centres d'intérêt
                response.githubUrl(),
                response.linkedinUrl(),
                response.assignedProjectId()
        );
    }
}