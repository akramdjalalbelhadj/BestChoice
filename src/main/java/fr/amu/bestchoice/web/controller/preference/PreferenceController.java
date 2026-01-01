package fr.amu.bestchoice.web.controller.preference;

import fr.amu.bestchoice.model.enums.PreferenceStatus;
import fr.amu.bestchoice.service.preference.PreferenceService;
import fr.amu.bestchoice.web.dto.preference.PreferenceCreateRequest;
import fr.amu.bestchoice.web.dto.preference.PreferenceResponse;
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
 * Contrôleur REST pour la gestion des préférences étudiantes (StudentPreferences).
 *
 * Endpoints disponibles :
 * - GET    /api/preferences/student/{studentId} : Récupérer les préférences d'un étudiant
 * - GET    /api/preferences/project/{projectId} : Récupérer les préférences pour un projet
 * - GET    /api/preferences/{id}                : Récupérer une préférence par ID
 * - POST   /api/preferences                     : Créer une nouvelle préférence
 * - DELETE /api/preferences/{id}                : Supprimer une préférence
 * - PATCH  /api/preferences/{id}/accept         : Accepter une préférence
 * - PATCH  /api/preferences/{id}/reject         : Rejeter une préférence
 *
 * Tous les endpoints nécessitent le rôle ADMIN.
 */
@Slf4j
@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Préférences", description = "Gestion des choix des étudiants (1-10 projets par ordre de préférence)")
public class PreferenceController {

    private final PreferenceService preferenceService;

    // ==================== READ ====================

    /**
     * Récupère toutes les préférences d'un étudiant
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<PreferenceResponse>> getPreferencesByStudent(@PathVariable Long studentId) {

        log.debug("GET /api/preferences/student/{} - Récupération des préférences de l'étudiant", studentId);

        List<PreferenceResponse> preferences = preferenceService.findByStudentId(studentId);

        log.info("GET /api/preferences/student/{} - {} préférence(s) retournée(s)",
                studentId, preferences.size());

        return ResponseEntity.ok(preferences);
    }

    /**
     * Récupère toutes les préférences pour un projet
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<PreferenceResponse>> getPreferencesByProject(@PathVariable Long projectId) {

        log.debug("GET /api/preferences/project/{} - Récupération des préférences pour le projet", projectId);

        List<PreferenceResponse> preferences = preferenceService.findByProjectId(projectId);

        log.info("GET /api/preferences/project/{} - {} préférence(s) retournée(s)",
                projectId, preferences.size());

        return ResponseEntity.ok(preferences);
    }

    /**
     * Récupère une préférence par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PreferenceResponse> getPreferenceById(@PathVariable Long id) {

        log.debug("GET /api/preferences/{} - Récupération de la préférence", id);

        PreferenceResponse preference = preferenceService.findById(id);

        log.info("GET /api/preferences/{} - Préférence retournée : studentId={}, projectId={}, rank={}",
                id, preference.studentId(), preference.projectId(), preference.rank());

        return ResponseEntity.ok(preference);
    }

    // ==================== CREATE ====================

    /**
     * Crée une nouvelle préférence pour un étudiant.
     *
     * RÈGLES MÉTIER :
     * - Un étudiant peut avoir maximum 10 préférences
     * - Un étudiant ne peut pas choisir 2 fois le même projet
     * - Un étudiant ne peut pas utiliser 2 fois le même rang
     * - Le rang doit être entre 1 et 10
     * - Le projet doit être actif et non complet
     */
    @PostMapping
    public ResponseEntity<PreferenceResponse> createPreference(@Valid @RequestBody PreferenceCreateRequest request) {

        log.info("POST /api/preferences - Création d'une préférence : studentId={}, projectId={}, rank={}",
                request.studentId(), request.projectId(), request.rank());

        PreferenceResponse createdPreference = preferenceService.create(request);

        log.info("POST /api/preferences - Préférence créée avec succès : id={}, studentId={}, projectId={}, rank={}",
                createdPreference.id(), createdPreference.studentId(), createdPreference.projectId(), createdPreference.rank());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdPreference);
    }

    // ==================== DELETE ====================

    /**
     * Supprime une préférence.
     *
     * DELETE /api/preferences/{id}
     *
     * IMPORTANT : Une préférence ne peut être supprimée que si elle est en statut PENDING.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePreference(@PathVariable Long id) {

        log.info("DELETE /api/preferences/{} - Suppression de la préférence", id);

        preferenceService.delete(id);

        log.info("DELETE /api/preferences/{} - Préférence supprimée avec succès", id);

        return ResponseEntity.noContent().build();
    }

    // ==================== CHANGEMENT DE STATUT ====================

    /**
     * Accepte une préférence (PENDING → ACCEPTED).
     *
     * Cette opération est utilisée par l'algorithme de matching ou par l'admin
     * pour marquer qu'un étudiant a été assigné à ce projet.
     */
    @PatchMapping("/{id}/accept")
    public ResponseEntity<PreferenceResponse> acceptPreference(@PathVariable Long id) {

        log.info("PATCH /api/preferences/{}/accept - Acceptation de la préférence", id);

        PreferenceResponse acceptedPreference = preferenceService.accept(id);

        log.info("PATCH /api/preferences/{}/accept - Préférence acceptée avec succès", id);

        return ResponseEntity.ok(acceptedPreference);
    }

    /**
     * Rejette une préférence (PENDING → REJECTED).
     *
     * PATCH /api/preferences/{id}/reject
     *
     * Cette opération est utilisée par l'algorithme de matching
     * pour marquer qu'un étudiant n'a pas été assigné à ce projet.
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<PreferenceResponse> rejectPreference(@PathVariable Long id) {

        log.info("PATCH /api/preferences/{}/reject - Rejet de la préférence", id);

        PreferenceResponse rejectedPreference = preferenceService.reject(id);

        log.info("PATCH /api/preferences/{}/reject - Préférence rejetée avec succès", id);

        return ResponseEntity.ok(rejectedPreference);
    }
}