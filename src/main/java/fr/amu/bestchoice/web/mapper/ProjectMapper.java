package fr.amu.bestchoice.web.mapper;

import fr.amu.bestchoice.model.entity.Project;
import fr.amu.bestchoice.model.entity.Skill;
import fr.amu.bestchoice.model.entity.Keyword;
import fr.amu.bestchoice.web.dto.project.ProjectResponse;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProjectMapper {

    public ProjectResponse toResponse(Project p) {
        Set<String> requiredSkills = p.getRequiredSkills() == null ? Set.of()
                : p.getRequiredSkills().stream().map(Skill::getName).collect(Collectors.toSet());

        Set<String> keywords = p.getKeywords() == null ? Set.of()
                : p.getKeywords().stream().map(Keyword::getLabel).collect(Collectors.toSet());

        var teacher = p.getTeacher();
        String teacherName = (teacher == null || teacher.getUser() == null)
                ? null
                : teacher.getUser().getFirstName() + " " + teacher.getUser().getLastName();

        return new ProjectResponse(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getWorkType(),
                p.getRemotePossible(),
                p.getActive(),
                p.getMinStudents(),
                p.getMaxStudents(),
                p.getFull(),
                teacher == null ? null : teacher.getId(),
                teacherName,
                requiredSkills,
                keywords
        );
    }
}
