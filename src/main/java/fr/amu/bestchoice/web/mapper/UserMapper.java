package fr.amu.bestchoice.web.mapper;

import fr.amu.bestchoice.model.entity.User;
import fr.amu.bestchoice.web.dto.user.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(),
                u.getStudentNumber(),
                u.getFirstName(),
                u.getLastName(),
                u.getEmail(),
                u.getActive(),
                u.getRoles(),
                u.getCreatedAt()
        );
    }
}
