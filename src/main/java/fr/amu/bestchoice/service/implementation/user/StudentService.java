package fr.amu.bestchoice.service.implementation.user;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final KeywordRepository keywordRepository;
    private final StudentMapper studentMapper;

    // ==================== CREATE & UPDATE ====================

    @Transactional
    public StudentResponse create(Long userId, StudentCreateRequest dto) {
        log.info("Début création profil étudiant : userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Utilisateur introuvable : userId={}", userId);
                    return new NotFoundException("Utilisateur introuvable avec l'ID : " + userId);
                });

        if (studentRepository.existsById(userId)) {
            log.warn("Tentative de création d'un profil étudiant existant : userId={}", userId);
            throw new BusinessException("Cet utilisateur a déjà un profil étudiant");
        }

        log.debug("Utilisateur trouvé : email={}", user.getEmail());

        Student student = studentMapper.toEntity(dto);
        student.setId(userId);
        student.setUser(user);

        log.debug("Student mappé : userId={}", student.getId());

        if (dto.skill() != null && !dto.skill().isEmpty()) {
            log.debug("Résolution de {} compétences", dto.skill().size());
            Set<Skill> skills = resolveSkills(dto.skill());
            student.setSkills(skills);
            log.info("Compétences résolues : {} compétences trouvées", skills.size());
        } else {
            student.setSkills(new HashSet<>());
            log.debug("Aucune compétence à résoudre");
        }

        if (dto.interestKeyword() != null && !dto.interestKeyword().isEmpty()) {
            log.debug("Résolution de {} centres d'intérêt", dto.interestKeyword().size());
            Set<Keyword> interests = resolveKeywords(dto.interestKeyword());
            student.setInterests(interests);
            log.info("Centres d'intérêt résolus : {} mots-clés trouvés", interests.size());
        } else {
            student.setInterests(new HashSet<>());
            log.debug("Aucun centre d'intérêt à résoudre");
        }

        boolean isComplete = calculateProfileComplete(student);
        student.setProfileComplete(isComplete);
        log.debug("Profil complet : {}", isComplete);

        Student savedStudent = studentRepository.save(student);
        log.info("Profil étudiant créé avec succès : id={}, profileComplete={}",
                savedStudent.getId(), savedStudent.getProfileComplete());

        return toStudentResponse(savedStudent);
    }

    @Transactional
    public StudentResponse update(Long id, StudentUpdateRequest dto) {
        log.info("Début mise à jour profil étudiant : id={}", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Profil étudiant introuvable : id={}", id);
                    return new NotFoundException("Profil étudiant introuvable avec l'ID : " + id);
                });

        log.debug("Profil étudiant trouvé : userId={}", student.getId());
        studentMapper.updateEntityFromDto(dto, student);

        if (dto.skill() != null) {
            if (!dto.skill().isEmpty()) {
                log.debug("Mise à jour des compétences : {} compétences", dto.skill().size());
                Set<Skill> skills = resolveSkills(dto.skill());
                student.setSkills(skills);
                log.info("Compétences mises à jour : {} compétences", skills.size());
            } else {
                log.debug("Suppression de toutes les compétences");
                student.setSkills(new HashSet<>());
            }
        }

        if (dto.interestKeyword() != null) {
            if (!dto.interestKeyword().isEmpty()) {
                log.debug("Mise à jour des centres d'intérêt : {} mots-clés", dto.interestKeyword().size());
                Set<Keyword> interests = resolveKeywords(dto.interestKeyword());
                student.setInterests(interests);
                log.info("Centres d'intérêt mis à jour : {} mots-clés", interests.size());
            } else {
                log.debug("Suppression de tous les centres d'intérêt");
                student.setInterests(new HashSet<>());
            }
        }

        boolean isComplete = calculateProfileComplete(student);
        student.setProfileComplete(isComplete);
        log.debug("Profil complet après mise à jour : {}", isComplete);

        Student updatedStudent = studentRepository.save(student);
        log.info("Profil étudiant mis à jour avec succès : id={}, profileComplete={}",
                updatedStudent.getId(), updatedStudent.getProfileComplete());

        return toStudentResponse(updatedStudent);
    }

    // ==================== READ ====================

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

    // ⚙️ NOUVELLE MÉTHODE PAGINÉE
    /**
     * ⚙️ Récupère tous les profils étudiants avec pagination.
     */
    public Page<StudentResponse> findAll(int page, int size, String sortBy, String sortDirection) {

        log.debug("⚙️ Récupération étudiants paginée : page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);

        Pageable pageable = createPageable(page, size, sortBy, sortDirection);
        Page<Student> studentsPage = studentRepository.findAll(pageable);

        log.info("⚙️ Page d'étudiants récupérée : page={}/{}, total={}",
                studentsPage.getNumber() + 1, studentsPage.getTotalPages(), studentsPage.getTotalElements());

        return studentsPage.map(this::toStudentResponse);
    }

    // ANCIENNE MÉTHODE (rétrocompatibilité)
    public List<StudentResponse> findAll() {
        log.debug("Récupération de tous les profils étudiants");
        List<Student> students = studentRepository.findAll();
        log.info("Nombre de profils étudiants trouvés : {}", students.size());
        return students.stream()
                .map(this::toStudentResponse)
                .collect(Collectors.toList());
    }

    public List<StudentResponse> findAllComplete() {
        log.debug("Récupération des profils étudiants complets uniquement");
        List<Student> students = studentRepository.findByProfileCompleteTrue();
        log.info("Nombre de profils complets trouvés : {}", students.size());
        return students.stream()
                .map(this::toStudentResponse)
                .collect(Collectors.toList());
    }

    // ==================== MÉTHODES PRIVÉES ====================

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

    private StudentResponse toStudentResponse(Student student) {
        StudentResponse response = studentMapper.toResponse(student);

        Set<String> skillNames = student.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toSet());

        Set<String> interestLabels = student.getInterests().stream()
                .map(Keyword::getLabel)
                .collect(Collectors.toSet());

        return new StudentResponse(
                response.id(),
                response.userId(),
                response.email(),
                response.firstName(),
                response.lastName(),
                response.studentNumber(),
                response.studyYear(),
                response.preferredWorkType(),
                skillNames,
                interestLabels,
                response.githubUrl(),
                response.linkedinUrl(),
                response.assignedProjectId()
        );
    }

    // ⚙️ MÉTHODE UTILITAIRE PRIVÉE
    /**
     * ⚙️ Crée un Pageable avec tri.
     */
    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {

        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "id";
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if ("DESC".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.DESC;
        }

        Sort sort = Sort.by(direction, sortBy);
        return PageRequest.of(page, size, sort);
    }
}