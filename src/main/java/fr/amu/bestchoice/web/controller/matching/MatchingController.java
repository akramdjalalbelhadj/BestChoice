package fr.amu.bestchoice.web.controller.matching;

import fr.amu.bestchoice.service.implementation.algorithmes.MatchingContextService;
import fr.amu.bestchoice.web.dto.matching.MatchingRunResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingContextService matchingContextService;

    /**
     * Lance le matching pour une campagne.
     */
    @PostMapping("/campaign/{campaignId}/run")
    public ResponseEntity<MatchingRunResponse> run(@PathVariable Long campaignId) {
        var result = matchingContextService.run(campaignId);

        var response = MatchingRunResponse.from(result);

        return ResponseEntity.ok(response);
    }
}