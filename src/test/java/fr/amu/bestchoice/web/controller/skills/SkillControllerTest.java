package fr.amu.bestchoice.web.controller.skills;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.amu.bestchoice.security.jwt.JwtAuthenticationFilter;
import fr.amu.bestchoice.security.jwt.JwtService;
import fr.amu.bestchoice.service.interfaces.ISkillService;
import fr.amu.bestchoice.web.dto.skill.SkillCreateRequest;
import fr.amu.bestchoice.web.dto.skill.SkillResponse;
import fr.amu.bestchoice.web.dto.skill.SkillUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

@WebMvcTest(SkillController.class)
@AutoConfigureMockMvc(addFilters = false)
class SkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ISkillService skillService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllSkills_ShouldReturnList() throws Exception {
        SkillResponse skill = new SkillResponse(1L, "Java", "Description", "Category", 3, true);
        when(skillService.findAll()).thenReturn(List.of(skill));

        mockMvc.perform(get("/api/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Java"));
    }

    @Test
    void getAllSkillsPaginated_ShouldReturnPage() throws Exception {
        SkillResponse skill = new SkillResponse(1L, "Java", "Description", "Category", 3, true);
        Page<SkillResponse> page = new PageImpl<>(List.of(skill));
        when(skillService.findAll(anyInt(), anyInt(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/skills/paginated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Java"));
    }

    @Test
    void getSkillById_ShouldReturnSkill() throws Exception {
        SkillResponse skill = new SkillResponse(1L, "Java", "Description", "Category", 3, true);
        when(skillService.findById(1L)).thenReturn(skill);

        mockMvc.perform(get("/api/skills/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Java"));
    }

    @Test
    void createSkill_ShouldReturnCreated() throws Exception {
        SkillCreateRequest request = new SkillCreateRequest("Java", "Description", "Category", 3);
        SkillResponse response = new SkillResponse(1L, "Java", "Description", "Category", 3, true);
        when(skillService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/skills")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Java"));
    }

    @Test
    void updateSkill_ShouldReturnOk() throws Exception {
        SkillUpdateRequest request = new SkillUpdateRequest("Java 17", "Description", "Category", 4, true);
        SkillResponse response = new SkillResponse(1L, "Java 17", "Description", "Category", 4, true);
        when(skillService.update(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/skills/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Java 17"));
    }

    @Test
    void deleteSkill_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/skills/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deactivateSkill_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(patch("/api/skills/1/deactivate")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
