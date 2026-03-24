package fr.amu.bestchoice.web.controller.matching;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.amu.bestchoice.security.jwt.JwtAuthenticationFilter;
import fr.amu.bestchoice.security.jwt.JwtService;
import fr.amu.bestchoice.service.implementation.algorithmes.MatchingAlgorithmType;
import fr.amu.bestchoice.service.implementation.algorithmes.MatchingContextService;
import fr.amu.bestchoice.web.dto.matching.MatchingRunResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchingController.class)
@AutoConfigureMockMvc(addFilters = false)
class MatchingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchingContextService matchingContextService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void run_ShouldReturnOk() throws Exception {
        MatchingRunResult result = new MatchingRunResult(1L, MatchingAlgorithmType.WEIGHTED, 10, 100, Instant.now(), Instant.now());
        when(matchingContextService.run(anyLong())).thenReturn(result);

        mockMvc.perform(post("/api/matching/campaign/1/run")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaignId").value(1))
                .andExpect(jsonPath("$.algorithmUsed").value("WEIGHTED"));
    }
}
