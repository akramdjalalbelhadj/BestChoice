package fr.amu.bestchoice.web.controller.skills;

import fr.amu.bestchoice.service.implementation.referential.SkillService;
import fr.amu.bestchoice.web.dto.PageResponseDto;          // 🌐 AJOUT
import fr.amu.bestchoice.web.dto.skill.SkillCreateRequest;
import fr.amu.bestchoice.web.dto.skill.SkillResponse;
import fr.amu.bestchoice.web.dto.skill.SkillUpdateRequest;
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
@RequestMapping("/api/skills")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Compétences", description = "Compétences")
public class SkillController {

    private final SkillService skillService;

    // ==================== READ ====================

    // 🌐 NOUVELLE VERSION PAGINÉE
    /**
     * 🌐 Récupère toutes les compétences avec pagination.
     */
    @Operation(
            summary = "Récupérer toutes les compétences (paginé)",
            description = "Retourne une page de compétences avec métadonnées de pagination"
    )
    @GetMapping("/paginated")
    public ResponseEntity<PageResponseDto<SkillResponse>> getAllSkillsPaginated(
            @Parameter(description = "Numéro de page (commence à 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Taille de page", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Champ de tri", example = "name")
            @RequestParam(required = false) String sortBy,

            @Parameter(description = "Direction du tri (ASC/DESC)", example = "ASC")
            @RequestParam(required = false) String sortDirection) {

        log.debug("🌐 GET /api/skills/paginated - page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);

        Page<SkillResponse> skillsPage = skillService.findAll(page, size, sortBy, sortDirection);
        PageResponseDto<SkillResponse> response = PageResponseDto.of(skillsPage);

        log.info("🌐 GET /api/skills/paginated - {} compétences retournées (page {}/{})",
                response.content().size(), response.pageNumber() + 1, response.totalPages());

        return ResponseEntity.ok(response);
    }

    // ANCIENNE VERSION (rétrocompatibilité)
    @GetMapping
    public ResponseEntity<List<SkillResponse>> getAllSkills() {
        log.debug("GET /api/skills - Récupération de toutes les compétences");
        List<SkillResponse> skills = skillService.findAll();
        log.info("GET /api/skills - {} compétences retournées", skills.size());
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/active")
    public ResponseEntity<List<SkillResponse>> getActiveSkills() {
        log.debug("GET /api/skills/active - Récupération des compétences actives");
        List<SkillResponse> skills = skillService.findAllActive();
        log.info("GET /api/skills/active - {} compétences actives retournées", skills.size());
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SkillResponse> getSkillById(@PathVariable Long id) {
        log.debug("GET /api/skills/{} - Récupération de la compétence", id);
        SkillResponse skill = skillService.findById(id);
        log.info("GET /api/skills/{} - Compétence retournée : name={}", id, skill.name());
        return ResponseEntity.ok(skill);
    }

    // ==================== CREATE ====================

    @PostMapping
    public ResponseEntity<SkillResponse> createSkill(@Valid @RequestBody SkillCreateRequest request) {
        log.info("POST /api/skills - Création d'une compétence : name={}", request.name());
        SkillResponse createdSkill = skillService.create(request);
        log.info("POST /api/skills - Compétence créée avec succès : id={}, name={}",
                createdSkill.id(), createdSkill.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSkill);
    }

    // ==================== UPDATE ====================

    @PutMapping("/{id}")
    public ResponseEntity<SkillResponse> updateSkill(
            @PathVariable Long id,
            @Valid @RequestBody SkillUpdateRequest request) {

        log.info("PUT /api/skills/{} - Mise à jour de la compétence", id);
        SkillResponse updatedSkill = skillService.update(id, request);
        log.info("PUT /api/skills/{} - Compétence mise à jour avec succès : name={}",
                id, updatedSkill.name());
        return ResponseEntity.ok(updatedSkill);
    }

    // ==================== DELETE ====================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        log.info("DELETE /api/skills/{} - Suppression de la compétence", id);
        skillService.delete(id);
        log.info("DELETE /api/skills/{} - Compétence supprimée avec succès", id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateSkill(@PathVariable Long id) {
        log.info("PATCH /api/skills/{}/deactivate - Désactivation de la compétence", id);
        skillService.deactivate(id);
        log.info("PATCH /api/skills/{}/deactivate - Compétence désactivée avec succès", id);
        return ResponseEntity.noContent().build();
    }
}