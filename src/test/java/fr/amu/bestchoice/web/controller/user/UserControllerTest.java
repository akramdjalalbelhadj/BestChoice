package fr.amu.bestchoice.web.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.amu.bestchoice.model.enums.Role;
import fr.amu.bestchoice.security.jwt.JwtAuthenticationFilter;
import fr.amu.bestchoice.security.jwt.JwtService;
import fr.amu.bestchoice.service.implementation.auth.RegisterService;
import fr.amu.bestchoice.service.interfaces.IUserService;
import fr.amu.bestchoice.web.dto.auth.RegisterRequest;
import fr.amu.bestchoice.web.dto.auth.RegisterResponse;
import fr.amu.bestchoice.web.dto.user.UserResponse;
import fr.amu.bestchoice.web.dto.user.UserUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IUserService userService;

    @MockitoBean
    private RegisterService registerService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllUsers_ShouldReturnList() throws Exception {
        UserResponse user = new UserResponse(1L, "123", "First", "Last", "test@test.com", true, Role.ADMIN, LocalDateTime.now());
        when(userService.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("test@test.com"));
    }

    @Test
    void getAllUsersPaginated_ShouldReturnPage() throws Exception {
        UserResponse user = new UserResponse(1L, "123", "First", "Last", "test@test.com", true, Role.ADMIN, LocalDateTime.now());
        Page<UserResponse> page = new PageImpl<>(List.of(user));
        when(userService.findAll(anyInt(), anyInt(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/users/paginated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("test@test.com"));
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        UserResponse user = new UserResponse(1L, "123", "First", "Last", "test@test.com", true, Role.ADMIN, LocalDateTime.now());
        when(userService.findById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void registerUser_ShouldReturnCreated() throws Exception {
        RegisterRequest request = new RegisterRequest("First", "Last", "test@test.com", "password", "123", Role.ETUDIANT);
        RegisterResponse response = new RegisterResponse(1L, "test@test.com", "First", "Last", "Success");
        when(registerService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void updateUser_ShouldReturnOk() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest("Updated", "User", "updated@test.com", "123");
        UserResponse response = new UserResponse(1L, "123", "Updated", "User", "updated@test.com", true, Role.ADMIN, LocalDateTime.now());
        when(userService.update(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@test.com"));
    }

    @Test
    void deactivateUser_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(patch("/api/users/1/deactivate")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void activateUser_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(patch("/api/users/1/activate")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
