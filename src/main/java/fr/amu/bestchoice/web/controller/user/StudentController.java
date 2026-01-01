package fr.amu.bestchoice.web.controller.user;

import fr.amu.bestchoice.service.user.StudentService;
import fr.amu.bestchoice.web.dto.student.StudentCreateRequest;
import fr.amu.bestchoice.web.dto.student.StudentResponse;
import fr.amu.bestchoice.web.dto.student.StudentUpdateRequest;
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
 * Contrôleur REST pour la gestion des profils étudiants (Students).
 */
@Slf4j
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Étudiants", description = "Étudiants")
public class StudentController {


    private final StudentService studentService;

    // ==================== READ ====================

    /**
     * Récupère tous les profils étudiants.
     */
    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents() {

        log.debug("GET /api/students - Récupération de tous les profils étudiants");

        List<StudentResponse> students = studentService.findAll();

        log.info("GET /api/students - {} profils étudiants retournés", students.size());

        return ResponseEntity.ok(students);
    }

    /**
     * Récupère uniquement les profils étudiants complets.
     *
     * Un profil complet a renseigné :
     * - Programme
     * - Année d'étude
     * - Au moins 1 compétence
     * - Au moins 1 centre d'intérêt
     * - Type de travail préféré
     */
    @GetMapping("/complete")
    public ResponseEntity<List<StudentResponse>> getCompleteStudents() {

        log.debug("GET /api/students/complete - Récupération des profils étudiants complets");

        List<StudentResponse> students = studentService.findAllComplete();

        log.info("GET /api/students/complete - {} profils complets retournés", students.size());

        return ResponseEntity.ok(students);
    }

    /**
     * Récupère un profil étudiant par son ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable Long id) {

        log.debug("GET /api/students/{} - Récupération du profil étudiant", id);

        StudentResponse student = studentService.findById(id);

        log.info("GET /api/students/{} - Profil étudiant retourné : email={}, complete={}",
                id, student.email(), student.id() != null);

        return ResponseEntity.ok(student);
    }

    // ==================== CREATE ====================

    /**
     * Crée un profil étudiant pour un utilisateur existant.
     * IMPORTANT : Normalement, le profil est créé automatiquement lors de l'inscription.
     * Ce endpoint permet de le créer manuellement si nécessaire.
     */
    @PostMapping("/user/{userId}")
    public ResponseEntity<StudentResponse> createStudentProfile(
            @PathVariable Long userId,
            @Valid @RequestBody StudentCreateRequest request) {

        log.info("POST /api/students/user/{} - Création du profil étudiant", userId);

        StudentResponse createdStudent = studentService.create(userId, request);

        log.info("POST /api/students/user/{} - Profil étudiant créé avec succès : id={}, email={}",
                userId, createdStudent.id(), createdStudent.email());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }

    // ==================== UPDATE ====================

    /**
     * Met à jour un profil étudiant existant.
     * Les champs null dans le DTO ne sont pas modifiés (stratégie IGNORE).
     */
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentUpdateRequest request) {

        log.info("PUT /api/students/{} - Mise à jour du profil étudiant", id);

        StudentResponse updatedStudent = studentService.update(id, request);

        log.info("PUT /api/students/{} - Profil étudiant mis à jour avec succès : complete={}",
                id, updatedStudent.id() != null);

        return ResponseEntity.ok(updatedStudent);
    }
}