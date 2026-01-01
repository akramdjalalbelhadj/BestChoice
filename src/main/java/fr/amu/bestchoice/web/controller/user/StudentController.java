package fr.amu.bestchoice.web.controller.user;

import fr.amu.bestchoice.service.implementation.user.StudentService;
import fr.amu.bestchoice.web.dto.PageResponseDto;
import fr.amu.bestchoice.web.dto.student.StudentCreateRequest;
import fr.amu.bestchoice.web.dto.student.StudentResponse;
import fr.amu.bestchoice.web.dto.student.StudentUpdateRequest;
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
@RequestMapping("/api/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Étudiants", description = "Étudiants")
public class StudentController {

    private final StudentService studentService;

    // ==================== READ ====================

    @Operation(
            summary = "Récupérer tous les étudiants (paginé)",
            description = "Retourne une page d'étudiants avec métadonnées de pagination"
    )
    @GetMapping("/paginated")
    public ResponseEntity<PageResponseDto<StudentResponse>> getAllStudentsPaginated(
            @Parameter(description = "Numéro de page (commence à 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Taille de page", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Champ de tri", example = "id")
            @RequestParam(required = false) String sortBy,

            @Parameter(description = "Direction du tri (ASC/DESC)", example = "ASC")
            @RequestParam(required = false) String sortDirection
    ) {
        log.debug("🌐 GET /api/students/paginated - page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);

        Page<StudentResponse> studentsPage = studentService.findAll(page, size, sortBy, sortDirection);
        PageResponseDto<StudentResponse> response = PageResponseDto.of(studentsPage);

        log.info("🌐 GET /api/students/paginated - {} étudiants retournés (page {}/{})",
                response.content().size(), response.pageNumber() + 1, response.totalPages());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        log.debug("GET /api/students - Récupération de tous les profils étudiants");
        List<StudentResponse> students = studentService.findAll();
        log.info("GET /api/students - {} profils étudiants retournés", students.size());
        return ResponseEntity.ok(students);
    }

    @GetMapping("/complete")
    public ResponseEntity<List<StudentResponse>> getCompleteStudents() {
        log.debug("GET /api/students/complete - Récupération des profils étudiants complets");
        List<StudentResponse> students = studentService.findAllComplete();
        log.info("GET /api/students/complete - {} profils complets retournés", students.size());
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable Long id) {
        log.debug("GET /api/students/{} - Récupération du profil étudiant", id);
        StudentResponse student = studentService.findById(id);
        log.info("GET /api/students/{} - Profil étudiant retourné : email={}", id, student.email());
        return ResponseEntity.ok(student);
    }

    // ==================== CREATE ====================

    @PostMapping("/user/{userId}")
    public ResponseEntity<StudentResponse> createStudentProfile(
            @PathVariable Long userId,
            @Valid @RequestBody StudentCreateRequest request
    ) {
        log.info("POST /api/students/user/{} - Création du profil étudiant", userId);
        StudentResponse createdStudent = studentService.create(userId, request);
        log.info("POST /api/students/user/{} - Profil étudiant créé avec succès : id={}, email={}",
                userId, createdStudent.id(), createdStudent.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }

    // ==================== UPDATE ====================

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentUpdateRequest request
    ) {
        log.info("PUT /api/students/{} - Mise à jour du profil étudiant", id);
        StudentResponse updatedStudent = studentService.update(id, request);
        log.info("PUT /api/students/{} - Profil étudiant mis à jour avec succès", id);
        return ResponseEntity.ok(updatedStudent);
    }
}
