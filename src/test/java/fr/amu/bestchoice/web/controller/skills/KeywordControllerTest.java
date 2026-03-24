package fr.amu.bestchoice.web.controller.skills;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.amu.bestchoice.security.jwt.JwtAuthenticationFilter;
import fr.amu.bestchoice.security.jwt.JwtService;
import fr.amu.bestchoice.service.interfaces.IKeywordService;
import fr.amu.bestchoice.web.dto.keyword.KeywordCreateRequest;
import fr.amu.bestchoice.web.dto.keyword.KeywordResponse;
import fr.amu.bestchoice.web.dto.keyword.KeywordUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(KeywordController.class)
@AutoConfigureMockMvc(addFilters = false)
class KeywordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IKeywordService keywordService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllKeywords_ShouldReturnList() throws Exception {
        KeywordResponse keyword = new KeywordResponse(1L, "Web", "Description", "Domain", true);
        when(keywordService.findAll()).thenReturn(List.of(keyword));

        mockMvc.perform(get("/api/keywords"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("Web"));
    }

    @Test
    void getAllKeywordsPaginated_ShouldReturnPage() throws Exception {
        KeywordResponse keyword = new KeywordResponse(1L, "Web", "Description", "Domain", true);
        Page<KeywordResponse> page = new PageImpl<>(List.of(keyword));
        when(keywordService.findAll(anyInt(), anyInt(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/keywords/paginated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].label").value("Web"));
    }

    @Test
    void getKeywordById_ShouldReturnKeyword() throws Exception {
        KeywordResponse keyword = new KeywordResponse(1L, "Web", "Description", "Domain", true);
        when(keywordService.findById(1L)).thenReturn(keyword);

        mockMvc.perform(get("/api/keywords/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("Web"));
    }

    @Test
    void createKeyword_ShouldReturnCreated() throws Exception {
        KeywordCreateRequest request = new KeywordCreateRequest("Web", "Description", "Domain");
        KeywordResponse response = new KeywordResponse(1L, "Web", "Description", "Domain", true);
        when(keywordService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/keywords")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.label").value("Web"));
    }

    @Test
    void updateKeyword_ShouldReturnOk() throws Exception {
        KeywordUpdateRequest request = new KeywordUpdateRequest("Web 2.0", "Description", "Domain", true);
        KeywordResponse response = new KeywordResponse(1L, "Web 2.0", "Description", "Domain", true);
        when(keywordService.update(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/keywords/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("Web 2.0"));
    }

    @Test
    void deleteKeyword_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/keywords/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deactivateKeyword_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(patch("/api/keywords/1/deactivate")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
