package fr.amu.bestchoice.service.implementation.admin;

import fr.amu.bestchoice.model.enums.WorkType;
import fr.amu.bestchoice.repository.ProjectRepository;
import fr.amu.bestchoice.repository.StudentRepository;
import fr.amu.bestchoice.repository.SubjectRepository;
import fr.amu.bestchoice.repository.TeacherRepository;
import fr.amu.bestchoice.web.dto.admin.AdminStatsResponse;
import fr.amu.bestchoice.web.dto.admin.AdminStatsResponse.NameCountEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsService {

    private final ProjectRepository projectRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    public AdminStatsResponse getStats() {

        // ── Projets (requêtes agrégées, pas de lazy loading) ──────────────────
        long totalProjects     = projectRepository.count();
        long activeProjects    = projectRepository.countByActiveTrue();
        long completedProjects = projectRepository.countByCompletTrue();
        Long rawProjCap        = projectRepository.sumMaxStudents();
        long totalProjectCap   = rawProjCap != null ? rawProjCap : 0L;

        Map<String, Long> projectsByWorkType = toWorkTypeMap(projectRepository.countByWorkType());
        Map<String, Long> projectsBySemester = toSemesterMap(projectRepository.countBySemester());
        List<NameCountEntry> topTeachersByProjects = toTeacherList(projectRepository.countByTeacherStats(), 5);

        // ── Options / Subjects ────────────────────────────────────────────────
        long totalSubjects  = subjectRepository.count();
        long activeSubjects = subjectRepository.countByActiveTrue();
        Long rawSubjCap     = subjectRepository.sumMaxStudents();
        long totalSubjectCap = rawSubjCap != null ? rawSubjCap : 0L;

        Map<String, Long> subjectsByWorkType = toWorkTypeMap(subjectRepository.countByWorkType());
        Map<String, Long> subjectsBySemester = toSemesterMap(subjectRepository.countBySemester());
        List<NameCountEntry> topTeachersBySubjects = toTeacherList(subjectRepository.countByTeacherStats(), 5);

        return new AdminStatsResponse(
                totalProjects, activeProjects, totalProjects - activeProjects, completedProjects, totalProjectCap,
                projectsByWorkType, projectsBySemester, topTeachersByProjects,
                totalSubjects, activeSubjects, totalSubjects - activeSubjects, totalSubjectCap,
                subjectsByWorkType, subjectsBySemester, topTeachersBySubjects,
                teacherRepository.count(), studentRepository.count()
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Map<String, Long> toWorkTypeMap(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                r -> ((WorkType) r[0]).name(),
                r -> (Long) r[1],
                (a, b) -> a,
                LinkedHashMap::new
        ));
    }

    private Map<String, Long> toSemesterMap(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                r -> "S" + r[0],
                r -> (Long) r[1],
                (a, b) -> a,
                LinkedHashMap::new
        ));
    }

    private List<NameCountEntry> toTeacherList(List<Object[]> rows, int limit) {
        return rows.stream()
                .limit(limit)
                .map(r -> new NameCountEntry(r[0] + " " + r[1], (Long) r[2]))
                .toList();
    }
}
