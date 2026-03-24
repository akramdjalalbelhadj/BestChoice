package fr.amu.bestchoice.web.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.amu.bestchoice.security.jwt.JwtAuthenticationFilter;
import fr.amu.bestchoice.security.jwt.JwtService;
import fr.amu.bestchoice.service.interfaces.ITeacherService;
import fr.amu.bestchoice.web.dto.teacher.TeacherCreateRequest;
import fr.amu.bestchoice.web.dto.teacher.TeacherResponse;
import fr.amu.bestchoice.web.dto.teacher.TeacherUpdateRequest;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeacherController.class)
@AutoConfigureMockMvc(addFilters = false)
class TeacherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ITeacherService teacherService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllTeachers_ShouldReturnList() throws Exception {
        TeacherResponse teacher = new TeacherResponse(1L, 1L, "teacher@test.com", "First", "Last", "Dept", "Rank", "Spec", null, Collections.emptySet());
        when(teacherService.findAll()).thenReturn(List.of(teacher));

        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("teacher@test.com"));
    }

    @Test
    void getAllTeachersPaginated_ShouldReturnPage() throws Exception {
        TeacherResponse teacher = new TeacherResponse(1L, 1L, "teacher@test.com", "First", "Last", "Dept", "Rank", "Spec", null, Collections.emptySet());
        Page<TeacherResponse> page = new PageImpl<>(List.of(teacher));
        when(teacherService.findAll(anyInt(), anyInt(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/teachers/paginated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("teacher@test.com"));
    }

    @Test
    void getTeacherById_ShouldReturnTeacher() throws Exception {
        TeacherResponse teacher = new TeacherResponse(1L, 1L, "teacher@test.com", "First", "Last", "Dept", "Rank", "Spec", null, Collections.emptySet());
        when(teacherService.findById(1L)).thenReturn(teacher);

        mockMvc.perform(get("/api/teachers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("teacher@test.com"));
    }

    @Test
    void createTeacherProfile_ShouldReturnCreated() throws Exception {
        TeacherCreateRequest request = new TeacherCreateRequest("Dept", "Rank", "Spec", null);
        TeacherResponse response = new TeacherResponse(1L, 1L, "teacher@test.com", "First", "Last", "Dept", "Rank", "Spec", null, Collections.emptySet());
        when(teacherService.create(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/api/teachers/user/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("teacher@test.com"));
    }

    @Test
    void updateTeacher_ShouldReturnOk() throws Exception {
        TeacherUpdateRequest request = new TeacherUpdateRequest("New Dept", "Rank", "Spec", null);
        TeacherResponse response = new TeacherResponse(1L, 1L, "teacher@test.com", "First", "Last", "New Dept", "Rank", "Spec", null, Collections.emptySet());
        when(teacherService.update(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/teachers/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department").value("New Dept"));
    }
}
