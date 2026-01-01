package fr.amu.bestchoice.service.implementation.project;

import fr.amu.bestchoice.model.entity.Keyword;
import fr.amu.bestchoice.model.entity.Project;
import fr.amu.bestchoice.model.entity.Skill;
import fr.amu.bestchoice.model.entity.Teacher;
import fr.amu.bestchoice.repository.KeywordRepository;
import fr.amu.bestchoice.repository.ProjectRepository;
import fr.amu.bestchoice.repository.SkillRepository;
import fr.amu.bestchoice.repository.TeacherRepository;
import fr.amu.bestchoice.web.dto.project.ProjectCreateRequest;
import fr.amu.bestchoice.web.dto.project.ProjectResponse;
import fr.amu.bestchoice.web.dto.project.ProjectUpdateRequest;
import fr.amu.bestchoice.web.exception.BusinessException;
import fr.amu.bestchoice.web.exception.NotFoundException;
import fr.amu.bestchoice.web.mapper.ProjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;              // ⚙️ AJOUT
import org.springframework.data.domain.PageRequest;       // ⚙️ AJOUT
import org.springframework.data.domain.Pageable;          // ⚙️ AJOUT
import org.springframework.data.domain.Sort;              // ⚙️ AJOUT
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
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TeacherRepository teacherRepository;
    private final SkillRepository skillRepository;
    private final KeywordRepository keywordRepository;
    private final ProjectMapper projectMapper;

    // ==================== CREATE ====================
    // ... tout le code CREATE reste inchangé ...

    @Transactional
    public ProjectResponse create(Long teacherId, ProjectCreateRequest dto) {
        log.info("Début création projet : teacherId={}, title={}", teacherId, dto.title());

        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> {
                    log.error("Enseignant introuvable : teacherId={}", teacherId);
                    return new NotFoundException("Enseignant introuvable avec l'ID : " + teacherId);
                });

        // Validation métier
        if (dto.minStudents() != null && dto.maxStudents() != null && dto.minStudents() > dto.maxStudents()) {
            log.warn("Validation échouée : minStudents={} > maxStudents={}", dto.minStudents(), dto.maxStudents());
            throw new BusinessException("Le nombre minimum d'étudiants ne peut pas être supérieur au nombre maximum");
        }

        log.debug("Enseignant trouvé : email={}", teacher.getUser().getEmail());

        Project project = projectMapper.toEntity(dto);
        project.setTeacher(teacher);

        log.debug("Project mappé : title={}", project.getTitle());

        // Résolution des compétences requises
        if (dto.requiredSkill() != null && !dto.requiredSkill().isEmpty()) {
            log.debug("Résolution de {} compétences requises", dto.requiredSkill().size());
            Set<Skill> skills = resolveSkills(dto.requiredSkill());
            project.setRequiredSkills(skills);
            log.info("Compétences requises résolues : {} compétences", skills.size());
        } else {
            project.setRequiredSkills(new HashSet<>());
            log.debug("Aucune compétence requise");
        }

        // Résolution des mots-clés
        if (dto.keyword() != null && !dto.keyword().isEmpty()) {
            log.debug("Résolution de {} mots-clés", dto.keyword().size());
            Set<Keyword> keywords = resolveKeywords(dto.keyword());
            project.setKeywords(keywords);
            log.info("Mots-clés résolus : {} mots-clés", keywords.size());
        } else {
            project.setKeywords(new HashSet<>());
            log.debug("Aucun mot-clé");
        }

        // Calcul du champ full
        boolean isFull = project.getAssignedStudents().size() >= project.getMaxStudents();
        project.setFull(isFull);

        Project savedProject = projectRepository.save(project);
        log.info("Projet créé avec succès : id={}, title={}, active={}",
                savedProject.getId(), savedProject.getTitle(), savedProject.getActive());

        return toProjectResponse(savedProject);
    }

    // ==================== UPDATE ====================
    // ... tout le code UPDATE reste inchangé ...

    @Transactional
    public ProjectResponse update(Long id, ProjectUpdateRequest dto) {
        log.info("Début mise à jour projet : id={}", id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Projet introuvable : id={}", id);
                    return new NotFoundException("Projet introuvable avec l'ID : " + id);
                });

        log.debug("Projet trouvé : title={}", project.getTitle());

        // Validation métier
        Integer newMinStudents = dto.minStudents() != null ? dto.minStudents() : project.getMinStudents();
        Integer newMaxStudents = dto.maxStudents() != null ? dto.maxStudents() : project.getMaxStudents();

        if (newMinStudents > newMaxStudents) {
            log.warn("Validation échouée : minStudents={} > maxStudents={}", newMinStudents, newMaxStudents);
            throw new BusinessException("Le nombre minimum d'étudiants ne peut pas être supérieur au nombre maximum");
        }

        projectMapper.updateEntityFromDto(dto, project);

        // Résolution des compétences (si fournis)
        if (dto.requiredSkill() != null) {
            if (!dto.requiredSkill().isEmpty()) {
                log.debug("Mise à jour des compétences requises : {} compétences", dto.requiredSkill().size());
                Set<Skill> skills = resolveSkills(dto.requiredSkill());
                project.setRequiredSkills(skills);
                log.info("Compétences requises mises à jour : {} compétences", skills.size());
            } else {
                log.debug("Suppression de toutes les compétences requises");
                project.setRequiredSkills(new HashSet<>());
            }
        }

        // Résolution des mots-clés (si fournis)
        if (dto.keyword() != null) {
            if (!dto.keyword().isEmpty()) {
                log.debug("Mise à jour des mots-clés : {} mots-clés", dto.keyword().size());
                Set<Keyword> keywords = resolveKeywords(dto.keyword());
                project.setKeywords(keywords);
                log.info("Mots-clés mis à jour : {} mots-clés", keywords.size());
            } else {
                log.debug("Suppression de tous les mots-clés");
                project.setKeywords(new HashSet<>());
            }
        }

        // Recalcul du champ full
        boolean isFull = project.getAssignedStudents().size() >= project.getMaxStudents();
        project.setFull(isFull);

        Project updatedProject = projectRepository.save(project);
        log.info("Projet mis à jour avec succès : id={}, title={}", updatedProject.getId(), updatedProject.getTitle());

        return toProjectResponse(updatedProject);
    }

    // ==================== READ ====================

    public ProjectResponse findById(Long id) {
        log.debug("Recherche projet par ID : id={}", id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Projet introuvable : id={}", id);
                    return new NotFoundException("Projet introuvable avec l'ID : " + id);
                });

        log.debug("Projet trouvé : title={}", project.getTitle());
        return toProjectResponse(project);
    }

    // ⚙️ NOUVELLE MÉTHODE PAGINÉE
    /**
     * ⚙️ Récupère tous les projets avec pagination.
     */
    public Page<ProjectResponse> findAll(int page, int size, String sortBy, String sortDirection) {

        log.debug("⚙️ Récupération projets paginée : page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);

        Pageable pageable = createPageable(page, size, sortBy, sortDirection);
        Page<Project> projectsPage = projectRepository.findAll(pageable);

        log.info("⚙️ Page de projets récupérée : page={}/{}, total={}",
                projectsPage.getNumber() + 1, projectsPage.getTotalPages(), projectsPage.getTotalElements());

        return projectsPage.map(this::toProjectResponse);
    }

    // ANCIENNE MÉTHODE (rétrocompatibilité)
    public List<ProjectResponse> findAll() {
        log.debug("Récupération de tous les projets");
        List<Project> projects = projectRepository.findAll();
        log.info("Nombre de projets trouvés : {}", projects.size());
        return projects.stream()
                .map(this::toProjectResponse)
                .collect(Collectors.toList());
    }

    public List<ProjectResponse> findAllActive() {
        log.debug("Récupération des projets actifs uniquement");
        List<Project> projects = projectRepository.findByActiveTrue();
        log.info("Nombre de projets actifs trouvés : {}", projects.size());
        return projects.stream()
                .map(this::toProjectResponse)
                .collect(Collectors.toList());
    }

    public List<ProjectResponse> findAllAvailable() {
        log.debug("Récupération des projets disponibles (actifs + non complets)");
        List<Project> projects = projectRepository.findAvailableProjects();
        log.info("Nombre de projets disponibles trouvés : {}", projects.size());
        return projects.stream()
                .map(this::toProjectResponse)
                .collect(Collectors.toList());
    }

    // ==================== ACTIVATION / DÉSACTIVATION ====================

    @Transactional
    public void activate(Long id) {
        log.info("Début activation projet : id={}", id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Projet introuvable : id={}", id);
                    return new NotFoundException("Projet introuvable avec l'ID : " + id);
                });

        if (project.getActive()) {
            log.warn("Projet déjà actif : id={}, title={}", id, project.getTitle());
            return;
        }

        project.setActive(true);
        projectRepository.save(project);
        log.info("Projet activé avec succès : id={}, title={}", id, project.getTitle());
    }

    @Transactional
    public void deactivate(Long id) {
        log.info("Début désactivation projet : id={}", id);

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Projet introuvable : id={}", id);
                    return new NotFoundException("Projet introuvable avec l'ID : " + id);
                });

        if (!project.getActive()) {
            log.warn("Projet déjà désactivé : id={}, title={}", id, project.getTitle());
            return;
        }

        project.setActive(false);
        projectRepository.save(project);
        log.info("Projet désactivé avec succès : id={}, title={}", id, project.getTitle());
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

    private ProjectResponse toProjectResponse(Project project) {
        ProjectResponse response = projectMapper.toResponse(project);

        Set<String> skillNames = project.getRequiredSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toSet());

        Set<String> keywordLabels = project.getKeywords().stream()
                .map(Keyword::getLabel)
                .collect(Collectors.toSet());

        Set<String> studentEmails = project.getAssignedStudents().stream()
                .map(student -> student.getUser().getEmail())
                .collect(Collectors.toSet());

        return new ProjectResponse(
                response.id(),
                response.teacherId(),
                response.teacherName(),
                response.title(),
                response.description(),
                response.workType(),
                response.remotePossible(),
                response.minStudents(),
                response.maxStudents(),
                skillNames,
                keywordLabels,
                studentEmails,
                response.active(),
                response.full()
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