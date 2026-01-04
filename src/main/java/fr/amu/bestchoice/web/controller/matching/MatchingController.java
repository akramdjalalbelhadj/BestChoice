package fr.amu.bestchoice.web.controller.matching;

import fr.amu.bestchoice.service.implementation.algorithmes.MatchingContextService;
import fr.amu.bestchoice.web.dto.matching.MatchingRunRequest;
import fr.amu.bestchoice.web.dto.matching.MatchingRunResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingContextService matchingContextService;

    @PostMapping("/run")
    public ResponseEntity<MatchingRunResponse> run(@Valid @RequestBody MatchingRunRequest request) {
        var result = matchingContextService.run(request);          // MatchingRunResult
        var response = MatchingRunResponse.from(result);           // MatchingRunResponse
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recompute")
    public ResponseEntity<MatchingRunResponse> recompute(@Valid @RequestBody MatchingRunRequest request) {
        MatchingRunRequest forced = request.withRecompute(true);

        var result = matchingContextService.run(forced);           // MatchingRunResult
        var response = MatchingRunResponse.from(result);           // MatchingRunResponse
        return ResponseEntity.ok(response);
    }
}
