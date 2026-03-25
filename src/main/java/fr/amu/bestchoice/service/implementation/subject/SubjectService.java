package fr.amu.bestchoice.service.implementation.subject;

import fr.amu.bestchoice.model.entity.Keyword;
import fr.amu.bestchoice.model.entity.Skill;
import fr.amu.bestchoice.model.entity.Subject;
import fr.amu.bestchoice.model.entity.Teacher;
import fr.amu.bestchoice.repository.KeywordRepository;
import fr.amu.bestchoice.repository.SkillRepository;
import fr.amu.bestchoice.repository.SubjectRepository;
import fr.amu.bestchoice.repository.TeacherRepository;
import fr.amu.bestchoice.web.dto.subject.SubjectCreateRequest;
import fr.amu.bestchoice.web.dto.subject.SubjectResponse;
import fr.amu.bestchoice.web.exception.BusinessException;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.SubjectMapper;
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
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final SkillRepository skillRepository;
    private final KeywordRepository keywordRepository;
    private final SubjectMapper subjectMapper;

    // ==================== CREATE ====================
    @Transactional
    public SubjectResponse create(Long teacherId, SubjectCreateRequest dto) {
        log.info("Début création matière : teacherId={}, title={}", teacherId, dto.title());

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> {
                    log.error("Enseignant introuvable : teacherId={}", teacherId);
                    return new NotFoundException("Enseignant introuvable avec l'ID : " + teacherId);
                });

        // Validation métier : Capacité
        if (dto.minStudents() != null && dto.maxStudents() != null && dto.minStudents() > dto.maxStudents()) {
            log.warn("Validation échouée : minStudents={} > maxStudents={}", dto.minStudents(), dto.maxStudents());
            throw new BusinessException("Le nombre minimum d'étudiants ne peut pas être supérieur au nombre maximum");
        }

        Subject subject = subjectMapper.toEntity(dto);
        subject.setTeacher(teacher);

        // Résolution Find-or-Create pour les compétences
        if (dto.requiredSkills() != null && !dto.requiredSkills().isEmpty()) {
            log.debug("Résolution de {} compétences requises", dto.requiredSkills().size());
            subject.setRequiredSkills(resolveSkills(dto.requiredSkills()));
        }

        // Résolution Find-or-Create pour les mots-clés
        if (dto.keywords() != null && !dto.keywords().isEmpty()) {
            log.debug("Résolution de {} mots-clés", dto.keywords().size());
            subject.setKeywords(resolveKeywords(dto.keywords()));
        }

        Subject savedSubject = subjectRepository.save(subject);
        log.info("Matière créée avec succès : id={}, title={}", savedSubject.getId(), savedSubject.getTitle());

        return subjectMapper.toResponse(savedSubject);
    }

    // ==================== READ ====================

    @Transactional(readOnly = true)
    public SubjectResponse findById(Long id) {
        log.debug("Recherche matière par ID : id={}", id);
        return subjectRepository.findById(id)
                .map(subjectMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Matière introuvable avec l'ID : " + id));
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> findByTeacherId(Long teacherId) {
        log.debug("Récupération des matières pour le professeur ID : {}", teacherId);
        return subjectRepository.findByTeacherId(teacherId).stream()
                .map(subjectMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère toutes les matières avec pagination.
     */
    @Transactional(readOnly = true)
    public Page<SubjectResponse> findAll(int page, int size, String sortBy, String sortDirection) {
        log.debug("Récupération matières paginée : page={}, size={}", page, size);

        Pageable pageable = createPageable(page, size, sortBy, sortDirection);
        return subjectRepository.findAll(pageable).map(subjectMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> findAllActive() {
        return subjectRepository.findByActiveTrue().stream()
                .map(subjectMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==================== ACTIVATION / DÉSACTIVATION ====================

    @Transactional
    public void activate(Long id) {
        log.info("Activation matière : id={}", id);
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Matière introuvable"));
        subject.setActive(true);
    }

    @Transactional
    public void deactivate(Long id) {
        log.info("Désactivation matière : id={}", id);
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Matière introuvable"));
        subject.setActive(false);
    }

    // ==================== MÉTHODES PRIVÉES (LOGIQUE MÉTIER) ====================

    /**
     * Résout les compétences : les récupère si elles existent, sinon les crée.
     */
    private Set<Skill> resolveSkills(Set<String> names) {
        Set<Skill> skills = new HashSet<>();
        for (String name : names) {
            Skill skill = skillRepository.findByName(name)
                    .orElseGet(() -> {
                        log.info("Création d'une nouvelle compétence : {}", name);
                        return skillRepository.save(Skill.builder().name(name).build());
                    });
            skills.add(skill);
        }
        return skills;
    }

    /**
     * Résout les mots-clés : les récupère si ils existent, sinon les crée.
     */
    private Set<Keyword> resolveKeywords(Set<String> names) {
        Set<Keyword> keywords = new HashSet<>();
        for (String name : names) {
            Keyword keyword = keywordRepository.findByLabel(name)
                    .orElseGet(() -> {
                        log.info("Création d'un nouveau mot-clé : {}", name);
                        return keywordRepository.save(Keyword.builder().label(name).build());
                    });
            keywords.add(keyword);
        }
        return keywords;
    }

    /**
     * Crée un Pageable avec tri.
     */
    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        String finalSortBy = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(direction, finalSortBy));
    }

    private SubjectResponse toSubjectResponse(Subject entity) {
        SubjectResponse response = subjectMapper.toResponse(entity);

        // On extrait manuellement les noms pour remplir le record
        Set<String> skillNames = entity.getRequiredSkills().stream()
                .map(s -> s.getName())
                .collect(Collectors.toSet());

        Set<String> keywordNames = entity.getKeywords().stream()
                .map(k -> k.getLabel())
                .collect(Collectors.toSet());

        // On reconstruit le record avec les listes de chaînes
        return new SubjectResponse(
                response.id(),
                response.title(),
                response.description(),
                response.objectives(),
                response.workTypes(),
                response.maxStudents(),
                response.minStudents(),
                response.credits(),
                response.semester(),
                response.academicYear(),
                response.active(),
                response.teacherId(),
                response.teacherName(),
                skillNames,
                keywordNames
        );
    }
}