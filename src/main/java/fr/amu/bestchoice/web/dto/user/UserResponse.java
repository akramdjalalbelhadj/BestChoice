package fr.amu.bestchoice.web.dto.user;

import fr.amu.bestchoice.model.enums.Role;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
        Long id,
        String studentNumber,
        String firstName,
        String lastName,
        String email,
        Boolean active,
        Set<Role> roles,
        LocalDateTime createdAt
) {}
