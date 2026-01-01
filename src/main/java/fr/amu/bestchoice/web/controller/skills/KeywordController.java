package fr.amu.bestchoice.web.controller.skills;

import fr.amu.bestchoice.service.skills.KeywordService;
import fr.amu.bestchoice.web.dto.keyword.KeywordCreateRequest;
import fr.amu.bestchoice.web.dto.keyword.KeywordResponse;
import fr.amu.bestchoice.web.dto.keyword.KeywordUpdateRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des mots-clés (Keywords).
 *
 * Endpoints disponibles :
 * - GET    /api/keywords           : Récupérer tous les mots-clés
 * - GET    /api/keywords/active    : Récupérer les mots-clés actifs uniquement
 * - GET    /api/keywords/{id}      : Récupérer un mot-clé par ID
 * - POST   /api/keywords           : Créer un nouveau mot-clé
 * - PUT    /api/keywords/{id}      : Modifier un mot-clé
 * - DELETE /api/keywords/{id}      : Supprimer un mot-clé
 * - PATCH  /api/keywords/{id}/deactivate : Désactiver un mot-clé
 *
 * Tous les endpoints nécessitent le rôle ADMIN.
 */
@Slf4j
@RestController
@RequestMapping("/api/keywords")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Mots-clés", description = "Référentiel des mots-clés / centres d'intérêt (IA, DevOps, Cybersécurité, etc.)")
public class KeywordController {


    private final KeywordService keywordService;

    // ==================== READ ====================

    /**
     * Récupère tous les mots-clés.
     */
    @GetMapping
    public ResponseEntity<List<KeywordResponse>> getAllKeywords() {

        log.debug("GET /api/keywords - Récupération de tous les mots-clés");

        List<KeywordResponse> keywords = keywordService.findAll();

        log.info("GET /api/keywords - {} mots-clés retournés", keywords.size());

        return ResponseEntity.ok(keywords);
    }

    /**
     * Récupère uniquement les mots-clés actifs.
     */
    @GetMapping("/active")
    public ResponseEntity<List<KeywordResponse>> getActiveKeywords() {

        log.debug("GET /api/keywords/active - Récupération des mots-clés actifs");

        List<KeywordResponse> keywords = keywordService.findAllActive();

        log.info("GET /api/keywords/active - {} mots-clés actifs retournés", keywords.size());

        return ResponseEntity.ok(keywords);
    }

    /**
     * Récupère un mot-clé par son ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<KeywordResponse> getKeywordById(@PathVariable Long id) {

        log.debug("GET /api/keywords/{} - Récupération du mot-clé", id);

        KeywordResponse keyword = keywordService.findById(id);

        log.info("GET /api/keywords/{} - Mot-clé retourné : label={}", id, keyword.label());

        return ResponseEntity.ok(keyword);
    }

    // ==================== CREATE ====================

    /**
     * Crée un nouveau mot-clé.
     */
    @PostMapping
    public ResponseEntity<KeywordResponse> createKeyword(@Valid @RequestBody KeywordCreateRequest request) {

        log.info("POST /api/keywords - Création d'un mot-clé : label={}", request.label());

        KeywordResponse createdKeyword = keywordService.create(request);

        log.info("POST /api/keywords - Mot-clé créé avec succès : id={}, label={}",
                createdKeyword.id(), createdKeyword.label());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdKeyword);
    }

    // ==================== UPDATE ====================

    /**
     * Met à jour un mot-clé existant.
     */
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

    /**
     * Supprime un mot-clé (hard delete).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKeyword(@PathVariable Long id) {

        log.info("DELETE /api/keywords/{} - Suppression du mot-clé", id);

        keywordService.delete(id);

        log.info("DELETE /api/keywords/{} - Mot-clé supprimé avec succès", id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Désactive un mot-clé (soft delete).
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateKeyword(@PathVariable Long id) {

        log.info("PATCH /api/keywords/{}/deactivate - Désactivation du mot-clé", id);

        keywordService.deactivate(id);

        log.info("PATCH /api/keywords/{}/deactivate - Mot-clé désactivé avec succès", id);

        return ResponseEntity.noContent().build();
    }
}