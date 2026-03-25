package fr.amu.bestchoice.web.controller.preference;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.amu.bestchoice.model.enums.PreferenceStatus;
import fr.amu.bestchoice.security.jwt.JwtAuthenticationFilter;
import fr.amu.bestchoice.security.jwt.JwtService;
import fr.amu.bestchoice.service.interfaces.IPreferenceService;
import fr.amu.bestchoice.web.dto.preference.PreferenceCreateRequest;
import fr.amu.bestchoice.web.dto.preference.PreferenceResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PreferenceController.class)
@AutoConfigureMockMvc(addFilters = false)
class PreferenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IPreferenceService preferenceService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getPreferencesByStudent_ShouldReturnList() throws Exception {
        PreferenceResponse preference = new PreferenceResponse(1L, 1L, 1L, 1L, null, 1, PreferenceStatus.PENDING, LocalDateTime.now());
        when(preferenceService.findByStudentId(1L)).thenReturn(List.of(preference));

        mockMvc.perform(get("/api/preferences/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rank").value(1));
    }

    @Test
    void getPreferencesByProject_ShouldReturnList() throws Exception {
        PreferenceResponse preference = new PreferenceResponse(1L, 1L, 1L, 1L, null, 1, PreferenceStatus.PENDING, LocalDateTime.now());
        when(preferenceService.findByProjectId(1L)).thenReturn(List.of(preference));

        mockMvc.perform(get("/api/preferences/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rank").value(1));
    }

    @Test
    void getPreferenceById_ShouldReturnPreference() throws Exception {
        PreferenceResponse preference = new PreferenceResponse(1L, 1L, 1L, 1L, null, 1, PreferenceStatus.PENDING, LocalDateTime.now());
        when(preferenceService.findById(1L)).thenReturn(preference);

        mockMvc.perform(get("/api/preferences/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rank").value(1));
    }

    @Test
    void createPreference_ShouldReturnCreated() throws Exception {
        PreferenceCreateRequest request = new PreferenceCreateRequest(1L, 1L, 1, "Motivation", "Comment");
        PreferenceResponse response = new PreferenceResponse(1L, 1L, 1L, 1L, null, 1, PreferenceStatus.PENDING, LocalDateTime.now());
        when(preferenceService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/preferences")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rank").value(1));
    }

    @Test
    void deletePreference_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/preferences/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void acceptPreference_ShouldReturnOk() throws Exception {
        PreferenceResponse response = new PreferenceResponse(1L, 1L, 1L, 1L, null, 1, PreferenceStatus.ACCEPTED, LocalDateTime.now());
        when(preferenceService.accept(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/preferences/1/accept")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void rejectPreference_ShouldReturnOk() throws Exception {
        PreferenceResponse response = new PreferenceResponse(1L, 1L, 1L, 1L, null, 1, PreferenceStatus.REJECTED, LocalDateTime.now());
        when(preferenceService.reject(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/preferences/1/reject")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }
}
