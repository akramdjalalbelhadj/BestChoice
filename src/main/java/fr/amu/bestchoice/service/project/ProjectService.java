package fr.amu.bestchoice.service.project;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service de gestion des projets (Projects).
 *
 * Opérations disponibles :
 * - Créer un projet
 * - Modifier un projet
 * - Récupérer un projet par ID
 * - Récupérer tous les projets
 * - Récupérer les projets actifs
 * - Récupérer les projets disponibles (actifs et non complets)
 * - Marquer un projet comme complet
 *
 * IMPORTANT : La résolution des Set<String> (requiredSkills, keywords) → Set<Entity>
 * est gérée dans ce service, PAS dans le mapper.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    // ==================== DÉPENDANCES ====================

    private final ProjectRepository projectRepository;
    private final TeacherRepository teacherRepository;
    private final SkillRepository skillRepository;
    private final KeywordRepository keywordRepository;
    private final ProjectMapper projectMapper;

    // ==================== CREATE ====================

    /**
     * Crée un nouveau projet.
     *
     * @param teacherId L'ID de l'enseignant responsable du projet
     * @param dto Les données du nouveau projet
     * @return ProjectResponse avec les données du projet créé
     * @throws NotFoundException Si l'enseignant n'existe pas
     * @throws BusinessException Si minStudents > maxStudents
     */
    @Transactional
    public ProjectResponse create(Long teacherId, ProjectCreateRequest dto) {

        log.info("Début création projet : title={}, teacherId={}", dto.title(), teacherId);

        // ===== VALIDATION ENSEIGNANT =====

        // Vérifier que l'enseignant existe
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> {
                    log.error("Enseignant introuvable : teacherId={}", teacherId);
                    return new NotFoundException("Enseignant introuvable avec l'ID : " + teacherId);
                });

        log.debug("Enseignant trouvé : userId={}, email={}", teacher.getId(), teacher.getUser().getEmail());

        // ===== VALIDATION MÉTIER =====

        // Vérifier que minStudents <= maxStudents
        if (dto.minStudents() != null && dto.maxStudents() != null && dto.minStudents() > dto.maxStudents()) {
            log.warn("minStudents > maxStudents : min={}, max={}", dto.minStudents(), dto.maxStudents());
            throw new BusinessException("Le nombre minimum d'étudiants ne peut pas être supérieur au maximum");
        }

        // ===== MAPPING DTO → ENTITY =====

        Project project = projectMapper.toEntity(dto);
        project.setTeacher(teacher);

        log.debug("Project mappé : title={}", project.getTitle());

        // ===== RÉSOLUTION DES REQUIRED SKILLS (Set<String> → Set<Skill>) =====

        if (dto.requiredSkill() != null && !dto.requiredSkill().isEmpty()) {

            log.debug("Résolution de {} compétences requises", dto.requiredSkill().size());

            Set<Skill> requiredSkills = resolveSkills(dto.requiredSkill());
            project.setRequiredSkills(requiredSkills);

            log.info("Compétences requises résolues : {} compétences", requiredSkills.size());
        } else {
            project.setRequiredSkills(new HashSet<>());
            log.debug("Aucune compétence requise");
        }

        // ===== RÉSOLUTION DES KEYWORDS (Set<String> → Set<Keyword>) =====

        if (dto.keyword() != null && !dto.keyword().isEmpty()) {

            log.debug("Résolution de {} mots-clés", dto.keyword().size());

            Set<Keyword> keywords = resolveKeywords(dto.keyword());
            project.setKeywords(keywords);

            log.info("Mots-clés résolus : {} mots-clés", keywords.size());
        } else {
            project.setKeywords(new HashSet<>());
            log.debug("Aucun mot-clé");
        }

        // ===== SAUVEGARDE =====

        Project savedProject = projectRepository.save(project);

        log.info("Projet créé avec succès : id={}, title={}, teacherId={}",
                savedProject.getId(), savedProject.getTitle(), teacherId);

        // ===== MAPPING ENTITY → DTO =====

        return toProjectResponse(savedProject);
    }

    // ==================== UPDATE ====================

    /**
     * Met à jour un projet existant.
     *
     * @param id L'ID du projet à modifier
     * @param dto Les nouvelles données
     * @return ProjectResponse avec les données mises à jour
     * @throws NotFoundException Si le projet n'existe pas
     */
    @Transactional
    public ProjectResponse update(Long id, ProjectUpdateRequest dto) {

        log.info("Début mise à jour projet : id={}", id);

        // ===== RÉCUPÉRATION =====

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Projet introuvable : id={}", id);
                    return new NotFoundException("Projet introuvable avec l'ID : " + id);
                });

        log.debug("Projet trouvé : title={}", project.getTitle());

        // ===== VALIDATION MÉTIER =====

        // Si minStudents et maxStudents sont modifiés, vérifier la cohérence
        Integer newMin = dto.minStudents() != null ? dto.minStudents() : project.getMinStudents();
        Integer newMax = dto.maxStudents() != null ? dto.maxStudents() : project.getMaxStudents();

        if (newMin != null && newMax != null && newMin > newMax) {
            log.warn("Tentative de mise à jour avec minStudents > maxStudents : min={}, max={}", newMin, newMax);
            throw new BusinessException("Le nombre minimum d'étudiants ne peut pas être supérieur au maximum");
        }

        // ===== MAPPING DTO → ENTITY =====

        projectMapper.updateEntityFromDto(dto, project);

        // ===== RÉSOLUTION DES REQUIRED SKILLS (si fournis) =====

        if (dto.requiredSkill() != null) {

            if (!dto.requiredSkill().isEmpty()) {
                log.debug("Mise à jour des compétences requises : {} compétences", dto.requiredSkill().size());
                Set<Skill> requiredSkills = resolveSkills(dto.requiredSkill());
                project.setRequiredSkills(requiredSkills);
                log.info("Compétences requises mises à jour : {} compétences", requiredSkills.size());
            } else {
                // Si Set vide fourni, supprimer toutes les compétences requises
                log.debug("Suppression de toutes les compétences requises");
                project.setRequiredSkills(new HashSet<>());
            }
        }

        // ===== RÉSOLUTION DES KEYWORDS (si fournis) =====

        if (dto.keyword() != null) {

            if (!dto.keyword().isEmpty()) {
                log.debug("Mise à jour des mots-clés : {} mots-clés", dto.keyword().size());
                Set<Keyword> keywords = resolveKeywords(dto.keyword());
                project.setKeywords(keywords);
                log.info("Mots-clés mis à jour : {} mots-clés", keywords.size());
            } else {
                // Si Set vide fourni, supprimer tous les mots-clés
                log.debug("Suppression de tous les mots-clés");
                project.setKeywords(new HashSet<>());
            }
        }

        // ===== VÉRIFICATION PROJET COMPLET =====

        // Recalculer si le projet est complet (maxStudents atteint)
        if (project.getMaxStudents() != null) {
            long assignedCount = project.getAssignedStudents().size();
            boolean isFull = assignedCount >= project.getMaxStudents();
            project.setFull(isFull);
            log.debug("Projet complet : {} / {} étudiants → full={}", assignedCount, project.getMaxStudents(), isFull);
        }

        // ===== SAUVEGARDE =====

        Project updatedProject = projectRepository.save(project);

        log.info("Projet mis à jour avec succès : id={}, title={}", updatedProject.getId(), updatedProject.getTitle());

        // ===== MAPPING ENTITY → DTO =====

        return toProjectResponse(updatedProject);
    }

    // ==================== READ ====================

    /**
     * Récupère un projet par son ID.
     *
     * @param id L'ID du projet
     * @return ProjectResponse
     * @throws NotFoundException Si le projet n'existe pas
     */
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

    /**
     * Récupère tous les projets.
     *
     * @return Liste de ProjectResponse
     */
    public List<ProjectResponse> findAll() {

        log.debug("Récupération de tous les projets");

        List<Project> projects = projectRepository.findAll();

        log.info("Nombre de projets trouvés : {}", projects.size());

        return projects.stream()
                .map(this::toProjectResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère uniquement les projets actifs.
     *
     * @return Liste de ProjectResponse
     */
    public List<ProjectResponse> findAllActive() {

        log.debug("Récupération des projets actifs uniquement");

        List<Project> projects = projectRepository.findByActiveTrue();

        log.info("Nombre de projets actifs trouvés : {}", projects.size());

        return projects.stream()
                .map(this::toProjectResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les projets disponibles (actifs ET non complets).
     *
     * Ces projets peuvent encore accueillir des étudiants.
     *
     * @return Liste de ProjectResponse
     */
    public List<ProjectResponse> findAllAvailable() {

        log.debug("Récupération des projets disponibles (actifs et non complets)");

        List<Project> projects = projectRepository.findAvailableProjects();

        log.info("Nombre de projets disponibles trouvés : {}", projects.size());

        return projects.stream()
                .map(this::toProjectResponse)
                .collect(Collectors.toList());
    }

    // ==================== ACTIVATION / DÉSACTIVATION ====================

    /**
     * Désactive un projet.
     *
     * @param id L'ID du projet à désactiver
     * @throws NotFoundException Si le projet n'existe pas
     */
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

    /**
     * Active un projet.
     *
     * @param id L'ID du projet à activer
     * @throws NotFoundException Si le projet n'existe pas
     */
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

    // ==================== MÉTHODES PRIVÉES ====================

    /**
     * Résout un Set de noms de compétences en Set d'entités Skill.
     *
     * @param skillNames Les noms des compétences
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
     * @param keywordLabels Les labels des mots-clés
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
     * Convertit Project en ProjectResponse.
     *
     * Le mapper ne peut pas gérer les conversions Set<Skill> → Set<String> automatiquement,
     * donc on le fait manuellement ici.
     *
     * @param project L'entité Project
     * @return ProjectResponse
     */
    private ProjectResponse toProjectResponse(Project project) {

        // Mapping de base (sans requiredSkills, keywords et teacherName)
        ProjectResponse response = projectMapper.toResponse(project);

        // Extraire les noms des compétences requises (Set<Skill> → Set<String>)
        Set<String> skillNames = project.getRequiredSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toSet());

        // Extraire les labels des mots-clés (Set<Keyword> → Set<String>)
        Set<String> keywordLabels = project.getKeywords().stream()
                .map(Keyword::getLabel)
                .collect(Collectors.toSet());

        // Générer le nom complet de l'enseignant
        String teacherName = project.getTeacher().getUser().getFirstName() + " " +
                project.getTeacher().getUser().getLastName();

        // Créer le ProjectResponse complet avec tous les champs
        return new ProjectResponse(
                response.id(),
                response.title(),
                response.description(),
                response.workType(),
                response.remotePossible(),
                response.active(),
                response.minStudents(),
                response.maxStudents(),
                response.full(),
                response.teacherId(),
                teacherName,          // Nom complet de l'enseignant
                skillNames,           // Set<String> des noms de compétences
                keywordLabels         // Set<String> des labels de mots-clés
        );
    }
}