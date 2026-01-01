package fr.amu.bestchoice.service.implementation.user;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final TeacherMapper teacherMapper;

    // ==================== CREATE & UPDATE ====================

    @Transactional
    public TeacherResponse create(Long userId, TeacherCreateRequest dto) {
        log.info("Début création profil enseignant : userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Utilisateur introuvable : userId={}", userId);
                    return new NotFoundException("Utilisateur introuvable avec l'ID : " + userId);
                });

        if (teacherRepository.existsById(userId)) {
            log.warn("Tentative de création d'un profil enseignant existant : userId={}", userId);
            throw new BusinessException("Cet utilisateur a déjà un profil enseignant");
        }

        log.debug("Utilisateur trouvé : email={}", user.getEmail());

        Teacher teacher = teacherMapper.toEntity(dto);
        teacher.setId(userId);
        teacher.setUser(user);

        log.debug("Teacher mappé : userId={}", teacher.getId());

        Teacher savedTeacher = teacherRepository.save(teacher);
        log.info("Profil enseignant créé avec succès : id={}, department={}",
                savedTeacher.getId(), savedTeacher.getDepartment());

        return toTeacherResponse(savedTeacher);
    }

    @Transactional
    public TeacherResponse update(Long id, TeacherUpdateRequest dto) {
        log.info("Début mise à jour profil enseignant : id={}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Profil enseignant introuvable : id={}", id);
                    return new NotFoundException("Profil enseignant introuvable avec l'ID : " + id);
                });

        log.debug("Profil enseignant trouvé : userId={}", teacher.getId());
        teacherMapper.updateEntityFromDto(dto, teacher);
        log.debug("Profil enseignant après mise à jour : department={}", teacher.getDepartment());

        Teacher updatedTeacher = teacherRepository.save(teacher);
        log.info("Profil enseignant mis à jour avec succès : id={}", updatedTeacher.getId());

        return toTeacherResponse(updatedTeacher);
    }

    // ==================== READ ====================

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
     * ⚙️ Récupère tous les profils enseignants avec pagination.
     */
    public Page<TeacherResponse> findAll(int page, int size, String sortBy, String sortDirection) {

        log.debug("⚙️ Récupération enseignants paginée : page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);

        Pageable pageable = createPageable(page, size, sortBy, sortDirection);
        Page<Teacher> teachersPage = teacherRepository.findAll(pageable);

        log.info("⚙️ Page d'enseignants récupérée : page={}/{}, total={}",
                teachersPage.getNumber() + 1, teachersPage.getTotalPages(), teachersPage.getTotalElements());

        return teachersPage.map(this::toTeacherResponse);
    }


    public List<TeacherResponse> findAll() {
        log.debug("Récupération de tous les profils enseignants");
        List<Teacher> teachers = teacherRepository.findAll();
        log.info("Nombre de profils enseignants trouvés : {}", teachers.size());
        return teachers.stream()
                .map(this::toTeacherResponse)
                .collect(Collectors.toList());
    }

    // ==================== MÉTHODES PRIVÉES ====================

    private TeacherResponse toTeacherResponse(Teacher teacher) {
        TeacherResponse response = teacherMapper.toResponse(teacher);

        Set<String> projectTitles = teacher.getProjects().stream()
                .map(project -> project.getTitle())
                .collect(Collectors.toSet());

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
                projectTitles
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