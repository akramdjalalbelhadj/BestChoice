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
     * Route : POST /api/matching/campaign/5/run
     */
    @PostMapping("/campaign/{campaignId}/run")
    public ResponseEntity<MatchingRunResponse> run(@PathVariable Long campaignId) {
        // Le service s'occupe de tout : charger la campagne, wipe, calculer, sauvegarder.
        var result = matchingContextService.run(campaignId);

        // On transforme le résultat technique en réponse API
        var response = MatchingRunResponse.from(result);

        return ResponseEntity.ok(response);
    }
}