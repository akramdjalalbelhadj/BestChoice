package fr.amu.bestchoice.web.controller.project;

import fr.amu.bestchoice.service.implementation.project.ProjectService;
import fr.amu.bestchoice.web.dto.PageResponseDto;          // 🌐 AJOUT
import fr.amu.bestchoice.web.dto.project.ProjectCreateRequest;
import fr.amu.bestchoice.web.dto.project.ProjectResponse;
import fr.amu.bestchoice.web.dto.project.ProjectUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;            // 🌐 AJOUT
import io.swagger.v3.oas.annotations.Parameter;            // 🌐 AJOUT
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;                // 🌐 AJOUT
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Projets", description = "Projets")
public class ProjectController {

    private final ProjectService projectService;

    // ==================== READ ====================

    // 🌐 NOUVELLE VERSION PAGINÉE
    /**
     * 🌐 Récupère tous les projets avec pagination.
     */
    @Operation(
            summary = "Récupérer tous les projets (paginé)",
            description = "Retourne une page de projets avec métadonnées de pagination"
    )
    @GetMapping("/paginated")
    public ResponseEntity<PageResponseDto<ProjectResponse>> getAllProjectsPaginated(
            @Parameter(description = "Numéro de page (commence à 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Taille de page", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Champ de tri", example = "title")
            @RequestParam(required = false) String sortBy,

            @Parameter(description = "Direction du tri (ASC/DESC)", example = "ASC")
            @RequestParam(required = false) String sortDirection) {

        log.debug("🌐 GET /api/projects/paginated - page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);

        Page<ProjectResponse> projectsPage = projectService.findAll(page, size, sortBy, sortDirection);
        PageResponseDto<ProjectResponse> response = PageResponseDto.of(projectsPage);

        log.info("🌐 GET /api/projects/paginated - {} projets retournés (page {}/{})",
                response.content().size(), response.pageNumber() + 1, response.totalPages());

        return ResponseEntity.ok(response);
    }

    // ANCIENNE VERSION (rétrocompatibilité)
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        log.debug("GET /api/projects - Récupération de tous les projets");
        List<ProjectResponse> projects = projectService.findAll();
        log.info("GET /api/projects - {} projets retournés", projects.size());
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ProjectResponse>> getActiveProjects() {
        log.debug("GET /api/projects/active - Récupération des projets actifs");
        List<ProjectResponse> projects = projectService.findAllActive();
        log.info("GET /api/projects/active - {} projets actifs retournés", projects.size());
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/available")
    public ResponseEntity<List<ProjectResponse>> getAvailableProjects() {
        log.debug("GET /api/projects/available - Récupération des projets disponibles");
        List<ProjectResponse> projects = projectService.findAllAvailable();
        log.info("GET /api/projects/available - {} projets disponibles retournés", projects.size());
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id) {
        log.debug("GET /api/projects/{} - Récupération du projet", id);
        ProjectResponse project = projectService.findById(id);
        log.info("GET /api/projects/{} - Projet retourné : title={}", id, project.title());
        return ResponseEntity.ok(project);
    }

    // ==================== CREATE ====================

    @PostMapping("/teacher/{teacherId}")
    public ResponseEntity<ProjectResponse> createProject(
            @PathVariable Long teacherId,
            @Valid @RequestBody ProjectCreateRequest request) {

        log.info("POST /api/projects/teacher/{} - Création d'un projet : title={}",
                teacherId, request.title());

        ProjectResponse createdProject = projectService.create(teacherId, request);

        log.info("POST /api/projects/teacher/{} - Projet créé avec succès : id={}, title={}",
                teacherId, createdProject.id(), createdProject.title());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    // ==================== UPDATE ====================

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateRequest request) {

        log.info("PUT /api/projects/{} - Mise à jour du projet", id);
        ProjectResponse updatedProject = projectService.update(id, request);
        log.info("PUT /api/projects/{} - Projet mis à jour avec succès : title={}",
                id, updatedProject.title());
        return ResponseEntity.ok(updatedProject);
    }

    // ==================== ACTIVATION / DÉSACTIVATION ====================

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateProject(@PathVariable Long id) {
        log.info("PATCH /api/projects/{}/deactivate - Désactivation du projet", id);
        projectService.deactivate(id);
        log.info("PATCH /api/projects/{}/deactivate - Projet désactivé avec succès", id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateProject(@PathVariable Long id) {
        log.info("PATCH /api/projects/{}/activate - Activation du projet", id);
        projectService.activate(id);
        log.info("PATCH /api/projects/{}/activate - Projet activé avec succès", id);
        return ResponseEntity.noContent().build();
    }
}