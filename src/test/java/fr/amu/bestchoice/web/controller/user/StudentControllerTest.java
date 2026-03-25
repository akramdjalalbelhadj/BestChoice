package fr.amu.bestchoice.web.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.amu.bestchoice.security.jwt.JwtAuthenticationFilter;
import fr.amu.bestchoice.security.jwt.JwtService;
import fr.amu.bestchoice.service.interfaces.IStudentService;
import fr.amu.bestchoice.web.dto.student.StudentCreateRequest;
import fr.amu.bestchoice.web.dto.student.StudentResponse;
import fr.amu.bestchoice.web.dto.student.StudentUpdateRequest;
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

@WebMvcTest(StudentController.class)
@AutoConfigureMockMvc(addFilters = false)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IStudentService studentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllStudents_ShouldReturnList() throws Exception {
        StudentResponse student = new StudentResponse(1L, 1L, "student@test.com", "First", "Last", "123", 3, Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), null, null, null);
        when(studentService.findAll()).thenReturn(List.of(student));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("student@test.com"));
    }

    @Test
    void getAllStudentsPaginated_ShouldReturnPage() throws Exception {
        StudentResponse student = new StudentResponse(1L, 1L, "student@test.com", "First", "Last", "123", 3, Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), null, null, null);
        Page<StudentResponse> page = new PageImpl<>(List.of(student));
        when(studentService.findAll(anyInt(), anyInt(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/students/paginated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("student@test.com"));
    }

    @Test
    void getStudentByUserId_ShouldReturnStudent() throws Exception {
        StudentResponse student = new StudentResponse(1L, 1L, "student@test.com", "First", "Last", "123", 3, Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), null, null, null);
        when(studentService.findByUserId(1L)).thenReturn(student);

        mockMvc.perform(get("/api/students/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("student@test.com"));
    }

    @Test
    void createStudentProfile_ShouldReturnCreated() throws Exception {
        StudentCreateRequest request = new StudentCreateRequest(3, "Program", "Track", Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), null, null, null);
        StudentResponse response = new StudentResponse(1L, 1L, "student@test.com", "First", "Last", "123", 3, Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), null, null, null);
        when(studentService.create(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/api/students/user/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("student@test.com"));
    }

    @Test
    void updateStudent_ShouldReturnOk() throws Exception {
        StudentUpdateRequest request = new StudentUpdateRequest(4, Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), null, null, null);
        StudentResponse response = new StudentResponse(1L, 1L, "student@test.com", "First", "Last", "123", 4, Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), null, null, null);
        when(studentService.update(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/students/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studyYear").value(4));
    }
}
