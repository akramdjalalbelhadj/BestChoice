package fr.amu.bestchoice.web.controller.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.amu.bestchoice.model.enums.WorkType;
import fr.amu.bestchoice.security.jwt.JwtAuthenticationFilter;
import fr.amu.bestchoice.security.jwt.JwtService;
import fr.amu.bestchoice.service.interfaces.IProjectService;
import fr.amu.bestchoice.web.dto.project.ProjectCreateRequest;
import fr.amu.bestchoice.web.dto.project.ProjectResponse;
import fr.amu.bestchoice.web.dto.project.ProjectUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IProjectService projectService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllProjects_ShouldReturnList() throws Exception {
        ProjectResponse project = new ProjectResponse(1L, "Title", "Desc", Set.of(WorkType.DEVELOPPEMENT), true, true, 1, 4, false, 1L, "Teacher", Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
        when(projectService.findAll()).thenReturn(List.of(project));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Title"));
    }

    @Test
    void getAllProjectsPaginated_ShouldReturnPage() throws Exception {
        ProjectResponse project = new ProjectResponse(1L, "Title", "Desc", Set.of(WorkType.DEVELOPPEMENT), true, true, 1, 4, false, 1L, "Teacher", Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
        Page<ProjectResponse> page = new PageImpl<>(List.of(project));
        when(projectService.findAll(anyInt(), anyInt(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/projects/paginated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Title"));
    }

    @Test
    void getProjectById_ShouldReturnProject() throws Exception {
        ProjectResponse project = new ProjectResponse(1L, "Title", "Desc", Set.of(WorkType.DEVELOPPEMENT), true, true, 1, 4, false, 1L, "Teacher", Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
        when(projectService.findById(1L)).thenReturn(project);

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    void createProject_ShouldReturnCreated() throws Exception {
        ProjectCreateRequest request = new ProjectCreateRequest("Title", "Desc", Set.of(WorkType.DEVELOPPEMENT), true, 1, 4, Collections.emptySet(), Collections.emptySet());
        ProjectResponse response = new ProjectResponse(1L, "Title", "Desc", Set.of(WorkType.DEVELOPPEMENT), true, true, 1, 4, false, 1L, "Teacher", Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
        when(projectService.create(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/api/projects/teacher/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    void updateProject_ShouldReturnOk() throws Exception {
        ProjectUpdateRequest request = new ProjectUpdateRequest("New Title", null, null, null, null, null, null, null, null);
        ProjectResponse response = new ProjectResponse(1L, "New Title", "Desc", Set.of(WorkType.DEVELOPPEMENT), true, true, 1, 4, false, 1L, "Teacher", Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
        when(projectService.update(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/projects/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"));
    }

    @Test
    void deactivateProject_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(patch("/api/projects/1/deactivate")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void activateProject_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(patch("/api/projects/1/activate")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void getProjectsByTeacher_ShouldReturnList() throws Exception {
        ProjectResponse project = new ProjectResponse(1L, "Title", "Desc", Set.of(WorkType.DEVELOPPEMENT), true, true, 1, 4, false, 1L, "Teacher", Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
        when(projectService.findByTeacherId(1L)).thenReturn(List.of(project));

        mockMvc.perform(get("/api/projects/teacher/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Title"));
    }
}
