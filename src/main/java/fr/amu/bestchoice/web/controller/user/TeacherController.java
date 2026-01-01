package fr.amu.bestchoice.web.controller.user;

import fr.amu.bestchoice.service.implementation.user.TeacherService;
import fr.amu.bestchoice.web.dto.PageResponseDto;
import fr.amu.bestchoice.web.dto.teacher.TeacherCreateRequest;
import fr.amu.bestchoice.web.dto.teacher.TeacherResponse;
import fr.amu.bestchoice.web.dto.teacher.TeacherUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Enseignants", description = "Enseignants")
public class TeacherController {

    private final TeacherService teacherService;

    // ==================== READ ====================

    @Operation(
            summary = "Récupérer tous les enseignants (paginé)",
            description = "Retourne une page d'enseignants avec métadonnées de pagination"
    )
    @GetMapping("/paginated")
    public ResponseEntity<PageResponseDto<TeacherResponse>> getAllTeachersPaginated(
            @Parameter(description = "Numéro de page (commence à 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Taille de page", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Champ de tri", example = "id")
            @RequestParam(required = false) String sortBy,

            @Parameter(description = "Direction du tri (ASC/DESC)", example = "ASC")
            @RequestParam(required = false) String sortDirection
    ) {
        log.debug("🌐 GET /api/teachers/paginated - page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);

        Page<TeacherResponse> teachersPage = teacherService.findAll(page, size, sortBy, sortDirection);
        PageResponseDto<TeacherResponse> response = PageResponseDto.of(teachersPage);

        log.info("🌐 GET /api/teachers/paginated - {} enseignants retournés (page {}/{})",
                response.content().size(), response.pageNumber() + 1, response.totalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TeacherResponse>> getAllTeachers() {
        log.debug("GET /api/teachers - Récupération de tous les profils enseignants");
        List<TeacherResponse> teachers = teacherService.findAll();
        log.info("GET /api/teachers - {} profils enseignants retournés", teachers.size());
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeacherResponse> getTeacherById(@PathVariable Long id) {
        log.debug("GET /api/teachers/{} - Récupération du profil enseignant", id);
        TeacherResponse teacher = teacherService.findById(id);
        log.info("GET /api/teachers/{} - Profil enseignant retourné : email={}, department={}",
                id, teacher.email(), teacher.department());
        return ResponseEntity.ok(teacher);
    }

    // ==================== CREATE ====================

    @PostMapping("/user/{userId}")
    public ResponseEntity<TeacherResponse> createTeacherProfile(
            @PathVariable Long userId,
            @Valid @RequestBody TeacherCreateRequest request
    ) {
        log.info("POST /api/teachers/user/{} - Création du profil enseignant", userId);
        TeacherResponse createdTeacher = teacherService.create(userId, request);
        log.info("POST /api/teachers/user/{} - Profil enseignant créé avec succès : id={}, email={}",
                userId, createdTeacher.id(), createdTeacher.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTeacher);
    }

    // ==================== UPDATE ====================

    @PutMapping("/{id}")
    public ResponseEntity<TeacherResponse> updateTeacher(
            @PathVariable Long id,
            @Valid @RequestBody TeacherUpdateRequest request
    ) {
        log.info("PUT /api/teachers/{} - Mise à jour du profil enseignant", id);
        TeacherResponse updatedTeacher = teacherService.update(id, request);
        log.info("PUT /api/teachers/{} - Profil enseignant mis à jour avec succès : department={}",
                id, updatedTeacher.department());
        return ResponseEntity.ok(updatedTeacher);
    }
}
