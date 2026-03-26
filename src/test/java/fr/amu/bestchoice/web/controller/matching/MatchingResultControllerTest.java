package fr.amu.bestchoice.web.controller.matching;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.amu.bestchoice.security.jwt.JwtAuthenticationFilter;
import fr.amu.bestchoice.security.jwt.JwtService;
import fr.amu.bestchoice.service.implementation.algorithmes.MatchingAlgorithmType;
import fr.amu.bestchoice.service.interfaces.IMatchingResultService;
import fr.amu.bestchoice.web.dto.matching.MatchingResultResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchingResultController.class)
@AutoConfigureMockMvc(addFilters = false)
class MatchingResultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IMatchingResultService matchingResultService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getResultsByCampaign_ShouldReturnList() throws Exception {
        MatchingResultResponse result = new MatchingResultResponse(1L, 1L, 1L, 1L, null, new BigDecimal("0.8"), new BigDecimal("0.7"), new BigDecimal("0.9"), 1, MatchingAlgorithmType.WEIGHTED, LocalDateTime.now(), null, null);
        when(matchingResultService.findByCampaignId(1L)).thenReturn(List.of(result));

        mockMvc.perform(get("/api/matching/campaign/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].globalScore").value(0.8));
    }

    @Test
    void deleteByCampaign_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/matching/campaign/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void getResultsByStudent_ShouldReturnList() throws Exception {
        MatchingResultResponse result = new MatchingResultResponse(1L, 1L, 1L, 1L, null, new BigDecimal("0.8"), new BigDecimal("0.7"), new BigDecimal("0.9"), 1, MatchingAlgorithmType.WEIGHTED, LocalDateTime.now(), null, null);
        when(matchingResultService.findByCampaignAndStudent(1L, 1L)).thenReturn(List.of(result));

        mockMvc.perform(get("/api/matching/campaign/1/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].globalScore").value(0.8));
    }

    @Test
    void getTopForStudent_ShouldReturnList() throws Exception {
        MatchingResultResponse result = new MatchingResultResponse(1L, 1L, 1L, 1L, null, new BigDecimal("0.8"), new BigDecimal("0.7"), new BigDecimal("0.9"), 1, MatchingAlgorithmType.WEIGHTED, LocalDateTime.now(), null, null);
        when(matchingResultService.findTopResultsForStudent(anyLong(), anyLong(), anyInt())).thenReturn(List.of(result));

        mockMvc.perform(get("/api/matching/campaign/1/student/1/top/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].globalScore").value(0.8));
    }

    @Test
    void getResultsByProject_ShouldReturnList() throws Exception {
        MatchingResultResponse result = new MatchingResultResponse(1L, 1L, 1L, 1L, null, new BigDecimal("0.8"), new BigDecimal("0.7"), new BigDecimal("0.9"), 1, MatchingAlgorithmType.WEIGHTED, LocalDateTime.now(), null, null);
        when(matchingResultService.findByCampaignAndProject(1L, 1L)).thenReturn(List.of(result));

        mockMvc.perform(get("/api/matching/campaign/1/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].globalScore").value(0.8));
    }

    @Test
    void getById_ShouldReturnResult() throws Exception {
        MatchingResultResponse result = new MatchingResultResponse(1L, 1L, 1L, 1L, null, new BigDecimal("0.8"), new BigDecimal("0.7"), new BigDecimal("0.9"), 1, MatchingAlgorithmType.WEIGHTED, LocalDateTime.now(), null, null);
        when(matchingResultService.findById(1L)).thenReturn(result);

        mockMvc.perform(get("/api/matching/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.globalScore").value(0.8));
    }
}
