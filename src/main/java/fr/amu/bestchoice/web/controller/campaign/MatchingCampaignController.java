package fr.amu.bestchoice.web.controller.campaign;

import fr.amu.bestchoice.service.interfaces.IMatchingCampaignService;
import fr.amu.bestchoice.web.dto.campaign.MatchingCampaignRequest;
import fr.amu.bestchoice.web.dto.campaign.MatchingCampaignResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
@Tag(name = "Campagnes de Matching")
public class MatchingCampaignController {

    private final IMatchingCampaignService campaignService;

    @PostMapping
    public ResponseEntity<MatchingCampaignResponse> create(@Valid @RequestBody MatchingCampaignRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campaignService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchingCampaignResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.findById(id));
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<MatchingCampaignResponse>> getByTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(campaignService.findByTeacherId(teacherId));
    }

    @PostMapping("/{id}/students")
    public ResponseEntity<Void> addStudents(@PathVariable Long id, @RequestBody List<Long> studentIds) {
        campaignService.addStudentsToCampaign(id, studentIds);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<Void> addItems(@PathVariable Long id, @RequestBody List<Long> itemIds) {
        campaignService.addItemsToCampaign(id, itemIds);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        campaignService.delete(id);
        return ResponseEntity.noContent().build();
    }
}