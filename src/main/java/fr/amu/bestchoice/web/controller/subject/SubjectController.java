package fr.amu.bestchoice.web.controller.subject;

import fr.amu.bestchoice.service.implementation.subject.SubjectService;
import fr.amu.bestchoice.web.dto.subject.SubjectCreateRequest;
import fr.amu.bestchoice.web.dto.subject.SubjectResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@Tag(name = "Matières / Options")
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping("/teacher/{teacherId}")
    public ResponseEntity<SubjectResponse> create(@PathVariable Long teacherId, @Valid @RequestBody SubjectCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.create(teacherId, request));
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<SubjectResponse>> getByTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(subjectService.findByTeacherId(teacherId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(subjectService.findById(id));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        subjectService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        subjectService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}