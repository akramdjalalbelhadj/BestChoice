package fr.amu.bestchoice.web.controller.subject;

import fr.amu.bestchoice.service.implementation.subject.SubjectService;
import fr.amu.bestchoice.web.dto.PageResponseDto;
import fr.amu.bestchoice.web.dto.subject.SubjectCreateRequest;
import fr.amu.bestchoice.web.dto.subject.SubjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@Tag(name = "Matières / Options", description = "Gestion des matières et options d'enseignement")
public class SubjectController {

    private final SubjectService subjectService;

    // ==================== READ ====================

    /**
     * 🌐 Récupère toutes les matières avec pagination.
     */
    @Operation(
            summary = "Récupérer toutes les matières (paginé)",
            description = "Retourne une page de matières avec métadonnées de pagination"
    )
    @GetMapping("/paginated")
    public ResponseEntity<PageResponseDto<SubjectResponse>> getAllSubjectsPaginated(
            @Parameter(description = "Numéro de page (commence à 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Taille de page", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Champ de tri", example = "title")
            @RequestParam(required = false) String sortBy,

            @Parameter(description = "Direction du tri (ASC/DESC)", example = "ASC")
            @RequestParam(required = false) String sortDirection) {

        log.debug("🌐 GET /api/subjects/paginated - page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);

        Page<SubjectResponse> subjectsPage = subjectService.findAll(page, size, sortBy, sortDirection);
        PageResponseDto<SubjectResponse> response = PageResponseDto.of(subjectsPage);

        log.info("🌐 GET /api/subjects/paginated - {} matières retournées (page {}/{})",
                response.content().size(), response.pageNumber() + 1, response.totalPages());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Récupérer toutes les matières actives")
    @GetMapping("/active")
    public ResponseEntity<List<SubjectResponse>> getActiveSubjects() {
        log.debug("GET /api/subjects/active - Récupération des matières actives");
        List<SubjectResponse> subjects = subjectService.findAllActive();
        log.info("GET /api/subjects/active - {} matières actives retournées", subjects.size());
        return ResponseEntity.ok(subjects);
    }

    @Operation(summary = "Récupérer une matière par son ID")
    @GetMapping("/{id}")
    public ResponseEntity<SubjectResponse> getById(@PathVariable Long id) {
        log.debug("GET /api/subjects/{} - Récupération de la matière", id);
        SubjectResponse subject = subjectService.findById(id);
        log.info("GET /api/subjects/{} - Matière retournée : title={}", id, subject.title());
        return ResponseEntity.ok(subject);
    }

    @Operation(summary = "Récupérer les matières d'un enseignant spécifique")
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<SubjectResponse>> getByTeacher(@PathVariable Long teacherId) {
        log.debug("GET /api/subjects/teacher/{} - Récupération des matières de l'enseignant", teacherId);
        List<SubjectResponse> subjects = subjectService.findByTeacherId(teacherId);
        log.info("GET /api/subjects/teacher/{} - {} matières retournées", teacherId, subjects.size());
        return ResponseEntity.ok(subjects);
    }

    @Operation(summary = "Récupérer les matières d'une campagne spécifique")
    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<List<SubjectResponse>> getByCampaign(@PathVariable Long campaignId) {
        log.debug("GET /api/subjects/campaign/{} - Récupération des matières de la campagne", campaignId);
        List<SubjectResponse> subjects = subjectService.findByCampaignId(campaignId);
        log.info("GET /api/subjects/campaign/{} - {} matières retournées", campaignId, subjects.size());
        return ResponseEntity.ok(subjects);
    }

    // ==================== CREATE ====================

    @Operation(summary = "Créer une nouvelle matière pour un enseignant")
    @PostMapping("/teacher/{teacherId}")
    public ResponseEntity<SubjectResponse> create(
            @PathVariable Long teacherId,
            @Valid @RequestBody SubjectCreateRequest request) {

        log.info("POST /api/subjects/teacher/{} - Création d'une matière : title={}",
                teacherId, request.title());

        SubjectResponse createdSubject = subjectService.create(teacherId, request);

        log.info("POST /api/subjects/teacher/{} - Matière créée avec succès : id={}, title={}",
                teacherId, createdSubject.id(), createdSubject.title());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdSubject);
    }



    // ==================== ACTIVATION / DÉSACTIVATION ====================

    @Operation(summary = "Activer une matière")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        log.info("PATCH /api/subjects/{}/activate - Activation de la matière", id);
        subjectService.activate(id);
        log.info("PATCH /api/subjects/{}/activate - Matière activée avec succès", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Désactiver une matière")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        log.info("PATCH /api/subjects/{}/deactivate - Désactivation de la matière", id);
        subjectService.deactivate(id);
        log.info("PATCH /api/subjects/{}/deactivate - Matière désactivée avec succès", id);
        return ResponseEntity.noContent().build();
    }

    // ==================== SUPPRESSION ====================

    @Operation(summary = "Supprimer définitivement une matière")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/subjects/{} - Suppression de la matière", id);
        subjectService.delete(id);
        log.info("DELETE /api/subjects/{} - Matière supprimée avec succès", id);
        return ResponseEntity.noContent().build();
    }
}