package fr.amu.bestchoice.service;

import fr.amu.bestchoice.model.entity.Teacher;
import fr.amu.bestchoice.model.entity.User;
import fr.amu.bestchoice.repository.TeacherRepository;
import fr.amu.bestchoice.repository.UserRepository;
import fr.amu.bestchoice.web.dto.teacher.TeacherCreateRequest;
import fr.amu.bestchoice.web.dto.teacher.TeacherResponse;
import fr.amu.bestchoice.web.dto.teacher.TeacherUpdateRequest;
import fr.amu.bestchoice.web.exception.BusinessException;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.TeacherMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service de gestion des profils enseignants (Teachers).
 *
 * Opérations disponibles :
 * - Créer un profil enseignant (lié à un User existant)
 * - Modifier un profil enseignant
 * - Récupérer un profil par ID
 * - Récupérer tous les profils
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherService {

    // ==================== DÉPENDANCES ====================

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final TeacherMapper teacherMapper;

    // ==================== CREATE ====================

    /**
     * Crée un profil enseignant pour un utilisateur existant.
     *
     * @param userId L'ID de l'utilisateur (doit avoir le rôle ENSEIGNANT)
     * @param dto Les données du profil enseignant
     * @return TeacherResponse avec les données du profil créé
     * @throws NotFoundException Si l'utilisateur n'existe pas
     * @throws BusinessException Si l'utilisateur a déjà un profil enseignant
     */
    @Transactional
    public TeacherResponse create(Long userId, TeacherCreateRequest dto) {

        log.info("Début création profil enseignant : userId={}", userId);

        // ===== VALIDATION UTILISATEUR =====

        // Vérifier que l'utilisateur existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Utilisateur introuvable : userId={}", userId);
                    return new NotFoundException("Utilisateur introuvable avec l'ID : " + userId);
                });

        // Vérifier que l'utilisateur n'a pas déjà un profil enseignant
        if (teacherRepository.existsById(userId)) {
            log.warn("Tentative de création d'un profil enseignant existant : userId={}", userId);
            throw new BusinessException("Cet utilisateur a déjà un profil enseignant");
        }

        log.debug("Utilisateur trouvé : email={}", user.getEmail());

        // ===== MAPPING DTO → ENTITY =====

        Teacher teacher = teacherMapper.toEntity(dto);
        teacher.setId(userId); // L'ID du Teacher = ID du User (@MapsId)
        teacher.setUser(user);

        log.debug("Teacher mappé : userId={}", teacher.getId());

        // ===== SAUVEGARDE =====

        Teacher savedTeacher = teacherRepository.save(teacher);

        log.info("Profil enseignant créé avec succès : id={}, department={}",
                savedTeacher.getId(), savedTeacher.getDepartment());

        // ===== MAPPING ENTITY → DTO =====

        return toTeacherResponse(savedTeacher);
    }

    // ==================== UPDATE ====================

    /**
     * Met à jour un profil enseignant existant.
     *
     * @param id L'ID du profil enseignant (= ID de l'utilisateur)
     * @param dto Les nouvelles données
     * @return TeacherResponse avec les données mises à jour
     * @throws NotFoundException Si le profil n'existe pas
     */
    @Transactional
    public TeacherResponse update(Long id, TeacherUpdateRequest dto) {

        log.info("Début mise à jour profil enseignant : id={}", id);

        // ===== RÉCUPÉRATION =====

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Profil enseignant introuvable : id={}", id);
                    return new NotFoundException("Profil enseignant introuvable avec l'ID : " + id);
                });

        log.debug("Profil enseignant trouvé : userId={}", teacher.getId());

        // ===== MAPPING DTO → ENTITY =====

        teacherMapper.updateEntityFromDto(dto, teacher);

        log.debug("Profil enseignant après mise à jour : department={}", teacher.getDepartment());

        // ===== SAUVEGARDE =====

        Teacher updatedTeacher = teacherRepository.save(teacher);

        log.info("Profil enseignant mis à jour avec succès : id={}", updatedTeacher.getId());

        // ===== MAPPING ENTITY → DTO =====

        return toTeacherResponse(updatedTeacher);
    }

    // ==================== READ ====================

    /**
     * Récupère un profil enseignant par son ID.
     *
     * @param id L'ID du profil enseignant
     * @return TeacherResponse
     * @throws NotFoundException Si le profil n'existe pas
     */
    public TeacherResponse findById(Long id) {

        log.debug("Recherche profil enseignant par ID : id={}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Profil enseignant introuvable : id={}", id);
                    return new NotFoundException("Profil enseignant introuvable avec l'ID : " + id);
                });

        log.debug("Profil enseignant trouvé : userId={}", teacher.getId());

        return toTeacherResponse(teacher);
    }

    /**
     * Récupère tous les profils enseignants.
     *
     * @return Liste de TeacherResponse
     */
    public List<TeacherResponse> findAll() {

        log.debug("Récupération de tous les profils enseignants");

        List<Teacher> teachers = teacherRepository.findAll();

        log.info("Nombre de profils enseignants trouvés : {}", teachers.size());

        return teachers.stream()
                .map(this::toTeacherResponse)
                .collect(Collectors.toList());
    }

    // ==================== MÉTHODES PRIVÉES ====================

    /**
     * Convertit Teacher en TeacherResponse.
     *
     * Le mapper ne peut pas gérer la conversion List<Project> → Set<String> automatiquement,
     * donc on le fait manuellement ici.
     *
     * @param teacher L'entité Teacher
     * @return TeacherResponse
     */
    private TeacherResponse toTeacherResponse(Teacher teacher) {

        // Mapping de base (sans les titres des projets)
        TeacherResponse response = teacherMapper.toResponse(teacher);

        // Extraire les titres des projets (List<Project> → Set<String>)
        Set<String> projectTitles = teacher.getProjects().stream()
                .map(project -> project.getTitle())
                .collect(Collectors.toSet());

        // Créer le TeacherResponse complet avec tous les champs
        return new TeacherResponse(
                response.id(),
                response.userId(),
                response.email(),
                response.firstName(),
                response.lastName(),
                response.department(),
                response.academicRank(),
                response.specialty(),
                response.websiteUrl(),
                projectTitles  // Set<String> des titres de projets
        );
    }
}