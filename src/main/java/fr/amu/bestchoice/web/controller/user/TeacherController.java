package fr.amu.bestchoice.web.controller.user;

import fr.amu.bestchoice.service.user.TeacherService;
import fr.amu.bestchoice.web.dto.teacher.TeacherCreateRequest;
import fr.amu.bestchoice.web.dto.teacher.TeacherResponse;
import fr.amu.bestchoice.web.dto.teacher.TeacherUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des profils enseignants (Teachers).
 */
@Slf4j
@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Enseignants", description = "Enseignants")
public class TeacherController {


    private final TeacherService teacherService;

    // ==================== READ ====================

    /**
     * Récupère tous les profils enseignants.
     */
    @GetMapping
    public ResponseEntity<List<TeacherResponse>> getAllTeachers() {

        log.debug("GET /api/teachers - Récupération de tous les profils enseignants");

        List<TeacherResponse> teachers = teacherService.findAll();

        log.info("GET /api/teachers - {} profils enseignants retournés", teachers.size());

        return ResponseEntity.ok(teachers);
    }

    /**
     * Récupère un profil enseignant par son ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TeacherResponse> getTeacherById(@PathVariable Long id) {

        log.debug("GET /api/teachers/{} - Récupération du profil enseignant", id);

        TeacherResponse teacher = teacherService.findById(id);

        log.info("GET /api/teachers/{} - Profil enseignant retourné : email={}, department={}",
                id, teacher.email(), teacher.department());

        return ResponseEntity.ok(teacher);
    }

    // ==================== CREATE ====================

    /**
     * Crée un profil enseignant pour un utilisateur existant.
     *
     * IMPORTANT : Normalement, le profil est créé automatiquement lors de l'inscription.
     * Ce endpoint permet de le créer manuellement si nécessaire.
     */
    @PostMapping("/user/{userId}")
    public ResponseEntity<TeacherResponse> createTeacherProfile(
            @PathVariable Long userId,
            @Valid @RequestBody TeacherCreateRequest request) {

        log.info("POST /api/teachers/user/{} - Création du profil enseignant", userId);

        TeacherResponse createdTeacher = teacherService.create(userId, request);

        log.info("POST /api/teachers/user/{} - Profil enseignant créé avec succès : id={}, email={}",
                userId, createdTeacher.id(), createdTeacher.email());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdTeacher);
    }

    // ==================== UPDATE ====================

    /**
     * Met à jour un profil enseignant existant.
     * Les champs null dans le DTO ne sont pas modifiés (stratégie IGNORE).
     */
    @PutMapping("/{id}")
    public ResponseEntity<TeacherResponse> updateTeacher(
            @PathVariable Long id,
            @Valid @RequestBody TeacherUpdateRequest request) {

        log.info("PUT /api/teachers/{} - Mise à jour du profil enseignant", id);

        TeacherResponse updatedTeacher = teacherService.update(id, request);

        log.info("PUT /api/teachers/{} - Profil enseignant mis à jour avec succès : department={}",
                id, updatedTeacher.department());

        return ResponseEntity.ok(updatedTeacher);
    }
}