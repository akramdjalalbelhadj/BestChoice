package fr.amu.bestchoice.web.controller.skills;

import fr.amu.bestchoice.service.interfaces.IKeywordService;
import fr.amu.bestchoice.web.dto.PageResponseDto;          // 🌐 AJOUT
import fr.amu.bestchoice.web.dto.keyword.KeywordCreateRequest;
import fr.amu.bestchoice.web.dto.keyword.KeywordResponse;
import fr.amu.bestchoice.web.dto.keyword.KeywordUpdateRequest;
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
@RequestMapping("/api/keywords")
@RequiredArgsConstructor
@Tag(name = "Mots-clés", description = "Mots-clés")
public class KeywordController {

    private final IKeywordService keywordService;

    // ==================== READ ====================

    // 🌐 NOUVELLE VERSION PAGINÉE
    /**
     * 🌐 Récupère tous les mots-clés avec pagination.
     */
    @Operation(
            summary = "Récupérer tous les mots-clés (paginé)",
            description = "Retourne une page de mots-clés avec métadonnées de pagination"
    )
    @GetMapping("/paginated")
    public ResponseEntity<PageResponseDto<KeywordResponse>> getAllKeywordsPaginated(
            @Parameter(description = "Numéro de page (commence à 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Taille de page", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Champ de tri", example = "label")
            @RequestParam(required = false) String sortBy,

            @Parameter(description = "Direction du tri (ASC/DESC)", example = "ASC")
            @RequestParam(required = false) String sortDirection) {

        log.debug("🌐 GET /api/keywords/paginated - page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);

        Page<KeywordResponse> keywordsPage = keywordService.findAll(page, size, sortBy, sortDirection);
        PageResponseDto<KeywordResponse> response = PageResponseDto.of(keywordsPage);

        log.info("🌐 GET /api/keywords/paginated - {} mots-clés retournés (page {}/{})",
                response.content().size(), response.pageNumber() + 1, response.totalPages());

        return ResponseEntity.ok(response);
    }

    // ANCIENNE VERSION (rétrocompatibilité)
    @GetMapping
    public ResponseEntity<List<KeywordResponse>> getAllKeywords() {
        log.debug("GET /api/keywords - Récupération de tous les mots-clés");
        List<KeywordResponse> keywords = keywordService.findAll();
        log.info("GET /api/keywords - {} mots-clés retournés", keywords.size());
        return ResponseEntity.ok(keywords);
    }

    @GetMapping("/active")
    public ResponseEntity<List<KeywordResponse>> getActiveKeywords() {
        log.debug("GET /api/keywords/active - Récupération des mots-clés actifs");
        List<KeywordResponse> keywords = keywordService.findAllActive();
        log.info("GET /api/keywords/active - {} mots-clés actifs retournés", keywords.size());
        return ResponseEntity.ok(keywords);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KeywordResponse> getKeywordById(@PathVariable Long id) {
        log.debug("GET /api/keywords/{} - Récupération du mot-clé", id);
        KeywordResponse keyword = keywordService.findById(id);
        log.info("GET /api/keywords/{} - Mot-clé retourné : label={}", id, keyword.label());
        return ResponseEntity.ok(keyword);
    }

    // ==================== CREATE ====================

    @PostMapping
    public ResponseEntity<KeywordResponse> createKeyword(@Valid @RequestBody KeywordCreateRequest request) {
        log.info("POST /api/keywords - Création d'un mot-clé : label={}", request.label());
        KeywordResponse createdKeyword = keywordService.create(request);
        log.info("POST /api/keywords - Mot-clé créé avec succès : id={}, label={}",
                createdKeyword.id(), createdKeyword.label());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdKeyword);
    }

    // ==================== UPDATE ====================

    @PutMapping("/{id}")
    public ResponseEntity<KeywordResponse> updateKeyword(
            @PathVariable Long id,
            @Valid @RequestBody KeywordUpdateRequest request) {

        log.info("PUT /api/keywords/{} - Mise à jour du mot-clé", id);
        KeywordResponse updatedKeyword = keywordService.update(id, request);
        log.info("PUT /api/keywords/{} - Mot-clé mis à jour avec succès : label={}",
                id, updatedKeyword.label());
        return ResponseEntity.ok(updatedKeyword);
    }

    // ==================== DELETE ====================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKeyword(@PathVariable Long id) {
        log.info("DELETE /api/keywords/{} - Suppression du mot-clé", id);
        keywordService.delete(id);
        log.info("DELETE /api/keywords/{} - Mot-clé supprimé avec succès", id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateKeyword(@PathVariable Long id) {
        log.info("PATCH /api/keywords/{}/deactivate - Désactivation du mot-clé", id);
        keywordService.deactivate(id);
        log.info("PATCH /api/keywords/{}/deactivate - Mot-clé désactivé avec succès", id);
        return ResponseEntity.noContent().build();
    }
}